import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class ServerSocketTask implements Runnable {
    private Socket connection;
    private Request request;
    private Reply reply;
    private UserInfo userInfo;
    private HashMap<String, UserInfo> usersMap = new HashMap<>();

    public ServerSocketTask(Socket socket, HashMap<String, UserInfo> usersMap) {
        this.connection = socket;
        this.usersMap = usersMap;
    }

    @Override
    public void run() {
        try {
            String message = "";
            // Input Stream
            InputStream inputStream = connection.getInputStream();
            ObjectInputStream in = new ObjectInputStream(inputStream);
            try {
                this.request = (Request) in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            // Task when client wants to Register
            if(request.getOperationType().equals("register")) {
                // Checking if the request object has the necessarry userinfo
                if(request.getUserInfo() != null) {
                    String newUserName = request.getUserInfo().getUserName();
                    String newPassword = request.getUserInfo().getPassword();
                    // Checking if the username already exists
                    if(!usersMap.containsKey(newUserName)) {
                        System.out.println("Adding new user with Username: " + newUserName);
                        userInfo = new UserInfo(newUserName, newPassword);
                        usersMap.put(newUserName, userInfo);
                    } else {
                        System.out.println("The username " + newUserName + " already exists. Aborting operation.");
                        message = "The username " + newUserName + " already exists. Please choose a different one and try again.";
                    }
                } else {
                    message = "Something is wrong. Please try again.";
                }

                // Waiting the thread for 2 seconds
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Checking if the user was added to the hashmap successfully and sending a reply to the client
                UserInfo userFromHashmap = usersMap.get(request.getUserInfo().getUserName());
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                if(message.equals("")) {
                    if(userFromHashmap != null) {
                        message = "User added successfully";
                        reply = new Reply(userFromHashmap, message);
                        System.out.println("Sending reply object to Client...");
                        out.writeObject(reply);
                        out.flush();
                    } else {
                        message = "Failed adding user";
                        reply = new Reply(message);
                        System.out.println("Sending reply object to Client...");
                        out.writeObject(reply);
                        out.flush();
                    }
                } else {
                    reply = new Reply(message);
                    System.out.println("Sending reply object to Client...");
                    out.writeObject(reply);
                    out.flush();
                }
                
                out.close();
                in.close();
            } 

            // Task when client wants to Login
            else if(request.getOperationType().equals("login")) {
                // Checking if the request object has the necessarry userinfo
                if(request.getUserInfo() != null) {
                    String loginUserName = request.getUserInfo().getUserName();
                    String loginPassword = request.getUserInfo().getPassword();
                    // Checking if the user credentials are correct
                    if(usersMap.containsKey(loginUserName)) {
                        if(usersMap.get(loginUserName).getPassword().equals(loginPassword)) {
                            System.out.println("User " + loginUserName + " login approved.");
                            System.out.println("Waiting for next command...");
                            message = "loginSuccess";

                        } else {
                            System.out.println("User " + loginUserName + " entered wrong password.");
                            message = "Wrong password entered. Please try again.";
                        }

                        userInfo = new UserInfo(loginUserName, loginPassword);
                        usersMap.put(loginUserName, userInfo);
                    } else {
                        System.out.println("The username " + loginUserName + " doesn't exist. Aborting operation.");
                        message = "The username " + loginUserName + " doesn't exist. Please enter valid credentials and try again.";
                    }
                } else {
                    message = "Something is wrong. Please try again.";
                }

                // Waiting the thread for 2 seconds
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Output Stream
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);

                // If login was successful, sending a reply to client and waiting for the next commands, then handling the commands
                if(message.equals("loginSuccess")) {
                    UserInfo loggedUser = new UserInfo(request.getUserInfo().getUserName(), request.getUserInfo().getPassword());
                    reply = new Reply(loggedUser, message);
                    out.writeObject(reply);
                    out.flush();
                    int joinState = 0;
                    while(true) {
                        try {
                            this.request = (Request) in.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        // Task when user wants to join a game
                        if(request.getOperationType().equals("join")) {
                            // Checking if the user is already connected in another session
                            if(joinState == 0) {
                                System.out.println("User " + loggedUser.getUserName() + " asked to join");
                                joinState = 1;
                                message = "Successfully joined";
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                reply = new Reply(message);
                                out.writeObject(reply);
                                out.flush();
                                //TODO Add Future Game Features
                            } else {
                                System.out.println("User already joined");
                                message = "Already joined";
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                reply = new Reply(message);
                                out.writeObject(reply);
                                out.flush();
                            }   
                        }
                        // Task when user want to exit the joined session
                        if(request.getOperationType().equals("exit")) {
                            System.out.println("User " + loggedUser.getUserName() + " asked to exit");
                            break;
                        }
                    }
                } else {
                    reply = new Reply(message);
                    out.writeObject(reply);
                    out.flush();
                }
                
                out.close();
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

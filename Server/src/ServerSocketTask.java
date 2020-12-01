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
            InputStream inputStream = connection.getInputStream();
            ObjectInputStream in = new ObjectInputStream(inputStream);
            try {
                this.request = (Request) in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if(request.getOperationType().equals("register")) {
                String newUserName = request.getUserInfo().getUserName();
                String newPassword = request.getUserInfo().getPassword();
                if(request.getUserInfo() != null) {
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

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                UserInfo userFromHashmap = usersMap.get(newUserName);
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
            else if(request.getOperationType().equals("login")) {
                if(request.getUserInfo() != null) {
                    String loginUserName = request.getUserInfo().getUserName();
                    String loginPassword = request.getUserInfo().getPassword();
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

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);

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
                        if(request.getOperationType().equals("join")) {
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientSocketTask implements Runnable {
    private Socket connection;
    private int port = 1000;
    private String ip = "localhost";

    private Request request;
    private Reply reply;
    private Reply joinReply;

    private Scanner scanner;
    private String gameLine;

    public ClientSocketTask(Request request) {
        this.request = request;
    }

    public ClientSocketTask(Request request, Scanner scanner) {
        this.request = request;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        
        try {
            connection = new Socket(ip, port);
            System.out.println("Connected! Server info: " + connection);

            // Task for registration
            if(request.getOperationType().equals("register")) {
                // Output Stream
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                
                System.out.println("Sending your information to the server...");
                out.writeObject(this.request);
                out.flush();

                // Input Stream
                InputStream inputStream = connection.getInputStream();
                ObjectInputStream in = new ObjectInputStream(inputStream);

                try {
                    this.reply = (Reply) in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.println("Server reply: " + reply);

                in.close();
                out.close();
            }

            // Task for login
            if(request.getOperationType().equals("login")) {
                // Output Stream
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                
                System.out.println("Sending your information to the server...");
                out.writeObject(this.request);
                out.flush();

                // Input Stream
                InputStream inputStream = connection.getInputStream();
                ObjectInputStream in = new ObjectInputStream(inputStream);

                try {
                    this.reply = (Reply) in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                // Task when login is successful
                if(reply.getMessage().equals("loginSuccess")) {
                    System.out.println("Login Successful");
                    while(true) {
                        // Prompts after login is successful
                        int input;
                        String stringInput;
                        System.out.println("Welcome " + reply.getUserInfo().getUserName());
                        System.out.println("Available Commands:");
                        System.out.println("1   Join");
                        System.out.println("0   Exit");
                        System.out.println("Please enter a command number");
                        System.out.print("> ");

                        if(scanner.hasNextInt()) {
                            input = scanner.nextInt();
                            if(input != 0) {
                                if(input == 1) {
                                    // Prompt and task for joining a game
                                    request = new Request("join");
                                    System.out.println("Teaming up with another player...");
                                    out.writeObject(this.request);
                                    out.flush();
                                    try {
                                        joinReply = (Reply) in.readObject();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println(joinReply);
                                    stringInput = scanner.next();
                                    if(stringInput.equals("yes")) {
                                        request = new Request("ready");
                                        out.writeObject(this.request);
                                        out.flush();
                                        System.out.println("Waiting for the other player to be ready...");
                                    }

                                    int startCounter = 0;
                                    while(true) {
                                        try {
                                            joinReply = (Reply) in.readObject();
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                        if(joinReply.toString().equals("START")) {
                                            startCounter = 1;
                                            continue;
                                        }
                                        if(joinReply.toString().equals("END")) {
                                            break;
                                        } else if(startCounter == 1) {
                                            this.gameLine = joinReply.toString();
                                            startCounter = 0;
                                            System.out.println(this.gameLine);
                                        } else {
                                            System.out.println(joinReply);
                                        }
                                    }

                                    scanner.nextLine();
                                    stringInput = scanner.nextLine();
                                    if(this.gameLine.equals(stringInput)) {
                                        System.out.println("Waiting for server to finish up...");
                                        request = new Request("done");
                                        out.writeObject(this.request);
                                        out.flush();
                                    } else {
                                        System.out.println("Your input didn't match. So, you're disqualified");
                                        request = new Request("disqualified");
                                        out.writeObject(this.request);
                                        out.flush();
                                    }

                                    while(true) {
                                        try {
                                            joinReply = (Reply) in.readObject();
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }

                                        if(joinReply.toString().equals("END")) {
                                            break;
                                        }
                                        System.out.println(joinReply);

                                    }

                                    break;

                                } else {
                                    System.out.println("Invalid Command. Please enter the number of the command");
                                }
                            } else {
                                // Task after client tried to exit joined state
                                request = new Request("exit");
                                out.writeObject(this.request);
                                out.flush();
                                break;
                            }
                        } else {
                            System.out.println("Invalid Command. Please enter the number of the command");
                            scanner.next();
                        }
                    }
                } else {
                    System.out.println(reply);
                }

                in.close();
                out.close();
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
        
    }
}

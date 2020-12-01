import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientSocketTask implements Runnable {
    private Socket connection;
    private int port = 1234;
    private String ip = "localhost";

    private Request request;
    private Reply reply;
    private Reply joinReply;

    private Scanner scanner;

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

            if(request.getOperationType().equals("register")) {
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                
                System.out.println("Sending your information to the server...");
                out.writeObject(this.request);
                out.flush();

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

            if(request.getOperationType().equals("login")) {
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                
                System.out.println("Sending your information to the server...");
                out.writeObject(this.request);
                out.flush();

                InputStream inputStream = connection.getInputStream();
                ObjectInputStream in = new ObjectInputStream(inputStream);

                try {
                    this.reply = (Reply) in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if(reply.getMessage().equals("loginSuccess")) {
                    System.out.println("Login Successful");
                    // Scanner scanner = new Scanner(System.in);
                    while(true) {
                        int input;
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
                                    request = new Request("join");
                                    System.out.println("Sending your information to the server...");
                                    out.writeObject(this.request);
                                    out.flush();
                                    try {
                                        joinReply = (Reply) in.readObject();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println(joinReply);
                                } else {
                                    System.out.println("Invalid Command. Please enter the number of the command");
                                }
                            } else {
                                request = new Request("exit");
                                // System.out.println("Sending your information to the server...");
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

                // System.out.println("Server reply: " + reply);


            }

        } catch(IOException e) {
            e.printStackTrace();
        }
        
    }
}

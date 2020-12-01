// import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {
    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        String userName, password;
        UserInfo userInfo;
        Request request;
        while(true) {
            int input;
            System.out.println("Welcome to the Game");
            System.out.println("Available Commands:");
            System.out.println("1   Register");
            System.out.println("2   Login");
            System.out.println("0   Exit");
            System.out.println("Please enter a command number");
            System.out.print("> ");

            if(scanner.hasNextInt()) {
                input = scanner.nextInt();

                if(input != 0) {
                    if(input == 1) {
                        System.out.println("Please enter Username");
                        userName = scanner.next();
                        
                        System.out.println("Please enter Password");
                        password = scanner.next();

                        userInfo = new UserInfo(userName, password);
                        request = new Request(userInfo, "register");

                        ClientSocketTask clientsocket = new ClientSocketTask(request);
                        clientsocket.run();
                    } else if(input == 2) {
                        System.out.println("Please enter Username");
                        userName = scanner.next();
                        
                        System.out.println("Please enter Password");
                        password = scanner.next();

                        userInfo = new UserInfo(userName, password);
                        request = new Request(userInfo, "login");

                        ClientSocketTask clientsocket = new ClientSocketTask(request, scanner);
                        clientsocket.run();
                    } else {
                        System.out.println("Invalid Command. Please enter the number of the command");
                    }
                } else {
                    break;
                }
            } else {
                System.out.println("Invalid Command. Please enter the number of the command");
                scanner.next();
            }
        }
        scanner.close();
    }
}
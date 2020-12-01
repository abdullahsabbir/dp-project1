import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private static Socket connection;
    private static ServerSocket serverSocket;
    private static HashMap<String, UserInfo> usersMap = new HashMap<>();

    public static void main(String args[]) throws IOException {
        int port = 1234;
        serverSocket = new ServerSocket(port);

        while(true) {
            System.out.println("Waiting for connection...");

            connection = serverSocket.accept();
            ServerSocketTask serverTask = new ServerSocketTask(connection, usersMap);
            serverTask.run();

            connection.close();
        }
    }
}
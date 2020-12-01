import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static Socket connection;
    private static ServerSocket serverSocket;
    private static HashMap<String, UserInfo> usersMap = new HashMap<>();
    private static ExecutorService serverThreadExecutor = Executors.newFixedThreadPool(10);

    public static void main(String args[]) throws IOException {
        int port = 1234;
        serverSocket = new ServerSocket(port);

        while(true) {
            System.out.println("Waiting for connection...");

            connection = serverSocket.accept();
            ServerSocketTask serverTask = new ServerSocketTask(connection, usersMap);
            serverThreadExecutor.execute(serverTask);
        }
    }
}
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server{
    private static Socket connection;
    private static ServerSocket serverSocket;
    private static HashMap<String, UserInfo> usersMap = new HashMap<>();
    private static HashMap<String, UserInfo> loggedUsersList = new HashMap<>();
    private static HashMap<String, Team> teamList = new HashMap<>();
    private static HashMap<String, String> teamIdForOtherPlayer = new HashMap<>();
    private static Queue<UserInfo> playerQueue = new LinkedList<>();
    private static ExecutorService serverThreadExecutor = Executors.newFixedThreadPool(10);
    final static Object monitor = new Object();
    private static HashMap<String, Double> recordBoard = new HashMap<>();

    //File
    private static List<String> wordList;

    public static void main(String args[]) throws IOException {       //Changed IOException to Exception

        int port = 1000;
        serverSocket = new ServerSocket(port);

        while(true) {
            System.out.println("Waiting for connection...");

            connection = serverSocket.accept();
            ServerSocketTask serverTask = new ServerSocketTask(connection, usersMap, loggedUsersList, teamList, teamIdForOtherPlayer, playerQueue, monitor, wordList, recordBoard);
            serverThreadExecutor.execute(serverTask);
        }
    }
}
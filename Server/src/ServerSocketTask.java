import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
// import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

public class ServerSocketTask implements Runnable {
    private Socket connection;
    private Request request;
    private Reply reply;
    private String gameLine;
    private UserInfo userInfo;
    final Object monitor;
    private String teamId;
    private int playerNumber;
    private int otherPlayerNumber;
    private int newRecordStatus;
    private UserInfo otherPlayer;
    private String leaderboardText;
    private HashMap<String, UserInfo> usersMap = new HashMap<>();
    private HashMap<String, UserInfo> loggedUsersList = new HashMap<>();
    private HashMap<String, Team> teamList = new HashMap<>();
    private HashMap<String, String> teamIdForOtherPlayer = new HashMap<>();
    private HashMap<String, Double> recordBoard = new HashMap<>();
    private Queue<UserInfo> playerQueue = new LinkedList<>();
    private List<String> wordList;

    public ServerSocketTask(Socket socket, HashMap<String, UserInfo> usersMap,
            HashMap<String, UserInfo> loggedUsersList, HashMap<String, Team> teamList,
            HashMap<String, String> teamIdForOtherPlayer, Queue<UserInfo> playerQueue, Object monitor,
            List<String> wordList, HashMap<String, Double> recordBoard) {
        this.connection = socket;
        this.usersMap = usersMap;
        this.loggedUsersList = loggedUsersList;
        this.teamList = teamList;
        this.teamIdForOtherPlayer = teamIdForOtherPlayer;
        this.playerQueue = playerQueue;
        this.monitor = monitor;
        this.wordList = wordList;
        this.recordBoard = recordBoard;
    }

    private String joinTeam(UserInfo loggedUser) {
        synchronized (this.monitor) {
            try {
                if (playerQueue.peek() != null) {
                    UserInfo player2 = playerQueue.remove();
                    Team newTeam = new Team(loggedUser, player2, this.teamList);
                    this.teamList.put(newTeam.getTeamId(), newTeam);
                    this.teamIdForOtherPlayer.put(player2.getUserName(), newTeam.getTeamId());
                    monitor.notifyAll();
                    return newTeam.getTeamId();
                } else {
                    this.playerQueue.add(loggedUser);
                    while (true) {
                        if (this.teamIdForOtherPlayer.containsKey(loggedUser.getUserName())) {
                            break;
                        } else {
                            monitor.wait();
                        }
                    }
                    return this.teamIdForOtherPlayer.get(loggedUser.getUserName());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    private String generateLine() throws IOException {
        this.wordList = Files.readAllLines(Paths.get("D:\\Java Codes\\CI3.04\\Project1\\Server", "src\\wordlist.txt"));
        String line = "";
        int randomIndex;
        int remainingLength;
        long startTime = System.currentTimeMillis();
        long endTime;
        while (true) {
            remainingLength = 20 - line.length();
            randomIndex = (int) (Math.random() * ((wordList.size() - 1) - 0 + 1) + 0);
            if (wordList.get(randomIndex).length() <= remainingLength) {
                line = line + wordList.get(randomIndex) + " ";
            }

            if (20 - line.length() == 0) {
                break;
            }
            endTime = System.currentTimeMillis();
            if (endTime - startTime >= 1000) {
                break;
            }
        }
        line = line.stripTrailing();
        return line;
    }

    private void play(ObjectOutputStream out, ObjectInputStream in, UserInfo player) throws IOException {
        // OutputStream outputStream = connection.getOutputStream();
        // ObjectOutputStream out = new ObjectOutputStream(outputStream);

        HashMap<String, Double> tempHashMap = new HashMap<>();

        reply = new Reply("3");
        out.writeObject(reply);
        out.flush();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        reply = new Reply("2");
        out.writeObject(reply);
        out.flush();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        reply = new Reply("1");
        out.writeObject(reply);
        out.flush();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        reply = new Reply("Enter the text below exactly as shown. When done, press enter");
        out.writeObject(reply);
        out.flush();

        reply = new Reply("START");
        out.writeObject(reply);
        out.flush();

        this.gameLine = generateLine();
        reply = new Reply(this.gameLine);
        out.writeObject(reply);
        out.flush();

        reply = new Reply("END");
        out.writeObject(reply);
        out.flush();

        // InputStream inputStream = connection.getInputStream();
        // ObjectInputStream in = new ObjectInputStream(inputStream);

        long startTime = System.currentTimeMillis();
        try {
            this.request = (Request) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        long timeTaken;

        if (request.getOperationType().equals("done")) {
            timeTaken = endTime - startTime;
            this.recordBoard.put(player.getUserName(), (double) timeTaken / 1000);
        } else {
            timeTaken = 999999L;
        }
        if (this.recordBoard.containsKey(player.getUserName())) {
            if (this.recordBoard.get(player.getUserName()) > (double) timeTaken / 1000) {
                this.newRecordStatus = 1;
            }
        }

        tempHashMap = sortHashMap(this.recordBoard);
        this.recordBoard = tempHashMap;

        // in.close();
        // out.close();
    }

    private static HashMap<String, Double> sortHashMap(HashMap<String, Double> hashMap) {
        Set<Entry<String, Double>> countryCapitalEntrySet = hashMap.entrySet();

        List<Entry<String, Double>> entryList = new ArrayList<Entry<String, Double>>(countryCapitalEntrySet);

        Collections.sort(entryList, new Comparator<Entry<String, Double>>() {

            @Override
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        LinkedHashMap<String, Double> sortedHashMap = new LinkedHashMap<String, Double>();
        for (Entry<String, Double> entry : entryList) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
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
            if (request.getOperationType().equals("register")) {
                // Checking if the request object has the necessarry userinfo
                if (request.getUserInfo() != null) {
                    String newUserName = request.getUserInfo().getUserName();
                    String newPassword = request.getUserInfo().getPassword();
                    // Checking if the username already exists
                    if (!usersMap.containsKey(newUserName)) {
                        System.out.println("Adding new user with Username: " + newUserName);
                        userInfo = new UserInfo(newUserName, newPassword);
                        usersMap.put(newUserName, userInfo);
                    } else {
                        System.out.println("The username " + newUserName + " already exists. Aborting operation.");
                        message = "The username " + newUserName
                                + " already exists. Please choose a different one and try again.";
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

                // Checking if the user was added to the hashmap successfully and sending a
                // reply to the client
                UserInfo userFromHashmap = usersMap.get(request.getUserInfo().getUserName());
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                if (message.equals("")) {
                    if (userFromHashmap != null) {
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
            else if (request.getOperationType().equals("login")) {
                // Checking if the request object has the necessarry userinfo
                if (request.getUserInfo() != null) {
                    String loginUserName = request.getUserInfo().getUserName();
                    String loginPassword = request.getUserInfo().getPassword();
                    // Checking if the user credentials are correct
                    if (usersMap.containsKey(loginUserName)) {
                        // Checking if the user is already logged in another session
                        if (!loggedUsersList.containsKey(loginUserName)) {
                            // Checking if the password is correct
                            if (usersMap.get(loginUserName).getPassword().equals(loginPassword)) {
                                System.out.println("User " + loginUserName + " login approved.");
                                System.out.println("Waiting for next command...");
                                // Adding the user to the logged in users list
                                loggedUsersList.put(loginUserName, request.getUserInfo());
                                message = "loginSuccess";
                            } else {
                                System.out.println("User " + loginUserName + " entered wrong password.");
                                message = "Wrong password entered. Please try again.";
                            }
                        } else {
                            System.out.println("User " + loginUserName + " is already logged in another session.");
                            message = loginUserName + " is already logged in another session";
                        }
                    } else {
                        System.out.println("The username " + loginUserName + " doesn't exist. Aborting operation.");
                        message = "The username " + loginUserName
                                + " doesn't exist. Please enter valid credentials and try again.";
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

                // If login was successful, sending a reply to client and waiting for the next
                // commands, then handling the commands
                if (message.equals("loginSuccess")) {
                    UserInfo loggedUser = new UserInfo(request.getUserInfo().getUserName(),
                            request.getUserInfo().getPassword());
                    reply = new Reply(loggedUser, message);
                    out.writeObject(reply);
                    out.flush();
                    while (true) {
                        try {
                            this.request = (Request) in.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        // Task when user wants to join a game
                        if (request.getOperationType().equals("join")) {
                            System.out.println("User " + loggedUser.getUserName() + " asked to join");
                            // joinTeam Codes here

                            this.teamId = joinTeam(loggedUser);
                            Team myTeam = teamList.get(teamId);
                            this.playerNumber = myTeam.getPlayerNumber(loggedUser);

                            if (this.playerNumber == 1) {
                                this.otherPlayer = this.teamList.get(teamId).getPlayer2();
                                this.otherPlayerNumber = 2;
                            } else if (this.playerNumber == 2) {
                                this.otherPlayer = this.teamList.get(teamId).getPlayer1();
                                this.otherPlayerNumber = 1;
                            }

                            message = "You are successfully teamed with " + this.otherPlayer.getUserName()
                                    + "\nAre you ready to play?\nEnter yes when ready";
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            reply = new Reply(message);
                            out.writeObject(reply);
                            out.flush();

                            try {
                                this.request = (Request) in.readObject();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            if (request.getOperationType().equals("ready")) {
                                myTeam.makePlayerReady(this.playerNumber);
                                synchronized (this.monitor) {
                                    this.teamList.replace(this.teamId, myTeam);
                                }
                            }

                            System.out.println("Waiting for the other player to be ready...");

                            while (true) {
                                if (this.otherPlayerNumber == 1) {
                                    if (this.teamList.get(teamId).getPlayer1ReadyStatus() == 1) {
                                        break;
                                    }
                                } else {
                                    if (this.teamList.get(teamId).getPlayer2ReadyStatus() == 1) {
                                        break;
                                    }
                                }
                            }

                            // out.close();
                            // in.close();
                            synchronized (monitor) {
                                try {
                                    if (playerNumber != this.teamList.get(teamId).getStartingPlayer()) {
                                        while (true) {
                                            if (this.teamList.get(teamId).getPlayer1finished() == 1) {
                                                play(out, in, loggedUser);
                                                myTeam = teamList.get(teamId);
                                                myTeam.setPlayer2finished(1);
                                                this.teamList.replace(teamId, myTeam);
                                                break;
                                            } else {
                                                System.out.println("waiting");
                                                monitor.wait();
                                            }
                                        }
                                    } else {
                                        play(out, in, loggedUser);
                                        // System.out.println("After play");
                                        myTeam = teamList.get(teamId);
                                        myTeam.setPlayer1finished(1);
                                        // System.out.println("After setting player 1 finished");
                                        // System.out.println(myTeam.getPlayer1finished());
                                        this.teamList.replace(teamId, myTeam);
                                        // System.out.println("replacing new team");
                                        monitor.notify();
                                        // System.out.println("after notify");
                                        // System.out.println(loggedUser.getUserName());

                                    }

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // try {
                            // Thread.sleep(100);
                            // } catch (InterruptedException e1) {
                            // e1.printStackTrace();
                            // }

                            // String leaderboard = "Name          Time(s)\n";

                            myTeam = teamList.get(teamId);

                            if (playerNumber == 1) {
                                myTeam.setPlayer1finished(0);
                            } else {
                                myTeam.setPlayer2finished(0);
                            }
                            this.teamList.replace(teamId, myTeam);

                            synchronized (monitor) {
                                try {
                                    String leaderboard = "Name          Time(s)\n";
                                    if (playerNumber == teamList.get(teamId).getStartingPlayer()) {
                                        if (playerNumber == 1) {
                                            while (true) {
                                                if (teamList.get(teamId).getPlayer2finished() == 1) {
                                                    if (this.newRecordStatus == 1) {
                                                        reply = new Reply(
                                                                "Congratulations! You have set a new record\nHere is the new Record Board");
                                                        out.writeObject(reply);
                                                        out.flush();
                                                    }
                                                    for (Map.Entry<String, Double> entry : this.recordBoard
                                                            .entrySet()) {
                                                        leaderboard = leaderboard + entry.getKey() + "            "
                                                                + entry.getValue() + "\n";
                                                    }
                                                    this.leaderboardText = leaderboard;
                                                    myTeam = teamList.get(teamId);
                                                    myTeam.setPlayer1finished(1);
                                                    
                                                    this.teamList.replace(teamId, myTeam);
                                                    break;
                                                } else {
                                                    System.out.println("inside wait");
                                                    monitor.wait();
                                                }
                                            }
                                        } else {
                                            while (true) {
                                                if (teamList.get(teamId).getPlayer1finished() == 1) {
                                                    if (this.newRecordStatus == 1) {
                                                        reply = new Reply(
                                                                "Congratulations! You have set a new record\nHere is the new Record Board");
                                                        out.writeObject(reply);
                                                        out.flush();
                                                    }
                                                    for (Map.Entry<String, Double> entry : this.recordBoard
                                                            .entrySet()) {
                                                        leaderboard = leaderboard + entry.getKey() + "            "
                                                                + entry.getValue() + "\n";
                                                    }
                                                    this.leaderboardText = leaderboard;
                                                    myTeam = teamList.get(teamId);
                                                    myTeam.setPlayer2finished(1);
                                                    
                                                    this.teamList.replace(teamId, myTeam);
                                                    break;
                                                } else {
                                                    System.out.println("inside wait");
                                                    monitor.wait();
                                                }
                                            }
                                        }

                                    } else {
                                        if (this.newRecordStatus == 1) {
                                            reply = new Reply(
                                                    "Congratulations! You have set a new record\nHere is the new Record Board");
                                            out.writeObject(reply);
                                            out.flush();
                                        }
                                        for (Map.Entry<String, Double> entry : this.recordBoard.entrySet()) {
                                            leaderboard = leaderboard + entry.getKey() + "            "
                                                    + entry.getValue() + "\n";
                                        }
                                        this.leaderboardText = leaderboard;
                                        myTeam = teamList.get(teamId);

                                        if (playerNumber == 1) {
                                            myTeam.setPlayer1finished(1);
                                        } else {
                                            myTeam.setPlayer2finished(1);
                                        }
                                        this.teamList.replace(teamId, myTeam);
                                        monitor.notify();
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                // if (this.newRecordStatus == 1) {
                                // reply = new Reply(
                                // "Congratulations! You have set a new record\nHere is the new Record Board");
                                // out.writeObject(reply);
                                // out.flush();
                                // }
                                // for (Map.Entry<String, Double> entry : this.recordBoard.entrySet()) {
                                // leaderboard = leaderboard + entry.getKey() + " " + entry.getValue() + "\n";
                                // }
                                // // leaderboard = this.recordBoard.toString();
                            }

                            reply = new Reply(this.leaderboardText);
                            out.writeObject(reply);
                            out.flush();

                            reply = new Reply("END");
                            out.writeObject(reply);
                            out.flush();

                            // try {
                            // Thread.sleep(1000);
                            // } catch (InterruptedException e) {
                            // e.printStackTrace();
                            // }
                            loggedUsersList.remove(loggedUser.getUserName());
                            if (playerNumber == 1) {
                                myTeam = teamList.get(teamId);
                                myTeam.setPlayer1finished(0);
                                myTeam.setPlayer1ReadyStatus(0);
                                this.teamList.replace(teamId, myTeam);
                            } else {
                                myTeam = teamList.get(teamId);
                                myTeam.setPlayer2finished(0);
                                myTeam.setPlayer2ReadyStatus(0);
                                this.teamList.replace(teamId, myTeam);
                            }

                            break;

                        }
                        // Task when user want to exit the joined session
                        if (request.getOperationType().equals("exit")) {
                            System.out.println("User " + loggedUser.getUserName() + " asked to exit");
                            loggedUsersList.remove(loggedUser.getUserName());
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

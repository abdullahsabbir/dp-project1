import java.util.HashMap;
import java.lang.Math;

public class Team {
    private String teamId;
    private UserInfo player1;
    private UserInfo player2;
    private int player1ReadyStatus = 0;
    private int player2ReadyStatus = 0;
    private int player1finished = 0;
    private int player2finished = 0;
    private int startingPlayer;
    private HashMap<String, Team> teamList = new HashMap<>();

    private void generateRandomId() {
        String randomId = String.valueOf(Math.random() * (999 - 1 + 1) + 1);

        while(true) {
            if(teamList.containsKey(randomId)) {
                randomId = String.valueOf(Math.random() * (999 - 1 + 1) + 1);
            } else {
                this.teamId = randomId;
                break;
            }
        }
    }

    private void determineStartingPlayer() {
        int randomPlayerNumber = (int)(Math.random() * (2 - 1 + 1) + 1);

        if(randomPlayerNumber == 1) {
            startingPlayer = 1;
        } else {
            startingPlayer = 2;
        }
    }

    public int getPlayerNumber(UserInfo player) {
        if(player.getUserName().equals(this.player1.getUserName())) {
            return 1;
        } else {
            return 2;
        }
    }

    public void makePlayerReady(int playerNumber) {
        if(playerNumber == 1) {
            this.player1ReadyStatus = 1;
        } else if(playerNumber == 2){
            this.player2ReadyStatus = 1;
        }
    }
    
    public Team(UserInfo player1, UserInfo player2, HashMap<String, Team> teamList) {
        this.player1 = player1;
        this.player2 = player2;
        this.teamList = teamList;
        generateRandomId();
        determineStartingPlayer();
    }

    //Getters and setters

    public String getTeamId() {
        return teamId;
    }

    public UserInfo getPlayer1() {
        return player1;
    }
    public void setPlayer1(UserInfo player1) {
        this.player1 = player1;
    }

    public UserInfo getPlayer2() {
        return player2;
    }
    public void setPlayer2(UserInfo player2) {
        this.player2 = player2;
    }

    public int getPlayer1ReadyStatus() {
        return player1ReadyStatus;
    }
    public void setPlayer1ReadyStatus(int player1ReadyStatus) {
        this.player1ReadyStatus = player1ReadyStatus;
    }

    public int getPlayer2ReadyStatus() {
        return player2ReadyStatus;
    }
    public void setPlayer2ReadyStatus(int player2ReadyStatus) {
        this.player2ReadyStatus = player2ReadyStatus;
    }

    public int getPlayer1finished() {
        return player1finished;
    }
    public void setPlayer1finished(int player1finished) {
        this.player1finished = player1finished;
    }

    public int getPlayer2finished() {
        return player2finished;
    }
    public void setPlayer2finished(int player2finished) {
        this.player2finished = player2finished;
    }

    public int getStartingPlayer() {
        return startingPlayer;
    }
}

import java.io.Serializable;

public class Reply implements Serializable{
    private static final long serialVersionUID = 1L;
    private UserInfo userInfo;
    private String message;

    public Reply(UserInfo userInfo, String message) {
        this.userInfo = userInfo;
        this.message = message;
    }

    public Reply(String message) {
        this.message = message;
    }

    public String toString() {
        if(this.userInfo != null) {
            return "Username: " + userInfo.getUserName() + "\n" + "Message: " + this.message;
        } else {
            return message;
        }
    }

    //Getters and Setters
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

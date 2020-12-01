import java.io.Serializable;

public class Request implements Serializable{
    private static final long serialVersionUID = 1L;
    private UserInfo userInfo;
    private String operationType;

    public Request(UserInfo userInfo, String operationType) {
        this.userInfo = userInfo;
        this.operationType = operationType;
    }

    public Request(String operationType) {
        this.operationType = operationType;
    }

    //Getters and Setters
    public UserInfo getUserInfo() {
        return userInfo;
    }
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getOperationType() {
        return operationType;
    }
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
}

public class ClientInfo {
    private String clientID;
    private int lastMsg;
    private long lastActiveTime;

    public ClientInfo(String clientID, int lastMsg, long lastActiveTime) {
        this.clientID = clientID;
        //Zu aufrufende Nachricht
        this.lastMsg = lastMsg;
        this.lastActiveTime = lastActiveTime;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public int getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(int lastMsg) {
        this.lastMsg = lastMsg;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "clientID='" + clientID + '\'' +
                ", Inactive Time=" + ((System.currentTimeMillis()-lastActiveTime)) + "ms "+
                "last Message: " + lastMsg +
                '}';
    }
}

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageService extends Remote {
    public String nextMessage(String clientID) throws
            RemoteException;
    public void newMessage(String clientID, String message) throws
            RemoteException;
}
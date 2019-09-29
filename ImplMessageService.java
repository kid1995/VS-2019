import java.rmi.RemoteException;

public class ImplMessageService implements MessageService {
    @Override
    public String nextMessage(String clientID) throws RemoteException {
        System.out.println("Hello, this is next Message of" + clientID);
        return null;
    }

    @Override
    public void newMessage(String clientID, String message) throws RemoteException {
        System.out.println("This is new Message");
    }
}

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// Servant Class für Dispatcher Skeleton und alle davon unterstürzen entfernten Objekt

//TODO: ADD RECEIVED TIMESTAMP TO "newMessage()" Function

public class ImplMessageService extends UnicastRemoteObject implements MessageService {

    private int msgID = 0;
    private DeliveryQueue messageQueue;

    protected ImplMessageService( int msgCapacity) throws RemoteException {
        super();
        this.messageQueue = new DeliveryQueue(msgCapacity);
    }


    @Override
    public String nextMessage(String clientID) throws RemoteException {
        Message msg = messageQueue.get();
        if(msg != null)
            return msg.toString();
        return null;
    }

    @Override
    public void newMessage(String clientID, String message) throws RemoteException {
        System.out.println("New Message from client : " + clientID);
        String id = Integer.toString(msgID++);
        Message msg = new Message(clientID, message, id);
        messageQueue.add(msg);
    }



}

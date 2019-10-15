import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

// Servant Class für Dispatcher Skeleton und alle davon unterstürzen entfernten Objekt

//TODO: ADD RECEIVED TIMESTAMP TO "newMessage()" Function

public class ImplMessageService implements MessageService {

    private int msgQueueSize;

    private int msgID = 0;

    private int oldestMsg = 0;
    private List<ClientInfo> clientInfos;



    private DeliveryQueue messageQueue;

    public ImplMessageService(int msgQueueSize, List<ClientInfo> clientInfos) {
        this.msgQueueSize = msgQueueSize;
        this.clientInfos = clientInfos;
        this.messageQueue = new DeliveryQueue(msgQueueSize);
    }

    @Override
    public String nextMessage(String clientID) throws RemoteException {
        ClientInfo currentClient;
        int clientExit = isClientExited(clientID);

        if(clientExit< 0){
            currentClient = new ClientInfo(clientID, 0, Timer.getCurrentTimeStamp());
            clientInfos.add(currentClient);
            clientExit = clientInfos.indexOf(currentClient);
            System.out.println("Client " + clientID +"was added at" + clientExit);
        }else {
            currentClient = clientInfos.get(clientExit);
            currentClient.setLastActiveTime(Timer.getCurrentTimeStamp());
        }
        Message msg = messageQueue.get(currentClient.getLastMsg());
        if(msg != null) {
            currentClient.setLastMsg(currentClient.getLastMsg()+1);
            clientInfos.set(clientExit, currentClient);
            return msg.toString();
        }
        return null;
    }

    @Override
    public void newMessage(String clientID, String message) throws RemoteException {
        if(msgID-msgQueueSize >oldestMsg){
            oldestMsg++;
            updateLastMsg(oldestMsg);
        }
        System.out.println("New Message from client : " + clientID);
        messageQueue.add(clientID,message, msgID);
        msgID++;
    }


    private int isClientExited(String clientID){
        Iterator<ClientInfo> it = clientInfos.iterator();
        int clientIndex = -1;
        ClientInfo currentClient;
        while (it.hasNext()){
             currentClient = it.next();
             if(currentClient.getClientID().equals(clientID)){
                 clientIndex = clientInfos.indexOf(currentClient);
                 break;
             }
        }
        return clientIndex;
    }

    private void updateLastMsg(int lastMsgID){
        Iterator<ClientInfo> it = clientInfos.iterator();
        ClientInfo currentClient;
        int clientIndex;
        while (it.hasNext()){
            currentClient = it.next();
            if(currentClient.getLastMsg() < lastMsgID){
                clientIndex = clientInfos.indexOf(currentClient);
                currentClient.setLastMsg(lastMsgID);
                clientInfos.set(clientIndex, currentClient);
            }
        }
    }

}

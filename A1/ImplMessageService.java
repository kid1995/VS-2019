import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

// Servant Class für Dispatcher Skeleton und alle davon unterstürzen entfernten Objekt

public class ImplMessageService implements MessageService {
    private int msgQueueSize;
    private int msgID = 0;
    private int oldestMsg = 0;
    private List<ClientInfo> clientInfos;
    private Semaphore sem;
    private DeliveryQueue messageQueue;

    ImplMessageService(int msgQueueSize, List<ClientInfo> clientInfos, Semaphore semaphore) {
        this.sem = semaphore;
        this.msgQueueSize = msgQueueSize;
        this.clientInfos = clientInfos;
        this.messageQueue = new DeliveryQueue(msgQueueSize);
    }

    @Override
    public String nextMessage(String clientID) {
        ClientInfo currentClient;
        try {
            sem.acquire();
            try {
                int clientIndex = isClientExited(clientID);
                //Ist der Client schon verbunden?
                if (clientIndex < 0) {
                	//Client in Liste ahinzufügen
                    currentClient = new ClientInfo(clientID, 0, Timer.getCurrentTimeStamp());
                    clientInfos.add(currentClient);
                    clientIndex = clientInfos.indexOf(currentClient);
                    System.out.println("Client " + clientID + " was added at" + clientIndex);
                } else {
                	//Letzte Aktivitätszeit aktualisieren und Client aus der Liste bekommen
                    currentClient = clientInfos.get(clientIndex);
                    currentClient.setLastActiveTime(Timer.getCurrentTimeStamp());
                }
                //Nachricht aus der Queue bekommen
                Message msg = messageQueue.get(currentClient.getLastMsg());
                if (msg != null) {
                	//Inderx der zu Aufrufenden Nachricht inkrementieren
                    currentClient.setLastMsg(currentClient.getLastMsg() + 1);
                    clientInfos.set(clientIndex, currentClient);
                    return msg.toString();
                }
            } finally {
                sem.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public void newMessage(String clientID, String message) throws RemoteException {
        try {
            sem.acquire();
            try {
            	//Wenn die Queue voll ist wird der Index der ältesten Nachricht um eins inkrementiert und somit wird die ältere Nachricht davor überschrieben
                if (msgID - msgQueueSize > oldestMsg) {
                    oldestMsg++;
                    updateLastMsg(oldestMsg);
                }
                System.out.println("New Message from client : " + clientID);
                //Nachricht der Queue hinzufügen
                messageQueue.add(clientID, message, msgID);
                //Message Index erhöhen
                msgID++;
            } finally {
                sem.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Prüft ob der Client verbunden ist oder schon die Verbindung abgebrochen hat
    private int isClientExited(String clientID) {
        Iterator<ClientInfo> it = clientInfos.iterator();
        int clientIndex = -1;
        ClientInfo currentClient;
        while (it.hasNext()) {
            currentClient = it.next();
            if (currentClient.getClientID().equals(clientID)) {
                clientIndex = clientInfos.indexOf(currentClient);
                break;
            }
        }
        return clientIndex;
    }
    //Der Index der ältesten Nachricht und somit der zu Aufrufenden Nachricht wird für alle Clients aktualisiert
    private void updateLastMsg(int lastMsgID) {
        Iterator<ClientInfo> it = clientInfos.iterator();
        ClientInfo currentClient;
        int clientIndex;
        while (it.hasNext()) {
            currentClient = it.next();
            if (currentClient.getLastMsg() < lastMsgID) {
                clientIndex = clientInfos.indexOf(currentClient);
                currentClient.setLastMsg(lastMsgID);
                clientInfos.set(clientIndex, currentClient);
            }
        }
    }

}
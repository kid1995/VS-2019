

// TODO: Correct Queue, add and remove is not right
public class DeliveryQueue{
    private Message[] deliveryQueue;
    private int capacity;


    public DeliveryQueue(int capacity) {
        this.capacity = capacity;
        this.deliveryQueue = new Message[capacity];
    }



    public void add(String clientID, String msg, int msgID){
        String id = Integer.toString(msgID);
        Message newMsg = new Message(clientID, msg, id);
        //Da der msgIndex stets inkrementiert wird, wird mit Modulo nie die Kapazität überschritten
        int queueIndex = msgID%capacity;
        deliveryQueue[queueIndex] =  newMsg;
    }

    public Message get(int msgID){
        return deliveryQueue[msgID%capacity];
    }


}

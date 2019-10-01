

// TODO: Correct Queue, add and remove is not right
public class DeliveryQueue{
    private Message[] deliveryQueue;
    private int front, rear, capacity;


    public DeliveryQueue(int capacity) {
        this.capacity = capacity;
        this.front = 0;
        this.rear = 0;
        this.deliveryQueue = new Message[capacity];
    }



    public void add(Message msg){
        this.front++;
        this.front %=this.capacity;
        deliveryQueue[this.front] =  msg;

        System.out.println("rear: " +rear +" front: " + front);
    }

    public Message get(){
        if(rear == front) {
           System.out.println("Rear == Front");
            return null;
        }
        System.out.println("rear: " +rear +" front: " + front);
        this.rear++;
        return this.deliveryQueue[this.rear];
    }


}

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Station {

    int[] lastFrame;
    int[] thisFrame;
    boolean[] collisionsList;

    byte chosenSlot = -1;
    byte stationClass;

    Random r;

    long utcOffset;

    long nextFrameTimeStamp;

    String multicastAddress;
    String networkInterface;

    int port;

    DataReader reader;
    Thread readerThread;
    Sender sender;
    Thread senderThread;
    Receiver receiver;
    Thread receiverThread;

    public Station(byte sClass, long offset, String ip, int p, String netIn) {
        lastFrame = new int[Config.SLOTS_IN_FRAME];
        collisionsList = new boolean[Config.SLOTS_IN_FRAME];
        utcOffset = offset;
        multicastAddress = ip;
        networkInterface = netIn;
        port = p;
        stationClass = sClass;

        r = new Random();

        reader = new DataReader(System.in, Config.DATA_SIZE);
        sender = new Sender();
        receiver = new Receiver();

        senderThread = new Thread(sender);
        receiverThread = new Thread(receiver);
        readerThread = new Thread(reader);

        senderThread.start();
        receiverThread.start();
        readerThread.start();
    }

    public byte findRandomSlot(int[] claimedSlots) {
        // find a random free slot
        int free = claimedSlots.length;
        for (int slot : claimedSlots) {
            if (slot == 1) {
                free--;
            }
        }

        int rand = r.nextInt(free);

        int num = 0;
        for (int i = 0; i < claimedSlots.length; i++) {
            if (claimedSlots[i] != 1) {
                if (num == rand) {
                    return (byte) i;
                }

                num++;
            }
        }
        System.out.println("All slots are full");
        // if no free slot is found return a random one
        return (byte) r.nextInt(Config.SLOTS_IN_FRAME);
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis() + utcOffset;
    }

    public long currentSecond() {
        return (currentTimeMillis() / 1000) * 1000;
    }

    public void syncTo(List<Message> msgList) {
        int numAPackets = 0;
        long offset = 0;
        for (Message msg : msgList) {
            if (msg.getType() == 'A') {
                offset += msg.getReceiveTimestamp() - msg.getTimestamp();
                numAPackets++;
            }
        }
        if (numAPackets > 0) {
            offset /= numAPackets;
        } else {
            offset = 0;
        }
        utcOffset -= offset;

    }


    class Sender implements Runnable {
        MulticastSocket socket;
        BlockingQueue<Message> msgQueue;
        InetAddress group;

        public Sender() {
            try {
                group = InetAddress.getByName(multicastAddress);
                socket = new MulticastSocket(port);
                socket.setNetworkInterface(NetworkInterface.getByName(networkInterface));
                socket.joinGroup(group);
                msgQueue = new LinkedBlockingQueue<>();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {

                while (true) {
                    // Wait until there is data in the buffer
                    Message msg = msgQueue.take();

                    long sleepTime = (Config.SLOT_TIME / 2);

                    Thread.sleep(sleepTime);

                    DatagramPacket packet = new DatagramPacket(msg.getPaket(), msg.getPaket().length,
                            new InetSocketAddress(InetAddress.getByName(multicastAddress), port));
                    msg.setTimestamp(currentTimeMillis());

                    socket.send(packet);

                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        public void send(Message data) {
            try {
                msgQueue.put(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class Receiver implements Runnable {
        MulticastSocket socket;
        byte[] buffer;

        InetAddress group;

        public Receiver() {
            try {
                buffer = new byte[Config.PACKET_LENGTH];
                group = InetAddress.getByName(multicastAddress);
                socket = new MulticastSocket(port);
                socket.setNetworkInterface(NetworkInterface.getByName(networkInterface));
                socket.joinGroup(group);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            List<Message> packetsInFrame;

            // ONLY B STATIONS
            // receive until a packet from an A Station is received and synchronize the
            // Station time to it.
            if (stationClass == 'B') {
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.setSoTimeout(0);

                        socket.receive(packet);
                        Message p = new Message(packet.getData(), currentTimeMillis(), 0);
                        if (p.getType() == 'A') {
                            ArrayList<Message> packetList = new ArrayList<>();
                            packetList.add(p);
                            syncTo(packetList);
                            break;
                        }

                    } catch (IOException ignored) {
                    }
                }
            }

            // wait until the next frame starts
            try {
                Thread.sleep((currentSecond() + 1000) - currentTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String data;
            nextFrameTimeStamp = currentSecond();
            // Receive loop
            while (true) {
                // clear everything before frame
                long currentSecond = nextFrameTimeStamp;
                nextFrameTimeStamp = nextFrameTimeStamp + 1000;

                if (reader.isEmpty()) {
                    chosenSlot = -1;
                    packetsInFrame = receiveUntil(nextFrameTimeStamp);
                    thisFrame = new int[Config.SLOTS_IN_FRAME];
                    for (Message p : packetsInFrame) {
                        thisFrame[p.getClaimedSlot()]++;
                    }
                } else {
                    data = reader.getString();
                    int oldSlot = chosenSlot;
                    // System.out.println(currentTimeMillis() + " ");
                    if (chosenSlot == -1) {
                        chosenSlot = findRandomSlot(lastFrame);
                    }
                    //receive until slot is reached


                    packetsInFrame = receiveUntil(currentSecond + Config.SLOT_TIME * chosenSlot);

                    // System.out.println("Now in Slot " + currentTimeMillis()%1000/Constants.SLOT_TIME + "\nOld Slot"+ oldSlot+"\nSending in Slot "+ _slot + "SleepTime " + sleepTime);

                    //which slots are claimed
                    thisFrame = new int[Config.SLOTS_IN_FRAME];
                    for (Message p : packetsInFrame) {
                        thisFrame[p.getClaimedSlot()]++;
                    }
                    //claim a slot that is not claimed
                    chosenSlot = findRandomSlot(thisFrame);
                    //send datapacket
                    sender.send(new Message(stationClass, data.getBytes(), chosenSlot, 0));

                    //check if packet collides
                    //long receiveTime = currentTimeMillis() + Constants.SLOT_TIME - currentTimeMillis();
                    List<Message> packetsInSlot = receiveUntil(currentTimeMillis() + Config.SLOT_TIME);
                    //System.out.println("Received "+receiveTime +"ms Packets in  CS " + currentTimeMillis()%1000/Constants.SLOT_TIME + " PL "+sendSlot+": "+ packetsInSlot.size());

                    packetsInFrame.addAll(packetsInSlot);
                    //receive until next frame start
                    List<Message> remainingPacketsInFrame = receiveUntil(nextFrameTimeStamp);
                    packetsInFrame.addAll(remainingPacketsInFrame);

                    //which slots are claimed in this frame
                    Arrays.fill(thisFrame, 0);
                    for (Message p : packetsInFrame) {
                        thisFrame[p.getClaimedSlot()]++;
                    }
                }
                syncTo(packetsInFrame);
                lastFrame = thisFrame;
            }
        }

        private List<Message> receiveUntil(long time) {
            List<Message> result = new ArrayList<>();
            long currentTime = currentTimeMillis();

            while (currentTime < time) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.setSoTimeout((int) (time - currentTime));
                    try {
                        socket.receive(packet);
                        result.add(new Message(packet.getData(), currentTimeMillis(), (int) (currentTimeMillis() % 1000 / Config.SLOT_TIME)));
                    } catch (SocketTimeoutException e) {
                        break;
                    }
                    currentTime = currentTimeMillis();

                } catch (IOException ignored) {
                }
            }
            return result;
        }

    }


    public static void main(String[] args)
    {
        String usage = "Usage: $0 <multicast-address> <receive-port> <station-class> <UTC-offset-(ms)>";
        if(args.length != 5)
        {
            System.out.println(usage);
            System.exit(1);
        }
        String multicastAddress = args[0];
        int receivePort = Integer.parseInt(args[1]);
        String stationClass = args[2];
        long utcOffset = Long.parseLong(args[3]);
        String networkInterface = args[4];

        new Station((byte) stationClass.charAt(0), utcOffset,multicastAddress,receivePort, networkInterface);
    }
}

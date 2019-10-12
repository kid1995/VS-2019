import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    private  static final String QUIT = "Q";
    private  static final String GET = "G";
    private  static final String SEND = "S";

    private static final int  SERVICE_PORT = 1099;
    private static final String SERVICE_NAME ="MessageService";
    private static final String SERVER_IP ="127.0.0.1";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.setProperty("java.security.policy","file:./test.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }



        try {


            String clientID;
            String cmd = " ";
            String msg = " ";

            // Getting the registry
            Registry registry = LocateRegistry.getRegistry(SERVER_IP, SERVICE_PORT);
            // Looking up the registry for the remote object
            MessageService stub = (MessageService) registry.lookup(SERVICE_NAME);
            // Calling the remote method using the obtained object

            // TODO: This is just for test, use Client IP instead after test was success
            System.out.println("Please enter your client ID: ");
            clientID = scanner.nextLine();
            while (!cmd.equals(QUIT)){
                System.out.println("Please select your command: \n G : get message \n S : send message \n Q : quit \n");
                cmd = scanner.nextLine();
                switch (cmd){
                    case QUIT:
                        cmd = "Q";
                        break;
                    case GET:
                        msg = stub.nextMessage(clientID);
                        if(msg != null)
                        System.out.println("received message: " + msg);
                        else
                            System.out.println("no new message !!!");
                        break;
                    case SEND:
                        System.out.println("Please enter your message: ");
                        msg = scanner.nextLine();
                        stub.newMessage(clientID,msg);
                        break;
                }
            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
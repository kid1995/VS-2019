import java.rmi.Naming;
import java.util.Scanner;

public class Client {
    private static final String QUIT = "Q";
    private static final String GET = "G";
    private static final String SEND = "S";

    private Client() {
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {

            String clientID;
            String cmd = " ";
            String msg;

            MessageService messageService = (MessageService) Naming.lookup("//localhost/MessageService");

            // TODO: This is just for test, use Client IP instead after test was success
            System.out.println("Please enter your client ID: ");
            clientID = scanner.nextLine();
            while (!cmd.equals(QUIT)) {
                System.out.println("Please select your command: \n G : get message \n S : send message \n Q : quit \n");
                cmd = scanner.nextLine();
                switch (cmd) {
                    case QUIT:
                        cmd = "Q";
                        break;
                    case GET:
                        msg = messageService.nextMessage(clientID);
                        if (msg != null)
                            System.out.println("received message: " + msg);
                        else
                            System.out.println("no new message !!!");
                        break;
                    case SEND:
                        System.out.println("Please enter your message: ");
                        msg = scanner.nextLine();
                        messageService.newMessage(clientID, msg);
                        break;
                }
            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
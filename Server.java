import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


public class Server {
    public static final int  SERVICE_PORT = 1099;
    public static final String SERVICE_NAME ="MessageService";

    public static void main(String args[]) {
        List<ClientInfo> clientInfos = new ArrayList<>();


        System.setProperty("java.security.policy","file:./test.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }


        try {
            ImplMessageService obj = new ImplMessageService(40,clientInfos);

            MessageService stub = (MessageService) UnicastRemoteObject.exportObject(obj, 0);

            Registry registry = LocateRegistry.getRegistry(SERVICE_PORT);
            registry.bind(SERVICE_NAME, stub);

            System.err.println("Server ready");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
} 
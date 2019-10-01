import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server extends ImplMessageService {

    public Server() {}

    public static void main(String args[]) {
        try {
            // Instantiating the implementation class
            ImplMessageService obj = new ImplMessageService();

            // Exporting the object of implementation class
            // (here we are exporting the remote object to the stub)
            MessageService stub = (MessageService) UnicastRemoteObject.exportObject(obj, 0);

            // Binding the remote object (stub) in the registry
            Registry registry = LocateRegistry.getRegistry();

            registry.bind("MessageService", stub);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
} 
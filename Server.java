import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import java.util.stream.Collectors;


public class Server {
    public static final int  SERVICE_PORT = 1099;
    public static final String SERVICE_NAME ="MessageService";
    public static final long REFRESH_TIME_IN_MIN = 1;

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

            long start = Timer.getCurrentTimeStamp();

            Predicate<ClientInfo> overTime = clientInfo -> Timer.calcDeactiveTime(clientInfo.getLastActiveTime()) > (REFRESH_TIME_IN_MIN+1);
            do {
                System.out.println("Start wait in 1 min");
                String result = clientInfos.stream().map(Object::toString).collect(Collectors.joining(","));
                System.out.println(result + "\n");
                while (Timer.calcDeactiveTime(start) < REFRESH_TIME_IN_MIN);
                start =  Timer.getCurrentTimeStamp();
                System.out.println("Start to filter");
                clientInfos.stream().filter(overTime).collect(Collectors.toList());
                result = clientInfos.stream().map(Object::toString).collect(Collectors.joining(","));
                System.out.println("finish filter");
                System.out.println(result + "\n");
            }while (true);

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }



} 
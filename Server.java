import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;

import java.util.stream.Collectors;


public class Server {
    public static final int SERVICE_PORT = 1099;
    public static final String SERVICE_NAME = "MessageService";
    public static final long REFRESH_TIME_IN_MIN = 800;
    static Semaphore semaphore = new Semaphore(1);


    static class CheckInactiveClient extends Thread {
        List<ClientInfo> clientInfos;

        CheckInactiveClient(List<ClientInfo> clientInfoList) {
            this.clientInfos = clientInfoList;
        }

        public void run() {
            System.err.println("Checker ready");
            do {
                try {

                   // System.out.println("CheckInactiveClient: acquiring lock...");
                    // System.out.println("CheckInactiveClient: available Semaphore permits now: "
                    //        + semaphore.availablePermits());
                    semaphore.availablePermits();
                    semaphore.acquire();
                    // System.out.println("CheckInactiveClient : got the permit!");

                    try {
                        System.out.println(" Before filtering");
                        System.out.println(clientInfos.size() + " are active");
                        List<String> listClient = clientInfos.stream()
                                .map(ClientInfo::toString)
                                .collect(Collectors.toList());
                        listClient.forEach(System.out::println);

                        clientInfos = clientInfos.stream().filter(clientInfo -> ((System.currentTimeMillis()-clientInfo.getLastActiveTime())>1000)).collect(Collectors.toList());

                        System.out.println(clientInfos.size() + " are active");
                        listClient = clientInfos.stream()
                                .map(ClientInfo::toString)
                                .collect(Collectors.toList());
                        listClient.forEach(System.out::println);
                        System.out.println("Start polling in 20s ...");
                    } finally {
                        // System.out.println("CheckInactiveClient : releasing lock...");
                        semaphore.release();
                        // System.out.println("CheckInactiveClient : available Semaphore permits now: "
                        //        + semaphore.availablePermits());
                        semaphore.availablePermits();
                    }
                    Thread.sleep(1000 * 20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);


        }
    }

    public static void main(String args[]) {
        List<ClientInfo> clientInfos = new ArrayList<>();
        ClientInfo c1 = new ClientInfo("1",2,1000);
        ClientInfo c2 = new ClientInfo("1",2,2000);
        ClientInfo c3 = new ClientInfo("1",2,3000);
        ClientInfo c4 = new ClientInfo("1",2,4000);

        clientInfos.add(c1);
        clientInfos.add(c2);
        clientInfos.add(c3);
        clientInfos.add(c4);
        clientInfos = clientInfos.stream().filter(clientInfo -> clientInfo.getLastActiveTime()>3000).collect(Collectors.toList());

        clientInfos.forEach(System.out::println);

        /*CheckInactiveClient checker = new CheckInactiveClient(clientInfos);
        System.setProperty("java.security.policy", "file:./test.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }


        try {
            ImplMessageService obj = new ImplMessageService(40, clientInfos);

            MessageService stub = (MessageService) UnicastRemoteObject.exportObject(obj, 0);

            Registry registry = LocateRegistry.getRegistry(SERVICE_PORT);
            checker.start();
            registry.bind(SERVICE_NAME, stub);

            System.err.println("Server ready");


        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

         */
    }
}
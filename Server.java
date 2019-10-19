import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;


public class Server {
	private static final int SERVICE_PORT = 1099;
	private static final String SERVICE_NAME = "MessageService";
	private static final long LIMITED_TIME_T = 60000;           // 60000ms = 1min
	private static Semaphore semaphore = new Semaphore(1);


	static class CheckInactiveClient extends Thread {
		List<ClientInfo> clientInfos;

		CheckInactiveClient(List<ClientInfo> clientInfoList) {
			this.clientInfos = clientInfoList;
		}

		// after
		public void run() {
			System.err.println("Checking to remove the clients,\r\n who was inactive more than " + LIMITED_TIME_T + " ms");
			do {
				try {
					semaphore.acquire();
					// System.out.println("CheckInactiveClient: available Semaphore permits now: " + semaphore.availablePermits());
					try {

						Iterator<ClientInfo> itClientList = clientInfos.iterator();
						long now = System.currentTimeMillis();
						ClientInfo checkedClient;

						List<String> listClient = clientInfos.stream()
								.map(ClientInfo::toString)
								.collect(Collectors.toList());
						listClient.forEach(System.out::println);

						while (itClientList.hasNext()){
							checkedClient = itClientList.next();
							if((now - checkedClient.getLastActiveTime()) > LIMITED_TIME_T){
								System.out.println("Client " + checkedClient.getClientID() + "will be removed");
								itClientList.remove();
							}
						}

						System.out.println("Sleep for 20s ...");
					} finally {
						// System.out.println("CheckInactiveClient : releasing lock...");
						semaphore.release();
						//System.out.println("CheckInactiveClient : available Semaphore permits now: " + semaphore.availablePermits());
					}
					Thread.sleep(1000 * 50); // after checking thread sleep for 50s
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		}
	}

	public static void main(String[] args) {
		List<ClientInfo> clientInfos = new ArrayList<>();
		CheckInactiveClient checker = new CheckInactiveClient(clientInfos);
		System.setProperty("java.security.policy", "file:./test.policy");

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}


		try {
			ImplMessageService obj = new ImplMessageService(40, clientInfos, semaphore);

			MessageService stub = (MessageService) UnicastRemoteObject.exportObject(obj, 0);

			Registry registry = LocateRegistry.getRegistry(SERVICE_PORT);
			checker.start();
			registry.rebind(SERVICE_NAME, stub);

			System.err.println("Server ready");


		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}


	}
}
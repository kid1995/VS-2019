import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Client extends Application {
    private static final String QUIT = "Q";
    private static final String GET = "G";
    private static final String SEND = "S";
	private static final int SERVICE_PORT = 1099;
	private static final String SERVICE_NAME = "MessageService";
	// private static final String SERVER_IP = "192.168.178.20";
	// private static final String SERVER_IP = "192.168.0.113";
	private static final String SERVER_IP = "141.22.27.107";

	private Registry registry;
	private MessageService stub;

	@Override
	public void start(Stage stage) throws Exception {
		// Getting the registry
		try {
			registry = LocateRegistry.getRegistry(SERVER_IP, SERVICE_PORT);
		} catch (RemoteException e) {
			// e.printStackTrace();
		}
		// Looking up the registry for the remote object
		try {
			stub = (MessageService) registry.lookup(SERVICE_NAME);
		} catch (Exception e) {

		}
		// Calling the remote method using the obtained object
		String ip = "";
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			ip = socket.getLocalAddress().getHostAddress();
			System.out.println("current host ip: " + ip);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		Text text = new Text("");
		text.setX(50);
		text.setY(50);
		text.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

		Text infoText = new Text("");
		infoText.setX(50);
		infoText.setY(260);
		infoText.setFill(Color.GREEN);
		infoText.setFont(Font.font("Verdana", 15));

		Text cmdInfo = new Text("Command Info: \n \n" + "Q: Quits out of the application \n \n"
				+ "G: Gets a message from the Delivery Queue \n \n" + "S: Sends a message to the Delivery Queue");
		cmdInfo.setX(350);
		cmdInfo.setY(110);
		cmdInfo.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

		Text clientIDText = new Text("");
		clientIDText.setX(550);
		clientIDText.setY(20);
		clientIDText.setFont(Font.font("Verdana", FontWeight.BOLD, 9));

		Text msgBoxText = new Text("Message Box:");
		msgBoxText.setX(50);
		msgBoxText.setY(170);
		msgBoxText.setFont(Font.font("Verdana", FontWeight.BOLD, 10));

		// Creating a text filed
		TextField textField = new TextField();

		// Setting the position of the text field
		textField.setLayoutX(50);
		textField.setLayoutY(100);

		TextField msgField = new TextField();
		msgField.setLayoutX(50);
		msgField.setLayoutY(180);

		textField.setOnAction(new EventHandler<ActionEvent>() {
			String msg = "";
			String clientID = "";

			@Override
			public void handle(ActionEvent event) {
				text.setText("Please enter your command");
				if (event.getEventType().equals(ActionEvent.ACTION)) {
					if (clientID.equals("")) {
                        clientID = textField.getText();
                        clientIDText.setText("ClientID: " + clientID);
                        textField.clear();
                    } else {
						switch (textField.getText()) {
						case QUIT:
							textField.clear();
							try {
								Platform.exit();
								System.exit(0);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						case GET:
							textField.clear();
							for (int i = 0; i >= 0; i--) {
								try {
									msg = stub.nextMessage(clientID);
								} catch (RemoteException e) {
									infoText.setText("Message could not be got!");
									remoteMethodeHandle();
									i++;
									System.out.println("Get Exception");
									// e.printStackTrace();
								}
							}
							if (msg != null) {

								infoText.setText("Received message: " + msg);
							} else {
								infoText.setText("No new message !!!");
							}
							text.setText("Please enter your command!");
							break;
						case SEND:
							textField.clear();
							text.setText("Please enter your message in the textfield below!");
							msgField.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent event) {
									if (event.getEventType().equals(ActionEvent.ACTION)) {
										msg = msgField.getText();
										System.out.println(msg);
										for (int i = 0; i >= 0; i--) {
											try {
												stub.newMessage(clientID, msg);
												msgField.clear();
											} catch (RemoteException e) {
												infoText.setText("Message could not be sent!");
												remoteMethodeHandle();
												i++;
												System.out.println("Send Exception");
												// e.printStackTrace();
											}
										}
										infoText.setText("Message sent!");
										text.setText("Please enter your command!");
									}
								}

							});
							break;
						}
				}
				}
			}
		});

		// Creating a Group object
		Group root = new Group(textField, text, msgField, infoText, cmdInfo, clientIDText, msgBoxText);

		// Creating a scene object
		Scene scene = new Scene(root, 800, 300);
		stage.setTitle(ip);
		stage.setScene(scene);
		stage.show();
	}

	private void remoteMethodeHandle() {
		try {
			registry = LocateRegistry.getRegistry(SERVER_IP, SERVICE_PORT);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			System.out.println("LOCATE REGISTRY Exception");
			// e1.printStackTrace();
		}
		try {
			stub = (MessageService) registry.lookup(SERVICE_NAME);
		} catch (RemoteException | NotBoundException e1) {
			System.out.println("LOOKUP Exception");
			// TODO Auto-generated catch block
			// e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.setProperty("java.security.policy", "file:./test.policy");

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		launch();
	}
}

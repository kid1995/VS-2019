
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
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client extends Application {
	private static final String QUIT = "Quit";
	private static final String GET = "Get";
	private static final String SEND = "Send";
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
		text.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
		text.setText("Please enter your ID");

		Text infoText = new Text("");
		infoText.setX(50);
		infoText.setY(260);
		infoText.setFill(Color.GREEN);
		infoText.setFont(Font.font("Verdana", 15));

		Text clientIDText = new Text("");
		clientIDText.setX(550);
		clientIDText.setY(20);
		clientIDText.setFont(Font.font("Verdana", FontWeight.BOLD, 9));

		Text msgBoxText = new Text("Message Box:");
		msgBoxText.setX(50);
		msgBoxText.setY(170);
		msgBoxText.setFont(Font.font("Verdana", FontWeight.BOLD, 10));

		TextField msgField = new TextField();

		ComboBox<String> comboBox = new ComboBox<String>();
		comboBox.getItems().addAll("Quit", "Send", "Get");

		TextArea textArea = new TextArea("");

		msgField.setOnAction(new EventHandler<ActionEvent>() {
			String msg = "";
			String clientID = "";

			@Override
			public void handle(ActionEvent event) {
				if (clientID.equals("")) {
					clientID = msgField.getText();
					clientIDText.setText(clientID);
					msgField.clear();
				}
				comboBox.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						System.out.println("Test1");

						switch (comboBox.getValue()) {
						case QUIT:
							System.out.println("QUIT");
							try {
								Platform.exit();
								System.exit(0);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						case GET:
							try {
								msg = stub.nextMessage(clientID);
							} catch (RemoteException e) {
								infoText.setText("Message could not be recevied!");
								System.out.println("Get Exception");
								Platform.exit();
								System.exit(0);
							}
							if (msg != null) {

								infoText.setText("Received message: " + msg);
							} else {
								infoText.setText("No new message !!!");
							}
							text.setText("Please enter your command!");
							break;
						case SEND:
							text.setText("Please enter your message in the textfield below!");
							msgField.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent event) {
									if (event.getEventType().equals(ActionEvent.ACTION)) {
										msg = msgField.getText();
										System.out.println(msg);
										for (int i = 0; i >= 0; i--) {
//											try {
//												stub.newMessage(clientID, msg);
//												msgField.clear();
//											} catch (RemoteException e) {
//												infoText.setText("Message could not be sent!");
//												remoteMethodeHandle();
//												i++;
//												System.out.println("Send Exception");
//												// e.printStackTrace();
//											}
											System.out.println("asd");
										}
										infoText.setText("Message sent!");
										text.setText("Please enter your command!");
									}
								}

							});
							break;
						}
					}
				});
			}
		});

		// Creating a scene object

		// GridPane grid = new GridPane();
		// grid.add(comboBox,30,0);
		// grid.add(new Label("Commands:"), 2, 0);
		//
		// // Creating a Group object
		// Group root = new Group();
		// root.getChildren().add(grid);
		// Scene scene = new Scene(root, 800, 300);
		// stage.setTitle(ip);
		// stage.setScene(scene);
		// stage.show();

		Scene scene = new Scene(new Group(), 450, 250);
		GridPane grid = new GridPane();
		grid.setVgap(4);
		grid.setHgap(10);
		grid.setPadding(new Insets(5, 5, 5, 5));
		grid.add(new Label("Commands :"), 0, 0);
		grid.add(comboBox, 1, 0);
		grid.add(new Label("ClientID:"), 2, 0);
		grid.add(clientIDText, 3, 0);
		grid.add(new Label("Messages: "), 0, 1);
		grid.add(msgField, 1, 1, 3, 1);

		Group root = (Group) scene.getRoot();
		root.getChildren().add(grid);
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

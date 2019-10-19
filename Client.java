
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Client extends Application {
	private static final int TOLERANCE__TIME = 10;
	private static final String QUIT = "Quit";
	private static final String GET = "Get";
	private static final String SEND = "Send";
	private static final int SERVICE_PORT = 1099;
	private static final String SERVICE_NAME = "MessageService";

	private Registry registry;
	private MessageService stub;
	private String serverIP;
	private String clientID;
	private String localIP;
	private String ip = "";

	@Override
	public void start(Stage stage) throws Exception {

		// Calling the remote method using the obtained object
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			localIP = socket.getLocalAddress().getHostAddress();
			System.out.println("current host ip: " + localIP);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		Scene scene = new Scene(new Group(), 1200, 450);
		GridPane grid = new GridPane();
		TextField ipInput = new TextField();

		Text serverIPText = new Text("Please put in Server IP, you wish to connect to");
		serverIPText.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

		Text infoText = new Text("");
		infoText.setX(50);
		infoText.setY(260);
		infoText.setFont(Font.font("Verdana", 15));

		Text clientIDText = new Text("");
		clientIDText.setX(550);
		clientIDText.setY(20);
		clientIDText.setFont(Font.font("Verdana", FontWeight.BOLD, 9));

		TextField msgField = new TextField();

		ComboBox<String> comboBox = new ComboBox<String>();
		comboBox.getItems().addAll("Quit", "Send", "Get");

		Button getButton = new Button("Get Message");
		Button getAllButton = new Button("Get all Messages");

		ipInput.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				 serverIP = ipInput.getText();
				clientID = localIP + "@" + serverIP;
				clientIDText.setText(clientID);
				// Getting the registry
				try {
					registry = LocateRegistry.getRegistry(serverIP, SERVICE_PORT);
				} catch (RemoteException e) {
					// e.printStackTrace();
				}
				// Looking up the registry for the remote object
				try {
					stub = (MessageService) registry.lookup(SERVICE_NAME);
				} catch (Exception e) {

				}
				System.out.println("2" + serverIP);
				grid.getChildren().remove(ipInput);
				grid.getChildren().remove(serverIPText);
				grid.add(new Label("Commands :"), 0, 0);
				grid.add(comboBox, 1, 0);
				grid.add(new Label("ClientID:"), 2, 0);
				grid.add(clientIDText, 3, 0);
				grid.add(new Label("Input: "), 0, 1);
				grid.add(msgField, 1, 1, 3, 1);

			}

		});

		comboBox.setOnAction(new EventHandler<ActionEvent>() {
			String msg = "";
			String clientID = ip;

			@Override
			public void handle(ActionEvent event) {
				switch (comboBox.getValue()) {
				case QUIT:
					try {
						Platform.exit();
						System.exit(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case GET:
					infoText.setText("");
					grid.add(getButton, 1, 3);
					grid.add(getAllButton, 3, 3);
					getButton.setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							try {
								msg = stub.nextMessage(clientID);
							} catch (RemoteException e) {
								infoText.setFill(Color.RED);
								infoText.setText("Message could not be recevied!");
								System.out.println("Get Exception");
								Platform.exit();
								System.exit(0);
							}
							if (msg != null) {
								infoText.setFill(Color.GREEN);
								infoText.setText("Received message: " + msg);
							} else {
								infoText.setFill(Color.RED);
								infoText.setText("No new message !!!");
							}
						}

					});
					// Get all Messages
					getAllButton.setOnAction(new EventHandler<ActionEvent>() {
						String allMsgs = "";

						@Override
						public void handle(ActionEvent arg0) {
							allMsgs = "Received messages: \n";
							do {
								try {
									msg = stub.nextMessage(clientID);
									if (msg != null) {
										allMsgs += msg;
										allMsgs += "\n";
										infoText.setFill(Color.GREEN);
										infoText.setText(allMsgs);
									}
								} catch (RemoteException e) {
									infoText.setFill(Color.RED);
									infoText.setText("Message could not be recevied!");
									System.out.println("Get Exception");
									Platform.exit();
									System.exit(0);
								}
							} while (msg != null);

						}

					});
					break;
				case SEND:
					infoText.setText("");
					grid.getChildren().remove(getButton);
					grid.getChildren().remove(getAllButton);
					msgField.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							boolean quit = false;
							long startTime = System.currentTimeMillis();
							long waitedTime = 0;
							if (event.getEventType().equals(ActionEvent.ACTION)) {
								msg = msgField.getText();
								System.out.println(msg);
								while (waitedTime <= TOLERANCE__TIME) {
									try {
										stub.newMessage(clientID, msg);
										msgField.clear();
										quit = false;
										break;
									} catch (RemoteException e) {
										infoText.setFill(Color.RED);
										infoText.setText("Message could not be sent!");
										remoteMethodeHandle();
										waitedTime = (System.currentTimeMillis() - startTime) / 1000;
										System.out.println("Send Exception");
										quit = true;
										// e.printStackTrace();
									}
								}
								if (quit) {
									Platform.exit();
									System.exit(0);
								}
								infoText.setFill(Color.GREEN);
								infoText.setText("Message: \"" + msg + "\" sent!");
							}
						}

					});
					break;
				}
			}
		});

		grid.getColumnConstraints().add(new ColumnConstraints(100)); // column 0 is 100 wide
		grid.getColumnConstraints().add(new ColumnConstraints(100)); // column 1 is 200 wide
		grid.setVgap(4);
		grid.setHgap(10);
		grid.setPadding(new Insets(6, 6, 6, 6));
		grid.add(ipInput, 0, 5);
		grid.add(serverIPText, 0, 0);
		grid.add(infoText, 1, 5);

		Group root = (Group) scene.getRoot();
		root.getChildren().add(grid);
		stage.setScene(scene);
		stage.show();

	}

	private void remoteMethodeHandle() {
		try {
			registry = LocateRegistry.getRegistry(serverIP, SERVICE_PORT);
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

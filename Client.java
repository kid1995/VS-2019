
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
	// Toleranzintervall für 'at-least-once'
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

	@Override
	public void start(Stage stage) throws Exception {

		// Herausfinden der eigenen IP-Adresse
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			localIP = socket.getLocalAddress().getHostAddress();
			System.out.println("current host ip: " + localIP);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// JAVAFX CONFIG START
		Scene scene = new Scene(new Group(), 1200, 450);
		GridPane grid = new GridPane();
		TextField ipInput = new TextField();

		Text serverIPRequest = new Text("Please put in Server IP, you wish to connect to");
		serverIPRequest.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

		Text infoOutput = new Text("");
		infoOutput.setX(50);
		infoOutput.setY(260);
		infoOutput.setFont(Font.font("Verdana", 15));

		Text clientIDText = new Text("");
		clientIDText.setX(550);
		clientIDText.setY(20);
		clientIDText.setFont(Font.font("Verdana", FontWeight.BOLD, 9));

		TextField msgField = new TextField();

		ComboBox<String> comboBox = new ComboBox<String>();
		comboBox.getItems().addAll("Quit", "Send", "Get");

		Button getButton = new Button("Get Message");
		Button getAllButton = new Button("Get all Messages");

		grid.getColumnConstraints().add(new ColumnConstraints(100)); // column 0 is 100 wide
		grid.getColumnConstraints().add(new ColumnConstraints(100)); // column 1 is 200 wide
		grid.setVgap(4);
		grid.setHgap(10);
		grid.setPadding(new Insets(6, 6, 6, 6));
		grid.add(ipInput, 0, 5);
		grid.add(serverIPRequest, 0, 0);
		grid.add(infoOutput, 1, 5);

		Group root = (Group) scene.getRoot();
		root.getChildren().add(grid);
		stage.setScene(scene);
		stage.show();
		// JAVAFX CONFIG END

		ipInput.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// Server IP von Benutzerinput
				serverIP = ipInput.getText();
				clientID = localIP + "@" + serverIP;
				clientIDText.setText(clientID);
				remoteMethodeHandle();
				// GUI von ServerIP Request zu Chat GUI ändern
				grid.getChildren().remove(ipInput);
				grid.getChildren().remove(serverIPRequest);
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
					infoOutput.setText("");
					grid.add(getButton, 1, 3);
					grid.add(getAllButton, 3, 3);
					getButton.setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							try {
								// Nachricht abrufen
								msg = stub.nextMessage(clientID);
							} catch (RemoteException e) {
								// Server nicht erreichbar --> Beenden des Programms
								Platform.exit();
								System.exit(0);
							}
							// Message vorhanden?
							if (msg != null) {
								infoOutput.setFill(Color.GREEN);
								infoOutput.setText("Received message: " + msg);
							} else {
								infoOutput.setFill(Color.RED);
								infoOutput.setText("No new message !!!");
							}
						}

					});
					// Get alle Messages
					getAllButton.setOnAction(new EventHandler<ActionEvent>() {
						// String zum Zusammenfügen alles Messages
						String allMsgs = "";

						@Override
						public void handle(ActionEvent arg0) {
							boolean noMsg = true;
							allMsgs = "Received messages: \n";
							do {
								try {
									msg = stub.nextMessage(clientID);
									if (msg != null) {
										// Message hinzufügen und in GUI anzeigen
										noMsg = false;
										allMsgs += msg;
										allMsgs += "\n";
										infoOutput.setFill(Color.GREEN);
										infoOutput.setText(allMsgs);
									}
								} catch (RemoteException e) {
									// Server nicht erreichbar --> Beenden des Programms
									Platform.exit();
									System.exit(0);
								}
							} while (msg != null);
							// Wenn gar keine Message vorhanden
							if (noMsg) {
								infoOutput.setFill(Color.RED);
								infoOutput.setText("No new message !!!");
							}
						}

					});
					break;
				case SEND:
					infoOutput.setText("");
					grid.getChildren().remove(getButton);
					grid.getChildren().remove(getAllButton);
					msgField.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							boolean quit = false;
							long startTime = System.currentTimeMillis();
							long waitedTime = 0;
							// Zu sendende Message
							msg = msgField.getText();
							// Toleranzintervall, wenn Server nicht erreichbar
							while (waitedTime <= TOLERANCE__TIME) {
								try {
									stub.newMessage(clientID, msg);
									msgField.clear();
									// Wenn eine Nachricht erhalten wurde, soll Programm nicht beendet werden
									quit = false;
									break;
								} catch (RemoteException e) {
									infoOutput.setFill(Color.RED);
									infoOutput.setText("Message could not be sent!");
									// Registry neu erreichen
									remoteMethodeHandle();
									// Zeit aktualisieren
									waitedTime = (System.currentTimeMillis() - startTime) / 1000;
									System.out.println("Server unreachable!");
									// Wenn die Schleife durchgelaufen ist und der Server nicht erreicht wurde, wird
									// das Programm beendet
									quit = true;
								}
							}
							// Server erreicht?
							if (quit) {
								Platform.exit();
								System.exit(0);
							}
							infoOutput.setFill(Color.GREEN);
							infoOutput.setText("Message: \"" + msg + "\" sent!");
						}

					});
					break;
				}
			}
		});

	}

	private void remoteMethodeHandle() {
		// Registry bekommen
		try {
			registry = LocateRegistry.getRegistry(serverIP, SERVICE_PORT);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		// Lookup der Registry für entferntes Objekt
		try {
			stub = (MessageService) registry.lookup(SERVICE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch();
	}
}

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


    private static final int  SERVICE_PORT = 1099;
    private static final String SERVICE_NAME ="MessageService";
    private static final String SERVER_IP ="127.0.0.1";


	@Override
	public void start(Stage stage) throws Exception {
        // Getting the registry
        Registry registry = LocateRegistry.getRegistry(SERVER_IP, SERVICE_PORT);
        // Looking up the registry for the remote object
        MessageService stub = (MessageService) registry.lookup(SERVICE_NAME);
		// Calling the remote method using the obtained object

		Text text = new Text("Please enter your ClientID");
		text.setX(50);
		text.setY(50);
		text.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

		Text infoText = new Text("");
		infoText.setX(50);
		infoText.setY(220);
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
						clientIDText.setText("ClientID: " + clientID );
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
							try {
								msg = stub.nextMessage(clientID);
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
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
									text.setText("Please enter your message2");
									if (event.getEventType().equals(ActionEvent.ACTION)) {
										try {
											msg = msgField.getText();
											System.out.println(msg);
											stub.newMessage(clientID, msg);
											msgField.clear();
										} catch (RemoteException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										infoText.setText("Message : \"" + msg + "\" sent!");
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
		Group root = new Group(textField, text, msgField, infoText, cmdInfo,clientIDText,msgBoxText);

		// Creating a scene object
		Scene scene = new Scene(root, 800, 300);
		stage.setTitle("VS_A1");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
        System.setProperty("java.security.policy","file:./test.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
		launch();
	}

}

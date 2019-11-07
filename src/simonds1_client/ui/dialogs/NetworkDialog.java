/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import simonds1.core.Network;


/**
 *
 * @author Olagoke Adedamola Farouq
 */
public class NetworkDialog extends ControlDialog{

    public NetworkDialog(String title, String netEmail) {
        super(title);
        init();
        this.email = netEmail;
        this.buildInputs();
        this.buildBut();
        this.addEvents();
        dialog.showAndWait();
    }
    
    private void init(){
        setupDialog();
        if(Network.connected.getValue())
            network = Network.getContext();
    }
    private void setupDialog(){
        this.userRoot = new GridPane();
        this.userRoot.setPadding(new Insets(2, 2, 2, 3));
        this.userRoot.setHgap(5.0);
        this.userRoot.setVgap(9.0); 
        dialog.getDialogPane().setContent(userRoot);
        dialog.setHeaderText("Server Connection");
    }
    private void buildInputs(){
        {
            Label label = new Label("Server Host");
            this.hostField = new TextField(Network.serverHost);
            this.hostField.setPromptText("Enter server host");
            this.hostField.setDisable(Network.connected.getValue());
            this.userRoot.addRow(0, label, this.hostField);
        }
        {
            Label label = new Label("Server Port");
            this.portField = new TextField(String.valueOf(Network.serverPort));
            this.portField.setPromptText("Enter server port");
            this.hostField.setDisable(Network.connected.getValue());
            this.userRoot.addRow(1, label, this.portField);
        }
        {
            Label label = new Label("Network Name");
            this.netNameField = new TextField(this.email);
            this.netNameField.setTooltip(new Tooltip("The Default project Email"));
            this.netNameField.setDisable(true);
            this.userRoot.addRow(2, label, this.netNameField);
        }
    }
    private void buildBut(){
        this.connectBut = new Button(Network.connected.getValue() ? "Disconnect" :"Connect");
        this.userRoot.addRow(3, this.connectBut);
        
        this.notif = new Label();
        this.notif.setTextFill(Paint.valueOf("blue")); 
        this.userRoot.add(notif, 0, 4, 4, 1);
    }
    private void connectNetwork(){
        if(this.network != null)
            if(this.network.serverOnline())
                return;
        if(this.hostField.getText().isEmpty() || this.portField.getText().isEmpty()){
            notif.setText("Cannot leave Any Entry Empty!");
            return;
        }
        this.network = Network.getNewContext(hostField.getText(), Integer.parseInt(portField.getText()), this.email);
        //notif.setText(String.valueOf(network.hasError()));
        if(network.serverOnline()){
            notif.setText("Server connected");
        }else{
            notif.setText(network.getErrorStr());
            this.network = null;
        }
    }
    private void disconnectNetwork(){
        if(network == null)
            System.out.println("JESUS PLEASE HELP ME!! " + Network.connected.getValue());
        if(!network.disconnect())
            notif.setText("Error: Couldn't disconnect server!!");
        else{
            notif.setText("Server disconnected");
            this.network = null;
        }
    }
    private void addEvents(){
        //this.portField.textFormatterProperty()
        this.connectBut.setOnAction(e ->  {
            if(Network.connected.getValue()){
                disconnectNetwork();
            }else{
                connectNetwork();
            }
        });
        
        Network.connected.addListener((ob, ov, nv) -> {
            boolean val = nv;
            connectBut.setText(nv ? "Disconnect" :"Connect");
            System.out.println("Server now "+ (nv ? "Connected" :"Disconnected"));
            this.hostField.setDisable(nv);
            this.portField.setDisable(nv);
        });
    }
    
    private TextField hostField,
            portField, netNameField;
    private Button connectBut;
    private Label notif;
    private Network network;
    private GridPane userRoot;
    private final String email;
}

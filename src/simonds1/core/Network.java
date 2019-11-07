/* 
    Simon Design Suite client app
 */
package simonds1.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javafx.beans.property.SimpleBooleanProperty;
import simonds1.core.transport.DataBox;

/**
 * The network handling class
 * @author Olagoke Adedamola Farouq
 */
public class Network extends socketClient{
    
    public static String serverHost = "localhost"; //get machine's public IP here
    public static int serverPort = 6677;
    public static SimpleBooleanProperty connected = new SimpleBooleanProperty(false);
    private static Network instnce = null;
    
    public Network(String host, int port){
        this.host = host;
        this.port = port;
        this.connect();
    }
    public Network(String host){
        this.host = host;
    }
    public static Network getContext(){
        return instnce;
    }
    public static Network getNewContext(String host, int port, String netName){
        if(instnce == null)
            instnce = new Network(host, port);
        else if(!instnce.serverOnline()){
            instnce = null;
            instnce = new Network(host, port);
        }else if(!instnce.host.equals(host) || instnce.port != port){
            instnce.close();
            instnce = null;
            instnce = new Network(host, port);
        }
        if(instnce.serverOnline())
            if(!instnce.establishNetworkShake(netName)){ //illegal network connection
                //could use some encryption here please
                instnce.close();
                instnce.error = "Secure Server Handshake Failed!";
            }
        return instnce;
    }
    public boolean establishNetworkShake(String netName){
        DataBox data = new DataBox();
        data.config.put("main_module","comm");
        data.config.put("sub_module","set_client_id");
        data.stringPayload.put("client_ip", getIP());
        data.stringPayload.put("client_email", netName);
        if(!this.send(data))
            return false;
        data = this.recieve();
        if(data == null)
            return false;
        //if(data.flag < 10)
            //return false;
        //later we could check the return data for anything
        //knowing that it sent back successfully is enough for now
        return true;
    }
    public boolean serverOnline(){
        return this.isConnected();
    }
    public boolean dataReceivable(){
        return !this.socket.isInputShutdown();
    }
    public String getErrorStr(){
        return this.error;
    }
    public boolean hasError(){
        return (this.errno > 0);
    }
    public boolean disconnect(){
        return this.close();
    }
    public static void SDisconnect(){
        instnce.disconnect();
    }
    public String getIP(){
        return socket.getInetAddress().getHostAddress();
    }
}

abstract class socketClient {

    public String host;
    public int port = 6677;
    protected Socket socket = null;
    protected int errno = 0;
    protected String error = null;

    protected boolean connect() {
        try {
            this.socket = new Socket(this.host, this.port);
            socket.setSoTimeout(4000);
            Network.connected.setValue(true);
            return true;
        } catch (IOException ex) {
            this.errno++;
            this.error = ex.getMessage();
            Network.connected.setValue(false);
            return false;
        }
    }

    public boolean send(DataBox objData) {
        try{
            ObjectOutputStream oStream = new ObjectOutputStream(this.socket.getOutputStream());
            oStream.writeObject(objData);
            oStream.flush();
            return true;
        } catch (IOException ex) {
            this.errno++;
            this.error = ex.getMessage();
            return false;
        }
    }

    public DataBox recieve() {
        try {
            ObjectInputStream iStream = new ObjectInputStream(this.socket.getInputStream());
            return (DataBox)iStream.readObject();
        } catch(SocketTimeoutException ex){
            this.errno++;
            this.error = "Server took too Long to respond! Please try to Reconnect.";
            return null;
        }catch (IOException | ClassNotFoundException ex) {
            this.errno++;
            this.error = ex.getMessage();
            return null;
        }
    }

    protected boolean isConnected() {
        if(this.socket != null)
            Network.connected.setValue(!this.socket.isClosed() && this.errno == 0);
        else
            Network.connected.setValue(false);
        
        return Network.connected.getValue();
    }

    protected boolean close() {
        try {
            this.socket.close();
            Network.connected.setValue(false);
            return true;
        } catch (IOException ex) {
            this.errno++;
            this.error = ex.getMessage();
            return false;
        }
    }
}
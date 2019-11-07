/* 
    Simon Design Suite version  1.0 
 */
package simonds1.core.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class boxes up all the data that needs to be sent over
 * @author ADEDAMOLA
 */
public class DataBox implements Serializable {
    public HashMap<String, String> config = new HashMap<>(); //payload index, main module, sub module
    public int flag = 10; // <10 = success, >10 = error
    public String errorString = "";
    public HashMap<String, String> stringPayload = new HashMap<>();
    public HashMap<String, ArrayList<String>> arrayPayload = new HashMap<>();
    //convinience method
    public void setError(String errStr, int flag){
        this.errorString = errStr;
        this.flag = flag;
    }
    //payload index can be accessed by config values
    public HashMap<String, DataBoxPayload> payload = new HashMap<>();
}

/* 
    Simon Design Suite version  1.0 
 */
package simonds1.core.transport;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Ephrahim Adedamola
 */
public class SDSNode implements Serializable {
    public SDSNode(){}
    public void addStringProp(String key, String value){
        stringProps.put(key, value);
    }
    public void addNumberProp(String key, Number value){
        numberProps.put(key, value);
    }
    public void addStringMapProp(String key, HashMap<String, String> value){
        stringMapProps.put(key, value);
    }
    public void addNumberMapProp(String key, HashMap<String, Number> value){
        numberMapProps.put(key, value);
    }
    public void addBoolMapProp(String key, HashMap<String, Boolean> value){
        boolMapProps.put(key, value);
    }
    
    public String getStringProp(String key){
        return stringProps.get(key);
    }
    public Number getNumberProp(String key){
        return numberProps.get(key);
    }
    public HashMap<String, String> getStringMapProp(String key){
        return stringMapProps.get(key);
    }
    public HashMap<String, Number> getNumberMapProp(String key){
        return numberMapProps.get(key);
    }
    public HashMap<String, Boolean> getBoolMapProp(String key){
        return boolMapProps.get(key);
    }
    private final HashMap<String, Number> numberProps = new HashMap<>();
    private final HashMap<String, String> stringProps = new HashMap<>();
    private final HashMap<String, HashMap<String, String>> stringMapProps = new HashMap<>();
    private final HashMap<String, HashMap<String, Number>> numberMapProps = new HashMap<>();
    private final HashMap<String, HashMap<String, Boolean>> boolMapProps = new HashMap<>();
    public String shape = null;
}

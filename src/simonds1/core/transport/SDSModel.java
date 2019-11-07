/* 
    Simon Design Suite version  1.0 
 */
package simonds1.core.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Olagoke Adedamola Farouq
 */
public class SDSModel implements Serializable, DataBoxPayload{
    public SDSModel(ArrayList<SDSNode> shapes){
        this.shapes = shapes;
    }
    @Override
    public String getTitle(){
        return title;
    }
    @Override
    public void setTitle(String title){
        this.title = title;
    }
    public SDSModel(){}
    public void addNumberProp(String key, Number value){
        numberProps.put(key, value);
    }
    public void addStringProp(String key, String value){
        stringProps.put(key, value);
    }
    public String getStringProp(String key){
        return stringProps.get(key);
    }
    public Number getNumberProp(String key){
        return numberProps.get(key);
    }
    private final HashMap<String, Number> numberProps = new HashMap<>();
    private final HashMap<String, String> stringProps = new HashMap<>();
    public ArrayList<SDSNode> shapes = new ArrayList<>();
    public String message, title;
    public int flag = 10; // 10> = success, 0 to 10 => error
}

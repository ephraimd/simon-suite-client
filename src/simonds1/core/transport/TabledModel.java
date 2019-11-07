/* 
    Simon Design Suite version  1.0 
 */
package simonds1.core.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** 
 * this class is used to represent table data
 * @author ADEDAMOLA
 * @param <T> to support multiple table value types
 */
public class TabledModel<T> implements DataBoxPayload, Serializable{
    public TabledModel(){
        this.table = new HashMap<>();
    }
    public TabledModel addColumnKey(String key){
        if(this.table.get(key) != null)
            return null;
        
        table.put(key, new ArrayList<>());
        return this;
    }
    public TabledModel addColumnCell(String key, T value){
        if(this.table.get(key) == null)
            return null;
        
        table.get(key).add(value);
        return this;
    }
    public ArrayList<T> getColumn(String key){
        if(this.table.get(key) == null)
            return null;
        
        return table.get(key);
    }
    public ArrayList<HashMap<String, String>> getModel(){
        ArrayList<HashMap<String, String>> tmp = new ArrayList<>();
        for(int i=0; i< getRowCount();i++){
            HashMap<String, String> tmph = new HashMap<>();
            for(String val: table.keySet()){
                tmph.put(val, String.valueOf(table.get(val).get(i)));
            }
            tmp.add(tmph);
        }
        return tmp;
    }
    public int getColumnCount(){
        return table.keySet().size();
    }
    public ArrayList<T> getRow(int rowno){
        ArrayList<T> tmp = new ArrayList<>();
        
        table.entrySet().forEach((it) -> {
            if(it.getValue().size()-1 < rowno)
                return;
            tmp.add(it.getValue().get(rowno));
        });
        
        return tmp;
    }
    public int getRowCount(){
        return table.values().stream().findFirst().get().size();
    }
    public boolean removeColumnCell(String key, T value){
        if(this.table.get(key) == null)
            return false;
        
        table.get(key).remove(value);
        return true;
    }
    public boolean removeColumn(String key){
        if(this.table.get(key) == null)
            return false;
        
        table.remove(key);
        return true;
    }
    public boolean removeRow(int rowno){
        table.entrySet().forEach((it) -> {
            if(it.getValue().size()-1 < rowno)
                return;
            it.getValue().remove(rowno);
        });
        return true;
    }
    
    public HashMap<String, ArrayList<T>> table;
    private String title;

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
}

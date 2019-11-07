/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.shapes;

import java.util.HashMap;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import simonds1.core.Resources;
import simonds1.core.SignalSlot;
import simonds1.core.transport.SDSNode;
import simonds1_client.ui.pad.Canvas2D;

/**
 * Custom Line object
 *
 * @author Olagoke Adedamola Farouq
 */
public final class CSlab extends Rectangle implements CShapes {

    /**
     * Create element shape
     *
     * @param startX
     * @param startY
     * @param width
     * @param isResultMode
     */
    public CSlab(double startX, double startY, double width, boolean isResultMode) {
        super(startX, startY, width, 10);//update to use scale
        this.isResultMode = isResultMode;
        if (!isResultMode) {
            this.init();
        }

    }

    public CSlab(boolean isResultMode) {
        this.isResultMode = isResultMode;
        this.setStrokeWidth(Canvas2D.strokeWidth * 2);
        setHeight(10);
    }

    public CSlab getThis() {
        //just a convinience method
        return this;
    }

    public void init() {
        this.setCursor(Cursor.HAND);
        this.setType();
    }

   
    @Override
    public void destroy() {
        //
    }

    @Override
    public SDSNode getSerialProps() {
        SDSNode tmp = new SDSNode();
        tmp.addStringProp("id", String.valueOf(getShapeId()));
        tmp.addNumberProp("fv", fv.get());
        tmp.addNumberProp("width", getWidth());
        tmp.addNumberProp("x", getX());
        tmp.addNumberProp("y", getY());
        tmp.addStringProp("result_mode", String.valueOf(isResultMode));
        tmp.addStringProp("steel_grade", steelGrade);
        tmp.addStringMapProp("str_props", stringProps);
        tmp.addNumberMapProp("num_props", numberProps);
        tmp.addBoolMapProp("bool_props", boolProps);
        tmp.shape = "beam";
        return tmp;
    }

    @Override
    public int getShapeId() {
        return id.get();
    }

    @Override
    public void move(double posX, double posY) {//move is disabled for this shape
        //this.startFullDrag();
        //lengthProperty.add(getLength());
    }

    @Override
    public void deselect() {
        //
    }

    @Override
    public void select() {
        //
    }
    public String getStringProp(String key){
        return stringProps.containsKey(key) ? stringProps.get(key) : ""; 
    }
    public double getNumberProp(String key){
        return numberProps.containsKey(key) ? numberProps.get(key).doubleValue() : 0; 
    }
    public boolean getBoolProp(String key){
        return boolProps.containsKey(key) ? boolProps.get(key) : false; 
    }
    public String setStringProp(String key, String value){
        return stringProps.put(key, value);
    }
    public Number setNumberProp(String key, Number value){
        return numberProps.put(key, value);
    }
    public boolean setBoolProp(String key, boolean value){
        return boolProps.put(key, value);
    }
    public double getLength() {
        return getX() + getWidth();
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setType() {
        this.type = -1; //not valid yet
        this.id.set(9);//just there
        this.title = "Slab" + this.id.get();
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public CSlab getObjectContext() {
        return this;
    }

    public boolean isResultMode = false;
    public int type;
    public SimpleIntegerProperty id = new SimpleIntegerProperty();
    private String title;
    public String steelGrade = "S275";
    public SimpleDoubleProperty fv = new SimpleDoubleProperty(0);
    public HashMap<String, String> stringProps = new HashMap<>();
    public HashMap<String, Number> numberProps = new HashMap<>();
    public HashMap<String, Boolean> boolProps = new HashMap<>();

}

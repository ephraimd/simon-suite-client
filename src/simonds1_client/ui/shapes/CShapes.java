/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.shapes;

import javafx.scene.Node;
import simonds1.core.transport.SDSNode;

/**
 *
 * @author ADEDAMOLA
 */
public interface CShapes{
    public void setType();
    public void setTitle(String title);
    
    public String getTitle();
    public int getShapeId();
    public SDSNode getSerialProps();
    public Node getObjectContext();
    public void deselect();
    public void destroy();
    public void select();
    public void move(double posX, double posY);
    
    public int getType(); //1=line, 2=load, 3=node, 4=rect
    
    default String getTypeStr(){
        switch(getType()){
            case 1:
                return "line";
            case 2:
                return "beam";
            case 3:
                return "node";
            case 4:
                return "column";
        }
        return "null";
    }
}

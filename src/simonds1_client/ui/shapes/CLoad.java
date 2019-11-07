/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.shapes;

import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import simonds1.core.SignalSlot;
import simonds1.core.transport.SDSNode;
import simonds1_client.ui.pad.Canvas2D;

/**
 *
 * @author Olagoke Adedamola Farouq
 */
public final class CLoad  extends ImageView implements CShapes{

    public CLoad(Canvas2D canvas, double posX, double posY){
        super(); 
        this.canvas = canvas;
        this.init();
        this.mkMenus();
        this.mkEvents();
        this.menuActions();
        this.setType();
    }
    public void init(){
        this.setCursor(Cursor.HAND);
    }
    private void mkMenus() {
        this.menu.getItems().addAll(propMenu, copyMenu, deleteMenu);
    }
    private void mkEvents() {
        //hover events
        this.setOnMouseEntered((MouseEvent e) -> {
            this.setEffect(new Glow(0.8));
            this.canvas.isCanvasShapeHovered = true;
        });
        this.setOnMouseExited((MouseEvent e) -> {
            this.canvas.isCanvasShapeHovered = false;
            this.setEffect(null);
        });
        //click events
        this.setOnMouseClicked((MouseEvent e) -> {
            if(e.getButton().equals(MouseButton.PRIMARY))//only if its a left click or tap
                SignalSlot.emitSignal(canvas.windowID, "ShapeClicked", this);
        });
        //contextmenu
        this.setOnContextMenuRequested((e) -> {
            this.menu.show(this, e.getSceneX(), e.getSceneY());
        });
        this.setOnMouseDragged(e -> {
            this.move(e.getX(), e.getY());
        });
    }
    private void menuActions() {
        this.deleteMenu.setOnAction((e) -> {
            this.destroy();
        });
        this.propMenu.setOnAction((e) -> {
            SignalSlot.emitSignal(canvas.windowID, "ShapeClicked", this);
        });
    }
    private void mkDimension(double posX, double posY) {
        //add dimensioning
        Text nodeTxt = new Text(posX, posY, "L" + this.id);
        nodeTxt.setFill(Paint.valueOf("gray"));
        nodeTxt.xProperty().bind(this.xProperty());
        nodeTxt.yProperty().bind(this.yProperty().subtract(6)); 
        canvas.root.getChildren().add(nodeTxt);
        this.visibleProperty().addListener((ob, ov, nv) -> {
            //this will be executed only once (when shape is disabled)
            if(!nv)
                canvas.root.getChildren().remove(nodeTxt);
        });
    }
    @Override
    public void destroy() {
        canvas.root.getChildren().remove(this);
        this.setVisible(false); //learn how to destroy an object
        this.setDisabled(true);
        SignalSlot.emitSignal(canvas.windowID, "ShapeDestroyed", this);
        //--canvas.nodeCount;
    }
    public void deSelectCurrent() {
        this.canvas.selectedShape.deselect();
    }
    public double getNormX(Double x){
        return (x == null ? getX() : x) + 19;
    }
    public double getNormY(Double y){
        return (y == null ? getY() : y) - 21;
    }
    @Override
    public int getShapeId(){
        return this.id;
    }
    @Override
    public SDSNode getSerialProps(){
        //load is attached to a node, so we don't send it over
        return null;
    }
    @Override
    public void move(double posX, double posY){
        this.setX(posX);
        this.setY(posY);
    }
    @Override
    public void deselect(){
        this.setEffect(null); 
        this.canvas.selectedShape = null;
    }

    @Override
    public void select() {
        deSelectCurrent();
        this.setEffect(new Glow(1.2));
        this.canvas.selectedShape = this;
    }
    public CLoad getThis(){
        //just a convinience method
        return this;
    }
    @Override
    public void setTitle(String title) {
        this.title = title;
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
        this.type = 2;
        this.id = ++canvas.loadCount;
        this.title = "Load" + this.id;
    }

    @Override
    public CLoad getObjectContext() {
        return this;
    }
    
    private final Canvas2D canvas;
    public ContextMenu menu = new ContextMenu();
    public int type, id;
    public double loadValue;
    public int direction = 1; //1=vertical, 2=horizontal
    private String title;
    public MenuItem cutMenu = new MenuItem("Cut");
    public MenuItem copyMenu = new MenuItem("Copy");
    public MenuItem propMenu = new MenuItem("Properties");
    public MenuItem duplMenu = new MenuItem("Duplicate");
    public MenuItem deleteMenu = new MenuItem("Delete");
    
}

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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import simonds1.core.SignalSlot;
import simonds1.core.SimonUtil;
import simonds1.core.transport.SDSNode;
import simonds1_client.ui.pad.Canvas2D;

/**
 * Custom Line object
 *
 * @author Olagoke Adedamola Farouq
 */
public final class CColumn extends Line implements CShapes {

    /**
     * Create element shape
     * @param canvas
     * @param startX
     * @param startY
     * @param height
     * @param isResultMode
     */
    public CColumn(Canvas2D canvas, double startX, double startY, double height, boolean isResultMode) {
        super(startX, startY, startX, startY+height);//update to use scale
        this.canvas = canvas;
        this.isResultMode = isResultMode;
        if(!isResultMode)
            this.init();
        
    }
    public CColumn(Canvas2D canvas, boolean isResultMode){
        this.canvas = canvas;
        this.isResultMode = isResultMode;
        setupColors();
        this.setStrokeWidth(Canvas2D.strokeWidth * 2);
    }
    public CColumn getThis(){
        //just a convinience method
        return this;
    }

    public void init() {
        this.setCursor(Cursor.HAND);
        lengthProperty = new SimpleDoubleProperty(getLength());
        this.mkMenus();
        this.moveAction();
        this.mkEvents();
        this.menuActions();
        this.mkBindingEvents();
        this.setType();
        this.mkDimension();
    }

    // root, select, hover, node, element, dimension
    private void setupColors(){
        HashMap<String, String> arr = Canvas2D.THEME_VARS.get(Canvas2D.canvasTheme.get());
        color = arr.get("node_obj");
        hoverColor = arr.get("hovered");
        selectColor = arr.get("selected");
        dimensionColor = arr.get("dimension");
        this.setStroke(Paint.valueOf(color));
        nodeTxt.setFill(Paint.valueOf(dimensionColor));
        fcTxt.setFill(Paint.valueOf(dimensionColor));
        mxTxt.setFill(Paint.valueOf(dimensionColor));
        myTxt.setFill(Paint.valueOf(dimensionColor));
        leTxt.setFill(Paint.valueOf(dimensionColor));
    }
    private void mkMenus() {
        this.menu.getItems().addAll(propMenu, copyMenu, deleteMenu);
    }

    private void moveAction() {
        this.setOnDragDetected((e) -> {
            //this.setTranslateX(e.getX());
            //this.setTranslateY(e.getY());
            this.move(e.getX(), e.getY());
            //this.relocate(e.getX(), e.getY());
        });
    }

    private void mkBindingEvents(){
        ///bind to flanges, to load arrow
    }
    private void mkEvents() {
        //hover
        this.setOnMouseEntered((MouseEvent e) -> {
            this.setStroke(Paint.valueOf(hoverColor));
            this.setStrokeWidth(this.getStrokeWidth() + getStrokeWidthPadding(false));
            this.canvas.isCanvasShapeHovered = true;
        });
        this.setOnMouseExited((MouseEvent e) -> {
            this.canvas.isCanvasShapeHovered = false;
            if(!this.canvas.selectedShape.equals(this)){ //if we have not been selected
                this.setStroke(Paint.valueOf(color));
                this.setStrokeWidth(this.getStrokeWidth() - getStrokeWidthPadding(false));
            }
        });
        //click events
        this.setOnMouseClicked((MouseEvent e) -> {
            if(e.getButton().equals(MouseButton.PRIMARY))//only if its a left click or tap
                SignalSlot.emitSignal(canvas.windowID, "ShapeClicked"+(isResultMode?"result":"main"), this);
        });
        //contextmenu
        this.setOnContextMenuRequested((e) -> {
            if(!isResultMode)
                this.menu.show(this, e.getSceneX(), e.getSceneY());
        });
        this.startXProperty().addListener((ob, ov, nv) -> lengthProperty.set(getLength()));
        this.startYProperty().addListener((ob, ov, nv) -> lengthProperty.set(getLength()));
        Canvas2D.canvasTheme.addListener((ob, ov, nv) -> setupColors());
    }

    private void menuActions() {
        this.deleteMenu.setOnAction((e) -> {
            this.destroy();
        });
        this.propMenu.setOnAction((e) -> {
            SignalSlot.emitSignal(canvas.windowID, "ShapeClicked"+(isResultMode?"result":"main"), this);
        });
    }
    private void buildFlanges(){
        //
    }
    private void mkDimension() {
        buildFlanges();
        //add dimensioning
        nodeTxt.setX(getMidPointX());
        nodeTxt.setY(getMidPointY());
        nodeTxt.setFill(Paint.valueOf(dimensionColor));
        nodeTxt.textProperty().bind(height.asString("Height: %s"));
        nodeTxt.xProperty().bind(this.startXProperty().add(getEndX()).divide(2).add(10));
        nodeTxt.yProperty().bind(this.startYProperty().add(getEndY()).divide(2)); 
        canvas.root.getChildren().add(nodeTxt);
        this.visibleProperty().addListener((ob, ov, nv) -> {
            //this will be executed only once (when shape is disabled)
            if(!nv)
                canvas.root.getChildren().remove(nodeTxt);
        });
        fcTxt.setX(getMidPointX());
        fcTxt.setX(getMidPointY());
        fcTxt.setFill(Paint.valueOf(dimensionColor));
        fcTxt.textProperty().bind(fc.asString("Axial Force, Fc: %s"));
        fcTxt.xProperty().bind(this.startXProperty().add(getEndX()).divide(2).add(10));
        fcTxt.yProperty().bind(this.startYProperty().add(getEndY()).divide(2).add(12)); 
        canvas.root.getChildren().add(fcTxt);
        this.visibleProperty().addListener((ob, ov, nv) -> {
            if(!nv)
                canvas.root.getChildren().remove(fcTxt);
        });
        mxTxt.setX(getMidPointX());
        mxTxt.setX(getMidPointY());
        mxTxt.setFill(Paint.valueOf(dimensionColor));
        mxTxt.textProperty().bind(mx.asString("Moment about Minor Axis, Mx: %s"));
        mxTxt.xProperty().bind(this.startXProperty().add(getEndX()).divide(2).add(10));
        mxTxt.yProperty().bind(this.startYProperty().add(getEndY()).divide(2).add(36)); 
        canvas.root.getChildren().add(mxTxt);
        this.visibleProperty().addListener((ob, ov, nv) -> {
            if(!nv)
                canvas.root.getChildren().remove(mxTxt);
        });
        myTxt.setX(getMidPointX());
        myTxt.setX(getMidPointY());
        myTxt.setFill(Paint.valueOf(dimensionColor));
        myTxt.textProperty().bind(my.asString("Moment about Major Axis, My: %s"));
        myTxt.xProperty().bind(this.startXProperty().add(getEndX()).divide(2).add(10));
        myTxt.yProperty().bind(this.startYProperty().add(getEndY()).divide(2).add(48)); 
        canvas.root.getChildren().add(myTxt);
        this.visibleProperty().addListener((ob, ov, nv) -> {
            if(!nv)
                canvas.root.getChildren().remove(myTxt);
        });
        leTxt.setX(getMidPointX());
        leTxt.setX(getMidPointY());
        leTxt.setFill(Paint.valueOf(dimensionColor));
        leTxt.textProperty().bind(le.asString("Effective Length: %s"));
        leTxt.xProperty().bind(this.startXProperty().add(getEndX()).divide(2).add(10));
        leTxt.yProperty().bind(this.startYProperty().add(getEndY()).divide(2).add(60)); 
        canvas.root.getChildren().add(leTxt);
        this.visibleProperty().addListener((ob, ov, nv) -> {
            if(!nv)
                canvas.root.getChildren().remove(leTxt);
        });
        betaTxt.setX(getMidPointX());
        betaTxt.setX(getMidPointY());
        betaTxt.setFill(Paint.valueOf(dimensionColor));
        betaTxt.textProperty().bind(beta.asString("Beta: %s"));
        betaTxt.xProperty().bind(this.startXProperty().add(getEndX()).divide(2).add(10));
        betaTxt.yProperty().bind(this.startYProperty().add(getEndY()).divide(2).add(72)); 
        canvas.root.getChildren().add(betaTxt);
        this.visibleProperty().addListener((ob, ov, nv) -> {
            if(!nv)
                canvas.root.getChildren().remove(betaTxt);
        });
        fcuTxt.setX(getMidPointX());
        fcuTxt.setX(getMidPointY());
        fcuTxt.setFill(Paint.valueOf(dimensionColor));
        fcuTxt.textProperty().bind(fcu.asString("Concrete Strength, Fcu: %s"));
        fcuTxt.xProperty().bind(this.startXProperty().add(getEndX()).divide(2).add(10));
        fcuTxt.yProperty().bind(this.startYProperty().add(getEndY()).divide(2).add(84)); 
        canvas.root.getChildren().add(fcuTxt);
        this.visibleProperty().addListener((ob, ov, nv) -> {
            if(!nv)
                canvas.root.getChildren().remove(fcuTxt);
        });
    }
    private double getMidPointX(){
        return (getStartX()+getEndX())/2;
    }
    private double getMidPointY(){
        return (getStartY()+getEndY())/2;
    }
    @Override
    public void destroy() {
        canvas.root.getChildren().remove(this);
        canvas.shapesList.remove(this);
        this.setVisible(false); 
        //leave it to GBC to murder him
        SignalSlot.emitSignal(canvas.windowID, "ShapeDestroyed", this);
    }

    public void deSelectCurrent() {
        if(this.canvas.selectedShape != null)
            this.canvas.selectedShape.deselect();
    }
    @Override
    public SDSNode getSerialProps(){
        SDSNode tmp = new SDSNode();
        tmp.addStringProp("id", String.valueOf(getShapeId()));
        tmp.addNumberProp("mx", mx.get());
        tmp.addNumberProp("my", my.get());
        tmp.addStringProp("result_mode", String.valueOf(isResultMode));
        tmp.addStringProp("steel_grade", steelGrade);
        tmp.addStringProp("trial_section", trialSection);
        tmp.addNumberProp("fc", fc.get());
        tmp.addNumberProp("height", height.get());
        tmp.addNumberProp("le", le.get());
        tmp.addNumberProp("beta", beta.get());
        tmp.addNumberProp("fcu", fcu.get());
        tmp.shape = "column";
        return tmp;
    }
    @Override
    public int getShapeId(){
        return id.get();
    }
    @Override
    public void move(double posX, double posY){//move is disabled for this shape
        //this.startFullDrag();
        //lengthProperty.add(getLength());
    }
    @Override
    public void deselect(){
        this.setStroke(Paint.valueOf(color));
        this.setStrokeWidth(getStrokeWidth() - getStrokeWidthPadding(true));
        this.canvas.selectedShape = null;
    }

    @Override
    public void select() {
        deSelectCurrent();
        this.setStroke(Paint.valueOf(selectColor));
        this.setStrokeWidth(this.getStrokeWidth() + getStrokeWidthPadding(true));
        this.canvas.selectedShape = this;
    }

    private double getStrokeWidthPadding(boolean isSelect) {
        return canvas.scale * ((isSelect) ? 0.5 : 0.3);
    }

    public double getLength() {
        return SimonUtil.round(Math.sqrt(Math.pow(getEndX() - getStartX(), 2) 
                + Math.pow(getEndY() - getStartY(), 2)), 3);
    }

    public void straighten(int orientation) {
        if (orientation == 1) { //horizontal
            this.setEndY(this.getStartY());
        } else {
            this.setEndX(this.getStartX());
        }
    }

    public boolean isStraight() {
        return (this.getStartY() == this.getEndY() || this.getStartX() == this.getEndX());
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
        this.type = 4;
        this.id.set(++canvas.elementCount);
        this.title = "Column" + this.id.get();
    }
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public CColumn getObjectContext() {
        return this;
    }

    private final Canvas2D canvas;
    private final Text nodeTxt = new Text(),
            mxTxt = new Text(),
            myTxt = new Text(),
            fcTxt = new Text(),
            leTxt = new Text(),
            betaTxt = new Text(),
            fcuTxt = new Text();
    public ContextMenu menu = new ContextMenu();
    public boolean isResultMode = false;
    public int type;
    public SimpleIntegerProperty id = new SimpleIntegerProperty();
    private String title, color, hoverColor, selectColor, dimensionColor; 
    public String steelGrade = "S275", trialSection = "UB";
    private SimpleDoubleProperty lengthProperty;
    public SimpleDoubleProperty height = new SimpleDoubleProperty(0),
            mx = new SimpleDoubleProperty(0),
            my = new SimpleDoubleProperty(0),
            fc = new SimpleDoubleProperty(0),
            le = new SimpleDoubleProperty(0),
            fcu = new SimpleDoubleProperty(0),
            beta = new SimpleDoubleProperty(0);
    public CNode nodeBind1, nodeBind2;
    public MenuItem propMenu = new MenuItem("Properties");
    public MenuItem copyMenu = new MenuItem("Copy");
    public MenuItem deleteMenu = new MenuItem("Delete");

}

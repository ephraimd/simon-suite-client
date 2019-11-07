/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.shapes;

import java.util.HashMap;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import simonds1.core.Resources;
import simonds1.core.SignalSlot;
import simonds1.core.SimonUtil;
import simonds1.core.transport.SDSNode;
import simonds1_client.ui.pad.Canvas2D;

/**
 * Custom Line object
 *
 * @author Olagoke Adedamola Farouq
 */
public final class CElement extends Line implements CShapes {

    /**
     * Create element shape
     *
     * @param canvas
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @deprecated Constructor should not be used since we no longer support
     * free hand element drawing
     */
    public CElement(Canvas2D canvas, double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);//update to use scale
        this.canvas = canvas;
        this.init();

    }

    public CElement(Canvas2D canvas, CNode bind1, CNode bind2, boolean isResultMode) {
        super(bind1.getCenterX(), bind1.getCenterY(), bind2.getCenterX(), bind2.getCenterY());
        this.canvas = canvas;
        this.nodeBind1 = bind1;
        this.nodeBind2 = bind2;
        this.isResultMode = isResultMode;
        if (!isResultMode) {
            this.init();
        }
    }

    public CElement getThis() {
        //just a convinience method
        return this;
    }

    public void init() {
        this.setCursor(Cursor.HAND);
        setupColors();
        this.setStrokeWidth(Canvas2D.strokeWidth + 0.7);
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
    private void setupColors() {
        HashMap<String, String> arr = Canvas2D.THEME_VARS.get(Canvas2D.canvasTheme.get());
        color = arr.get("node_obj");
        hoverColor = arr.get("hovered");
        selectColor = arr.get("selected");
        dimensionColor = arr.get("dimension");
        this.setStroke(Paint.valueOf(color));
        elemTxt.setFill(Paint.valueOf(dimensionColor));
        loadTxt.setFill(Paint.valueOf(dimensionColor));
        if (loadType.get().equals("p")) {
            loadImg.setImage(Resources.getImage(dimensionColor.equals("white") ? "imgArrowDownLight" : "imgArrowDown").getImage());
        } else if (loadType.get().equals("u")) {
            loadImg.setImage(Resources.getImage(dimensionColor.equals("white") ? "imgUDLDark" : "imgUDLLight").getImage());
            loadD2O.set(getLength() / 3);
        }
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

    private void mkBindingEvents() {
        ChangeListener<? super Boolean> lev = (ob, ov, nv) -> {
            if (!nv) {
                destroy();
            }
        };
        nodeBind1.visibleProperty().addListener(lev); //when node's visibility goes away. destroy
        nodeBind2.visibleProperty().addListener(lev);
        this.startXProperty().bind(nodeBind1.centerXProperty());
        this.startYProperty().bind(nodeBind1.centerYProperty());
        this.endXProperty().bind(nodeBind2.centerXProperty());
        this.endYProperty().bind(nodeBind2.centerYProperty());
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
            if (!this.canvas.selectedShape.equals(this)) { //if we have not been selected
                this.setStroke(Paint.valueOf(color));
                this.setStrokeWidth(this.getStrokeWidth() - getStrokeWidthPadding(false));
            }
        });
        //click events
        this.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton().equals(MouseButton.PRIMARY))//only if its a left click or tap
            {
                SignalSlot.emitSignal(canvas.windowID, "ShapeClicked" + (isResultMode ? "result" : "main"), this);
            }
        });
        //contextmenu
        this.setOnContextMenuRequested((e) -> {
            if (!isResultMode) {
                this.menu.show(this, e.getSceneX(), e.getSceneY());
            }
        });

        //if you go beyond the length of the element with the d20
        loadD2O.addListener((ob, ov, nv) -> {
            if (nv.doubleValue() > getLength()) {
                loadD2O.set(ov.doubleValue());
            }
        });
        loadType.addListener((ob, ov, nv) -> {
            switch (nv) {
                case "p":
                    loadImg.setImage(Resources.getImage(dimensionColor.equals("white") ? "imgArrowDownLight" : "imgArrowDown").getImage());
                    break;
                case "u":
                    loadImg.setImage(Resources.getImage(dimensionColor.equals("white") ? "imgUDLDark" : "imgUDLLight").getImage());
                    loadD2O.set(getLength() / 3);
                    break;
                default:
                    loadValue.set("0");
                    break;
            }
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
            SignalSlot.emitSignal(canvas.windowID, "ShapeClicked" + (isResultMode ? "result" : "main"), this);
        });
    }

    private void updateDimension(){
        if (isVertical()) {
            loadImg.setY(getRealStartY() - canvas.getScaleY(loadD2O.doubleValue(), false) -9);//-9 shifts it up a bit
        }else{
            //
        }
    }
    private void mkDimension() {
        //add dimensioning
        elemTxt.setFill(Paint.valueOf(dimensionColor));
        elemTxt.textProperty().bind(this.id.asString("E%s"));
        elemTxt.xProperty().bind(this.startXProperty().add(getEndX()).divide(2).add(2));
        elemTxt.yProperty().bind(this.startYProperty().add(getEndY()).divide(2).subtract(2));
        { //fix dimensioning for verticals too...!!!!!!
            //loadD2O_model.set(this.getStartX() - 9);
            if (isVertical()) { //for the tilting, use the same angle this element makes with the next one
                //loadD2O_model.set(canvas.getScaleY(loadD2O.doubleValue(), false) + this.getRealStartY());
                loadImg.xProperty().bind(this.startXProperty().add(getEndX()).divide(2).subtract(32));
                loadImg.setRotate(-90);//make it rotate to as its been before, use the ifs of the degs already done!
                loadImg.setY(getRealStartY() - canvas.getScaleY(loadD2O.doubleValue(), false) -9);//-9 shifts it up a bit
                loadD2O.addListener((ob, ov, nv) -> {
                    loadImg.setY(this.getRealStartY() - canvas.getScaleY(nv.doubleValue(), false) - 9);
                    //System.out.println(canvas.getScaleY(nv.doubleValue(), false) +", -r"+nv.doubleValue() +", rsty -"+getRealStartY());
                    //converts theoretical spacing to model spacing
                    //we add the y axis padding to it
                    //we remove the extra arrow image padding
                });
                this.nodeBind1.setNodeAngle(90); //turn the node           
            } else {
                loadD2O_model.set(canvas.getScaleX(loadD2O.doubleValue(), false) + this.getStartX());
                loadImg.yProperty().bind(this.startYProperty().add(getEndY()).divide(2).subtract(18));
                loadImg.xProperty().bind(loadD2O_model);
                loadD2O.addListener((ob, ov, nv) -> {
                    loadD2O_model.set(canvas.getScaleX(nv.doubleValue(), false) + this.getStartX());
                    //converts theoretical spacing to model spacing
                    //we add the x axis padding to it
                    //we remove the extra arrow image padding
                });
            }

            loadTxt.textProperty().bind(loadValue);
            loadTxt.setFill(Paint.valueOf(dimensionColor));
            loadTxt.xProperty().bind(loadImg.xProperty().subtract(3));
            loadTxt.yProperty().bind(loadImg.yProperty().subtract(4)); //pull higher

            loadImg.setScaleY(2);
            loadTxt.visibleProperty().bind(loadImg.visibleProperty());
            loadImg.setVisible(Double.valueOf(loadValue.getValueSafe()) > 0.0d);
            loadValue.addListener((ob, ov, nv) -> {
                loadImg.setVisible(Double.valueOf(nv) > 0.0d);
            });
        }
        canvas.root.getChildren().addAll(elemTxt, loadImg, loadTxt);
        this.visibleProperty().addListener((ob, ov, nv) -> {
            //this will be executed only once (when shape is disabled)
            if (!nv) {
                canvas.root.getChildren().removeAll(elemTxt, loadImg, loadTxt);
            }
        });
    }

    private double getMidPointX() {
        return (getStartX() + getEndX()) / 2;
    }

    private double getMidPointY() {
        return (getStartY() + getEndY()) / 2;
    }

    private double getStrokeWidthPadding(boolean isSelect) {
        return canvas.scale * ((isSelect) ? 0.5 : 0.3);
    }

    //the math parts
    public int getCY(String op) {
        if ("high".equals(op)) {
            return (int) ((this.getStartY() > this.getEndY()) ? this.getStartY() : this.getEndY());
        } else {
            return (int) ((this.getStartY() < this.getEndY()) ? this.getStartY() : this.getEndY());
        }
    }

    public int getCX(String op) {
        if ("high".equals(op)) {
            return (int) ((this.getStartX() > this.getEndX()) ? this.getStartX() : this.getEndX());
        } else {
            return (int) ((this.getStartX() < this.getEndX()) ? this.getStartX() : this.getEndX());
        }
    }

    public boolean isVertical() {
        int straightZone = 2;
        return (Math.max(this.getEndX(),this.getStartX()) - Math.min(this.getEndX(),this.getStartX())) <= straightZone;
    }

    public double getLength() {
        return SimonUtil.round(Math.sqrt(Math.pow(nodeBind2.nx.doubleValue() - nodeBind1.nx.doubleValue(), 2)
                + Math.pow(nodeBind2.ny.doubleValue() - nodeBind1.ny.doubleValue(), 2)), 3);
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

    /**
     * Computes the actual startY of the element by finding the highest of the
     * start and end Y
     *
     * @return double
     */
    public double getRealStartY() {
        return Math.max(getStartY(), getEndY());
    }

    /**
     * Computes the actual startX of the element by finding the highest of the
     * start and end X
     *
     * @return double
     */
    public double getRealStartX() {
        return Math.max(getStartX(), getEndX());
    }

    public void deSelectCurrent() {
        if (this.canvas.selectedShape != null) {
            this.canvas.selectedShape.deselect();
        }
    }

    @Override
    public void destroy() {
        canvas.root.getChildren().remove(this);
        canvas.shapesList.remove(this);
        this.setVisible(false); //learn how to destroy an object
        //this.setDisabled(true);
        if (this.nodeBind1.nodeBound1.equals(this.nodeBind2.getTitle())) {
            this.nodeBind1.nodeBound1 = "";
        } else if (this.nodeBind1.nodeBound2.equals(this.nodeBind2.getTitle())) {
            this.nodeBind1.nodeBound2 = "";
        }
        //leave it to GBC to murder him
        SignalSlot.emitSignal(canvas.windowID, "ShapeDestroyed", this);
        //--canvas.elementCount;
        //if we reduce it count, them subsequent removal of shapes will cause
        //duplicate shape titles
    }

    @Override
    public SDSNode getSerialProps() {
        SDSNode tmp = new SDSNode();
        tmp.addStringProp("id", String.valueOf(getShapeId()));
        tmp.addStringProp("node1Id", String.valueOf(nodeBind1.getShapeId()));
        tmp.addStringProp("node2Id", String.valueOf(nodeBind2.getShapeId()));
        tmp.addStringProp("result_mode", String.valueOf(isResultMode));
        tmp.addStringProp("load_type", loadType.getValueSafe());
        tmp.addStringProp("load_value", loadValue.getValueSafe());
        tmp.addStringProp("loadD2O", String.valueOf(loadD2O.get()));
        tmp.addStringProp("area", carea);
        tmp.addNumberProp("length", getLength());
        tmp.addStringProp("ym", ym);
        tmp.addNumberProp("force", force);
        tmp.addStringProp("inertia", inertia);
        tmp.addNumberProp("stress", stress);
        tmp.shape = "element";
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
        this.type = 1;
        this.id.set(++canvas.elementCount);
        this.title = "Element" + this.id.get();
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public CElement getObjectContext() {
        return this;
    }

    private final Canvas2D canvas;
    private final Text elemTxt = new Text(),
            loadTxt = new Text();
    private ImageView loadImg = new ImageView();
    public ContextMenu menu = new ContextMenu();
    public boolean isResultMode = false;
    public int type;
    public SimpleIntegerProperty id = new SimpleIntegerProperty();
    public double force = 0, stress = 0;
    private String title, color, hoverColor, selectColor, dimensionColor;
    public String ym = "209000000", carea = "10", inertia = "0";//young modulus, no load
    public SimpleStringProperty loadValue = new SimpleStringProperty("0"),
            loadType = new SimpleStringProperty("nl");
    private SimpleDoubleProperty lengthProperty;
    public SimpleDoubleProperty loadD2O = new SimpleDoubleProperty(0),
            loadD2O_model = new SimpleDoubleProperty(0);
    public CNode nodeBind1, nodeBind2;
    public MenuItem propMenu = new MenuItem("Properties");
    public MenuItem copyMenu = new MenuItem("Copy");
    public MenuItem deleteMenu = new MenuItem("Delete");

}

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
import simonds1.core.SimonUtil;
import simonds1.core.transport.SDSNode;
import simonds1_client.ui.pad.Canvas2D;

/**
 * Custom Line object
 *
 * @author Olagoke Adedamola Farouq
 */
public final class CBeam extends Rectangle implements CShapes {

    /**
     * Create element shape
     *
     * @param canvas
     * @param startX
     * @param startY
     * @param width
     * @param isResultMode
     */
    public CBeam(Canvas2D canvas, double startX, double startY, double width, boolean isResultMode) {
        super(startX, startY, width, 10);//update to use scale
        this.canvas = canvas;
        this.isResultMode = isResultMode;
        if (!isResultMode) {
            this.init();
        }

    }

    public CBeam(Canvas2D canvas, boolean isResultMode) {
        this.canvas = canvas;
        this.isResultMode = isResultMode;
        setupColors();
        this.setStrokeWidth(Canvas2D.strokeWidth * 2);
        setHeight(10);
    }

    public CBeam getThis() {
        //just a convinience method
        return this;
    }

    public void init() {
        this.setCursor(Cursor.HAND);
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
        this.setFill(Paint.valueOf(color));
        nodeTxt.setFill(Paint.valueOf(dimensionColor));
        spanTxt.setFill(Paint.valueOf(dimensionColor));
        vLoadTxt.setFill(Paint.valueOf(dimensionColor));
        loadTxt.setFill(Paint.valueOf(dimensionColor));
        loadImg.setImage(Resources.getImage(dimensionColor.equals("white") ? "imgArrowDownLight" : "imgArrowDown").getImage());
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

    private void buildFlanges() {
        //
    }

    private void mkDimension() {
        //buildFlanges();
        //add dimensioning
        nodeTxt.setX(getLength() + 3);
        nodeTxt.setY(getY());
        nodeTxt.setFill(Paint.valueOf(dimensionColor));
        nodeTxt.textProperty().bind(id.asString("Beam%s"));
        nodeTxt.xProperty().bind(xProperty().add(widthProperty()).add(10));
        nodeTxt.yProperty().bind(yProperty());

        this.visibleProperty().addListener((ob, ov, nv) -> {
            //this will be executed only once (when shape is disabled)
            if (!nv) {
                canvas.root.getChildren().remove(nodeTxt);
            }
        });
        spanTxt.setX((getX() + getWidth() / 2) - 5);
        spanTxt.setY(getY() + 35);
        spanTxt.setFill(Paint.valueOf(dimensionColor));
        spanTxt.textProperty().bind(span.asString("Span: %s"));
        spanTxt.xProperty().bind(xProperty().add(widthProperty().divide(2)).subtract(5));
        spanTxt.yProperty().bind(yProperty().add(30));

        this.visibleProperty().addListener((ob, ov, nv) -> {
            //this will be executed only once (when shape is disabled)
            if (!nv) {
                canvas.root.getChildren().remove(spanTxt);
            }
        });
        {
            loadImg.yProperty().bind(yProperty().subtract(30));
            loadImg.xProperty().bind(xProperty().add(widthProperty().divide(2)));
            loadTxt.setX((getX() + getWidth() / 2));
            loadTxt.setX(getY() - 30);
            loadTxt.textProperty().bind(vLoad.asString());
            loadTxt.setFill(Paint.valueOf(dimensionColor));
            loadTxt.xProperty().bind(loadImg.xProperty().add(7));
            loadTxt.yProperty().bind(loadImg.yProperty().subtract(7));

            loadImg.setScaleY(2);
            loadTxt.visibleProperty().bind(loadImg.visibleProperty());
            loadImg.setVisible(vLoad.get() > 0);
            vLoad.addListener((ob, ov, nv) -> {
                loadImg.setVisible(nv.doubleValue() > 0);
            });
            loadImg.visibleProperty().addListener((ob, ov, nv) -> {
                //this will be executed only once (when shape is disabled)
                if (!nv) {
                    canvas.root.getChildren().remove(loadTxt);
                }
            });
        }
        canvas.root.getChildren().addAll(nodeTxt, spanTxt, loadImg, loadTxt);
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
        if (this.canvas.selectedShape != null) {
            this.canvas.selectedShape.deselect();
        }
    }

    @Override
    public SDSNode getSerialProps() {
        SDSNode tmp = new SDSNode();
        tmp.addStringProp("id", String.valueOf(getShapeId()));
        tmp.addNumberProp("vload", vLoad.get());
        tmp.addNumberProp("vloadU", vLoadU.get());
        tmp.addNumberProp("moment", moment.get());
        tmp.addNumberProp("fv", fv.get());
        tmp.addNumberProp("width", getWidth());
        tmp.addNumberProp("x", getX());
        tmp.addNumberProp("y", getY());
        tmp.addStringProp("result_mode", String.valueOf(isResultMode));
        tmp.addStringProp("steel_grade", steelGrade);
        tmp.addStringProp("beam_type", beamType);
        tmp.addStringProp("trial_section", trialSection);
        tmp.addStringProp("hasPlasters", String.valueOf(hasPlasters));
        tmp.addNumberProp("span", span.get());
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
    public void setVload(double vLoad){
        this.vLoad.set(vLoad);
    }

    private double getStrokeWidthPadding(boolean isSelect) {
        return canvas.scale * ((isSelect) ? 0.5 : 0.3);
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
        this.type = 2;
        this.id.set(++canvas.elementCount);
        this.title = "Beam" + this.id.get();
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public CBeam getObjectContext() {
        return this;
    }

    private final Canvas2D canvas;
    private final Text nodeTxt = new Text(),
            vLoadTxt = new Text(),
            sp1Txt = new Text(),
            sp2Txt = new Text(),
            spanTxt = new Text(),
            loadTxt = new Text();
    private final ImageView loadImg = new ImageView();
    public ContextMenu menu = new ContextMenu();
    public boolean isResultMode = false,
            hasPlasters = false;
    public int type;
    public SimpleIntegerProperty id = new SimpleIntegerProperty();
    private String title, color, hoverColor, selectColor, dimensionColor;
    public String steelGrade = "S275", trialSection = "UB", beamType = "Cantilever";
    public SimpleDoubleProperty span = new SimpleDoubleProperty(0),
            vLoad = new SimpleDoubleProperty(0),
            vLoadU = new SimpleDoubleProperty(0),
            moment = new SimpleDoubleProperty(0),
            fv = new SimpleDoubleProperty(0);
    public HashMap<String, String> stringProps = new HashMap<>();
    public HashMap<String, Number> numberProps = new HashMap<>();
    public HashMap<String, Boolean> boolProps = new HashMap<>();
    public CNode nodeBind1, nodeBind2;
    public MenuItem propMenu = new MenuItem("Properties");
    public MenuItem copyMenu = new MenuItem("Copy");
    public MenuItem deleteMenu = new MenuItem("Delete");

}

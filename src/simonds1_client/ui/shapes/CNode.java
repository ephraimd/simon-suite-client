/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.shapes;

import java.util.HashMap;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import simonds1.core.Resources;
import simonds1.core.SignalSlot;
import simonds1.core.transport.SDSNode;
import simonds1_client.ui.pad.Canvas2D;

/**
 *
 * @author ADEDAMOLA
 */
public final class CNode extends Circle implements CShapes {

    public CNode(Canvas2D canvas, double posX, double posY, boolean isResultMode) {
        super(7, Paint.valueOf("black"));
        this.setCenterX(posX);
        nx.set(posX);
        this.setCenterY(posY);
        ny.set(posY);
        this.canvas = canvas;
        this.isResultMode = isResultMode;
        setupColors();
        if (!isResultMode) {
            this.init();
        }

    }

    public void init() {
        this.setCursor(Cursor.HAND);
        this.mkMenus();
        this.mkEvents();
        this.menuActions();
        this.setType();
        this.mkDimension(getCenterX(), getCenterY());
    }

    private void setupColors() {
        HashMap<String, String> arr = Canvas2D.THEME_VARS.get(Canvas2D.canvasTheme.get());
        color = arr.get("node_obj");
        hoverColor = arr.get("hovered");
        selectColor = arr.get("selected");
        dimensionColor = arr.get("dimension");
        this.setFill(Paint.valueOf(color));
        nodeTxt.setFill(Paint.valueOf(dimensionColor));
        hloadTxt.setFill(Paint.valueOf(dimensionColor));
        loadTxt.setFill(Paint.valueOf(dimensionColor));
        loadImg.setImage(Resources.getImage(dimensionColor.equals("white") ? "imgArrowDownLight" : "imgArrowDown").getImage());
        hloadImg.setImage(Resources.getImage(dimensionColor.equals("white") ? "imgArrowRightLight" : "imgArrowRight").getImage());
    }

    private void mkMenus() {
        this.menu.getItems().addAll(propMenu, copyMenu, deleteMenu, 
                turnSupportLeft, turnSupportRight, turnSupportBelow);
    }

    private void mkBindingEvents() {

    }

    private void mkEvents() {
        //hover events
        this.setOnMouseEntered((MouseEvent e) -> {
            this.setFill(Paint.valueOf(hoverColor));
            this.canvas.isCanvasShapeHovered = true;
        });
        this.setOnMouseExited((MouseEvent e) -> {
            this.canvas.isCanvasShapeHovered = false;
            if (!this.canvas.selectedShape.equals(this)) {
                this.setFill(Paint.valueOf(color));
            }
        });
        //click events
        this.setOnMouseClicked((MouseEvent e) -> {
            if (e.isControlDown() && !isResultMode) {
                if (canvas.bufNode == null) {
                    canvas.bufNode = this;
                } else if (this.getTitle().equals(canvas.bufNode.getTitle())) {
                    select();
                    return;
                } else {
                    if (this.getTitle().equals(canvas.bufNode.getTitle())) {
                        return;
                    }
                    CNode pbd1 = (CNode) canvas.getShape("node", canvas.bufNode.nodeBound1);
                    if (pbd1 == null && !this.nodeBound1.equals(canvas.bufNode.getTitle())
                            && !this.nodeBound1.equals(canvas.bufNode.getTitle())) {
                        canvas.createElements(null, canvas.bufNode, this, null, false);
                    } else if (!canvas.bufNode.nodeBound1.isEmpty() && !canvas.bufNode.nodeBound2.isEmpty()) {
                        SignalSlot.emitSignal(canvas.windowID, "NotifyUI",
                                canvas.bufNode.getTitle() + " Cannot be binded to more than two Nodes!");
                    } else if (!canvas.bufNode.nodeBound1.equals(this.getTitle())
                            && !canvas.bufNode.nodeBound2.equals(this.getTitle())
                            && !this.nodeBound1.equals(canvas.bufNode.getTitle())
                            && !this.nodeBound2.equals(canvas.bufNode.getTitle())) {
                        canvas.createElements(null, canvas.bufNode, pbd1, this, false);
                    } else {
                        //
                    }
                    canvas.bufNode = null;
                }
                select();
            } else if (e.getButton().equals(MouseButton.PRIMARY))//only if its a left click or tap
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
        /*this.setOnMouseDragged(e -> {
        this.move(e.getX(), e.getY()); //removed node movement
        });*/
        Canvas2D.canvasTheme.addListener((ob, ov, nv) -> setupColors());
    }

    private void menuActions() {
        this.deleteMenu.setOnAction((e) -> {
            this.destroy();
        });
        this.turnSupportLeft.setOnAction((e) -> {
            this.setNodeAngle(90);
        });
        this.turnSupportRight.setOnAction((e) -> {
            this.setNodeAngle(-90);
        });
        this.turnSupportBelow.setOnAction((e) -> {
            this.setNodeAngle(0);
        });
        this.propMenu.setOnAction((e) -> {
            SignalSlot.emitSignal(canvas.windowID, "ShapeClicked" + (isResultMode ? "result" : "main"), this);
        });
    }

    /**
     * Turns the node to specified degree
     * @param degree  degree to which the node should turn
     */
    public void setNodeAngle(double degree){
        this.nodeAngle = degree;
        supportImg.setRotate(degree);
        double x = 0, y = 0;
        switch((int)degree){
            case 90:
                x = getCenterX() - 30;
                y = getCenterY() - 7;
                break;
            case -90:
                x = getCenterX() + 10;
                y = getCenterY() - 7;
                break;
            default:
                x = getCenterX() - 9;
                y = getCenterY() + 10;
                break;
        }
        supportImg.setX(x);
        supportImg.setY(y);//Y goes up with minus and down with plus
    }

    private void mkDimension(double posX, double posY) {
        supportImg.setY(getCenterY() + 10);
        supportImg.setX(getCenterX() - 9);
        this.centerYProperty().addListener((ob, ov, nv) -> {
            if(this.nodeAngle == 0)
                supportImg.setY(nv.doubleValue() + 10);
            else
                supportImg.setY(nv.doubleValue() - 30);
        });
        this.centerXProperty().addListener((ob, ov, nv) -> {
            supportImg.setX(nv.doubleValue() - 9);
        });
        supportImg.setScaleY(1);   
        canvas.root.getChildren().add(supportImg);
        supportImg.setVisible(!boundary.get().equals("None"));
        if (!boundary.get().equals("None")) {
            supportImg.setImage(Resources.getImage(boundary.get() + "Control").getImage());
            supportImg.setVisible(true);
        }
        boundary.addListener((ob, ov, nv) -> {
            if (nv.equals("None")) {
                supportImg.setVisible(false);
            } else {
                supportImg.setImage(Resources.getImage(nv + "Control").getImage());
                supportImg.setVisible(true);
            }
        });
        //add dimensioning
        nodeTxt.textProperty().bind(this.id.asString("N%s"));
        nodeTxt.setFill(Paint.valueOf(dimensionColor));
        nodeTxt.xProperty().bind(this.centerXProperty().add(7));
        nodeTxt.yProperty().bind(this.centerYProperty().subtract(7));
        {
            loadImg.yProperty().bind(this.centerYProperty().subtract(27));
            loadImg.xProperty().bind(this.centerXProperty().subtract(13));
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
        }
        {
            hloadImg.yProperty().bind(this.centerYProperty().subtract(20));
            hloadImg.xProperty().bind(this.centerXProperty().subtract(18));
            hloadTxt.textProperty().bind(hLoad.asString());
            hloadTxt.setFill(Paint.valueOf(dimensionColor));
            hloadTxt.xProperty().bind(hloadImg.xProperty().subtract(18));
            hloadTxt.yProperty().bind(hloadImg.yProperty().add(3));

            hloadImg.setScaleX(2);
            hloadImg.setVisible(hLoad.get() > 0);
            hloadTxt.visibleProperty().bind(hloadImg.visibleProperty());
            hLoad.addListener((ob, ov, nv) -> {
                hloadImg.setVisible(nv.doubleValue() >= 0.01);
            });
        }
        canvas.root.getChildren().addAll(loadImg, nodeTxt, loadTxt, hloadTxt, hloadImg);

        this.visibleProperty().addListener((ob, ov, nv) -> {
            //this will be executed only once (when shape is disabled)
            if (!nv) {
                canvas.root.getChildren().removeAll(loadImg, nodeTxt, loadTxt, hloadTxt, hloadImg, supportImg);
            }
        });
    }

    public CNode setLoad(double hLoad, double vLoad) {
        this.hLoad.set(hLoad);
        this.vLoad.set(vLoad);
        return this;
    }

    public void setHLoad(double hLoad) {
        this.hLoad.set(hLoad);
    }

    public void setVLoad(double vLoad) {
        this.vLoad.set(vLoad);
    }

    public void setBoundary(String condition) {
        switch (condition) {
            case "0":
                condition = "None";
                break;
            case "1":
                condition = "Roller";
                break;
            case "2":
                condition = "Pinned";
                break;
            case "3":
                condition = "Fixed";
                break;
        }
        this.boundary.set(condition);
    }

    public void setNCoord(double nposX, double nposY) {
        nx.set(nposX);
        ny.set(nposY);
    }

    @Override
    public void destroy() {
        canvas.root.getChildren().remove(this);
        canvas.shapesList.remove(this);
        vLoad.set(0);
        this.setVisible(false); //learn how to destroy an object
        //this.setDisabled(true);
        //leave it to GBC to murder him
        SignalSlot.emitSignal(canvas.windowID, "ShapeDestroyed", this);
        //--canvas.nodeCount;
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
        tmp.addNumberProp("posX", nx.doubleValue());
        tmp.addNumberProp("posY", ny.doubleValue());
        tmp.addNumberProp("posNX", getCenterX());
        tmp.addNumberProp("posNY", getCenterY());
        String bd;
        switch (boundary.get()) {
            case "None":
                bd = "0";
                break;
            case "Fixed":
                bd = "3";
                break;
            case "Pinned":
                bd = "2";
                break;
            default:
                bd = "1";
                break;
        }
        tmp.addStringProp("boundary", bd);
        tmp.addStringProp("result_mode", String.valueOf(isResultMode));
        tmp.addNumberProp("hLoadValue", hLoad.get());
        tmp.addNumberProp("vLoadValue", vLoad.get());
        tmp.addNumberProp("forcex", forcex);
        tmp.addNumberProp("forcey", forcey);
        tmp.addNumberProp("displx", displacementx);
        tmp.addNumberProp("disply", displacementy);
        tmp.shape = "node";
        return tmp;
    }

    @Override
    public int getShapeId() {
        return this.id.get();
    }

    @Override
    public void move(double posX, double posY) {
        this.setCenterX(posX);
        this.setCenterY(posY);
        nx.set(canvas.getScaleX(posX, true));
        nx.set(canvas.getScaleY(posY, true));
    }

    @Override
    public void deselect() {
        this.setFill(Paint.valueOf(color));
        this.canvas.selectedShape = null;
    }

    @Override
    public void select() {
        deSelectCurrent();
        this.setFill(Paint.valueOf(selectColor));
        this.canvas.selectedShape = this;
    }

    public CNode getThis() {
        //just a convinience method
        return this;
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
        this.type = 3;
        this.id.set(++canvas.nodeCount);
        this.title = "Node" + this.id.get();
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public CNode getObjectContext() {
        return this;
    }

    private final Canvas2D canvas;
    private ImageView loadImg = new ImageView(),
            hloadImg = new ImageView(),
            supportImg = new ImageView();
    private final Text nodeTxt = new Text(),
            loadTxt = new Text(),
            hloadTxt = new Text();
    private double nodeAngle = 0;
    public ContextMenu menu = new ContextMenu();
    public int type;
    public SimpleIntegerProperty id = new SimpleIntegerProperty();
    public boolean isResultMode = false;
    public double displacementx = 0, displacementy = 0,
            forcex = 0, forcey = 0;
    public SimpleDoubleProperty nx = new SimpleDoubleProperty(0),
            ny = new SimpleDoubleProperty(0);
    private String title, color, hoverColor, selectColor, dimensionColor;
    public String nodeBound1 = "", nodeBound2 = "";
    public SimpleStringProperty boundary = new SimpleStringProperty("None");
    public SimpleDoubleProperty hLoad = new SimpleDoubleProperty(0),
            vLoad = new SimpleDoubleProperty(0);
    public MenuItem copyMenu = new MenuItem("Copy");
    public MenuItem propMenu = new MenuItem("Properties");
    public MenuItem deleteMenu = new MenuItem("Delete");
    public MenuItem turnSupportLeft = new MenuItem("Turn Support Left");
    public MenuItem turnSupportRight = new MenuItem("Turn Support Right");
    public MenuItem turnSupportBelow = new MenuItem("Turn Support Below");

}

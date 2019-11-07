/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.pad;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import simonds1.core.SignalSlot;
import simonds1.core.transport.SDSModel;
import simonds1_client.ui.shapes.CBeam;
import simonds1_client.ui.shapes.CColumn;
import simonds1_client.ui.shapes.CElement;
import simonds1_client.ui.shapes.CNode;
import simonds1_client.ui.shapes.CShapes;

/**
 *
 * @author Olagoke Adedamola Farouq
 */
public class Canvas2D extends ScrollPane {

    public static double CANVAS_PADDING_X = 100;
    public static double CANVAS_PADDING_Y = 400;

    public double insituScale,
            modelScale;
    public static HashMap<String, HashMap<String, String>> THEME_VARS;
    public String windowID = "";


    public Canvas2D(double insituScale, double modelScale, String windowID) {
        super();
        this.insituScale = insituScale;
        this.modelScale = modelScale;
        this.windowID = windowID;

        this.buildRoot();
        this.mkScroll();
        helper = new CanvasHelper(this);
    }

    public Canvas2D(String windowID) {
        this.windowID = windowID;
        this.buildRoot();
        this.mkScroll();
        helper = new CanvasHelper(this);
    }

    private void mkScroll() {
        this.setContent(gRoot);
        this.setPannable(false);
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private void buildRoot() {
        gRoot = new Group(this.root);
        gRoot.setAutoSizeChildren(true);
        this.gRoot.setStyle("-fx-background-color:" + THEME_VARS.get(canvasTheme.get()).get("root_pane"));
        this.setStyle("-fx-background-color:" + THEME_VARS.get(canvasTheme.get()).get("root_pane"));
        this.root.prefHeightProperty().bind(this.heightProperty());
        this.root.prefWidthProperty().bind(this.widthProperty());
        this.root.setCursor(Cursor.MOVE);
        root.setSnapToPixel(true);
        this.root.setStyle("-fx-background-color:" + THEME_VARS.get(canvasTheme.get()).get("root_pane"));
        //draw the gridlines
        applyGridlines();
    }
    private void themeEvents(){
        Canvas2D.canvasTheme.addListener((ob, ov, nv) -> {
            SignalSlot.emitSignal(windowID, "ThemeChanged", Canvas2D.canvasTheme.getValueSafe());
            this.gRoot.setStyle("-fx-background-color:" + THEME_VARS.get(canvasTheme.get()).get("root_pane"));
            this.root.setStyle("-fx-background-color:" + THEME_VARS.get(canvasTheme.get()).get("root_pane"));
        });
    }

    private void signalEvents(){
        //gridX[j].setStroke(Paint.valueOf("#FFF3F2"));
        //SignalSlot.addSlot("ThemeChanged", (obj) -> coordNotif.setText((String) obj), windowID);
    }
    private void drawEvent() {
        EventHandler<MouseEvent> event = (MouseEvent e) -> {
            if (isCanvasShapeHovered || this.isDisabled) //if there s a shape under mouse..
            {
                return;//1=select, 2=line, 3=node, 4=load
            }
            switch (shapeType) {
                case 2:
                    helper.lineDrawer(e); //2 = line
                    break;
                case 3:
                    helper.nodeDrawer(e);
                    break;
                default: //select is handled here
                    break;
            }
        };
        root.addEventHandler(MouseEvent.MOUSE_PRESSED, event);
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED, event); //during shape draw
        root.addEventHandler(MouseEvent.MOUSE_RELEASED, event); //end shape draw
        //root.setRotate(180);
    }

    private void initEvents() {
        root.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            root.requestFocus();
            //to make things better
        });
        
        //now rewrite the id's of the objects when one is deleted
        //this essentially means we recount all the objects
        SignalSlot.addSlot(windowID, "ShapeDestroyed", obj -> {
            //deleted objects will still live in SignalSlot array
            nodeCount = 0;
            elementCount = 0;
            shapesList.stream().forEach(shape -> {
                if (shape.getTypeStr().equals("line")) {
                    ((CElement) shape).id.set(++elementCount);
                    ((CElement) shape).setTitle("Element" + elementCount);
                } else if (shape.getTypeStr().equals("node")) {
                    ((CNode) shape).id.set(++nodeCount);
                    ((CNode) shape).setTitle("Node" + nodeCount);
                }
            });
        });
    }

    private void keyEvents() {
        root.setOnKeyPressed((e) -> {
            if (null != e.getCode()) {
                switch (e.getCode()) {
                    case DELETE:
                        helper.deleteCurrentShape();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        helper.moveCurrentShape(e.getCode());
                        break;
                    default:
                        break;
                }
            }
        });
        /* ///should fix zooming
        root.setOnZoom(e -> {
            double zoom = e.getTotalZoomFactor();
            System.out.println("Zoom " + zoom);
            scale = zoom;
            root.setScaleX(zoom);
            root.setScaleY(zoom);
            root.setScaleZ(zoom);
        });
        */
    }

    public SDSModel exportModel() {
        SDSModel model = new SDSModel();
        if (this.shapesList.isEmpty()) {
            return null;
        }
        this.shapesList.stream()
                .forEach(shape -> {
                    model.shapes.add(shape.getSerialProps());
                });
        model.title = canvasTitle.getValueSafe();
        model.addNumberProp("insitu_scale", insituScale);
        model.addNumberProp("model_scale", modelScale);
        return model;
    }

    public String importModel(SDSModel model, boolean isAdded) {
        if (model.flag < 10) { //some errors are actually warnings
            return model.message; //return error message if error
        }
        insituScale = model.getNumberProp("insitu_scale").doubleValue();
        modelScale = model.getNumberProp("model_scale").doubleValue();
        if (!isAdded) {
            shapesList.clear();
            root.getChildren().clear();
        }
        model.shapes.stream()
                .filter(snode -> "node".equals(snode.shape))
                .forEach(snode -> {
                    CNode tmp = createNode("Node" + snode.getStringProp("id"),
                            snode.getStringProp("boundary"),
                            snode.getNumberProp("posNX").intValue(),
                            snode.getNumberProp("posNY").intValue(),
                            snode.getNumberProp("posX").doubleValue(),
                            snode.getNumberProp("posY").doubleValue(),
                            snode.getStringProp("result_mode").equals("true"))
                            .setLoad(snode.getNumberProp("hLoadValue").doubleValue(),
                                    snode.getNumberProp("vLoadValue").doubleValue());
                    if (snode.getStringProp("result_mode").equals("true")) {
                        tmp.forcex = snode.getNumberProp("forcex").doubleValue();
                        tmp.forcey = snode.getNumberProp("forcey").doubleValue();
                        tmp.displacementx = snode.getNumberProp("displx").doubleValue();
                        tmp.displacementy = snode.getNumberProp("disply").doubleValue();
                        tmp.init();
                    }
                });
        model.shapes.stream()
                .filter(snode -> "element".equals(snode.shape))
                .forEach(snode -> {
                    //System.out.println("Element" + snode.getStringProp("id"));
                    CElement tmp = createElements("Element" + snode.getStringProp("id"),
                            (CNode) getShape("node", "Node" + snode.getStringProp("node1Id")),
                            (CNode) getShape("node", "Node" + snode.getStringProp("node2Id")), null,
                            snode.getStringProp("result_mode").equals("true"));
                    tmp.carea = snode.getStringProp("area");
                    tmp.ym = snode.getStringProp("ym");
                    tmp.inertia = snode.getStringProp("inertia");
                    tmp.loadValue.setValue(snode.getStringProp("load_value"));
                    tmp.loadD2O.setValue(Double.valueOf(snode.getStringProp("loadD2O")));
                    tmp.loadType.setValue(snode.getStringProp("load_type"));
                    if (snode.getStringProp("result_mode").equals("true")) {
                        tmp.force = snode.getNumberProp("force").doubleValue();
                        tmp.stress = snode.getNumberProp("stress").doubleValue();
                        tmp.init();
                    }
                });
        model.shapes.stream()
                .filter(snode -> "column".equals(snode.shape))
                .forEach(snode -> {
                    CColumn tmp = new CColumn(this, false);
                    tmp.mx.set(snode.getNumberProp("mx").doubleValue());
                    tmp.my.set(snode.getNumberProp("my").doubleValue());
                    tmp.fc.set(snode.getNumberProp("fc").doubleValue());
                    tmp.height.set(snode.getNumberProp("height").doubleValue());
                    tmp.le.set(snode.getNumberProp("le").doubleValue());
                    tmp.beta.set(snode.getNumberProp("beta").doubleValue());
                    tmp.fcu.set(snode.getNumberProp("fcu").doubleValue());
                    tmp.steelGrade = snode.getStringProp("steel_grade");
                    tmp.trialSection = snode.getStringProp("trial_section");
                    double x = getScaleX(0, false) + 400,
                            y = -getScaleY(0, false) + 400;
                    tmp.setStartX(x);
                    tmp.setStartY(y); //IMPORTANT: '-' inverts y axis
                    tmp.setEndX(x);
                    tmp.setEndY((-getScaleY(tmp.height.get(), false)) + y); //IMPORTANT: '-' inverts y axis
                    tmp.init();
                    createColumn(tmp);
                });
        model.shapes.stream()
                .filter(snode -> "beam".equals(snode.shape))
                .forEach(snode -> {
                    CBeam tmp = new CBeam(this, false);
                    tmp.hasPlasters = snode.getStringProp("hasPlasters").equals("true");
                    tmp.vLoad.set(snode.getNumberProp("vload").doubleValue());
                    tmp.vLoadU.set(snode.getNumberProp("vloadU").doubleValue());
                    tmp.span.set(snode.getNumberProp("span").doubleValue());
                    tmp.setX(snode.getNumberProp("x").doubleValue());
                    tmp.setY(snode.getNumberProp("y").doubleValue());
                    tmp.setWidth(getScaleY(snode.getNumberProp("width").doubleValue(), false));
                    tmp.steelGrade = snode.getStringProp("steel_grade");
                    tmp.beamType = snode.getStringProp("beam_type");
                    tmp.trialSection = snode.getStringProp("trial_section");
                    if(snode.getNumberMapProp("num_props") != null)
                        tmp.numberProps = snode.getNumberMapProp("num_props");
                    if(snode.getStringMapProp("str_props") != null)
                        tmp.stringProps = snode.getStringMapProp("str_props");
                    if(snode.getBoolMapProp("bool_props") != null)
                        tmp.boolProps = snode.getBoolMapProp("bool_props");
                    double x = getScaleX(0, false) + 400,
                            y = -getScaleY(0, false) + 400;
                    tmp.init();
                    createBeam(tmp);
                });
        canvasTitle.set(model.title);
        return null;
    }

    public CColumn createColumn(CColumn column) {
        shapesList.add(column);
        root.getChildren().add(shapesList.get(helper.shapesSize() - 1).getObjectContext()); //add to canvas
        selectedShape = column;
        return column;
    }

    public CBeam createBeam(CBeam beam) {
        shapesList.add(beam);
        root.getChildren().add(shapesList.get(helper.shapesSize() - 1).getObjectContext()); //add to canvas
        selectedShape = beam;
        return beam;
    }
    /**
     * Creates a new Node here
     * @param title Title of the Node. (e.g. N1)
     * @param boundary boundary condition are 0,1,2,3. none, roller, pin, fixed
     * @param posX the x-axis pos on the screen (scaled)
     * @param posY the y-axis pos on the screen (scaled)
     * @param nposX the expected x-axis inputed (the real one)
     * @param nposY the expected y-axis inputed (the real one)
     * @param isResultMode are we displaying results or just modeling?
     * @return the newly created node.
     */
    public CNode createNode(String title, String boundary, double posX, double posY, double nposX, double nposY, boolean isResultMode) {
        CNode newObj = new CNode(this, posX, posY, isResultMode);
        shapesList.add(newObj); //add shape to list
        root.getChildren().add(newObj.getObjectContext()); //add to canvas
        //if a shape has been selected before, remove its selection state and status
        if (selectedShape != null) {
            selectedShape.deselect();
        }
        selectedShape = newObj; //our new shape is the selected one now
        selectedShape.select();
        ((CNode) selectedShape).setBoundary(boundary); //
        ((CNode) selectedShape).nx.set(nposX);
        ((CNode) selectedShape).ny.set(nposY);
        if (title != null) {
            selectedShape.setTitle(title);
        }
        return ((CNode) selectedShape);
    }

    //can bind to anyone or both param nodes
    public CElement createElements(String title, CNode parentNode, CNode node1, CNode node2, boolean isResultMode) {
        CElement newObj = null;
        if (node1 != null) {
            //only if the new node isnt the same node in place
            if (!parentNode.nodeBound1.equals(node1.getTitle())) {
                try {
                    if (!parentNode.nodeBound1.isEmpty()) {
                        getElement(parentNode.nodeBound1, parentNode.getTitle()).destroy();
                    }
                } catch (NullPointerException ex) {
                    //
                }
                newObj = new CElement(this, parentNode, node1, isResultMode);
                shapesList.add(newObj); //connect parent to node1
                root.getChildren().add(newObj.getObjectContext()); //add to canvas
                parentNode.nodeBound1 = node1.getTitle();
                if (title != null) {
                    newObj.setTitle(title);
                }
                //return newObj;
            }
        }
        if (node2 != null) {
            if (!parentNode.nodeBound2.equals(node2.getTitle())) {
                try {
                    if (!parentNode.nodeBound2.isEmpty()) {
                        getElement(parentNode.nodeBound2, parentNode.getTitle()).destroy();
                    }
                } catch (NullPointerException ex) {
                    //
                }
                newObj = new CElement(this, parentNode, node2, isResultMode);
                shapesList.add(newObj); //connect parent to node2
                root.getChildren().add(newObj.getObjectContext()); //add to canvas
                //let parent know its been bound to these dudes in UI context
                parentNode.nodeBound2 = node2.getTitle();
                if (title != null) {
                    newObj.setTitle(title);
                }
                //return newObj;
            }
        }
        //System.out.printf("Last: %s\n", shapesList.get(helper.shapesSize() - 1).getTitle());
         //if(!shapesList.get(helper.shapesSize() - 1) instanceof CElement)
             //return 
        return newObj;
    }

    /**
     * Get a particular shape object
     * @param shapeType either line, node, beam, column
     * @param shapeTitle e.g. "Node1"
     * @return CShapes object that matches the parameters
     */
    public CShapes getShape(String shapeType, String shapeTitle) {
        try {
            return shapesList.parallelStream()
                    .filter(shape -> shape.getTypeStr().equals(shapeType) && shape.getTitle().equals(shapeTitle))
                    .findFirst().get();//.ifPresent(shape -> tmpShape = shape);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * @deprecated Depreciated since each shape can now delete themselves
     * @param shapeType
     * @param shapeTitle
     * @return 
     */
    public boolean deleteShape(String shapeType, String shapeTitle) {
        for (int i = 0; i < root.getChildren().size(); i++) {
            CShapes shape = shapesList.get(i);
            if (shape.getTypeStr().equals(shapeType) && shape.getTitle().equals(shapeTitle)) {
                //

            }
        }
        return true;
    }

    /**
     * Returns an element where the two bound nodes are node1 and node2
     * <hr />
     * ??- what if the element had node1 and node2 and vice versa
     * @param node1 node1's title from <b>.getTitle()</b>
     * @param node2 node2's title from <b>.getTitle()</b>
     * @return CElement
     */
    public CElement getElement(String node1, String node2) {
        try {
            return (CElement) shapesList.parallelStream()
                    .filter(shape -> shape.getTypeStr().equals("line"))
                    .filter(shape -> ((CElement) shape).nodeBind1.getTitle().equals(node1) &&
                            ((CElement) shape).nodeBind2.getTitle().equals(node2))
                    .findFirst().get();//.ifPresent(shape -> tmpShape = shape);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public List<CShapes> getShapesList(String shapeType) {
        return this.shapesList.isEmpty() ? null
                : shapesList.parallelStream()
                        .filter(shape -> shape.getTypeStr().equals(shapeType))
                        .collect(Collectors.toList());
    }

    public List<String> getShapesListStr(String shapeType) {
        return this.shapesList.isEmpty() ? null
                : shapesList.parallelStream()
                        .filter(shape -> shape.getTypeStr().equals(shapeType))
                        .map(shape -> shape.getTitle())
                        .collect(Collectors.toList());
    }

    public void applyGridlines() {
        if(true)return;
        final int bars = 1100;
        gridX = new Line[(bars) / 10];//1100 for total lines at 10 pixels apart
        gridY = new Line[(bars) / 10];
        int xEnd = 1700, yEnd = 700;
        for (int i = 0, j = 0; i < bars; j++, i += 10) {//grid at every 10 pixels
            gridX[j] = new Line(0, i, xEnd, i);
            gridY[j] = new Line(i, 0, i, yEnd); //sartx, starty
            gridX[j].setStroke(Paint.valueOf(THEME_VARS.get(canvasTheme.get()).get("grid_lines")));
            gridY[j].setStroke(Paint.valueOf(THEME_VARS.get(canvasTheme.get()).get("grid_lines")));
            root.getChildren().addAll(gridX[j], gridY[j]);
        }
        SignalSlot.addSlot(windowID, "ThemeChanged", obj -> {
            for(int i =0;i < gridX.length; i++){
                gridX[i].setStroke(Paint.valueOf(THEME_VARS.get((String) obj).get("grid_lines")));
                gridY[i].setStroke(Paint.valueOf(THEME_VARS.get((String) obj).get("grid_lines")));
            }
        });
    }

    public Point transformLocal2GlobalCoord(double x, double y){
        return null;
    }
    public Point transformCoordn(int x, int y, boolean isNormalize) { //normalize is undo transform
        return new Point(x, !isNormalize ? (int) root.getHeight() - y : y); //java's coord starts from upper left
    }

    public double getScaleX(double px, boolean deScale) {//convert theoretical metric to model metric if not descale
        return deScale ? (px / modelScale) * insituScale : (px / insituScale) * modelScale;
    }

    public double getScaleY(double py, boolean deScale) {
        return deScale ? (py / modelScale) * insituScale : (py / insituScale) * modelScale;
    }

    public Group gRoot;
    public final Pane root = new Pane();
    private final CanvasHelper helper;
    public final ObservableList<CShapes> shapesList = FXCollections.observableArrayList();
    private final Line guideX = new Line();
    private final Line guideY = new Line();
    private final Line guideZ = new Line();
    private Line[] gridX, gridY;
    //double t = 0;
    private double startX, startY, endX, endY;
    public double scale = 0.7; //follow this variable value

    public CShapes selectedShape = null;
    public CNode bufNode = null;
    public ArrayList<CNode> bufNodeArr = new ArrayList<>();
    public int nodeCount,
            elementCount,
            loadCount;
    public SimpleStringProperty canvasTitle = new SimpleStringProperty("Model"); //used to recognize a canvas instance
    public static int shapeType = 3; //-1=select, 2=line, 3=load, 4=node, 4=rect
    public boolean isCanvasShapeHovered = false;
    public static double strokeWidth = 2;
    public static String strokeColor = "black"; //we need to move these settings into configuration files
    public static SimpleStringProperty canvasTheme = new SimpleStringProperty("Dry White");
    public boolean isDisabled = false;

    class CanvasHelper {

        public CanvasHelper(Canvas2D cs) {
            this.cs = cs;
            this.initShapes();
            initEvents();
            keyEvents();
            drawEvent();
            themeEvents();
            signalEvents();
        }

        /**
         * draws lines. Some performance issues though; calling the shape from
         * the array is very bad
         *
         * @param e {@code MouseEvent)
         * @param state {@code int} state of mouse 1=pressed, 2=dragged, 3=released
         */
        public void lineDrawer(MouseEvent e) { //1=pressed, 2=dragged, 3=released
            if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                if (isCanvasShapeHovered || isDisabled) //if there s a shape under mouse..
                {
                    return;
                }
                startX = e.getX();
                startY = e.getY();
                shapesList.add(new CElement(this.cs, startX, startY, startX + 5, startY + 5)); //add shape to list
                root.getChildren().add(shapesList.get(shapesSize() - 1).getObjectContext()); //add to canvas
                //if a shape has been selected before, remove its selection state and status
                if (selectedShape != null) {
                    selectedShape.deselect();
                }
                selectedShape = shapesList.get(shapesSize() - 1); //our new shape is the selected one now
                selectedShape.select(); //our new shape is the selected one now
            } else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                if (isCanvasShapeHovered || isDisabled) //if there s a shape under mouse..
                {
                    return;
                }
                //here we dint perform selectedShape null checks cos it should NEVER be null
                CElement tmpSelectedShape = (CElement) shapesList.get(shapesSize() - 1);
                tmpSelectedShape.setEndX(e.getX());
                tmpSelectedShape.setEndY(e.getY());
                this.drawGuide(e.getX(), e.getY(), true);
            } else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
                this.drawGuide(endX, endY, false);
            }
        }

        public void nodeDrawer(MouseEvent e) {
            if (e.getEventType() == MouseEvent.MOUSE_CLICKED || e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                if (isCanvasShapeHovered || isDisabled) //if there s a shape under mouse..
                {
                    return;
                }
                startX = e.getX();
                startY = e.getY();
                shapesList.add(new CNode(this.cs, startX, startY, false)); //add shape to list
                root.getChildren().add(shapesList.get(shapesSize() - 1).getObjectContext()); //add to canvas
                //if a shape has been selected before, remove its selection state and status
                if (selectedShape != null) {
                    selectedShape.deselect();
                }
                selectedShape = shapesList.get(shapesSize() - 1); //our new shape is the selected one now
                selectedShape.select();
            } else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                if (isCanvasShapeHovered || isDisabled) //if there s a shape under mouse..
                {
                    return;
                }
                //here we dint perform selectedShape null checks cos it should NEVER be null
                //BUG ALERT!! if an element was prior selected, trying to drsg this node will cause Nullpointer error
                CNode tmpSelectedShape = (CNode) shapesList.get(shapesSize() - 1);
                tmpSelectedShape.setCenterX(e.getX());
                tmpSelectedShape.setCenterY(e.getY());
                this.drawGuide(e.getX(), e.getY(), true);
            } else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
                this.drawGuide(endX, endY, false);
            }
        }

        private void deleteCurrentShape() {
            if (selectedShape == null) {
                return;
            }
            selectedShape.destroy();
        }

        private void moveCurrentShape(KeyCode code) {
            if (selectedShape == null) {
                return;
            }
            double tx = selectedShape.getObjectContext().getLayoutX();
            double ty = selectedShape.getObjectContext().getLayoutY();
            if (null != code) {
                switch (code) {
                    //check to know t.. isnt going outta bounds or smn
                    case UP:
                        ty -= 0.8;
                        break;
                    case DOWN:
                        ty += 0.8;
                        break;
                    case RIGHT:
                        tx += 0.8;
                        break;
                    case LEFT:
                        tx -= 0.8;
                        break;
                    default:
                        break;
                }
            }
            selectedShape.move(tx, ty);
        }

        private void drawGuide(double x, double y, boolean isDraw) {
            int guideLength = 380;
            if (!isDraw) {
                guideX.setVisible(false);
                guideY.setVisible(false);
                guideZ.setVisible(false);
            } else {
                guideX.setVisible(true);
                guideY.setVisible(true);
                guideZ.setVisible(true);

                guideX.setStartX(x + guideLength);
                guideX.setStartY(y);
                guideX.setEndX(x - guideLength);
                guideX.setEndY(y);

                guideY.setStartX(x);
                guideY.setStartY(y + guideLength);
                guideY.setEndX(x);
                guideY.setEndY(y - guideLength);

                guideZ.setEndX(guideX.getStartX() - (guideLength / 5));
                guideZ.setEndY(guideY.getStartY() + (guideLength / 5));
                guideZ.setStartX(guideX.getEndX() + (guideLength / 5));
                guideZ.setStartY(guideY.getEndY() - (guideLength / 5));
            }
        }

        private int shapesSize() {
            return shapesList.size();
        }

        private void initShapes() {
            guideX.setStroke(Paint.valueOf("#EDC1FD"));
            guideY.setStroke(Paint.valueOf("#EDC1FD"));
            guideZ.setStroke(Paint.valueOf("#EDC1FD"));
            root.getChildren().addAll(guideX, guideY, guideZ);
        }

        private final Canvas2D cs;
        //private Text text = new Text();
    }
}

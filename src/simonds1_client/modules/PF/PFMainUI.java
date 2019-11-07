package simonds1_client.modules.PF;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import simonds1.core.CSlot;
import simonds1.core.ProjectManager;
import simonds1.core.SignalSlot;
import simonds1.core.SimonUtil;
import simonds1.core.transport.DataBox;
import simonds1.core.transport.DataBoxPayload;
import simonds1_client.modules.ModuleMainUI;
import simonds1_client.ui.NumberField;
import simonds1_client.ui.dialogs.ControlDialog;
import simonds1_client.ui.pad.Canvas2D;
import simonds1_client.ui.pad.CanvasTabManager;
import simonds1_client.ui.shapes.CElement;
import simonds1_client.ui.shapes.CNode;
import simonds1_client.ui.shapes.CShapes;

/**
 * Fresh new Plane Frame Pane, not window
 *
 * @author ADEDAMOLA
 */
public class PFMainUI {

    public PFMainUI(ModuleMainUI ui) {
        super();
        this.ui = ui;
        buildCanvas();
        mainEvents();
    }

    private void buildCanvas() {
        HBox.setHgrow(ui.canvasPane, Priority.ALWAYS);
        ui.en.canvasManager = new CanvasTabManager(Float.parseFloat(ui.en.getProp("insitu_scale")),
                Float.parseFloat(ui.en.getProp("model_scale")),
                ProjectManager.getFileTypeExtStr(ProjectManager.FileType.FRAME_MODEL),
                ui.WINDOW_ID);
        VBox.setVgrow(ui.en.canvasManager, Priority.ALWAYS);

        ui.canvasPane.getItems().addAll(ui.en.canvasManager);
    }

    ///////////////////
    private void mainEvents() {

        CSlot slot = (Object object) -> {
            if (ui.en.canvasManager.getCanvasAt(null).isDisabled) {
                return;
            }
            CShapes tmp = (CShapes) object;
            //objectsList.getSelectionModel().select(tmp);
            tmp.select();
            //since we are working with the current shape, we call him oveer there
            switch (tmp.getTypeStr()) {
                case "line":
                    new ControlDialog(new ElementPropertyPane((CElement) tmp), tmp.getTitle() + " Properties").dialog.show();
                    break;
                case "node":
                    new ControlDialog(new NodePropertyPane((CNode) tmp), tmp.getTitle() + " Properties").dialog.show();
                    break;
            }
        };
        SignalSlot.addSlot(ui.WINDOW_ID, "ShapeClickedresult", slot);
        SignalSlot.addSlot(ui.WINDOW_ID, "ShapeClickedmain", slot);

        SignalSlot.addSlot(ui.WINDOW_ID, "PrepareModel", datab -> prepareUploadModel((DataBoxPayload) datab));
    }

    private void prepareUploadModel(DataBoxPayload tModel) {
        //box up the payload
        DataBox data = new DataBox();
        data.config.put("main_module", "frame");
        data.config.put("sub_module", "2D_frame_design");
        data.config.put("sub_module2", "full_frame_design");
        data.config.put("project_name", ui.en.prj.props.get("project_name"));
        data.config.put("project_email", ui.en.prj.props.get("project_email"));
        data.config.put("payload_id", tModel.getTitle().substring(0, tModel.getTitle().length() - 4)
                + new SimpleDateFormat("Y-M-d-hh.mm").format(new Date()));
        //the probability of the time+int (id) of two payload
        //uploads being the same in one second is very low
        data.payload.put("frame_model", tModel); //id on server is time

        SignalSlot.emitSignal(ui.WINDOW_ID, "ModelPrepared", data);
    }

    private final ModuleMainUI ui;

    final class NodePropertyPane extends GridPane {

        public NodePropertyPane(CNode node) {
            canvas = ui.en.canvasManager.getCanvasAt(null);
            this.node = node;

            if (node.isResultMode) {
                showResultInfo();
                return;
            }
            this.mkUi();
            this.mkUi2();

            this.mkEvents();
            this.mkEvents2();
        }

        public void showResultInfo() {
            addRow(0, SimonUtil.decorateLabel(new Label("Title: "), null), new Label(node.getTitle()));
            addRow(1, SimonUtil.decorateLabel(new Label("Nodal Force X(KN): "), null), new Label(String.format("%s", node.forcex)));
            addRow(2, SimonUtil.decorateLabel(new Label("Nodal Force Y(KN): "), null), new Label(String.format("%s", node.forcey)));
            addRow(3, SimonUtil.decorateLabel(new Label("Displacement X(KN): "), null), new Label(String.format("%s", node.displacementx)));
            addRow(4, SimonUtil.decorateLabel(new Label("Displacement Y(KN): "), null), new Label(String.format("%s", node.displacementy)));
        }

        //col,row, colspan, rowspan
        public void mkUi() {
            title = new TextField(node.getTitle());
            title.setEditable(false);
            posX = new NumberField(String.valueOf(node.nx.doubleValue()));
            posY = new NumberField(String.valueOf(node.ny.doubleValue()));
            duplSpace = new NumberField("2");
            duplAngle = new NumberField("0");
            duplAmount = new NumberField("1");
            duplX = new Button("on X:");
            duplY = new Button("on Y:");
            bind = new Button("Bind");

            addRow(0, SimonUtil.decorateLabel(new Label("Title:"), null), title);
            addRow(1, SimonUtil.decorateLabel(new Label("X Coord:"), null), posX);
            addRow(2, SimonUtil.decorateLabel(new Label("Y Coord:"), null), posY);
            addRow(4, new Separator(Orientation.HORIZONTAL));
            addRow(5, new Label(""), SimonUtil.decorateLabel(new Label("Duplicate"), null));
            addRow(6, SimonUtil.decorateLabel(new Label("Space(m):"), null), duplSpace);
            addRow(7, SimonUtil.decorateLabel(new Label("Angle(deg):"), null), duplAngle);
            addRow(8, SimonUtil.decorateLabel(new Label("Amount:"), null), duplAmount);
            addRow(9, duplX, duplY);
            addRow(10, new Label(""));
        }

        public void mkUi2() {
            shapes = canvas.shapesList;
            ArrayList<String> listNode = new ArrayList<>(canvas.getShapesListStr("node"));
            conn1Box = new ComboBox<>(FXCollections.observableArrayList(listNode));
            conn1Box.getSelectionModel().select(node.nodeBound1);
            conn2Box = new ComboBox<>(FXCollections.observableArrayList(listNode));
            conn2Box.getSelectionModel().select(node.nodeBound2);
            loadVBox = new NumberField(String.valueOf(node.vLoad.get()));
            loadHBox = new NumberField(String.valueOf(node.hLoad.get()));
            boundaryBox = new ComboBox<>(FXCollections.observableArrayList("None", "Fixed", "Roller", "Pinned"));

            boundaryBox.setValue(node.boundary.get());

            addRow(11, new Separator(Orientation.HORIZONTAL));
            addRow(12, SimonUtil.decorateLabel(new Label("Bind:"), null), conn1Box, conn2Box, bind);
            addRow(14, new Separator(Orientation.HORIZONTAL));
            addRow(15, SimonUtil.decorateLabel(new Label("V-Load(KN):"), null), loadVBox);
            addRow(16, SimonUtil.decorateLabel(new Label("H-Load(KN):"), null), loadHBox);
            addRow(17, new Label("Boundary Condition"), boundaryBox);
        }

        public void mkEvents() {
            //possible issues? 
            bind.setOnAction(e -> {
                String bind1 = conn1Box.getValue(),
                        bind2 = conn2Box.getValue();
                if (bind1.isEmpty() && bind2.isEmpty()) {
                    return;
                }
                //get the first node
                canvas.createElements(null, node,
                        (CNode) canvas.getShape("node", bind1),
                        (CNode) canvas.getShape("node", bind2), false);//not in result mode
            });
            EventHandler<ActionEvent> ev = (e) -> {
                String space = duplSpace.getText(),
                        angle = duplAngle.getText(),
                        amount = duplAmount.getText();
                if (space == null || angle == null || amount == null) {
                    return;
                }
                if (space.isEmpty() || angle.isEmpty() || amount.isEmpty()) {
                    return;
                }
                double sp = canvas.getScaleX(Float.parseFloat(space), false),
                        nsp = Float.parseFloat(space),
                        ang = Float.parseFloat(angle),
                        x, y, nx, ny;
                int am = Integer.parseInt(amount);
                if (ang < 0) {
                    ang = 0;
                }
                double asp = sp;
                if (e.getSource().equals(duplX)) {
                    x = node.getCenterX() + sp;
                    y = node.getCenterY();
                    nx = node.nx.doubleValue() + nsp;
                    ny = node.ny.doubleValue();
                    while (am-- > 0) {
                        canvas.createNode(null, "0",
                                ang == 0 ? x : x + Math.cos(Math.toRadians(ang)) * asp,
                                ang == 0 ? y : y + Math.sin(Math.toRadians(ang)) * asp,
                                nx, ny, false).setLoad(node.hLoad.get(), node.vLoad.get());
                        //canvas.createNode(x, y);
                        asp += sp;
                        x += sp;
                        nx += nsp;
                    }
                } else { //if(e.getSource().equals(duplY)) --get it?
                    x = (int) node.getCenterX();
                    y = (int) node.getCenterY() - sp;//IMPORTANT: '-' inverts y
                    nx = node.nx.doubleValue();
                    ny = node.ny.doubleValue() + nsp;
                    while (am-- > 0) {
                        canvas.createNode(null, "0",
                                ang == 0 ? x : x + Math.cos(Math.toRadians(ang)) * asp,
                                ang == 0 ? y : y + Math.sin(Math.toRadians(ang)) * asp,
                                nx, ny, false);
                        //canvas.createNode(x, y);
                        asp += sp;
                        y += sp;
                        ny += nsp;
                    }
                }
            };
            duplX.setOnAction(ev);
            duplY.setOnAction(ev);
        }

        public void mkEvents2() {
            //dont allow non numerics in fields
            boundaryBox.valueProperty().addListener((ob, ov, nv) -> node.setBoundary(nv));
            loadVBox.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.setVLoad(Double.valueOf(nv));
                }
            });
            loadHBox.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.setHLoad(Double.valueOf(nv));
                }
            });
            posX.textProperty().addListener((ObservableValue<? extends String> ob, String ov, String nv) -> {
                if (!nv.isEmpty()) {
                    node.nx.set(Double.valueOf(nv));
                    node.setCenterX(canvas.getScaleX(Float.parseFloat(nv), false) + Canvas2D.CANVAS_PADDING_X);
                    //System.out.printf("nx:%s , x:%s\n", Float.parseFloat(nv), canvas.getScaleX(Float.parseFloat(nv), false)  +100);
                }
            });
            posY.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.ny.set(Double.valueOf(nv));
                    double tmp = canvas.getScaleY(Float.parseFloat(nv), false);
                    node.setCenterY(-tmp + Canvas2D.CANVAS_PADDING_Y); //IMPORTANT: '-' inverts y axis
                }
            });
        }
        private final CNode node;
        private ComboBox<String> conn1Box, conn2Box, boundaryBox;
        TextField title;
        NumberField posX, posY, duplSpace,
                duplAngle, duplAmount,
                loadVBox, loadHBox;
        Button duplX, duplY, bind;
        private ObservableList<CShapes> shapes;
        private final Canvas2D canvas;
    }

    final class ElementPropertyPane extends GridPane {

        public ElementPropertyPane(CElement node) {
            this.node = node;
            if (node.isResultMode) {
                showResultInfo();
                return;
            }
            this.mkUi();
            this.mkEvents();
        }

        public void showResultInfo() {
            addRow(0, SimonUtil.decorateLabel(new Label("Title: "), null), new Label(node.getTitle()));
            addRow(1, SimonUtil.decorateLabel(new Label("Member Force(KN):"), null), new Label(String.format("%s", node.force)));
            addRow(2, SimonUtil.decorateLabel(new Label("Member Stress(KN):"), null), new Label(String.format("%s", node.stress)));
        }

        public void mkUi() {
            title = new TextField(node.getTitle());
            title.setEditable(false);
            nodeBind1 = new Label(node.nodeBind1.getTitle());
            nodeBind2 = new Label(node.nodeBind2.getTitle());
            length = new NumberField(String.valueOf(SimonUtil.round(node.getLength(), 2)));
            length.setEditable(false);
            ym = new NumberField(node.ym);
            carea = new NumberField(node.carea);
            inertia = new NumberField(node.inertia);
            loadValue = new NumberField(node.loadValue.getValueSafe());
            loadValue.setDisable(node.loadType.get().equals("nl"));
            loadD2O = new NumberField(String.valueOf(node.loadD2O.get()));
            loadD2O.setDisable(!node.loadType.get().equals("p"));
            loadType = new ComboBox<>(FXCollections.observableArrayList("No Load", "Point Load", "UDL"));
            loadType.setValue(node.loadType.getValueSafe().equals("p") ? "Point Load" : node.loadType.getValueSafe().equals("u") ? "UDL" : "No Load");

            addRow(0, SimonUtil.decorateLabel(new Label("Title:"), null), title);
            addRow(1, SimonUtil.decorateLabel(new Label("Node 1:"), null), nodeBind1);
            addRow(2, SimonUtil.decorateLabel(new Label("Node 2:"), null), nodeBind2);
            addRow(3, SimonUtil.decorateLabel(new Label("Length(m):"), null), length);
            addRow(4, SimonUtil.decorateLabel(new Label("Young Modulus(KN/m^2):"), null), ym);
            addRow(5, SimonUtil.decorateLabel(new Label("Cross Sectional Area(m^2):"), null), carea);
            addRow(6, SimonUtil.decorateLabel(new Label("Linear Moment of Inertia(mm^4):"), null), inertia);
            addRow(7, new Separator(Orientation.HORIZONTAL));
            addRow(8, SimonUtil.decorateLabel(new Label("Load Type:"), null), loadType);
            addRow(9, SimonUtil.decorateLabel(new Label("Load Value (KN):"), null), loadValue);
            addRow(10, SimonUtil.decorateLabel(new Label("Distance to Origin(m):"), null), loadD2O);
        }

        private void mkEvents() {
            ym.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.ym = nv;
                }
            });
            carea.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.carea = nv;
                }
            });
            inertia.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.inertia = nv;
                }
            });
            loadValue.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.loadValue.setValue(nv);
                }
            });
            loadD2O.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.loadD2O.set(Double.valueOf(nv));
                }
            });
            loadType.valueProperty().addListener((ob, ov, nv) -> {
                node.loadType.setValue(nv.equals("Point Load") ? "p" : nv.equals("UDL") ? "u" : "nl");
                switch (nv) {
                    case "Point Load":
                        loadValue.setDisable(false);
                        loadD2O.setDisable(false);
                        break;
                    case "UDL":
                        loadValue.setDisable(false);
                        loadD2O.setDisable(true);
                        break;
                    default:
                        loadValue.setDisable(true);
                        loadD2O.setDisable(true);
                        break;
                }
            });
        }

        TextField title;
        private ComboBox<String> loadType;
        NumberField length, ym, carea, inertia, loadValue, loadD2O;
        Label nodeBind1, nodeBind2;
        private final CElement node;
    }
}

package simonds1_client.modules.TA;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
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
 * Fresh new Truss Pane, not window
 *
 * @author ADEDAMOLA
 */
public class TAMainUI {

    public TAMainUI(ModuleMainUI ui) {
        super();
        this.ui = ui;
        buildLeftAnalysisTab();
        buildCanvas();
        mainEvents();
    }

    private void buildLeftAnalysisTab() {
        VBox tmpl = new VBox();
        {
            VBox tmp = new VBox();
            tmp.setAlignment(Pos.CENTER);
            tmp.getChildren().add(doPurlinDesign);
            tmp.getChildren().add(new Label(" "));
            tmp.getChildren().addAll(new Label("Truss Spacing (m)"), trussSpacing);
            tmp.getChildren().addAll(new Label("Rafter Boundary"), trussRafterBound);
            trussRafterBound.setTooltip(new Tooltip("Comma Separated ID of the Elements along the rafter"));
            tmp.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tmp.getChildren().addAll(new Label("Dead Load on Slope (KN/m^2)"), deadLoad);
            tmp.getChildren().addAll(new Label("Imposed Load on Plan (KN/m^2)"), imposedLoad);
            tmp.getChildren().add(new Separator(Orientation.HORIZONTAL));
            purlinSection.getItems().addAll("Equal Angles", "Unequal Angles", "RHS", "CHS");
            purlinSection.getSelectionModel().selectFirst();
            tmp.getChildren().addAll(new Label("Section Type"), purlinSection);
            tmp.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tmpl.getChildren().add(new TitledPane("Purlin Design", tmp));
        }
        {
            VBox tmp = new VBox();
            tmp.setAlignment(Pos.CENTER);
            tmp.getChildren().add(doTiesDesign);
            tmp.getChildren().add(new Label(" "));
            tmp.getChildren().addAll(tiesIsEccentric);
            tiesConnectType.getItems().addAll("Welded", "Bolted");
            tiesConnectType.getSelectionModel().selectFirst();
            tmp.getChildren().addAll(new Label("Connection Tie(m)"), tiesConnectType);
            tmp.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tmp.getChildren().addAll(new Label("Bolt Hole"), tiesBoltHole);
            tmp.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tilesTable.getItems().addAll("UB", "UC", "Equal Angles", "Unequal Angles", "RHS");
            tilesTable.getSelectionModel().selectFirst();
            tmp.getChildren().addAll(new Label("Tiles Table"), tilesTable);
            tilesSteelGrade.getItems().addAll("S275", "S355", "S460");
            tilesSteelGrade.getSelectionModel().selectFirst();
            tmp.getChildren().addAll(new Label("Tiles Steel Grade"), tilesSteelGrade);
            tmp.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tmpl.getChildren().add(new TitledPane("Ties Design", tmp));
        }
        {
            VBox tmp = new VBox();
            tmp.setAlignment(Pos.CENTER);
            tmp.getChildren().add(doStrutDesign);
            tmp.getChildren().add(new Label(" "));
            structSteelGrade.getItems().addAll("S275", "S355", "S460");
            structSteelGrade.getSelectionModel().selectFirst();
            tmp.getChildren().addAll(new Label("Struct Steel Grade"), structSteelGrade);

            structTable.getItems().addAll("UB", "UC", "Channels", "RHS");
            structTable.getSelectionModel().selectFirst();
            tmp.getChildren().addAll(new Label("Struct Table"), structTable);
            tmp.getChildren().add(new Separator(Orientation.HORIZONTAL));
            structEffLenFactr.getItems().addAll("1.0", "0.85", "2.0", "0.7");
            structEffLenFactr.setEditable(true);
            structEffLenFactr.getSelectionModel().selectFirst();
            tmp.getChildren().addAll(new Label("Eff Length Factor"), structEffLenFactr);
            tmp.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tmpl.getChildren().add(new TitledPane("Struct Design", tmp));
        }
        Tab ttb = new Tab("Design", new ScrollPane(tmpl));
        ttb.setClosable(false);
        ui.leftBar.getTabs().add(ttb);
    }

    private void buildCanvas() {
        HBox.setHgrow(ui.canvasPane, Priority.ALWAYS);
        ui.en.canvasManager = new CanvasTabManager(Float.parseFloat(ui.en.getProp("insitu_scale")),
                Float.parseFloat(ui.en.getProp("model_scale")),
                ProjectManager.getFileTypeExtStr(ProjectManager.FileType.TRUSS_MODEL),
                ui.WINDOW_ID);
        VBox.setVgrow(ui.en.canvasManager, Priority.ALWAYS);

        ui.canvasPane.getItems().addAll(ui.en.canvasManager);
    }
    
    ///////////////////
    private void mainEvents() {

            doPurlinDesign.setSelected(true);
            doPurlinDesign.selectedProperty().addListener((ob, ov, nv) -> {
                trussSpacing.setDisable(!nv);
                trussRafterBound.setDisable(!nv);
                deadLoad.setDisable(!nv);
                imposedLoad.setDisable(!nv);
                purlinSection.setDisable(!nv);
            });
            doTiesDesign.setSelected(true);
            doTiesDesign.selectedProperty().addListener((ob, ov, nv) -> {
                tiesIsEccentric.setDisable(!nv);
                tiesConnectType.setDisable(!nv);
                tiesBoltHole.setDisable(!nv);
                tilesTable.setDisable(!nv);
                tilesSteelGrade.setDisable(!nv);
            });
            doStrutDesign.setSelected(true);
            doStrutDesign.selectedProperty().addListener((ob, ov, nv) -> {
                structSteelGrade.setDisable(!nv);
                structTable.setDisable(!nv);
                structEffLenFactr.setDisable(!nv);
            });
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
    private void prepareUploadModel(DataBoxPayload tModel){
        //box up the payload
            DataBox data = new DataBox();
            data.config.put("main_module", "truss");
            data.config.put("sub_module", "truss_design");
            data.config.put("sub_module2", "full_truss_design");
            data.config.put("project_name", ui.en.prj.props.get("project_name"));
            data.config.put("project_email", ui.en.prj.props.get("project_email"));
            data.config.put("payload_id", tModel.getTitle().substring(0, tModel.getTitle().length() - 4)+
                    new SimpleDateFormat("Y-M-d-hh.mm").format(new Date()));
            //the probability of the time+int (id) of two payload
            //uploads being the same in one second is very low
            data.payload.put("truss_model", tModel); //id on server is time
            data.stringPayload.put("dead_load", deadLoad.getText());
            data.stringPayload.put("imposed_load", imposedLoad.getText());
            data.stringPayload.put("truss_spacing", trussSpacing.getText());
            data.stringPayload.put("truss_rafterboundary", trussRafterBound.getText());
            data.stringPayload.put("ties_steelgrade", tilesSteelGrade.getValue());
            data.stringPayload.put("ties_iseccentric", tiesIsEccentric.isSelected() ? "true":"false");
            data.stringPayload.put("ties_conntype", tiesConnectType.getValue());
            data.stringPayload.put("ties_bolthole", tiesBoltHole.getText());
            data.stringPayload.put("struct_steelgrade", structSteelGrade.getValue());
            data.stringPayload.put("struct_table", structTable.getValue());
            data.stringPayload.put("struct_efflft", structEffLenFactr.getValue());
            data.stringPayload.put("ties_table", tilesTable.getValue());
            data.stringPayload.put("purlin_section", purlinSection.getValue());
            data.stringPayload.put("do_purlin", String.valueOf(doPurlinDesign.isSelected()));
            data.stringPayload.put("do_ties", String.valueOf(doTiesDesign.isSelected()));
            data.stringPayload.put("do_strut", String.valueOf(doStrutDesign.isSelected()));

            SignalSlot.emitSignal(ui.WINDOW_ID, "ModelPrepared", data);
    }
    

    private final ModuleMainUI ui;

    public NumberField deadLoad = new NumberField("0", "Dead load on Slope"),
            imposedLoad = new NumberField("0", "Imposed load on Slope"),
            trussSpacing = new NumberField("0", "Spacing Between Trusses");
    public TextField trussRafterBound = new TextField();
    public NumberField tiesBoltHole = new NumberField("0", "Specify Bolt Hole");//in mm
    public ComboBox<String> tilesTable = new ComboBox<>(),
            structTable = new ComboBox<>(),
            structEffLenFactr = new ComboBox<>(),
            tilesSteelGrade = new ComboBox<>(),
            structSteelGrade = new ComboBox<>(),
            tiesConnectType = new ComboBox<>(),
            purlinSection = new ComboBox<>();
    public CheckBox tiesIsEccentric = new CheckBox("Eccentric Connection?"),
            doPurlinDesign = new CheckBox("Design Purlin Members?"),
            doTiesDesign = new CheckBox("Design Ties Members?"),
            doStrutDesign = new CheckBox("Design Strut Members?");
//////////////////////////////

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
                double nsp = Float.parseFloat(space),
                        sp = canvas.getScaleX(nsp, false),
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

            addRow(0, SimonUtil.decorateLabel(new Label("Title:"), null), title);
            addRow(1, SimonUtil.decorateLabel(new Label("Node 1:"), null), nodeBind1);
            addRow(2, SimonUtil.decorateLabel(new Label("Node 2:"), null), nodeBind2);
            addRow(3, SimonUtil.decorateLabel(new Label("Length(m):"), null), length);
            addRow(4, SimonUtil.decorateLabel(new Label("Young Modulus(KN/m^2):"), null), ym);
            addRow(5, SimonUtil.decorateLabel(new Label("Cross Sectional Area(m^2):"), null), carea);
            addRow(6, new Separator(Orientation.HORIZONTAL));
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
        }

        TextField title;
        NumberField length, ym, carea;
        Label nodeBind1, nodeBind2;
        private final CElement node;
    }
}

package simonds1_client.modules.CD;

import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
import simonds1_client.ui.shapes.CColumn;
import simonds1_client.ui.shapes.CShapes;

/**
 *
 * @author ADEDAMOLA
 */
public class CDMainUI {

    public CDMainUI(ModuleMainUI ui) {
        super();
        this.ui = ui;
        buildCanvas();
        mainEvents();
    }

    private void buildCanvas() {
        HBox.setHgrow(ui.canvasPane, Priority.ALWAYS);
        ui.en.canvasManager = new CanvasTabManager(Float.parseFloat(ui.en.getProp("insitu_scale")),
                Float.parseFloat(ui.en.getProp("model_scale")),
                ProjectManager.getFileTypeExtStr(ProjectManager.FileType.COLUMN_MODEL),
                ui.WINDOW_ID);
        VBox.setVgrow(ui.en.canvasManager, Priority.ALWAYS);

        ui.canvasPane.getItems().addAll(ui.en.canvasManager);
    }

    ///////////////////
    private void mainEvents() {
        ui.toolbar.columnBut.setOnAction(e -> {
            new ControlDialog(new ColumnPropertyPane(null), "New Column Properties").dialog.show();
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
                    case "column":
                        new ControlDialog(new ColumnPropertyPane((CColumn) tmp), tmp.getTitle() + " Properties").dialog.show();
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
            data.config.put("main_module", "design");
            data.config.put("sub_module", "column_design");
            data.config.put("sub_module2", "full_column_design");
            data.config.put("project_name", ui.en.prj.props.get("project_name"));
            data.config.put("project_email", ui.en.prj.props.get("project_email"));
            data.config.put("payload_id", ui.en.prj.props.get("project_name")
                    + tModel.getTitle().substring(0, tModel.getTitle().length() - 4)+
                    new SimpleDateFormat("Y-M-d-hh.mm ").format(new Date()));
            //the probability of the time+int (id) of two payload
            //uploads being the same in one second is very low
            data.payload.put("column_model", tModel); //each column has all you can ask for

        SignalSlot.emitSignal(ui.WINDOW_ID, "ModelPrepared", data);
    }
    private final ModuleMainUI ui;

    final class ColumnPropertyPane extends GridPane {

        public ColumnPropertyPane(CColumn node) {
            canvas = ui.en.canvasManager.getCanvasAt(null);
            this.node = node == null ? new CColumn(canvas, false) : node;

            if (this.node.isResultMode) { //dont worry, wont be null
                showResultInfo();
                return;
            }
            this.mkUi();
            this.mkEvents2();
            if(node == null)
                mkEvents();
        }

        public void showResultInfo() {
            addRow(0, SimonUtil.decorateLabel(new Label("Title: "), null), new Label(node.getTitle()));
            addRow(1, SimonUtil.decorateLabel(new Label("Nodal Force X(KN): "), null),
                    new Label(String.format("%s", node.fc)));
            addRow(2, SimonUtil.decorateLabel(new Label("Nodal Force Y(KN): "), null),
                    new Label(String.format("%s", node.fc)));
            addRow(3, SimonUtil.decorateLabel(new Label("Displacement X(KN): "), null),
                    new Label(String.format("%s", node.fc)));
            addRow(4, SimonUtil.decorateLabel(new Label("Displacement Y(KN): "), null),
                    new Label(String.format("%s", node.fc)));
        }

        //col,row, colspan, rowspan
        public void mkUi() {
            title = new TextField(node.getTitle());
            title.setEditable(false);
            cFc = new NumberField(String.valueOf(node.fc.get()));
            cFcu = new NumberField(String.valueOf(node.fcu.get()));
            cMx = new NumberField(String.valueOf(node.mx.get()));
            cMy = new NumberField(String.valueOf(node.my.get()));
            cHeight = new NumberField(String.valueOf(node.height.get()));
            cBeta = new NumberField(String.valueOf(node.beta.get()));
            steelGrade = new ComboBox<>(FXCollections.observableArrayList("S275", "S355", "S460"));
            trialSection = new ComboBox<>(FXCollections.observableArrayList("UB", "UC"));
            bracedOption = new ComboBox<>(FXCollections.observableArrayList("Braced Column", "UnBraced Column"));
            bracedOption.getSelectionModel().select(String.valueOf(node.le.get()));
            steelGrade.getSelectionModel().select(node.steelGrade);
            trialSection.getSelectionModel().select(node.trialSection);
            bracedOption.setEditable(true);

            addRow(0, SimonUtil.decorateLabel(new Label("Title: "), null), title);
            addRow(1, SimonUtil.decorateLabel(new Label("Fc (KN): "), null), cFc);
            addRow(2, SimonUtil.decorateLabel(new Label("Height(m): "), null), cHeight);
            addRow(3, SimonUtil.decorateLabel(new Label("Mx (KN/m): "), null), cMx);
            addRow(4, SimonUtil.decorateLabel(new Label("My (KN/m): "), null), cMy);
            addRow(5, SimonUtil.decorateLabel(new Label("Fcu (N/mm^2): "), null), cFcu);
            addRow(6, SimonUtil.decorateLabel(new Label("beta value: "), null), cBeta);
            addRow(7, new Label(""));
            addRow(8, SimonUtil.decorateLabel(new Label("Column Type: "), null), bracedOption);
            addRow(9, SimonUtil.decorateLabel(new Label("Steel Grade: "), null), steelGrade);
            addRow(10, SimonUtil.decorateLabel(new Label("Trial Section: "), null), trialSection);
        }

        public void mkEvents() {
            Button createBut = new Button("Create");
            addRow(9, SimonUtil.decorateLabel(new Label(""), null), createBut);
            createBut.setOnAction(e -> {
                node.setStartX(node.getStartX() - (beamCount*15));
                node.init(); //build first
                canvas.createColumn(node);
                beamCount++;
            });
        }

        public void mkEvents2() {
            //dont allow non numerics in fields
            bracedOption.valueProperty().addListener((ob, ov, nv) -> {
                 if (nv.isEmpty())
                     return;
                switch (nv) {
                    case "Braced Column":
                        node.le.set(0.7*node.height.get());
                        break;
                    case "UnBraced Column":
                        node.le.set(1.2*node.height.get());
                        break;
                    default:
                        node.le.set(Double.valueOf(nv));
                        break;
                }
            });
            steelGrade.valueProperty().addListener((ob, ov, nv)->{
                node.steelGrade = nv;
            });
            trialSection.valueProperty().addListener((ob, ov, nv)->{
                node.trialSection = nv;
            });
            cMx.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.mx.set(Double.valueOf(nv));
                }
            });
            cMy.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.my.set(Double.valueOf(nv));
                }
            });
            cFc.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.fc.set(Double.valueOf(nv));
                }
            });
            cFcu.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.fcu.set(Double.valueOf(nv));
                }
            });
            cBeta.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.beta.set(Double.valueOf(nv));
                }
            });
            cHeight.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.height.set(Double.valueOf(nv));
                    double x = canvas.getScaleX(0, false) + 400,
                            y = -canvas.getScaleY(0, false) + 400;
                    node.setStartX(x);
                    node.setStartY(y); //IMPORTANT: '-' inverts y axis
                    node.setEndX(x);
                    node.setEndY((-canvas.getScaleY(Float.parseFloat(nv), false)) +y); //IMPORTANT: '-' inverts y axis
                    //System.out.println(node.getEndY());
                }
            });
        }
        private CColumn node = null;
        TextField title;
        public NumberField cMx = new NumberField("0", "Moment about minor axis"),
                cMy = new NumberField("0", "Moment about major axis"),
                cFc = new NumberField("0", "Axial Force"),
                cHeight = new NumberField("0", "Columns Height"),
                cFcu = new NumberField("0", "Concrete Grade"),
                cBeta = new NumberField("0", "Ratio of Smaller to Larger End Moments");
        public ComboBox<String> steelGrade = new ComboBox<>(),
                trialSection = new ComboBox<>(),
                bracedOption = new ComboBox<>();
        public int beamCount = 0;
        private final Canvas2D canvas;
    }
}

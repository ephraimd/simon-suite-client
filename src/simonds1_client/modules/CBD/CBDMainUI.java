package simonds1_client.modules.CBD;

import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
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
import simonds1_client.ui.shapes.CBeam;
import simonds1_client.ui.shapes.CShapes;

/**
 *
 * @author ADEDAMOLA
 */
public class CBDMainUI {

    public CBDMainUI(ModuleMainUI ui) {
        super();
        this.ui = ui;
        buildCanvas();
        mainEvents();
    }

    private void buildCanvas() {
        HBox.setHgrow(ui.canvasPane, Priority.ALWAYS);
        ui.en.canvasManager = new CanvasTabManager(Float.parseFloat(ui.en.getProp("insitu_scale")),
                Float.parseFloat(ui.en.getProp("model_scale")),
                ProjectManager.getFileTypeExtStr(ProjectManager.FileType.CONCRETE_BEAM_MODEL),
                ui.WINDOW_ID);
        VBox.setVgrow(ui.en.canvasManager, Priority.ALWAYS);

        ui.canvasPane.getItems().addAll(ui.en.canvasManager);
    }

    ///////////////////
    private void mainEvents() {
        ui.toolbar.rcbeamBut.setOnAction(e -> {
            new ControlDialog(new BeamPropertyPane(null), "New RCD Beam Properties").dialog.show();
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
                    case "beam":
                        new ControlDialog(new BeamPropertyPane((CBeam) tmp), tmp.getTitle() + " Properties").dialog.show();
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
            data.config.put("main_module", "concrete");
            data.config.put("sub_module", "beam_design");
            data.config.put("sub_module2", "full_beam_design");
            data.config.put("project_name", ui.en.prj.props.get("project_name"));
            data.config.put("project_email", ui.en.prj.props.get("project_email"));
            data.config.put("payload_id", ui.en.prj.props.get("project_name")
                    + tModel.getTitle().substring(0, tModel.getTitle().length() - 4)+
                    new SimpleDateFormat("Y-M-d-hh.mm ").format(new Date()));
            //the probability of the time+int (id) of two payload
            //uploads being the same in one second is very low
            data.payload.put("beam_model", tModel); //each beam has all you can ask for

        SignalSlot.emitSignal(ui.WINDOW_ID, "ModelPrepared", data);
    }
    private final ModuleMainUI ui;

    final class BeamPropertyPane extends GridPane {

        public BeamPropertyPane(CBeam node) {
            canvas = ui.en.canvasManager.getCanvasAt(null);
            this.node = node == null ? new CBeam(canvas, false) : node;

            if (this.node.isResultMode) { //dont worry, wont be null
                showResultInfo();
                return;
            }
            this.mkUi();
            this.mkEvents2();
            if (node == null) {
                mkEvents();
            }
        }

        public void showResultInfo() {
            addRow(0, SimonUtil.decorateLabel(new Label("Title: "), null), new Label(node.getTitle()));
            addRow(1, SimonUtil.decorateLabel(new Label("Results are displayed in Result Table"), null));
        }

        //col,row, colspan, rowspan
        public void mkUi() {
            title = new TextField(node.getTitle());
            title.setEditable(false);
            cSpan = new NumberField(String.valueOf(node.span.get()));
            deadLoad = new NumberField(String.valueOf(node.getNumberProp("dead_load")));
            liveLoad = new NumberField(String.valueOf(node.getNumberProp("live_load")));
            Fcu = new NumberField(String.valueOf(node.getNumberProp("concrete_grade")));
            reBarCSize = new NumberField(String.valueOf(node.getNumberProp("c_rebar")));
            reBarTSize = new NumberField(String.valueOf(node.getNumberProp("t_rebar")));
            steelGrade = new ComboBox<>(FXCollections.observableArrayList("S275", "S355", "S460"));
            beamType = new ComboBox<>(FXCollections.observableArrayList("Simply Supported", "Cantilever", "Continous"));
            beamType.getSelectionModel().select(node.beamType);
            steelGrade.getSelectionModel().select(node.steelGrade);
            isFlanged.setSelected(node.getBoolProp("is_flanged"));
            tFlanged.setSelected(node.getBoolProp("t_flanged"));
            tFlanged.disableProperty().bind(isFlanged.selectedProperty().not());

            addRow(0, SimonUtil.decorateLabel(new Label("Title: "), null), title);
            addRow(1, SimonUtil.decorateLabel(new Label("Span (m): "), null), cSpan);
            addRow(2, SimonUtil.decorateLabel(new Label("Dead Load (KNm): "), null), deadLoad);
            addRow(3, SimonUtil.decorateLabel(new Label("Imposed Load (KNm):"), null), liveLoad);
            addRow(4, SimonUtil.decorateLabel(new Label("Concrete Grade: "), null), Fcu);
            addRow(5, SimonUtil.decorateLabel(new Label("Compression Rebar Diameter(mm): "), null), reBarCSize);
            addRow(6, SimonUtil.decorateLabel(new Label("Tension Rebar Diameter(mm): "), null), reBarTSize);
            addRow(7, SimonUtil.decorateLabel(new Label("Beam Type:"), null), beamType);
            addRow(8, new Label(""));
            addRow(9, SimonUtil.decorateLabel(new Label("Steel Grade: "), null), steelGrade);
            addRow(10, isFlanged, tFlanged);
        }

        public void mkEvents() {
            Button createBut = new Button("Create RCD Beam");
            addRow(11, SimonUtil.decorateLabel(new Label(""), null), createBut);
            createBut.setOnAction(e -> {
                node.init(); //build first
                canvas.createBeam(node);
            });
        }

        public void mkEvents2() {
            //dont allow non numerics in fields
            steelGrade.valueProperty().addListener((ob, ov, nv) -> {
                node.steelGrade = nv;
            });
            beamType.valueProperty().addListener((ob, ov, nv) -> {
                node.beamType = nv;
            });
            deadLoad.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.setNumberProp("dead_load", Double.valueOf(nv));
                }
            });
            liveLoad.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.setNumberProp("live_load", Double.valueOf(nv));
                }
            });
            Fcu.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.setNumberProp("concrete_grade", Double.valueOf(nv));
                }
            });
            reBarCSize.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.setNumberProp("c_rebar", Double.valueOf(nv));
                }
            });
            reBarTSize.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.setNumberProp("t_rebar", Double.valueOf(nv));
                }
            });
            isFlanged.setOnAction(ev -> {
                node.setBoolProp("is_flanged", isFlanged.isSelected());
            });
            tFlanged.setOnAction(ev -> {
                node.setBoolProp("t_flanged", tFlanged.isSelected());
            });
            
            cSpan.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.span.set(Double.valueOf(nv));
                    double x = canvas.getScaleX(0, false) + 400,
                            y = -canvas.getScaleY(0, false) + 400,
                            width = canvas.getScaleY(Double.valueOf(nv), false) + node.getWidth();
                    node.setX(x);
                    node.setY(y); //IMPORTANT: '-' inverts y axis
                    node.setWidth(width);
                    //System.out.println(node.getEndY());
                }
            });
        }
        private CBeam node = null;
        TextField title;
        public NumberField deadLoad = new NumberField("0", "Dead Load (KNm)"),
                liveLoad = new NumberField("0", "Live Load (KNm)"),
                cSpan = new NumberField("0", "Beam Span (m)"),
                Fcu = new NumberField("0", "Concrete Grade "),
                reBarTSize = new NumberField("0", "Tension Rebar Diameter"),
                reBarCSize = new NumberField("0", "Compression Rebar Diameter");
        public ComboBox<String> steelGrade = new ComboBox<>(),
                beamType = new ComboBox<>();
        private CheckBox isFlanged = new CheckBox("is Beam Flanged?"),
                tFlanged = new CheckBox("is Beam T-Flanged?");
        private final Canvas2D canvas;
    }
}

package simonds1_client.modules.BD;

import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import simonds1_client.ui.shapes.CBeam;
import simonds1_client.ui.shapes.CShapes;

/**
 *
 * @author ADEDAMOLA
 */
public class BDMainUI {

    public BDMainUI(ModuleMainUI ui) {
        super();
        this.ui = ui;
        buildCanvas();
        mainEvents();
    }

    private void buildCanvas() {
        HBox.setHgrow(ui.canvasPane, Priority.ALWAYS);
        ui.en.canvasManager = new CanvasTabManager(Float.parseFloat(ui.en.getProp("insitu_scale")),
                Float.parseFloat(ui.en.getProp("model_scale")),
                ProjectManager.getFileTypeExtStr(ProjectManager.FileType.BEAM_MODEL),
                ui.WINDOW_ID);
        VBox.setVgrow(ui.en.canvasManager, Priority.ALWAYS);

        ui.canvasPane.getItems().addAll(ui.en.canvasManager);
    }

    ///////////////////
    private void mainEvents() {
        ui.toolbar.beamBut.setOnAction(e -> {
            new ControlDialog(new BeamPropertyPane(null), "New Steel Beam Properties").dialog.show();
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
            data.config.put("main_module", "design");
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
            addRow(1, SimonUtil.decorateLabel(new Label("Nodal Force X(KN): "), null),
                    new Label(String.format("%s", node.span)));
            addRow(2, SimonUtil.decorateLabel(new Label("Nodal Force Y(KN): "), null),
                    new Label(String.format("%s", node.span)));
            addRow(3, SimonUtil.decorateLabel(new Label("Displacement X(KN): "), null),
                    new Label(String.format("%s", node.span)));
            addRow(4, SimonUtil.decorateLabel(new Label("Displacement Y(KN): "), null),
                    new Label(String.format("%s", node.span)));
        }

        //col,row, colspan, rowspan
        public void mkUi() {
            title = new TextField(node.getTitle());
            title.setEditable(false);
            cSpan = new NumberField(String.valueOf(node.span.get()));
            cLoad = new NumberField(String.valueOf(node.vLoad.get()));
            cLoadU = new NumberField(String.valueOf(node.vLoadU.get()));
            cFv = new NumberField(String.valueOf(node.fv.get()));
            cMoment = new NumberField(String.valueOf(node.moment.get()));
            steelGrade = new ComboBox<>(FXCollections.observableArrayList("S275", "S355", "S460"));
            trialSection = new ComboBox<>(FXCollections.observableArrayList("UB", "UC"));
            beamType = new ComboBox<>(FXCollections.observableArrayList("Simply Supported", "Cantilever"));
            beamType.getSelectionModel().select(node.beamType.equals("ss") ? "Simply Supported" : "Cantilever");
            steelGrade.getSelectionModel().select(node.steelGrade);
            trialSection.getSelectionModel().select(node.trialSection);
            hasPlaster.setSelected(node.hasPlasters);

            addRow(0, SimonUtil.decorateLabel(new Label("Title: "), null), title);
            addRow(1, SimonUtil.decorateLabel(new Label("Span (m): "), null), cSpan);
            addRow(2, SimonUtil.decorateLabel(new Label("Imposed Point Load (KN): "), null), cLoad);
            addRow(3, SimonUtil.decorateLabel(new Label("Imposed UDL (KNm):"), null), cLoadU);
            addRow(4, SimonUtil.decorateLabel(new Label("Moment (KNm): "), null), cMoment);
            addRow(5, SimonUtil.decorateLabel(new Label("Support Reaction (KN): "), null), cFv);
            addRow(6, SimonUtil.decorateLabel(new Label("Beam Type:"), null), beamType, hasPlaster);
            addRow(7, new Label(""));
            addRow(8, SimonUtil.decorateLabel(new Label("Steel Grade: "), null), steelGrade);
            addRow(9, SimonUtil.decorateLabel(new Label("Trial Section: "), null), trialSection);
        }

        public void mkEvents() {
            Button createBut = new Button("Create Beam");
            addRow(10, SimonUtil.decorateLabel(new Label(""), null), createBut);
            createBut.setOnAction(e -> {
                node.setY(node.getY() - (beamCount*15));
                node.init(); //build first
                canvas.createBeam(node);
                ++beamCount;
            });
        }

        public void mkEvents2() {
            //dont allow non numerics in fields
            steelGrade.valueProperty().addListener((ob, ov, nv) -> {
                node.steelGrade = nv;
            });
            trialSection.valueProperty().addListener((ob, ov, nv) -> {
                node.trialSection = nv;
            });
            beamType.valueProperty().addListener((ob, ov, nv) -> {
                node.beamType = nv.contains("Simply") ? "ss" : "cl";
            });
            cLoad.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.vLoad.set(Double.valueOf(nv));
                }
            });
            cLoadU.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.vLoadU.set(Double.valueOf(nv));
                }
            });
            cFv.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.fv.set(Double.valueOf(nv));
                }
            });
            cMoment.textProperty().addListener((ob, ov, nv) -> {
                if (!nv.isEmpty()) {
                    node.moment.set(Double.valueOf(nv));
                }
            });
            hasPlaster.selectedProperty().addListener((ob, ov, nv) -> node.hasPlasters = nv);
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
        public NumberField cLoad = new NumberField("0", "Imposed Point Load value"),
                cLoadU = new NumberField("0", "Imposed UDL value"),
                cSpan = new NumberField("0", "Beam Span"),
                cMoment = new NumberField("0", "Moment"),
                cFv = new NumberField("0", "Support Load value");
        public ComboBox<String> steelGrade = new ComboBox<>(),
                trialSection = new ComboBox<>(),
                beamType = new ComboBox<>();
        public int beamCount = 0;
        private CheckBox hasPlaster = new CheckBox("is Beam Plastered?");
        private final Canvas2D canvas;
    }
}

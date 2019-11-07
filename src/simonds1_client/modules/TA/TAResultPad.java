package simonds1_client.modules.TA;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import simonds1.core.SimonUtil;
import simonds1.core.transport.DataBox;
import simonds1.core.transport.SDSModel;
import simonds1.core.transport.TabledModel;
import simonds1_client.ui.pad.Canvas2D;
import simonds1_client.ui.shapes.CElement;
import simonds1_client.ui.shapes.CNode;

/**
 * Models the truss results
 *
 * @author ADEDAMOLA
 */
public final class TAResultPad extends Stage {
    public String windowID;

    public TAResultPad(DataBox result, String windowID) {
        this.result = result;
        this.windowID = windowID;
        this.setTitle(result.config.get("payload_title") + " Result - Simon Design Suite");
        this.scene = new Scene(this.root, 900, 500);
        //scene.getStylesheets().add(SimonUtil.resPath + "bootstrap2.css");
        this.setScene(this.scene);
        this.initStyle(StageStyle.UNIFIED);
        this.setupUI();
    }

    public void setupUI() {
        buildTabs();
        root.setCenter(tpane);
        buildBottom();
        new ResultEngine();
    }

    private void buildTabs() {
        SplitPane tmp_layout = new SplitPane(buildLeftPane(), buildRightPane());
        tmp_layout.setDividerPositions(0.3f, 0.7f);
        tpane.getTabs().add(new Tab("Analysis & Design", tmp_layout));
    }
    private Node buildLeftPane() {
        tableRoot.getPanes().add(new TitledPane("Nodal Forces", mkNFTable()));
        tableRoot.getPanes().add(new TitledPane("Nodal Displacement", mkNDTable()));
        tableRoot.getPanes().add(new TitledPane("Forces and Stresses", mkFSTable()));
        if (result.arrayPayload.get("purlin_sizes") != null){
            VBox tmpl = new VBox();
            tmpl.getChildren().addAll(purlinNotif);
            tmpl.getChildren().addAll(new Label("    "));
            tmpl.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tmpl.getChildren().addAll(new Label("Sizes"), purlinSizes);
            tableRoot.getPanes().add(new TitledPane("Purlin Design", tmpl));
        }
        if (result.arrayPayload.get("ties_sizes") != null){
            VBox tmpl = new VBox();
            tmpl.getChildren().addAll(tilesNotif);
            tmpl.getChildren().addAll(new Label("    "));
            tmpl.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tmpl.getChildren().addAll(new Label("Sizes"), tilesSizes);
            tableRoot.getPanes().add(new TitledPane("Ties Design", tmpl));
        }
        if (result.arrayPayload.get("struct_sizes") != null){
            VBox tmpl = new VBox();
            tmpl.getChildren().addAll(new HBox(structNotif));
            tmpl.getChildren().addAll(new Label("    "));
            tmpl.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tmpl.getChildren().addAll(new Label("Sizes"), structSizes);
            tmpl.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tableRoot.getPanes().add(new TitledPane("Struct Design", tmpl));
        }
        SplitPane.setResizableWithParent(this.tableRoot, false);
        return tableRoot;
    }
    private Node buildRightPane() {
        return buildDsiplCanv();
    }

    private Node buildDsiplCanv() {
        ArrayList<Number> dispArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_dsp")).table.get("Displacements");//should always be divisible by two
        ArrayList<Number> stressArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_fs")).table.get("STRESS(N/mm^2)");
        ArrayList<Number> forcesArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_fs")).table.get("FORCES(kN)");
        ArrayList<Number> nfxArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_nf")).table.get("Node Force X(KN)");
        ArrayList<Number> nfyArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_nf")).table.get("Node Force Y(KN)");
        SDSModel model = (SDSModel) result.payload.get("truss_model");

        Canvas2D canv = new Canvas2D(windowID);
        canv.isDisabled = true;
        //canv.importModel(model, false);

        Canvas2D canv2 = new Canvas2D(windowID);
        canv2.importModel(model, false);

        int dispArrcount = 0, ndct = 0, elect = 0;
        double ndispx, ndispy;
        for (int ij = 0; ij < canv2.shapesList.size(); ij++) {
            if (canv2.shapesList.get(ij).getTypeStr().equals("line")) {
                CElement etmp = (CElement) canv2.shapesList.get(ij);
                etmp.force = forcesArr.get(elect).doubleValue();
                etmp.stress = stressArr.get(elect).doubleValue();
                etmp.isResultMode = true;
                canv2.shapesList.set(ij, etmp);
                elect++;
            } else if (canv2.shapesList.get(ij).getTypeStr().equals("node")) {
                CNode ntmp = (CNode) canv2.shapesList.get(ij);
                ndispx = dispArr.get(dispArrcount++).doubleValue();
                ndispy = dispArr.get(dispArrcount++).doubleValue();
                ntmp.setCenterX(ntmp.getCenterX() + ndispx);
                ntmp.setCenterY(ntmp.getCenterY() - ndispy); //IMPORTANT: '-' inverts y axis
                ntmp.displacementx = ndispx;
                ntmp.displacementy = ndispy;
                ntmp.forcex = nfxArr.get(ndct).doubleValue();
                ntmp.forcey = nfyArr.get(ndct).doubleValue();
                ntmp.isResultMode = true;
                canv2.shapesList.set(ij, ntmp);
                ndct++;
            }
        }
        String tmp = Canvas2D.strokeColor;
        Canvas2D.strokeColor = "red";
        //canv2.modelScale = canv2.modelScale / 2;
        //draw the gridlines
        canv.applyGridlines();
        canv.importModel(canv2.exportModel(), true);//added
        Canvas2D.strokeColor = tmp;

        return canv;
        //tpane.getTabs().add(new Tab("Displacement plot", canv));
        //tpane.getSelectionModel().selectLast();
    }

    private Node mkNFTable() {
        TabledModel tbl = (TabledModel) result.payload.get("table_nf");
        TableView<HashMap<String, String>> tbv = new TableView<>(FXCollections.observableArrayList(tbl.getModel()));
        TableColumn<HashMap<String, String>, String> nno = new TableColumn<>("Node No");
        tbv.getColumns().add(nno);
        nno.setCellValueFactory(e -> {
            String t = e.getValue().get("Node No");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nfx = new TableColumn<>("Node Force X(KN)");
        tbv.getColumns().add(nfx);
        nfx.setCellValueFactory(e -> {
            String t = e.getValue().get("Node Force X(KN)");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nfy = new TableColumn<>("Node Force Y(KN)");
        tbv.getColumns().add(nfy);
        nfy.setCellValueFactory(e -> {
            String t = e.getValue().get("Node Force Y(KN)");
            return new SimpleStringProperty(t);
        });
        return tbv;
    }

    private Node mkNDTable() {
        TabledModel tbl = (TabledModel) result.payload.get("table_nd");
        TableView<HashMap<String, String>> tbv = new TableView<>(FXCollections.observableArrayList(tbl.getModel()));
        TableColumn<HashMap<String, String>, String> ldc = new TableColumn<>("Load Case");
        tbv.getColumns().add(ldc);
        ldc.setCellValueFactory(e -> {
            String t = e.getValue().get("Load Case");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nno = new TableColumn<>("Node No");
        tbv.getColumns().add(nno);
        nno.setCellValueFactory(e -> {
            String t = e.getValue().get("Node No");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nfx = new TableColumn<>("Node Disp X(cm)");
        tbv.getColumns().add(nfx);
        nfx.setCellValueFactory(e -> {
            String t = e.getValue().get("Node Disp X(cm)");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nfy = new TableColumn<>("Node Disp Y(cm)");
        tbv.getColumns().add(nfy);
        nfy.setCellValueFactory(e -> {
            String t = e.getValue().get("Node Disp Y(cm)");
            return new SimpleStringProperty(t);
        });
        return tbv;
    }
    private Node mkFSTable() {
        TabledModel tbl = (TabledModel) result.payload.get("table_fs");
        TableView<HashMap<String, String>> tbv = new TableView<>(FXCollections.observableArrayList(tbl.getModel()));
        TableColumn<HashMap<String, String>, String> ldc = new TableColumn<>("Load Case");
        tbv.getColumns().add(ldc);
        ldc.setCellValueFactory(e -> {
            String t = e.getValue().get("Load Case");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nno = new TableColumn<>("Member");
        tbv.getColumns().add(nno);
        nno.setCellValueFactory(e -> {
            String t = e.getValue().get("Member");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nfx = new TableColumn<>("FORCES(kN)");
        tbv.getColumns().add(nfx);
        nfx.setCellValueFactory(e -> {
            String t = e.getValue().get("FORCES(kN)");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nfy = new TableColumn<>("Member");
        tbv.getColumns().add(nfy);
        nfy.setCellValueFactory(e -> {
            String t = Double.valueOf(e.getValue().get("STRESS(N/mm^2)")) < 0 ? "TIES":"STRUT";
            return new SimpleStringProperty(t);
        });
        return tbv;
    }

    private void buildBottom() {
        root.setBottom(new HBox(notif));
    }

    private final DataBox result;
    private final Label purlinNotif = new Label("Section Size Adequate"),
            structNotif = new Label(""),
            tilesNotif = new Label("Section Size Adequate");
    private final ComboBox<String> purlinSizes = new ComboBox<>(),
            structSizes = new ComboBox<>(),
            tilesSizes = new ComboBox<>();
    public final BorderPane root = new BorderPane();
    public final SplitPane midLayout = new SplitPane();
    private final TabPane tpane = new TabPane();
    private final Accordion rytPanes = new Accordion();
    public Accordion tableRoot = new Accordion();
    public Label notif = new Label("");
    public Scene scene;

    private class ResultEngine {

        public ResultEngine() {
            updateUI();
            uiEvents();
        }

        private void updateUI() {
            purlinNotif.setText(result.stringPayload.get("purlin_result"));
            tilesNotif.setText(result.stringPayload.get("ties_result"));
            structNotif.setText(result.stringPayload.get("struct_result"));
            if (result.arrayPayload.get("purlin_sizes") != null) {
                purlinSizes.setItems(FXCollections.observableArrayList(result.arrayPayload.get("purlin_sizes")));
                purlinSizes.getSelectionModel().selectFirst();
            }
            if (result.arrayPayload.get("ties_sizes") != null) {
                tilesSizes.setItems(FXCollections.observableArrayList(result.arrayPayload.get("ties_sizes")));
                tilesSizes.getSelectionModel().selectFirst();
            }
            if (result.arrayPayload.get("struct_sizes") != null) {
                structSizes.setItems(FXCollections.observableArrayList(result.arrayPayload.get("struct_sizes")));
                structSizes.getSelectionModel().selectFirst();
            }

        }

        private void uiEvents() {
            purlinNotif.setWrapText(true);
            tilesNotif.setWrapText(true);
            structNotif.setWrapText(true);
        }
    }
}

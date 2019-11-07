/*
 A Pretty cool Source File on a God Blessed day!
 */
package simonds1_client.modules.CCD;

import simonds1_client.modules.CSLB.*;
import java.util.HashMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import simonds1.core.SimonUtil;
import simonds1.core.transport.DataBox;
import simonds1.core.transport.TabledModel;
import simonds1_client.modules.ModuleMainUI;
import simonds1_client.ui.NumberField;
import simonds1_client.ui.pad.WorkPane;
import simonds1_client.ui.shapes.CSlab;

/**
 *
 * @author Ephrahim Adedamola <olagokedammy@gmail.com>
 */
public final class RCDColumnUI extends WorkPane {

    public RCDColumnUI(ModuleMainUI ui, String windowID) {
        super(windowID, "Concrete Flat Slab Design");
        this.ui = ui;
        setup();
    }

    public void setup() {
        buildLeft();
        buildRight();
        mkEvents();
    }

    public void buildLeft() {
        super.buildLeftPane();
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(load.getPromptText()), null), load);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(length.getPromptText()), null), length);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(breadth.getPromptText()), null), breadth);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(momentx.getPromptText()), null), momentx);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(momenty.getPromptText()), null), momenty);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(height.getPromptText()), null), height);
        leftPane.getChildren().addAll(isBraced);
        
        GridPane grid = new GridPane();
        grid.addRow(0, SimonUtil.decorateLabel(new Label("Top Condition:  "), null), topCondition);
        grid.addRow(1, SimonUtil.decorateLabel(new Label("Bottom Condition:  "), null), bottomCondition);
        grid.addRow(2, SimonUtil.decorateLabel(new Label("Steel Grade:  "), null), steelGrade);
        leftPane.getChildren().addAll(grid);
        
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(Fcu.getPromptText()), null), Fcu);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(cover.getPromptText()), null), cover);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(bar.getPromptText()), null), bar);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(momentXBottom.getPromptText()), null), momentXBottom);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(momentYBottom.getPromptText()), null), momentYBottom);
        leftPane.getChildren().addAll(new Label());
        leftPane.getChildren().add(designBut);
        steelGrade.getSelectionModel().selectFirst();
        topCondition.getSelectionModel().selectFirst();
        bottomCondition.getSelectionModel().selectFirst();
    }

    public void buildRight() {
        super.buildRightPane();
        notif.setStyle("-fx-padding: 12px 5px 7px 7px;");
        rightPane.getChildren().add(notif);
    }
    public void buildResultTable(TabledModel tbModel){
        rightPane.getChildren().clear();
        if (tbModel == null) {
            rightPane.getChildren().add(new TableView<>());
            return;
        }
        //System.out.println(tbl.getModel());
        TableView<HashMap<String, String>> tbv = new TableView<>(FXCollections.observableArrayList(tbModel.getModel()));
        TableColumn<HashMap<String, String>, String> res = new TableColumn<>("Result");
        tbv.getColumns().add(res);
        res.setCellValueFactory(e -> {
            String t = e.getValue().get("Result");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nfx = new TableColumn<>("Value");
        tbv.getColumns().add(nfx);
        nfx.setCellValueFactory(e -> {
            String t = e.getValue().get("Value");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> cc = new TableColumn<>("Comment");
        tbv.getColumns().add(cc);
        cc.setCellValueFactory(e -> {
            String t = e.getValue().get("Comment");
            return new SimpleStringProperty(t);
        });
        rightPane.getChildren().add(tbv);
    }
    public void mkEvents(){
        designBut.setOnAction(ev -> engine.design());
    }

    private final ModuleMainUI ui;
    public NumberField load = new NumberField("0", "Axial Load (KN)"),
            momentx = new NumberField("0", "X-Axis Moment (KNm)"),
            momenty = new NumberField("0", "Y-Axis Moment (KNm)"),
            length = new NumberField("0", "X-Axis Length (mm)"),
            breadth = new NumberField("0", "Y-Axis Breadth (mm)"),
            Fcu = new NumberField("0", "Concrete Grade (N/mm^2)"),
            bar = new NumberField("0", "ReBar Diameter (mm)"),
            cover = new NumberField("0", "Reinforcement Cover (mm)"),
            height = new NumberField("0", "Clear Height (mm)"),
            momentXBottom = new NumberField("0", "X-Axis Bottom Moment (In case Column is Slender) (KNm)"),
            momentYBottom = new NumberField("0", "Y-Axis Bottom Moment (In case Column is Slender) (KNm)");
    public Label notif = new Label();
    public Button designBut = new Button("Design Column");
    public ComboBox<String> steelGrade = new ComboBox<>(FXCollections.observableArrayList("S275", "S355", "S460"));
    public ComboBox<Integer> topCondition = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4));
    public ComboBox<Integer> bottomCondition = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3));
    public CheckBox isBraced = new CheckBox("Is Column Braced?   ");
    private final Engine engine = new Engine();
    private final CSlab node = new CSlab(false);

    class Engine {

        public Engine() {
            //
        }

        public void buildModel() {
            DataBox data = new RCDColumn(node).buildResults();
            buildResultTable((TabledModel) data.payload.get("slab_table"));
            notif.setText(data.stringPayload.get("result_notif"));
        }
        public void design() {
            node.numberProps.put("load", Double.valueOf(load.getText()));
            node.numberProps.put("length", Double.valueOf(length.getText()));
            node.numberProps.put("breadth", Double.valueOf(breadth.getText()));
            node.numberProps.put("momentx", Double.valueOf(momentx.getText()));
            node.numberProps.put("momenty", Double.valueOf(momenty.getText()));
            node.stringProps.put("isbraced", isBraced.selectedProperty().get() ? "true" : "false");
            node.numberProps.put("height", Double.valueOf(height.getText()));
            node.numberProps.put("tc", topCondition.getSelectionModel().getSelectedItem());
            node.numberProps.put("bc", bottomCondition.getSelectionModel().getSelectedItem());
            node.stringProps.put("fy", steelGrade.getSelectionModel().getSelectedItem());
            node.numberProps.put("fcu", Double.valueOf(Fcu.getText()));
            node.numberProps.put("cover", Double.valueOf(cover.getText()));
            node.numberProps.put("bar", Double.valueOf(bar.getText()));
            node.numberProps.put("mxb", Double.valueOf(momentXBottom.getText()));
            node.numberProps.put("myb", Double.valueOf(momentYBottom.getText()));
            buildModel();
        }
    }
}

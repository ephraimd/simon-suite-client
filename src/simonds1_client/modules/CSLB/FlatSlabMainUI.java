/*
 A Pretty cool Source File on a God Blessed day!
 */
package simonds1_client.modules.CSLB;

import java.util.HashMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
public final class FlatSlabMainUI extends WorkPane {

    public FlatSlabMainUI(ModuleMainUI ui, String windowID) {
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
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(deadLoad.getPromptText()), null), deadLoad);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(liveLoad.getPromptText()), null), liveLoad);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(bc.getPromptText()), null), bc);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(Fcu.getPromptText()), null), Fcu);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(tita.getPromptText()), null), tita);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(ly.getPromptText()), null), ly);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(lx.getPromptText()), null), lx);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label("Steel Grade"), null), steelGrade);
        leftPane.getChildren().addAll(new Label());
        leftPane.getChildren().add(designBut);
        steelGrade.getSelectionModel().selectFirst();
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
    public NumberField deadLoad = new NumberField("0", "Dead Load (KNm)"),
            liveLoad = new NumberField("0", "Live Load (KNm)"),
            bc = new NumberField("0", "Column Breadth (mm)"),
            Fcu = new NumberField("0", "Concrete Grade (N/mm^2)"),
            tita = new NumberField("0", "Rebar Diameter (mm)"),
            ly = new NumberField("0", "Longer Span (m)"),
            lx = new NumberField("0", "Shorter Span (m)");
    public Label notif = new Label();
    public Button designBut = new Button("Design Slab");
    public ComboBox<String> steelGrade = new ComboBox<>(FXCollections.observableArrayList("S275", "S355", "S460"));
    private final Engine engine = new Engine();
    private final CSlab node = new CSlab(false);

    class Engine {

        public Engine() {
            //
        }

        public void buildModel() {
            DataBox data = new FlatSlab(node).buildResults();
            buildResultTable((TabledModel) data.payload.get("slab_table"));
            notif.setText(data.stringPayload.get("result_notif"));
        }
        public void design() {
            node.numberProps.put("gk", Double.valueOf(deadLoad.getText()));
            node.numberProps.put("qk", Double.valueOf(liveLoad.getText()));
            node.numberProps.put("bc", Double.valueOf(bc.getText()));
            node.numberProps.put("fcu", Double.valueOf(Fcu.getText()));
            node.numberProps.put("tita", Double.valueOf(tita.getText()));
            node.numberProps.put("ly", Double.valueOf(ly.getText()));
            node.numberProps.put("lx", Double.valueOf(lx.getText()));
            node.stringProps.put("fy", steelGrade.getSelectionModel().getSelectedItem());
            buildModel();
        }
    }
}

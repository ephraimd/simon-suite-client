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
public final class SolidSlabMainUI extends WorkPane {

    public SolidSlabMainUI(ModuleMainUI ui, String windowID) {
        super(windowID, "Concrete Solid Slab Design");
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
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(longSpan.getPromptText()), null), longSpan);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(shortSpan.getPromptText()), null), shortSpan);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(bar.getPromptText()), null), bar);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(gk.getPromptText()), null), gk);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(qk.getPromptText()), null), qk);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(fcu.getPromptText()), null), fcu);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(cover.getPromptText()), null), cover);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label("Steel Grade"), null), steelGrade);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label("Slab Type"), null), slabType);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label("Boundary"), null), boundary);
        leftPane.getChildren().addAll(new Label());
        leftPane.getChildren().add(designBut);
        steelGrade.getSelectionModel().selectFirst();
        boundary.getSelectionModel().selectFirst();
        slabType.getSelectionModel().selectFirst();
    }

    public void buildRight() {
        super.buildRightPane();
        notif.setStyle("-fx-padding: 12px 5px 7px 7px;");
        rightPane.getChildren().add(notif);
    }

    public void buildResultTable(TabledModel tbModel) {
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

    public void mkEvents() {
        designBut.setOnAction(ev -> engine.design());
    }

    private final ModuleMainUI ui;
    public NumberField longSpan = new NumberField("0", "Long Span (mm)"), //cahnge the labels here
            shortSpan = new NumberField("0", "Short Span (mm)"),
            bar = new NumberField("0", "Preferred Bar size (mm)"), //
            gk = new NumberField("0", "Dead Load (KN/m^2)"),
            qk = new NumberField("0", "Live Load (KN/m^2)"),
            cover = new NumberField("0", "Reinforcement Cover (N/m^2)"),
            fcu = new NumberField("0", "Concrete Grade (N/mm^2)"); //characteristic strength of concrete
    public Label notif = new Label();
    public Button designBut = new Button("Design Slab");
    public ComboBox<String> steelGrade = new ComboBox<>(FXCollections.observableArrayList("S275", "S355", "S460")),
            slabType = new ComboBox<>(FXCollections.observableArrayList("Continous", "Cantilever", "Simply Supported")),
            boundary = new ComboBox<>(FXCollections.observableArrayList("Interior", "One Short Discontinous", "One Long Discontinous", "Two Adjacent Discontinous",
                    "Two Short Discountinous", "Two Long Discountinous", "One Long Continous", "One Short Continous", "Four Edges Discontinuos"));
    private final Engine engine = new Engine();
    private final CSlab node = new CSlab(false);

    class Engine {

        public Engine() {
            //
        }

        public void buildModel() {
            DataBox data = new SolidSlab(node).buildResults();
            buildResultTable((TabledModel) data.payload.get("slab_table"));
            notif.setText(data.stringPayload.get("result_notif"));
        }

        public void design() {
            node.numberProps.put("longspan", Double.valueOf(longSpan.getText()));
            node.numberProps.put("shortspan", Double.valueOf(shortSpan.getText()));
            node.numberProps.put("bar", Double.valueOf(bar.getText()));
            node.numberProps.put("fcu", Double.valueOf(fcu.getText()));
            node.numberProps.put("gk", Double.valueOf(gk.getText()));
            node.numberProps.put("qk", Double.valueOf(qk.getText()));
            node.numberProps.put("cover", Double.valueOf(cover.getText()));
            node.stringProps.put("fy", steelGrade.getSelectionModel().getSelectedItem());
            node.stringProps.put("slabtype", slabType.getSelectionModel().getSelectedItem());
            node.stringProps.put("boundary", boundary.getSelectionModel().getSelectedItem());
            buildModel();
        }
    }
}

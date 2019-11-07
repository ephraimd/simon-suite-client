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
public final class RibbedSlabMainUI extends WorkPane {

    public RibbedSlabMainUI(ModuleMainUI ui, String windowID) {
        super(windowID, "Concrete Ribbed Slab Design");
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
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(ly.getPromptText()), null), ly);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(lx.getPromptText()), null), lx);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(th.getPromptText()), null), th);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(rw.getPromptText()), null), rw);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(tita.getPromptText()), null), tita);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(rd.getPromptText()), null), rd);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(ll.getPromptText()), null), ll);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(bw.getPromptText()), null), bw);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(ladd.getPromptText()), null), ladd);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(c.getPromptText()), null), c);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label(fcu.getPromptText()), null), fcu);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label("Steel Grade"), null), steelGrade);
        leftPane.getChildren().addAll(SimonUtil.decorateLabel(new Label("Beam Type"), null), beamType);
        leftPane.getChildren().addAll(new Label());
        leftPane.getChildren().add(designBut);
        steelGrade.getSelectionModel().selectFirst();
        beamType.getSelectionModel().selectFirst();
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
    public NumberField ly = new NumberField("0", "Long Span (m)"), //cahnge the labels here
            lx = new NumberField("0", "Short Span (m)"),
            th = new NumberField("0", "Topping Thickness (mm)"), //
            rw = new NumberField("0", "Rib Width (mm)"),
            rd = new NumberField("0", "Rib Depth (mm)"),
            bw = new NumberField("0", "Block Width (mm)"),
            ll = new NumberField("0", "Live Load (KN/m^2)"),
            ladd = new NumberField("0", "Additional Dead Load (KN/m^2)"),
            tita = new NumberField("0", "Bar Diameter (mm)"),
            c = new NumberField("0", "Cover (mm)"),
            fcu = new NumberField("0", "Concrete Grade (N/mm^2)"); //characteristic strength of concrete
    public Label notif = new Label();
    public Button designBut = new Button("Design Slab");
    public ComboBox<String> steelGrade = new ComboBox<>(FXCollections.observableArrayList("S275", "S355", "S460")),
            beamType = new ComboBox<>(FXCollections.observableArrayList("Continous", "Cantilever", "Simply Supported"));
    private final Engine engine = new Engine();
    private final CSlab node = new CSlab(false);

    class Engine {

        public Engine() {
            //
        }

        public void buildModel() {
            DataBox data = new RibbedSlab(node).buildResults();
            buildResultTable((TabledModel) data.payload.get("slab_table"));
            notif.setText(data.stringPayload.get("result_notif"));
        }

        public void design() {
            node.numberProps.put("th", Double.valueOf(th.getText()));
            node.numberProps.put("rw", Double.valueOf(rw.getText()));
            node.numberProps.put("rd", Double.valueOf(rd.getText()));
            node.numberProps.put("ll", Double.valueOf(ll.getText()));
            node.numberProps.put("tita", Double.valueOf(tita.getText()));
            node.numberProps.put("ly", Double.valueOf(ly.getText()));
            node.numberProps.put("lx", Double.valueOf(lx.getText()));
            node.numberProps.put("bw", Double.valueOf(bw.getText()));
            node.numberProps.put("ladd", Double.valueOf(ladd.getText()));
            node.numberProps.put("fcu", Double.valueOf(fcu.getText()));
            node.numberProps.put("c", Double.valueOf(c.getText()));
            node.stringProps.put("fy", steelGrade.getSelectionModel().getSelectedItem());
            node.stringProps.put("beam-type", beamType.getSelectionModel().getSelectedItem());
            buildModel();
        }
    }
}

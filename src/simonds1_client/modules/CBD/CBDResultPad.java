package simonds1_client.modules.CBD;

import java.util.HashMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import simonds1.core.SimonUtil;
import simonds1.core.transport.DataBox;
import simonds1.core.transport.TabledModel;

/**
 * Models the truss results
 *
 * @author ADEDAMOLA
 */
public final class CBDResultPad extends Stage {

    public CBDResultPad(DataBox result) {
        this.result = result;
        this.setTitle(result.config.get("payload_title") + " Result - Simon Design Suite");
        this.scene = new Scene(this.root, 500, 400);
        //this.scene.getStylesheets().add(SimonUtil.resPath + "bootstrap2.css");
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
        tpane.getTabs().add(new Tab("Design", buildLeftPane()));
    }

    private Node buildLeftPane() {
        //tableRoot.getPanes().add(new TitledPane("Nodal Forces", mkNFTable()));
        //tableRoot.getPanes().add(new TitledPane("Nodal Displacement", mkNDTable()));
        //tableRoot.getPanes().add(new TitledPane("Forces and Stresses", mkFSTable()));
        {
            VBox tmpl = new VBox();
            columnNotif.setStyle("-fx-padding: 12px 5px 7px 7px;");
            tmpl.getChildren().add(columnNotif);
            tmpl.getChildren().add(new Separator(Orientation.HORIZONTAL));
            tmpl.getChildren().add(mkCDTable());
            tableRoot.getChildren().add(tmpl);
        }
        return tableRoot;
    }

    private Node mkCDTable() {
        if (result.payload.get("bd_table") == null) {
            return new TableView<>();
        }
        TabledModel tbl = (TabledModel) result.payload.get("bd_table");
        //System.out.println(tbl.getModel());
        TableView<HashMap<String, String>> tbv = new TableView<>(FXCollections.observableArrayList(tbl.getModel()));
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
        return tbv;
    }

    private void buildBottom() {
        root.setBottom(new HBox(notif));
    }

    private final DataBox result;
    private final Label columnNotif = new Label("");
    public final BorderPane root = new BorderPane();
    public final SplitPane midLayout = new SplitPane();
    private final TabPane tpane = new TabPane();
    public VBox tableRoot = new VBox();
    public Label notif = new Label("");
    public Scene scene;

    private class ResultEngine {

        public ResultEngine() {
            updateUI();
            uiEvents();
        }

        private void updateUI() {
            columnNotif.setText(result.stringPayload.get("result_notif"));
        }

        private void uiEvents() {
            columnNotif.setWrapText(true);
        }
    }
}

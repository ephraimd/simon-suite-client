package simonds1_client.modules.PF;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import simonds1.core.transport.DataBox;
import simonds1.core.transport.SDSModel;
import simonds1.core.transport.TabledModel;
import simonds1_client.ui.pad.Canvas2D;
import simonds1_client.ui.shapes.CElement;
import simonds1_client.ui.shapes.CNode;

/**
 * Models the frame results
 *
 * @author ADEDAMOLA
 */
public final class PFResultPad extends Stage {
    public String windowID;
    private static final double CONVERSION = 100d;

    public PFResultPad(DataBox result, String windowID) {
        this.result = result;
        this.windowID = windowID;
        this.setTitle(result.config.get("payload_title") + " Result - Simon Design Suite");
        this.scene = new Scene(this.root, 1100, 500);
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
        System.out.println("We got this far 4");
    }

    private void buildTabs() {
        SplitPane tmp_layout = new SplitPane(buildLeftPane(), buildRightPane());
        System.out.println("We got this far 1");
        tmp_layout.setDividerPositions(0.3f, 0.7f);
        tpane.getTabs().add(new Tab("Analysis & Design", tmp_layout));
        System.out.println("We got this far 2");
        //tpane.getTabs().add(new Tab("Report Generation", new Label("Work in Progress!!")));
    }
    private Node buildLeftPane() {
        tableRoot.getPanes().add(new TitledPane("Element Forces", mkEFTable()));
        tableRoot.getPanes().add(new TitledPane("Nodal Displacement", mkNDTable()));
        tableRoot.getPanes().add(new TitledPane("Nodal Forces", mkNFTable()));
        SplitPane.setResizableWithParent(this.tableRoot, false);
        return tableRoot;
    }
    
    private Node buildRightPane() {
        SplitPane graphRoot = new SplitPane();
        graphRoot.setDividerPositions(0.6f, 0.4f);
        graphRoot.setOrientation(Orientation.VERTICAL);
        
        Tab bmTab = new Tab("Bending Moment", buildBMCanv());
        bmTab.setClosable(false);
        System.out.println("BM Done");
        Tab sfTab = new Tab("Shear Force", buildDsiplCanv());
        sfTab.setClosable(false);
        System.out.println("SF Done");
        Tab dfTab = new Tab("Displacement", buildDsiplCanv());
        dfTab.setClosable(false);
        System.out.println("Disp Done");
        //TabPane tbPane = new TabPane(dfTab, sfTab, bmTab);
        
        
        graphRoot.getItems().add(new TabPane(dfTab, sfTab, bmTab));
        //graphRoot.getItems().add(new TitledPane("Original Model", buildOriginalModel()));
        
        return graphRoot;
    }

    private Node buildOriginalModel(){
        SDSModel model = (SDSModel) result.payload.get("frame_model");
        Canvas2D canv = new Canvas2D(windowID);
        
        canv.isDisabled = true;
        //draw the gridlines
        canv.applyGridlines();
        canv.importModel(model, true);
        
        return canv;
    }
    private Node buildDsiplCanv() {
        //ArrayList<Number> dispArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_dsp")).table.get("Displacement");//should always be divisible by two
        //ArrayList<Number> nfxArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_fs")).table.get("Node Force X(KN)");
        //ArrayList<Number> nfyArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_fs")).table.get("Node Force Y(KN)");
        ArrayList<Number> ndxArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_nd")).table.get("Disp (X)(cm)");
        ArrayList<Number> ndyArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_nd")).table.get("Disp (Y)(cm)");
        //ArrayList<Number> nfArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_nf")).table.get("Nodal Force(KN)");
        SDSModel model = (SDSModel) result.payload.get("frame_model");

        //canv.importModel(model, false);

        Canvas2D canv2 = new Canvas2D(windowID);
        canv2.importModel(model, false);

        int dispArrcount = 0, ndct = 0;
        double ndispx, ndispy;
        
        for (int ij = 0; ij < canv2.shapesList.size(); ij++) {
            //System.out.println("ij1= "+ij);
            if (canv2.shapesList.get(ij).getTypeStr().equals("node")) {
                CNode ntmp = (CNode) canv2.shapesList.get(ij);
                ndispx = ndxArr.get(ndct).doubleValue();
                ndispy = ndyArr.get(ndct).doubleValue();
                //System.out.printf("DispX = %s, DispY = %s -..\n", ndispx, ndispy);
                ntmp.setCenterX(ntmp.getCenterX() + canv2.getScaleX(ndispx, false));
                ntmp.setCenterY(ntmp.getCenterY() - canv2.getScaleY(ndispy, false)); //IMPORTANT: '-' inverts y axis
                ntmp.nx.set(ntmp.nx.doubleValue() + ndispx);
                ntmp.ny.set(ntmp.ny.doubleValue() + ndispy);
                ntmp.displacementx = ndispx;
                ntmp.displacementy = ndispy;
                //System.out.println("ij2a= "+ij);
                ntmp.forcex = Double.NaN;
                ntmp.forcey = Double.NaN;
                ntmp.isResultMode = true;
                canv2.shapesList.set(ij, ntmp);
                ndct++;
            }
            else if (canv2.shapesList.get(ij).getTypeStr().equals("line")){
                CElement ntmp = (CElement) canv2.shapesList.get(ij);
                ntmp.isResultMode = true;
                //System.out.println("ij3= "+ij);
            }
            //System.out.println("ij4= "+ij);
        }
        //reposition the canvas in the middle and draw the BM and SF diagram in separate tabs
        String tmp = Canvas2D.strokeColor;
        Canvas2D.strokeColor = "red";
        canv2.modelScale = canv2.modelScale / 2;
        //System.out.println("ij5= ");
        Canvas2D canv = new Canvas2D(windowID);
        //draw the gridlines
        canv.applyGridlines();
        //System.out.println("ij6= ");
        canv.isDisabled = true;
        canv.importModel(canv2.exportModel(), true);//added
        Canvas2D.strokeColor = tmp;
        //System.out.println("Here1");

        return canv;
        //tpane.getTabs().add(new Tab("Displacement plot", canv));
        //tpane.getSelectionModel().selectLast();
    }
    private Node buildSFCanv(){
        //ArrayList<Number> nfArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_nf")).table.get("Nodal Force(KN)");
        ArrayList<Number> fsArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_fs")).table.get("Element Force Y(KN)");
        SDSModel model = (SDSModel) result.payload.get("frame_model");

        Canvas2D canv2 = new Canvas2D(windowID);
        canv2.importModel(model, false);

        int ndct = 0;
        
        for (int ij = 0; ij < canv2.shapesList.size(); ij++) {
            if (canv2.shapesList.get(ij).getTypeStr().equals("node")) {
                CNode ntmp = (CNode) canv2.shapesList.get(ij);
                CNode dtmp = new CNode(canv2, ntmp.getCenterX(), ntmp.getCenterY() - canv2.getScaleY(fsArr.get(ndct).doubleValue(), false), true);
                ///System.out.printf("DispZ = %s\n", ndispx, ndispy);
                ///ntmp.setCenterX(ntmp.getCenterX() + ndispx); //not needed
                ////canv2.getScaleX(nfArr.get(ndct).doubleValue(), false)
                //ntmp.setCenterY(ntmp.getCenterY() - canv2.getScaleY(fsArr.get(ndct).doubleValue(), false)); //IMPORTANT: '-' inverts y axis
                dtmp.displacementx = Double.NaN;
                dtmp.displacementy = Double.NaN;
                dtmp.forcex = Double.NaN;
                dtmp.forcey = Double.NaN;
                ntmp.isResultMode = true;
                canv2.shapesList.add(dtmp);
                canv2.shapesList.remove(ntmp); //only the new lines should show
                ndct++;
            }
            else if (canv2.shapesList.get(ij).getTypeStr().equals("line")){
                CElement ntmp = (CElement) canv2.shapesList.get(ij);
                ntmp.isResultMode = true;
                canv2.shapesList.remove(ntmp); //only the new lines should show
            }
        }
        //reposition the canvas in the middle and draw the BM and SF diagram in separate tabs
        String tmp = Canvas2D.strokeColor;
        canv2.modelScale = canv2.modelScale / 2;
        Canvas2D.strokeColor = "blue";
        //will not connect the dots and display until its in the new canvas
        Canvas2D canv = new Canvas2D(windowID);
        //draw the gridlines
        canv.applyGridlines();
        canv.isDisabled = true;
        canv.importModel(canv2.exportModel(), true);//added
        Canvas2D.strokeColor = tmp;

        return canv;
    }
    private Node buildBMCanv(){
        ArrayList<Number> nfzArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_fs")).table.get("Moment Z(KN)");
        ArrayList<Number> nnArr = (ArrayList<Number>) ((TabledModel) result.payload.get("table_fs")).table.get("Node No");
        SDSModel model = (SDSModel) result.payload.get("frame_model");

        Canvas2D canv2 = new Canvas2D(windowID);
        canv2.importModel(model, false);

        int ndct = 0, lastNode = -1;
        
        for (int ij = 0; ij < canv2.shapesList.size(); ij++) {
            if (canv2.shapesList.get(ij).getTypeStr().equals("node")) {
                if(nnArr.get(ndct).intValue() == lastNode){
                    System.out.println("Continue on Node: "+lastNode);
                    continue;
                }else{
                    lastNode = nnArr.get(ndct).intValue();
                    System.out.println("Work on Node: "+lastNode);
                }
                CNode ntmp = (CNode) canv2.shapesList.get(ij);
                CNode dtmp = new CNode(canv2, ntmp.getCenterX(), ntmp.getCenterY() - canv2.getScaleY(nfzArr.get(ndct).doubleValue(), false), true);
                //System.out.printf("DispZ = %s\n", ndispx, ndispy);
                //ntmp.setCenterX(ntmp.getCenterX() + ndispx); //not needed
                //canv2.getScaleX(nfArr.get(ndct).doubleValue(), false)
                //ntmp.setCenterY(ntmp.getCenterY() - canv2.getScaleY(nfzArr.get(ndct).doubleValue(), false)); //IMPORTANT: '-' inverts y axis
                dtmp.displacementx = Double.NaN;
                dtmp.displacementy = Double.NaN;
                dtmp.forcex = Double.NaN;
                dtmp.forcey = Double.NaN;
                ntmp.isResultMode = true;
                canv2.shapesList.add(dtmp);
                //canv2.shapesList.remove(ntmp); //only the new lines should show
                ndct++;
            }
            else if (canv2.shapesList.get(ij).getTypeStr().equals("line")){
                CElement ntmp = (CElement) canv2.shapesList.get(ij);
                ntmp.isResultMode = true;
                //canv2.shapesList.remove(ntmp); //only the new lines should show
            }
        }
        System.out.println("Loop ended on Node: "+lastNode);
        //reposition the canvas in the middle and draw the BM and SF diagram in separate tabs
        String tmp = Canvas2D.strokeColor;
        canv2.modelScale = canv2.modelScale / 2;
        Canvas2D.strokeColor = "blue";
        Canvas2D canv = new Canvas2D(windowID);
        //draw the gridlines
        canv.applyGridlines();
        canv.isDisabled = true;
        //canv.importModel(model, true);//added
        canv.importModel(canv2.exportModel(), true);//added
        Canvas2D.strokeColor = tmp;

        return canv;
        //tpane.getTabs().add(new Tab("Displacement plot", canv));
        //tpane.getSelectionModel().selectLast();
    }

    private Node mkNFTable() {
        TabledModel tbl = (TabledModel) result.payload.get("table_nf");
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
        TableColumn<HashMap<String, String>, String> nfx = new TableColumn<>("Nodal Force(KN)");
        tbv.getColumns().add(nfx);
        nfx.setCellValueFactory(e -> {
            String t = e.getValue().get("Nodal Force(KN)");
            return new SimpleStringProperty(t);
        });
        return tbv;
    }
    
    private Node mkEFTable() {
        TabledModel tbl = (TabledModel) result.payload.get("table_fs");
        TableView<HashMap<String, String>> tbv = new TableView<>(FXCollections.observableArrayList(tbl.getModel()));
        //System.out.printf("%s --", tbl.getModel());
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
        TableColumn<HashMap<String, String>, String> nfx = new TableColumn<>("Element Force X(KN)");
        tbv.getColumns().add(nfx);
        nfx.setCellValueFactory(e -> {
            String t = e.getValue().get("Element Force X(KN)");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nfy = new TableColumn<>("Element Force Y(KN)");
        tbv.getColumns().add(nfy);
        nfy.setCellValueFactory(e -> {
            String t = e.getValue().get("Element Force Y(KN)");
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> ndz = new TableColumn<>("Moment Z(KN)");
        tbv.getColumns().add(ndz);
        ndz.setCellValueFactory(e -> {
            String t = e.getValue().get("Moment Z(KN)");
            return new SimpleStringProperty(t);
        });
        return tbv;
    }

    private Node mkNDTable() {
        TabledModel tbl = (TabledModel) result.payload.get("table_nd");
        TableView<HashMap<String, String>> tbv = new TableView<>(FXCollections.observableArrayList(tbl.getModel()));
        //System.out.printf("%s --", tbl.getModel());
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
        TableColumn<HashMap<String, String>, String> nfx = new TableColumn<>("Disp (X)(cm)");
        tbv.getColumns().add(nfx);
        nfx.setCellValueFactory(e -> {
            String t = e.getValue().get("Disp (X)(cm)");
            t = String.format("%s", Double.parseDouble(t) * CONVERSION);
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> nfy = new TableColumn<>("Disp (Y)(cm)");
        tbv.getColumns().add(nfy);
        nfy.setCellValueFactory(e -> {
            String t = e.getValue().get("Disp (Y)(cm)");
            t = String.format("%s", Double.parseDouble(t) * CONVERSION);
            return new SimpleStringProperty(t);
        });
        TableColumn<HashMap<String, String>, String> ndz = new TableColumn<>("Rotation (cm)");
        tbv.getColumns().add(ndz);
        ndz.setCellValueFactory(e -> {
            String t = e.getValue().get("Rotation (cm)");
            t = String.format("%s", Double.parseDouble(t) * CONVERSION);
            return new SimpleStringProperty(t);
        });
        return tbv;
    }

    private void buildBottom() {
        root.setBottom(new HBox(notif));
        System.out.println("We got this far 3");
    }

    private final DataBox result;
    public final BorderPane root = new BorderPane();
    public final SplitPane midLayout = new SplitPane();
    private final TabPane tpane = new TabPane();
    private final Accordion rytPanes = new Accordion();
    public Accordion tableRoot = new Accordion();
    //public SplitPane graphRoot = new SplitPane();
    public Label notif = new Label("");
    public Scene scene;

    private class ResultEngine {

        public ResultEngine() {
            //updateUI();
            //uiEvents();
        }

        private void updateUI() {
            //
        }

        private void uiEvents() {
            //
        }
    }
}

/* 
    Simon Design Suite version  1.0 (Simon DS1)
    Final year project being supervised by Dr Olawale Simon
 */
package simonds1_client.ui;

import simonds1_client.modules.ModuleEngine;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import simonds1.core.ProjectManager;
import simonds1.core.Resources;
import simonds1_client.ui.dialogs.SimpleAlert;

/**
 *
 * @author Olagoke Adedamola Farouq
 */
public class MainUI extends VBox {

    public MainUI() {
        super();
        setupUI();
    }

    public String getAppTitle() {
        return "Simon Design Suite Client";
    }

    public Scene getUiContext() {
        Scene scene = new Scene(this, 450, 300);
        //scene.getStylesheets().add(SimonUtil.resPath + "main.css");
        //scene.getStylesheets().add(SimonUtil.resPath + "bootstrap2.css");
        return scene;
    }
    private void setupUI(){
        this.addToolbar();
        this.addMainPanel();
        this.addEvents();
    }

    private void addToolbar() {
        this.getChildren().add(TOOLBAR);
    }

    private void addMainPanel() {
        {
            taBut = new Button("Truss Analysis & Design", Resources.getImage("imgProcess"));
            taBut.setPadding(new Insets(5));
            pfBut = new Button("2D Frame Analysis", Resources.getImage("imgProcess"));
            pfBut.setPadding(new Insets(5));
            GridPane tmpl = new GridPane();
            tmpl.setHgap(17);
            tmpl.addRow(0, taBut, pfBut);
            SApane = new TitledPane("Structural Analysis and Design", tmpl);
            SApane.setCollapsible(false);
            SApane.setGraphic(Resources.getImage("imgTmp"));
            SApane.getStyleClass().add("info");
        }
        {
            bdBut = new Button("Beam Design", Resources.getImage("imgProcess"));
            bdBut.setPadding(new Insets(5));
            cdBut = new Button("Column Design", Resources.getImage("imgProcess"));
            cdBut.setPadding(new Insets(5));
            GridPane tmpl = new GridPane();
            tmpl.setHgap(17);
            tmpl.addRow(0, bdBut, cdBut);
            SDpane = new TitledPane("Steel Structure Design", tmpl);
            SDpane.setCollapsible(false);
            SDpane.setGraphic(Resources.getImage("imgTmp"));
            SDpane.getStyleClass().add("info");
        }
        {
            cbdBut = new Button("Beam Design", Resources.getImage("imgProcess"));
            cbdBut.setPadding(new Insets(5));
            ccdBut = new Button("Column Design", Resources.getImage("imgProcess"));
            ccdBut.setPadding(new Insets(5));
            slbBut = new Button("Slab Design", Resources.getImage("imgProcess"));
            slbBut.setPadding(new Insets(5));
            GridPane tmpl = new GridPane();
            tmpl.setHgap(17);
            tmpl.addRow(0, cbdBut, ccdBut, slbBut);///, cdBut);
            RCDpane = new TitledPane("Reinforced Concrete Design", tmpl);
            RCDpane.setCollapsible(false);
            RCDpane.setGraphic(Resources.getImage("imgTmp"));
            RCDpane.getStyleClass().add("info");
        }
        this.root.getChildren().addAll(this.SApane, this.SDpane, this.RCDpane);
        this.getChildren().add(this.root);
        VBox.setVgrow(this.root, Priority.ALWAYS);
        //this.SApane = new TitledPane("Structural Analysis", this.SApaneList);
    }

    private void addEvents() {
        taBut.setOnAction(e -> {
            ModuleEngine.options.put("project_type","TA");
            ModuleEngine.options.put("ui","Canvas2DPane");
            ModuleEngine.options.put("cmdBar","yes"); //needs command bar
            ModuleEngine.options.put("module_new_heading","New Truss Project");
            new ModuleEngine();
        });
        bdBut.setOnAction(e -> {
            ModuleEngine.options.put("project_type","BD");
            ModuleEngine.options.put("ui","Canvas2DPane");
            ModuleEngine.options.put("module_new_heading","New Beam Project");
            new ModuleEngine();
        });
        cbdBut.setOnAction(e -> {
            ModuleEngine.options.put("project_type","CBD");
            ModuleEngine.options.put("ui","WorkPane");
            ModuleEngine.options.put("module_new_heading","New RCD Beam Project");
            new ModuleEngine();
        });
        ccdBut.setOnAction(e -> {
            ModuleEngine.options.put("project_type","CCD");
            ModuleEngine.options.put("ui","WorkPane");
            ModuleEngine.options.put("module_new_heading","New RCD Column Project");
            new ModuleEngine();
        });
        slbBut.setOnAction(e -> {
            ModuleEngine.options.put("project_type","CSLB");
            ModuleEngine.options.put("ui","WorkPane");
            ModuleEngine.options.put("module_new_heading","New Slab Project");
            new ModuleEngine();
        });
        cdBut.setOnAction(e -> {
            ModuleEngine.options.put("project_type","CD");
            ModuleEngine.options.put("ui","Canvas2DPane");
            ModuleEngine.options.put("module_new_heading","New Column Project");
            new ModuleEngine();
        });
        pfBut.setOnAction(e -> {
            ModuleEngine.options.put("project_type","PF");
            ModuleEngine.options.put("ui","Canvas2DPane");
            ModuleEngine.options.put("cmdBar","yes");
            ModuleEngine.options.put("module_new_heading","New Plane Frame Project");
            new ModuleEngine();
        });
    }

    private final Toolbar TOOLBAR = new Toolbar();
    public VBox root = new VBox();
    public TitledPane SApane,
            SDpane, RCDpane;
    public Button taBut, bdBut, cdBut, pfBut, cbdBut, ccdBut, slbBut;
    
    /**
 *
 * @author Olagoke Adedamola Farouq
 */
class Toolbar extends ToolBar {

    public Toolbar() {
        super();
        this.addButtons();
        this.addEvents();
    }

    private void addButtons() {
        //this.serverButton = new ToggleButton("", new ImageView(SimonUtil.resPath +
                //(Network.connected.getValue() ? "image/disconnect.png" : "image/connect.png")));
        //this.serverButton.setTooltip(new Tooltip("Open server connection dialog"));
        openButton = new Button("", Resources.getImage("imgProject"));
        aboutButton = new Button("", Resources.getImage("imgAbout"));

        this.getItems().addAll(openButton, aboutButton);
    }

    private void addEvents() {
        /*//serverButton.selectedProperty().bind(Network.connected);
        Network.connected.addListener((ob, ov, nv) -> {
        boolean val = nv;
        if (val) {
        serverButton.setGraphic(new ImageView(SimonUtil.resPath + "image/disconnect.png"));
        serverButton.setSelected(val);
        } else {
        serverButton.setGraphic(new ImageView(SimonUtil.resPath + "image/connect.png"));
        serverButton.setSelected(val);
        }
        
        });
        serverButton.setOnAction(e -> {
        serverButton.setSelected(Network.connected.getValue());
        new NetworkDialog("Connect to Server", 300, 200);
        });*/
        openButton.setOnAction(e -> {
                ProjectManager.route();
            });
        aboutButton.setOnAction(e -> {
            new SimpleAlert("About Simon Design Suite", "About App", 
                    "A Structural Steel Analysis and Design Software developed by \n"
                            + "Dr Simon Olawale, Mustapha Afeez and Adedamola Olagoke\n\n"
                            + "(c) 2019.", 2).showAndWait();
        });
    }

    //public ToggleButton serverButton;
    public Button openButton, aboutButton;
}

}

/*
area of element is cross section
bounda
truss -> chord
*/
package simonds1_client.modules;

import java.io.File;
import java.util.HashMap;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import simonds1.core.Commander;
import simonds1.core.Logger;
import simonds1.core.ProjectManager;
import simonds1.core.Resources;
import simonds1.core.SignalSlot;
import simonds1.core.SimonUtil;
import simonds1.core.transport.DataBoxPayload;
import simonds1_client.modules.BD.BDMainUI;
import simonds1_client.modules.CBD.CBDMainUI;
import simonds1_client.modules.CCD.RCDColumnUI;
import simonds1_client.modules.CD.CDMainUI;
import simonds1_client.modules.CSLB.FlatSlabMainUI;
import simonds1_client.modules.CSLB.RibbedSlabMainUI;
import simonds1_client.modules.CSLB.SolidSlabMainUI;
import simonds1_client.modules.PF.PFMainUI;
import simonds1_client.modules.TA.TAMainUI;
import simonds1_client.ui.NumberField;
import simonds1_client.ui.SToolBar;
import simonds1_client.ui.dialogs.SimpleAlert;
import simonds1_client.ui.pad.Canvas2D;
import simonds1_client.ui.shapes.CShapes;

/**
 * A generic Main Activity with Modular and Reusable Components.
 *
 * @author ADEDAMOLA
 */
public final class ModuleMainUI extends Stage {

    public String WINDOW_ID; //unique Stage ID

    ModuleMainUI(ModuleEngine engine) {
        this.en = engine;
        this.setTitle(engine.getProp("name") + " - " + engine.getProp("path") + " - Simon Design Suite");
        this.scene = new Scene(this.root, 1000, 900);
        //scene.getStylesheets().add(SimonUtil.resPath + "bootstrap2.css");
        this.setScene(this.scene);
        this.initStyle(StageStyle.UNIFIED);
        if (!engine.isWorkPaneUI) {
            buildToolbar();
            setupCanvasTheme();
            this.setupUI();
            toolbarEvents();
            mainEvents();
            tabEvent();
            treeEvent();
            leftEvents();
            bottomEvent();
        } else {
            setupWorkUI();
        }
        //new UiEvents();
    }

    private void setupWorkUI() {
        this.WINDOW_ID = SimonUtil.getRandomString(6);
        switch (en.prj.props.get("project_type")) {
            case "CSLB":
                SToolBar.WINDOW_ID = this.WINDOW_ID; //sets the toolbar window id first
                this.buildToolbar();//only the workpane uses the signal slot system for now
                FlatSlabMainUI flat = new FlatSlabMainUI(this, WINDOW_ID); //lets use cached instances please, less GC works and more effecient
                RibbedSlabMainUI ribbed = new RibbedSlabMainUI(this, WINDOW_ID);
                SolidSlabMainUI solid = new SolidSlabMainUI(this, WINDOW_ID);
                this.root.setCenter(flat); //flat slab is shown by default. should be configurabe in some config file
                SignalSlot.addSlot(WINDOW_ID, "ActivateSlabPane", obj -> {
                    String slabType = (String) obj;
                    if (slabType.contains("flat")) {
                        this.root.setCenter(flat);
                    } else if (slabType.contains("ribbed")) {
                        this.root.setCenter(ribbed);
                    } else if (slabType.contains("solid")) {
                        this.root.setCenter(solid);
                    }
                });
                break;
            case "CCD":
                this.root.setCenter(new RCDColumnUI(this, WINDOW_ID));
                break;
            default:
                Logger.displayAlert("Unrecognized Project Type. Could not Load Module UI!", true);
        }
    }

    private void setupUI() {
        this.WINDOW_ID = SimonUtil.getRandomString(6);
        setupContent();
        this.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> { //inorder to avoid excess useless slots 
            SignalSlot.removeSlotGroup(WINDOW_ID);
            //this.close();
        }); //we should add not set
        SimonUtil.primaryStage = this;
    }

    public void setupContent() {
        SplitPane.setResizableWithParent(this.leftBar, false);
        contentRoot = new SplitPane(this.leftBar, this.canvasPane);
        contentRoot.setDividerPositions(0.2f, 10f);
        VBox.setVgrow(contentRoot, Priority.ALWAYS);

        canvasPane.setOrientation(Orientation.VERTICAL);
        canvasPane.setDividerPositions(11f, 0.1f);

        buildLeftBar();
        switch (en.prj.props.get("project_type")) {
            case "TA":
                new TAMainUI(this);
                break;
            case "BD":
                new BDMainUI(this);
                break;
            case "CBD":
                new CBDMainUI(this);
                break;
            case "CD":
                new CDMainUI(this);
                break;
            case "PF":
                new PFMainUI(this);
                break;
            default:
                Logger.displayAlert("Unrecognized Project Type. Could not Load Module UI!", true);
        }
        this.root.setCenter(contentRoot);
        if (ModuleEngine.options.get("cmdBar") != null && ModuleEngine.options.get("cmdBar").equals("yes")) {
            buildCommandsBar();
        }

        buildBottomBar();
    }

    public void buildToolbar() {
        this.toolbar = new SToolBar(en.prj.props);
        this.root.setTop(this.toolbar);
    }

    private void setupCanvasTheme() { //should move this to a conf file
        Canvas2D.THEME_VARS = new HashMap<>();
        HashMap<String, String> dw = new HashMap<>();
        dw.put("root_pane", "white"); //should use putAll here.
        dw.put("selected", "red");
        dw.put("hovered", "blue");
        dw.put("node_obj", "black");
        dw.put("element_obj", "black");
        dw.put("dimension", "red");
        dw.put("grid_lines", "#FFF3F2");
        HashMap<String, String> db = new HashMap<>();
        db.put("root_pane", "black");
        db.put("selected", "#A9CBC5");
        db.put("hovered", "white");
        db.put("node_obj", "#FA9132");
        db.put("element_obj", "red");
        db.put("dimension", "white");
        db.put("grid_lines", "#392F24");
        Canvas2D.THEME_VARS.put("Dry White", dw);
        Canvas2D.THEME_VARS.put("Dry Black", db);
    }

    private void buildBottomBar() {
        this.notif.setAlignment(Pos.CENTER_LEFT);
        this.notif.setTextFill(Paint.valueOf("red"));
        HBox.setHgrow(this.notif, Priority.ALWAYS);

        this.scaler = new Slider(0.5, 2, 0.7);
        this.scaler.setMajorTickUnit(0.2);
        this.scaler.setShowTickMarks(true);
        this.scaler.setMinorTickCount(1);
        this.scaler.setBlockIncrement(0.1);
        this.scaler.setSnapToTicks(true);
        zoomNotif.setText(String.valueOf(scaler.getValue()));

        Button zoomIn = new Button("", Resources.getImage("imgZoomIn"));
        zoomIn.setOnAction(e -> {
            scaler.increment();
            zoomNotif.setText(String.valueOf(scaler.getValue()));
        });
        Button zoomOut = new Button("", Resources.getImage("imgZoomOut"));
        zoomOut.setOnAction(e -> {
            scaler.decrement();
            zoomNotif.setText(String.valueOf(scaler.getValue()));
        });

        this.scale.setText(String.format("%sm Insitu x %scm on Model   ", en.getProp("insitu_scale"), en.getProp("model_scale")));

        HBox tmp_layout = new HBox();
        tmp_layout.setSpacing(10);
        tmp_layout.setPadding(new Insets(1, 1, 1, 1));
        tmp_layout.setAlignment(Pos.CENTER_RIGHT);
        tmp_layout.getChildren().addAll(this.notif,
                new Separator(Orientation.VERTICAL),
                this.zoomNotif,
                zoomOut,
                this.scaler,
                zoomIn,
                new Separator(Orientation.VERTICAL),
                this.scale);

        this.root.setBottom(tmp_layout);
    }

    private void buildCommandsBar() {
        this.commandInput.setPromptText("Enter your Commands");
        commandInput.setStyle("color:red;");
        HBox.setHgrow(commandInput, Priority.ALWAYS);
        HBox.setHgrow(commandAct, Priority.NEVER);

        this.commandOutput.setEditable(false);
        this.commandOutput.setWrapText(false);
        //this.commandOutput.setPrefRowCount(3);
        commandOutput.setStyle("color:red;");
        VBox.setVgrow(commandOutput, Priority.ALWAYS);
        Tab tmpTab = new Tab("Command Output", new VBox(commandOutput, new HBox(commandHelp, commandInput, commandAct)));
        tmpTab.setClosable(false);
        butPane.getTabs().add(tmpTab);
        SplitPane.setResizableWithParent(butPane, true);

        canvasPane.getItems().add(butPane);
    }

    private void buildLeftBar() {
        this.leftBar.setSide(Side.LEFT);

        buildLeftObjectsTab();
        buildLeftProjectTab();
        buildLeftToolsTab();
        //buildLeftServerCommsTab();  //later
    }

    private void buildLeftObjectsTab() {
        this.objectsList = new ListView<>();
        Tab tab = new Tab("Objects", this.objectsList);
        tab.setClosable(false);
        this.leftBar.getTabs().add(tab);
    }

    private void buildLeftProjectTab() {
        this.projectItem = new TreeItem<>(en.getProp("name"));
        this.projectItem.setExpanded(true);
        this.projectItem.setGraphic(Resources.getImage("imgProject"));
        this.projectTree = new TreeView<>(this.projectItem);
        VBox.setVgrow(this.projectTree, Priority.ALWAYS);
        prjOpen.setGraphic(Resources.getImage("imgNewRes"));
        prjDelete.setGraphic(Resources.getImage("imgDeleteFile"));
        Tab tab = new Tab("Project", new VBox(new HBox(prjOpen, prjDelete), this.projectTree));
        tab.setClosable(false);
        this.leftBar.getTabs().add(tab);
    }

    private void buildLeftToolsTab() {

        Tab tab = new Tab("Tools", new ScrollPane(new VBox(buildElementProfileSection())));
        tab.setClosable(false);
        this.leftBar.getTabs().add(tab);
    }

    private Node buildElementProfileSection() { //make this a tools class
        VBox tmpLayout = new VBox();
        this.profilesList = new ComboBox(ModuleEngine.ELEM_PROFILES);
        profileName.setPromptText("Enter Profile Name");
        profileName.setDisable(true);
        profileYm.setPromptText("Enter Young Modulus");
        profileYm.setDisable(true);
        profileMmi.setPromptText("Enter Moment of Inertia");
        profileMmi.setDisable(true);
        profileCrsArea.setPromptText("Enter Cross Section Area");
        profileCrsArea.setDisable(true);
        editProfile.setDisable(true);

        tmpLayout.getChildren().addAll(new HBox(profilesList, newProfile, editProfile),
                new VBox(SimonUtil.decorateLabel(null, "Name"), profileName),
                new VBox(SimonUtil.decorateLabel(null, "Young Modulus(KN/m^2)"), profileYm),
                new VBox(SimonUtil.decorateLabel(null, "Moment of Inertia(mm^4)"), profileMmi),
                new VBox(SimonUtil.decorateLabel(null, "Cross Sectional Area(m^2)"), profileCrsArea),
                new HBox(profilePaneNotif, saveProfile));

        TitledPane tmpPane = new TitledPane("Elements Profile", tmpLayout);
        SplitPane.setResizableWithParent(tmpPane, true);
        return tmpPane;
    }

    private void buildLeftServerCommsTab() {
        Tab tab = new Tab("Online", new VBox());
        tab.setClosable(false);
        this.leftBar.getTabs().add(tab);
    }

    ////////////////////////////////////////////////////////////////////////
    public String newTab(String title) {
        String tabId = en.canvasManager.addTab(title);
        ModuleEngine.canvasObjects.put(tabId, en.canvasManager.getCanvasAt(null).shapesList);
        objectsList.setItems(ModuleEngine.canvasObjects.get(tabId));
        return tabId;
    }

    public String newTabFromModel(DataBoxPayload data) {
        String tabId = en.canvasManager.addTabFromData(data);
        ModuleEngine.canvasObjects.put(tabId, en.canvasManager.getCanvasAt(null).shapesList);
        objectsList.setItems(ModuleEngine.canvasObjects.get(tabId));
        return tabId;
    }

    private void toolbarEvents() {
        toolbar.fileNew.setOnAction(event -> newTab(null));
        toolbar.fileSave.setOnAction(event -> {
            Canvas2D model = en.canvasManager.getCanvasAt(null);
            //using a single model instance wont reload the current canvas every time
            System.out.println("Now then ->" + en.getProp("path") + model.canvasTitle.getValueSafe());
            en.prj.saveDataFile(en.getProp("path") + model.canvasTitle.getValueSafe(),
                    model.exportModel());
            ProjectManager.refreshProjectTree(en.prj.projectPath, projectItem);
        });
        toolbar.fileSaveAs.setOnAction(event -> {
            Canvas2D model = en.canvasManager.getCanvasAt(null);
            String resTitle = en.prj.saveAs(model.exportModel());
            if (resTitle != null) {
                model.canvasTitle.setValue(resTitle);
                ProjectManager.refreshProjectTree(en.prj.projectPath, projectItem);
            }
            //when tab title changes, the hash value DOESNT change; halleluyah!
        });
        toolbar.fileOpen.setOnAction(event -> {
            DataBoxPayload tmp = en.prj.openFile(null);
            if (tmp == null) {
                SignalSlot.emitSignal(WINDOW_ID, "NotifyUI", "No File Opened!");
                return;
            }
            newTabFromModel(tmp);
        });
    }

    private void mainEvents() {
        objectsList.setCellFactory((objectListView) -> new ObjectListViewCell());
        en.canvasManager.tabPane.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
            if (null == nv) {
                return;
            }
            objectsList.setItems(ModuleEngine.canvasObjects.get(String.valueOf(nv.hashCode())));
        });
        newTab(null);
        objectsList.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
            SignalSlot.emitSignal(WINDOW_ID, "ShapeClickedmain", nv);
        }); //causing infinite calling of next slot when //.select(tmp); triggers this slot again
        SignalSlot.addSlot(WINDOW_ID, "ShapeDestroyed", obj -> {
            String tabId = en.canvasManager.getTabIdAt(null);
            ModuleEngine.canvasObjects.put(tabId, en.canvasManager.getCanvasAt(null).shapesList);
            objectsList.setItems(ModuleEngine.canvasObjects.get(tabId));
        });

    }

    private void leftEvents() {
        newProfile.setOnAction((e) -> {
            profileName.setDisable(false);
            profileName.setText("");
            profileYm.setDisable(false);
            profileYm.setText("0.0");
            profileMmi.setDisable(false);
            profileMmi.setText("0.0");
            profileCrsArea.setDisable(false);
            profileCrsArea.setText("0.0");
        });
        saveProfile.disableProperty().bind(profileCrsArea.disabledProperty());
        saveProfile.setOnAction((e) -> { //trim input lengths
            String name = profileName.getText().toLowerCase();
            double ym = Double.valueOf(profileYm.getText());
            double mmi = Double.valueOf(profileMmi.getText()); //can never be empty
            double cra = Double.valueOf(profileCrsArea.getText());//!!!!!!!!!!!convert getText() of Numberfield to return number
            if (name.isEmpty()) {
                return;
            }
            if (profileName.isDisabled())//if we are in edit mode...yes Lord
            {
                ModuleEngine.ELEM_PROFILES.set(profilesList.getSelectionModel().getSelectedIndex(),
                        new ElementProfile(name, ym, mmi, cra));//the index of the combo == the Observable List
            } else {
                ModuleEngine.ELEM_PROFILES.add(new ElementProfile(name, ym, mmi, cra));
            }
            profileName.setDisable(true);
            profileYm.setDisable(true);
            profileMmi.setDisable(true);
            profileCrsArea.setDisable(true);
        });
        editProfile.setOnAction((e) -> { //trim input lengths
            profileName.setDisable(true);
            profileYm.setDisable(false);
            profileMmi.setDisable(false);
            profileCrsArea.setDisable(false);
        });
        profilesList.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
            profileName.setText(nv.name);
            profileYm.setText(String.valueOf(nv.youngModulus));
            profileMmi.setText(String.valueOf(nv.mmi));
            profileCrsArea.setText(String.valueOf(nv.crsArea));
            editProfile.setDisable(false);
        });

    }

    private void tabEvent() {
        SignalSlot.addSlot(WINDOW_ID, "createTabFromData", obj -> newTabFromModel((DataBoxPayload) obj));
    }

    private void treeEvent() {
        prjOpen.setOnAction(e -> {
            String value = projectTree.getSelectionModel().getSelectedItem().getValue();
            if (!value.contains(en.prj.getProjectFileTypeExt(null))) {
                return;
            }
            DataBoxPayload tmp = en.prj.openFile(new File(en.getProp("path") + value));
            if (tmp == null) {
                SignalSlot.emitSignal(WINDOW_ID, "NotifyUI", "Failed to open file");
                return;
            }
            newTabFromModel(tmp);
        });
        prjDelete.setOnAction(e -> {
            String value = projectTree.getSelectionModel().getSelectedItem().getValue();
            new File(en.getProp("path") + value).delete();
            ProjectManager.refreshProjectTree(en.prj.projectPath, projectItem);
        });
        ProjectManager.fillProjectTree(en.prj.projectPath, projectItem);
    }

    private void bottomEvent() {
        this.commander = new Commander();
        EventHandler<ActionEvent> ev = (e) -> {
            String cmd = commandInput.getText();
            commandOutput.appendText("-->" + cmd + "\n");
            if (cmd.isEmpty()) {
                return;
            }
            commandOutput.appendText(commander.execute(cmd, en.canvasManager.getCanvasAt(null)));
            commandOutput.appendText("\n");
        };
        commandHelp.setOnAction(e -> {
            new SimpleAlert("Commands Help", ""
                    + "Commands List", "DRAWNODE POSX POSY BOUNDARY \n\t Draws a Node on axis ( X Y )\n"
                    + "DUPLNODE ID X DIST AMOUNT\n\t Duplicates a Node; ID is the node id eg. N1 or N3 \n\t"
                    + " DIST is the distance interval at which nodes are duplicated\n\t"
                    + "AMOUNT is the number of nodes to created\n\t"
                    + "BOUNDARY is the boundary condition i.e. PINNED , FIXED, ROLLER\n"
                    + "DELNODE NODE-ID1,NODE-ID2,...\n\t"
                    + "Deletes nodes with the specified node id\n"
                    + "DRAWELEMENT NODE1 NODE2 NODE3\n\t"
                    + "Draws Element joining the specified node IDs with the first being the host node\n"
                    + "ALTERELEMENT ATTR VALUE ELEM-ID1,ELEMID2,... \n\t"
                    + "Alter element settings; \n\t"
                    + "ATTR is the attribute to alter ->\n\t\t"
                    + "(load_type; load_value; load_d2o; elem_area; elem_inertia; elem_ym)\n\t"
                    + "VALUE is the value to be assigned to attribute\n\t"
                    + "ELEM-ID1,ELEMID2,... means an infinte list of element IDs to alter\n"
                    + "ALTERNODE ATTR VALUE NODE-ID1,NODE-ID2,...  \n\t"
                    + "ATTR is the attribute to alter ->\n\t\t"
                    + "((boundary:pinned,fixed,roller; h_loading; v_loading)\n\t"
                    + "VALUE is the value to be assigned to attribute\n\t"
                    + "NODE-ID1,NODE-ID2,... means an infinte list of node IDs to alter\n"
                    + "SETPROFILE PROFILE_NAME ELEM-ID1,ELEMID2,...\n\t"
                    + "Set an element profile from by specifying the profile name and a list of \n"
                    + "Elements to alter by their IDs", 2).exec();
        });
        commandInput.setOnAction(ev);
        commandInput.textProperty().addListener((ob, ov, nv) -> {
            commandInput.setText(nv.toUpperCase());
        });

        scaler.valueProperty().addListener((ObservableValue<? extends Number> observer, Number oldv, Number newv) -> {
            Canvas2D tmp = en.canvasManager.getCanvasAt(null);
            tmp.scale = newv.doubleValue();
            tmp.root.setScaleX(newv.doubleValue());
            tmp.root.setScaleY(newv.doubleValue());
            tmp.root.setScaleZ(newv.doubleValue());
        });
        SignalSlot.addSlot(WINDOW_ID, "NotifyUI", (obj) -> zoomNotif.setText((String) obj));
    }

    public final ModuleEngine en;
    private final Scene scene;
    public final BorderPane root = new BorderPane();
    public SplitPane contentRoot;
    public final SplitPane canvasPane = new SplitPane();
    public SToolBar toolbar;
    public TabPane leftBar = new TabPane(),
            butPane = new TabPane();
    private final Button prjDelete = new Button();
    private final Button prjOpen = new Button();
    public Label notif = new Label(""),
            zoomNotif = new Label(""),
            scale = new Label("");
    public Slider scaler;
    public ListView<CShapes> objectsList;
    public TreeView<String> projectTree;
    public TreeItem<String> projectItem;
    private final TextField commandInput = new TextField();
    private final TextArea commandOutput = new TextArea();
    private final Button commandAct = new Button("", Resources.getImage("imgProcess")),
            commandHelp = new Button("Help");
    private ComboBox<ElementProfile> profilesList;
    private final Button newProfile = new Button("New"),
            saveProfile = new Button("Save"),
            editProfile = new Button("Edit");
    private final Label profilePaneNotif = new Label();
    private final TextField profileName = new TextField(),
            profileYm = new NumberField(),
            profileMmi = new NumberField(),
            profileCrsArea = new NumberField();
    private Commander commander;
}

class ObjectListViewCell extends ListCell<CShapes> {

    @Override
    public void updateItem(CShapes shape, boolean empty) {
        super.updateItem(shape, empty);
        setGraphic(null == shape ? null : Resources.getImage("img" + SimonUtil.toCamelCase(shape.getTypeStr()) + "Tool"));
        setText(null == shape ? null : shape.getTitle());
    }
}

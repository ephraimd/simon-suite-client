package simonds1_client.ui;

import java.util.HashMap;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import simonds1.core.Logger;
import simonds1.core.Network;
import simonds1.core.Resources;
import simonds1.core.SignalSlot;
import simonds1_client.ui.pad.Canvas2D;

/**
 * Brand new Generic toolbar, can be altered by configuration
 *
 * @author ADEDAMOLA
 */
public class SToolBar extends TabPane {

    public SToolBar(HashMap<String, String> options) {
        super();
        this.optionsMap = options;
        if (optionsMap.get("project_type").equals("CSLB")) {
            this.getTabs().add(this.buildSlabsTab());
        } else {
            this.getTabs().addAll(this.buildHomeTab(), this.buildToolsTab());
        }
        this.addEvents();
    }

    private Tab buildSlabsTab() {
        ToolBar tmptb = new ToolBar(this.addDrawControlButtons());
        Tab tmp = new Tab("Tool", tmptb);
        tmp.setClosable(false);
        return tmp;
    }

    private Tab buildHomeTab() {
        ToolBar tmptb = new ToolBar(
                this.addHFileButtons(), new Separator(Orientation.VERTICAL),
                this.addHEditButtons(), new Separator(Orientation.VERTICAL));
        Tab tmp = new Tab("Home", tmptb);
        tmp.setClosable(false);
        return tmp;
    }

    private Tab buildToolsTab() {
        ToolBar tmptb = new ToolBar(
                this.addTCmdButtons(), new Separator(Orientation.VERTICAL),
                this.addDrawControlButtons(), new Separator(Orientation.VERTICAL),
                this.addTShapesButtons(), new Separator(Orientation.VERTICAL));
        Tab tmp = new Tab("Design", tmptb);
        tmp.setClosable(false);
        return tmp;
    }

    private GridPane addTCmdButtons() {
        this.networkBut = new ToggleButton("",
                Network.connected.getValue() ? Resources.getImage("imgNoNetwork") : Resources.getImage("imgNoNetwork"));
        this.networkBut.setTooltip(new Tooltip("Open Network dialog"));
        networkBut.setSelected(Network.connected.getValue());
        this.runBut = new Button("", Resources.getImage("imgProcess"));
        this.runBut.setTooltip(new Tooltip("Run Analysis"));

        GridPane grid = new GridPane();
        grid.add(networkBut, 0, 0); //col,row
        grid.add(runBut, 1, 0); //col,row
        return grid;
    }

    private GridPane addDrawControlButtons() {
        switch (optionsMap.get("project_type")) {
            case "TA":
                return addTADrawControlButtons();
            case "BD":
                return addBDDrawControlButtons();
            case "CBD":
                return addCBDDrawControlButtons();
            case "CD":
                return addCDDrawControlButtons();
            case "PF":
                return addPFDrawControlButtons();
            case "CSLB":
                return addSLBControlButtons();
            default:
                Logger.displayAlert("Failed to Draw Toolbar Control Buttons. Unrecognized Project Type", true);
                return new GridPane();
        }
    }

    private GridPane addSLBControlButtons() {
        ComboBox<String> slabType = new ComboBox<>(FXCollections.observableArrayList("Flat Slab", "Ribbed Slab", "Solid Slab"));
        slabType.setTooltip(new Tooltip("Select the type of Slab you'd like to design"));
        slabType.getSelectionModel().selectFirst();
        slabType.valueProperty().addListener((ob, ov, nv) -> {
            SignalSlot.emitSignal(WINDOW_ID, "ActivateSlabPane", nv.toLowerCase());
        });

        GridPane grid = new GridPane();
        grid.add(slabType, 0, 0, 1, 3); //col,row, colspan, rowspan
        return grid;
    }

    private GridPane addPFDrawControlButtons() {
        ToggleGroup grp = new ToggleGroup();
        this.nodeBut = new ToggleButton("", Resources.getImage("imgNodeTool"));
        nodeBut.setTooltip(new Tooltip("Node Tool. Use to create Nodes"));
        this.nodeBut.setToggleGroup(grp);
        this.selectBut = new ToggleButton("", Resources.getImage("imgSelectTool"));
        selectBut.setTooltip(new Tooltip("Select Tool. Use when you want to avoid drawing anything"));
        this.selectBut.setToggleGroup(grp);
        this.nodeBut.setSelected(true);

        GridPane grid = new GridPane();
        //grid.add(lineBut, 0, 0); //col,row
        grid.add(nodeBut, 1, 0); //col,row
        grid.add(selectBut, 2, 0); //col,row
        return grid;
    }

    private GridPane addCDDrawControlButtons() {
        this.columnBut = new Button("", Resources.getImage("imgLineTool"));
        columnBut.setTooltip(new Tooltip("Column Tool. Use to create Columns"));

        GridPane grid = new GridPane();
        grid.add(columnBut, 1, 0); //col,row
        return grid;
    }

    private GridPane addBDDrawControlButtons() {
        this.beamBut = new Button("", Resources.getImage("imgLineTool"));
        beamBut.setTooltip(new Tooltip("Beam Tool. Use to create Beams"));

        GridPane grid = new GridPane();
        grid.add(beamBut, 1, 0); //col,row
        return grid;
    }

    private GridPane addCBDDrawControlButtons() {
        this.rcbeamBut = new Button("", Resources.getImage("imgLineTool"));
        rcbeamBut.setTooltip(new Tooltip("RCD Beam Tool. Use to create Reinforced Concrete Beams"));

        GridPane grid = new GridPane();
        grid.add(rcbeamBut, 1, 0); //col,row
        return grid;
    }

    private GridPane addTADrawControlButtons() {
        ToggleGroup grp = new ToggleGroup();
        //this.lineBut = new ToggleButton("", Resources.img.get("imgLineTool"));
        //this.lineBut.setToggleGroup(grp);
        this.nodeBut = new ToggleButton("", Resources.getImage("imgNodeTool"));
        nodeBut.setTooltip(new Tooltip("Node Tool. Use to create Nodes"));
        this.nodeBut.setToggleGroup(grp);
        this.selectBut = new ToggleButton("", Resources.getImage("imgSelectTool"));
        selectBut.setTooltip(new Tooltip("Select Tool. Use when you want to avoid drawing anything"));
        this.selectBut.setToggleGroup(grp);
        this.nodeBut.setSelected(true);

        GridPane grid = new GridPane();
        //grid.add(lineBut, 0, 0); //col,row
        grid.add(nodeBut, 1, 0); //col,row
        grid.add(selectBut, 2, 0); //col,row
        return grid;
    }

    private GridPane addTShapesButtons() {
        //ObservableList<Number> sizeList = FXCollections.observableArrayList(SimonUtil.range(2, 10, 2));
        //this.strokeSize = new ComboBox<>(sizeList);//setmaxsize(d.max
        //this.strokeSize.getSelectionModel().select(Integer.parseInt(Settings.getConf("ui-canvas-stroke_size")));
        //this.strokeSize.setEditable(true);
        //this.strokeSize.setMaxWidth(3);
        //strokeSize.setTooltip(new Tooltip("Pen Size. Set Size of canvas drawing pen"));

        this.canvasTheme = new ComboBox<>(FXCollections.observableArrayList("Dry White", "Dry Black"));
        canvasTheme.getSelectionModel().select(Canvas2D.canvasTheme.get());
        canvasTheme.setTooltip(new Tooltip("Change Canvas Theme"));

        GridPane grid = new GridPane();
        grid.add(canvasTheme, 0, 0, 2, 1); //col,row, colspan, rowspan
        //grid.add(strokeSize, 3, 0, 2, 1); //col,row, colspan, rowspan
        return grid;
    }

    private GridPane addHFileButtons() {
        this.fileNew = new Button("", Resources.getImage("imgNewRes"));
        fileNew.setTooltip(new Tooltip("New File. Creates New Model Window"));
        this.fileOpen = new Button("", Resources.getImage("imgOpenFileTool"));
        fileOpen.setTooltip(new Tooltip("Open File. Opens existing file"));
        this.fileSave = new Button("", Resources.getImage("imgSaveTool"));
        fileSave.setTooltip(new Tooltip("Save File. Saves current file"));
        this.fileSaveAs = new Button("", Resources.getImage("imgSaveAsTool"));
        fileSaveAs.setTooltip(new Tooltip("Save File As. Saves current file as a specified name"));

        GridPane grid = new GridPane();
        grid.add(fileNew, 0, 0); //col,row
        grid.add(fileOpen, 1, 0); //col,row
        grid.add(fileSave, 2, 0); //col,row
        grid.add(fileSaveAs, 3, 0); //col,row
        return grid;
    }

    private GridPane addHEditButtons() {
        this.editCopy = new Button("", Resources.getImage("imgCopyTool"));
        editCopy.setTooltip(new Tooltip("Copy. Copies selected Objects"));
        this.editCut = new Button("", Resources.getImage("imgCutTool"));
        editCut.setTooltip(new Tooltip("Cut. Cut selected Objects"));
        this.editPaste = new Button("", Resources.getImage("imgPasteTool"));
        editPaste.setTooltip(new Tooltip("Paste. Pastes objects from clipboard Objects"));
        //this.editUndo = new Button("", Resources.getImage("imgUndoTool"));
        //editUndo.setTooltip(new Tooltip("Undo. Undo last action"));
        //this.editRedo = new Button("", Resources.getImage("imgRedoTool"));
        editCopy.setTooltip(new Tooltip("Redo. Redo last Action Undone"));

        GridPane grid = new GridPane();
        grid.add(this.editCopy, 0, 0); //col,row
        grid.add(editCut, 1, 0); //col,row
        grid.add(editPaste, 2, 0); //col,row
        //grid.add(editUndo, 3, 0);
        //grid.add(editRedo, 4, 0);
        return grid;
    }

    private void addEvents() {
        //lineBut.setOnAction(e -> Canvas2D.shapeType = 2);
        if (nodeBut != null || selectBut != null) {
            nodeBut.setOnAction(e -> Canvas2D.shapeType = 3);
            selectBut.setOnAction(e -> Canvas2D.shapeType = 1);
        }
        //strokeSize.valueProperty().addListener((ob, ov, nv) -> Canvas2D.strokeWidth = nv.intValue());
        if (canvasTheme != null) {
            Canvas2D.canvasTheme.bind(canvasTheme.valueProperty());
        }
        if (networkBut != null) {
            Network.connected.addListener((ob, ov, nv) -> {
                networkBut.setGraphic(Resources.getImage(nv ? "imgNoNetwork" : "imgNetwork"));
                networkBut.setSelected(nv);
            });
        }
    }

    public ToggleButton networkBut = null, //lineBut, 
            nodeBut = null, selectBut = null;
    public Button columnBut, beamBut, rcbeamBut;
    public Button fileNew, fileOpen, fileSave,
            fileSaveAs, fileProps, editCopy, editCut,
            editPaste;//, editUndo, editRedo;
    public Button newBut, runBut;
    //public ComboBox<Number> strokeSize;
    public ComboBox<String> canvasTheme = null;
    private final HashMap<String, String> optionsMap;
    public static String WINDOW_ID = null;
}

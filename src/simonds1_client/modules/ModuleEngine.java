package simonds1_client.modules;

import java.util.HashMap;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import simonds1.core.Logger;
import simonds1.core.Network;
import simonds1.core.ProjectManager;
import simonds1.core.SignalSlot;
import simonds1.core.SimonUtil;
import simonds1.core.transport.DataBox;
import simonds1.core.transport.DataBoxPayload;
import simonds1_client.modules.BD.BDResultPad;
import simonds1_client.modules.CBD.CBDResultPad;
import simonds1_client.modules.CD.CDResultPad;
import simonds1_client.modules.PF.PFResultPad;
import simonds1_client.modules.TA.TAResultPad;
import simonds1_client.ui.dialogs.NetworkDialog;
import simonds1_client.ui.dialogs.StartupDialog;
import simonds1_client.ui.pad.CanvasTabManager;
import simonds1_client.ui.shapes.CShapes;

/**
 * A generic Main Engine with Modular and Reusable Components. !!Ensure you
 * setup the options before you init the class
 *
 * @author ADEDAMOLA
 */
public class ModuleEngine {

    /**
     * Every Engine can be initialized from a project dialog
     *
     * @param result Results from a project Dialog
     */
    public ModuleEngine(Optional<HashMap<String, String>> result) {
        this.initProject(result);
    }

    public ModuleEngine() {
        Optional<HashMap<String, String>> result = new StartupDialog(ModuleEngine.options).dialog.showAndWait();
        this.initProject(result);
    }

    public ModuleEngine(ProjectManager prjj) {
        this.prj = prjj;
        if (prj.props.isEmpty()) {
            return; //test to see if the project creation was successful
        }
        this.setupUI();
    }

    private void initProject(Optional<HashMap<String, String>> result) {
        result.ifPresent((HashMap<String, String> pair) -> {
            this.prj = new ProjectManager(pair);
        });
        if (!result.isPresent()) {
            return; //test to see if the project creation was successful
        }
        this.setupUI();
    }

    public String getProp(String key) {
        switch (key) {
            case "name":
                return prj.props.get("project_name");
            case "path":
                return prj.props.get("project_path");
            case "insitu_scale":
                return prj.props.get(key);
            case "model_scale":
                return prj.props.get(key);
            default:
                return null;
        }
    }
  
    public void setupUI() {
        SimonUtil.APP_ID = SimonUtil.getRandomString(6);
        if(options.get("ui") != null){
            //System.out.println("--->"+options.get("ui"));
            isWorkPaneUI = options.get("ui").contains("Work");
        }
        ui = new ModuleMainUI(this);
        ui.show();
        if(!isWorkPaneUI)
            setupEngines();
        //then integrate the engines
    } //override

    private void setupEngines() {
        SignalSlot.addSlot(ui.WINDOW_ID, "StartUpload", datab -> prepareModel());
        SignalSlot.addSlot(ui.WINDOW_ID, "ModelPrepared", datab -> uploadModel((DataBox) datab));
        
        setupNetworkActions();
    }

    public void setupNetworkActions() {
        ui.toolbar.networkBut.setOnMouseClicked(e -> {
            new NetworkDialog("Connect to Server", this.prj.props.get("project_email"));
            ui.toolbar.networkBut.setSelected(Network.connected.getValue());
        });
        ui.toolbar.runBut.setOnMouseClicked(e -> SignalSlot.emitSignal(ui.WINDOW_ID, "StartUpload", "Fire all Engines!"));
    }  //override

    private boolean prepareModel() {
        SignalSlot.emitSignal(ui.WINDOW_ID, "NotifyUI", "Starting Model Upload!");
        //get the Transport-ready model
        DataBoxPayload tModel = canvasManager.getCanvasAt(null).exportModel();
        //save file first
        prj.saveDataFile(prj.projectPath.getAbsolutePath() +"\\"+ tModel.getTitle(), tModel);
        SignalSlot.emitSignal(ui.WINDOW_ID, "PrepareModel", tModel); //harvest the additional model data elsewhere
        return true;
    }

    private boolean uploadModel(DataBox data) {
        Network network = Network.getContext();
        if (!network.serverOnline() || !network.dataReceivable()) {
            SignalSlot.emitSignal(ui.WINDOW_ID, "NotifyUI", "Server is Offline, Cannot process analysis");
            return Logger.displayAlert("Server is Offline! Please Connect/Reconnect to Server First!", true);
        }
        if (network.send(data)) {
            SignalSlot.emitSignal(ui.WINDOW_ID, "NotifyUI", "Model Uploaded!! Waiting for Analysis Result");
            data = network.recieve();
            if (data == null) {
                SignalSlot.emitSignal(ui.WINDOW_ID, "NotifyUI", "Result from server is Malformed!");
                return Logger.displayAlert("Result from server is malformed!", true);
            }
            if (data.flag < 10) { //success
                SignalSlot.emitSignal(ui.WINDOW_ID, "NotifyUI", "Model Analysis Successful!");
                showResult(data);
            } else {
                SignalSlot.emitSignal(ui.WINDOW_ID, "NotifyUI", "Model Analysis Failed!");
                return Logger.displayAlert(data.errorString, true);
            }
        } else {
            SignalSlot.emitSignal(ui.WINDOW_ID, "NotifyUI", "Failed to upload Model! Check your network.");
            return Logger.displayAlert("Couldn't upload the model. This may be due to bad network access.", true);
        }
        return true;
    }

    private void showResult(DataBox data) {
        //just pour in the result
        //SignalSlot.emitSignal("NotifyUI", "done");
        switch (prj.props.get("project_type")) {
            case "TA":
                new TAResultPad(data, ui.WINDOW_ID).show();
                break;
            case "PF":
                new PFResultPad(data, ui.WINDOW_ID).show();
                break;
            case "BD":
                new BDResultPad(data).show();
                break;
            case "CBD":
                new CBDResultPad(data).show();
                break;
            case "CD":
                new CDResultPad(data).show();
                break;
        }
    }

    public ModuleMainUI ui;
    public ProjectManager prj;
    public CanvasTabManager canvasManager;
    public static HashMap<String, ObservableList<CShapes>> canvasObjects = new HashMap<>();
    public static HashMap<String, String> options = new HashMap<>();
    public static ObservableList<ElementProfile> ELEM_PROFILES = FXCollections.observableArrayList();
    public boolean isWorkPaneUI = false;
}

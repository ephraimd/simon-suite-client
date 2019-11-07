/* 
    Simon Design Suite version  1.0 
 */
package simonds1.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import simonds1.core.transport.DataBoxPayload;
import simonds1.core.transport.SDSModel;
import simonds1_client.modules.ModuleEngine;
import simonds1_client.ui.dialogs.OpenProjectDialog;

/**
 * This class manages projects by providing convenience helper methods. The
 * class contains methods that help with project files and folders too which is
 * very convenient as the project manager uses the project root folder every
 * time
 *
 * @author Olagoke Adedamola Farouq
 */
public class ProjectManager {

    /**
     * The ProjectManager class must be fed with correct project properties. the
     * properties can then be used through the props property
     *
     * @param props the project properties
     */
    public ProjectManager(HashMap<String, String> props) { //So we use Design By Contract Here
        this.props = props;
        if (this.props.isEmpty()) {
            return;
        }
        this.projectPath = new File(props.get("project_path"));
        this.projectName = props.get("project_name");
        this.projectType = props.get("project_type");

        try {
            this.setupProjectConfig();
        } catch (IOException ex) {
            Logger.displayAlert("While Opening Project: " + ex.getMessage(), true);
        }
    }

    public static void route() {
        Optional<HashMap<String, String>> result = new OpenProjectDialog().dialog.showAndWait();
        result.ifPresent((HashMap<String, String> pair) -> {
            route(pair);
        });

    }

    public static void route(HashMap<String, String> prop) {
        //!!!!we should check prop for vital keys to avoid breaking
        ProjectManager tmp = new ProjectManager(prop);
        if (tmp.props.isEmpty()) {
            return;
        }
        new ModuleEngine(tmp);

    }

    /**
     * The list of files in the project root
     *
     * @param filter a string to filter for matching suffix. e.g
     * <pre>".sds" or ".txt"</pre>
     *
     * @return {@code File[]} a list of File objects that point to each file in
     * the project
     */
    public File[] getFilesList(String filter) { //if filter not needed, simply send empty string
        return projectPath.listFiles((File dir, String name) -> name.endsWith(filter));
    }

    /**
     * The list of folders in the project root
     *
     * @return {@code File[]} a list of File objects that point to each folder
     * in the project
     */
    public File[] getFoldersList() { //if filter not needed, simply send empty string
        return projectPath.listFiles((File pathname) -> pathname.isDirectory());
    }

    /**
     * The list of everything in the project root
     *
     * @return {@code File[]} a list of File objects that point to each file and
     * folder in the project
     */
    public String[] getContentsList() { //if filter not needed, simply send empty string
        return projectPath.list();
    }

    /**
     * This method helps to save project attributes very easily.
     *
     * @param keys The attribute key
     * @param vals The new project attribute value
     * @return {@code false} if the length of keys and values do not match or
     * failed to save. returns {@code true} if all goes well
     */
    public boolean saveProjectAttr(String[] keys, String[] vals) {
        if (keys.length != vals.length) {
            return false;
        }

        Settings.resetPropHandle(projectConfig.getAbsolutePath());
        for (int i = 0; i < keys.length; i++) {
            Settings.setConf(keys[i], vals[i]);
        }
        Settings.saveConf();
        Settings.resetPropHandle(null);

        return true;
    }

    /**
     * This method helps creates new files right in the project directory
     *
     * @param fileName Full name of the file excluding the extension
     * @param fileType type of file to create
     * @return
     */
    public boolean newFile(String fileName, FileType fileType) {
        try {
            return new File(projectPath.getAbsolutePath()
                    .concat(fileName.concat(getFileTypeExtStr(fileType))))
                    .createNewFile();
        } catch (IOException ex) {
            return Logger.displayAlert("New File Issues: " + ex.getMessage(), true);
        }
    }

    public boolean newFolder(String folderName) {
        return new File(projectPath.getAbsolutePath().concat(folderName)).mkdir();
    }

    public void changeProjectDir(String newProjectPath) {
        projectPath = new File(newProjectPath);
    }

    public SDSModel openFile(File filePath) {
        if (filePath == null) {
            filePath = SimonUtil.getFileDialog("Open Model File",
                    this.projectPath.getPath(),
                    new FileChooser.ExtensionFilter[]{
                        new FileChooser.ExtensionFilter("SDS Model",
                                getProjectFileTypeExt(null))
                    }).showOpenDialog(SimonUtil.primaryStage);
        }
        if (filePath == null) {
            return null;
        }
        return (SDSModel) openDataFile(filePath);
    }

    public String saveAs(SDSModel model) {
        File filePath = SimonUtil.getFileDialog("Save Model As",
                this.projectPath.getPath(),
                new FileChooser.ExtensionFilter[]{
                    new FileChooser.ExtensionFilter("SDS Model", getProjectFileTypeExt(null)),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                }).showSaveDialog(SimonUtil.primaryStage);
        if (filePath == null) { //please ensure design contract is valid. consider  )|| model == null)?
            //the user pressed cancel, so exit silently
            return null;
        }
        model.title = filePath.getName(); //here model title has been updated. Also happens to be the filename
        saveDataFile(filePath.getAbsolutePath(), model);
        return filePath.getName(); //update the canvas title, and tab title
        //before changing tab name, get the hash and the object
    }

    /**
     * Opens a data file from the specified path. You should never call this
     * method if you don't have a correct absolute file path
     *
     * @param absPath absolute file path of the data file
     * @return {@code DataBoxPayload} the data
     */
    public DataBoxPayload openDataFile(File absPath) {
        try {
            return (DataBoxPayload) new ObjectInputStream(new FileInputStream(absPath.getAbsolutePath())).readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException ex) {
            Logger.displayAlert(ex.getMessage(), true); //even if file not exist
            return null;
        }

    }

    public boolean saveDataFile(String absFilename, DataBoxPayload model) {
        if (model == null) {
            return false;
        }
        //System.out.println(absFilename);
        //the model title must have been updated for saveas or left unchanged if just normal save
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(absFilename))) {
            outputStream.writeObject(model);
            outputStream.flush();
            SignalSlot.emitSignal(null, "NotifyUI", model.getTitle() + " Saved");
            return true;
        } catch (IOException ex) {
            SignalSlot.emitSignal(null, "NotifyUI", "Failed to Save file " + model.getTitle());
            return Logger.displayAlert(ex.getMessage(), true);
        }
    }

    private void setupProjectConfig() throws IOException {
        String pPath = projectPath.getAbsolutePath();
        if (projectPath.isDirectory()) { //here we load from hashmap to config
            this.isNew = true;
            pPath += (!pPath.endsWith("\\") ? "\\" : "")
                    + this.projectName.trim().replace(" ", "-");
            projectPath = new File(pPath);
            //now we are sure the name and type exists
            this.projectConfig = new File(pPath + "\\" + this.projectName.trim().replace(" ", "-") + ".sdsproj");

            projectPath.mkdir(); //creates project folder
            projectConfig.createNewFile(); //creates project file
            this.props.put("project_path", projectPath.getAbsolutePath());

            Settings.resetPropHandle(this.projectConfig.getAbsolutePath());
            this.props.entrySet().forEach(entry -> {
                Settings.setConf(entry.getKey(), entry.getValue());
            });
            Settings.setConf("date_time", new Date().toString()); //manual addition
            Settings.saveConf();
        } else if (projectPath.isFile() && projectPath.exists()) { //here we load from config to hashmap
            //in this case, the porject path is actually pointing to the project hook
            this.projectConfig = projectPath;
            projectPath = new File(projectPath.getParent());
            Settings.resetPropHandle(this.projectConfig.getAbsolutePath());
            Settings.getAllConf().forEach(entry -> { //populate the project virtual props with the config
                this.props.put((String) entry.getKey(), (String) entry.getValue());
            });
            this.props.put("project_file", this.projectConfig.getAbsolutePath());
            this.props.put("project_path", this.projectPath.getAbsolutePath() + "\\");
            this.projectName = Settings.getConf("project_name"); //constant by contract
            this.projectType = Settings.getConf("project_type");
        }

        Settings.resetPropHandle(null);//its very very important that we resetPropHandle to default
    }

    public static void refreshProjectTree(File path, TreeItem<String> rootItem) {
        rootItem.getChildren().clear();
        ProjectManager.fillProjectTree(path, rootItem);
    }

    public static void fillProjectTree(File path, TreeItem<String> rootItem) {
        File[] list = path.listFiles();
        for (File each : list) {
            TreeItem<String> tmp = new TreeItem<>(each.getName());
            if (each.isDirectory()) {
                //tmp.setGraphic(Resources.imgExpand); 
                tmp.setExpanded(false);
                tmp.setGraphic(Resources.getImage("imgExpand"));
                fillProjectTree(each, tmp);
            } else if (each.getAbsolutePath().contains(getFileTypeExtStr(FileType.PROJECT_FILE))) {
                tmp.setGraphic(Resources.getImage("imgTmp"));
            } else //any other file!! change this sometime maybe?
            {
                tmp.setGraphic(Resources.getImage("imgModelFile"));
            }
            rootItem.getChildren().add(tmp);
        }
    }

    //the configurations for project types, also in ModuleEngine#setUIModule() and SToolBar
    //update the MainUI to setup the required buttons
    //to make new projects, configure these methods & properties below, ModuleEngine#setUIModule() for UI type, and toolbar requirements
    public Type getProjectType() {
        switch (props.get("project_type")) {
            case "TA":
                return Type.TA;
            case "BD":
                return Type.BD;
            case "CBD":
                return Type.CBD;
            case "CCD":
                return Type.CCD;
            case "CSLB":
                return Type.CSLB;
            case "CD":
                return Type.CD;
            case "PF":
                return Type.PF;
            default:
                return Type.TA;
        }
    }

    public String getProjectFileTypeExt(String projectType) {
        switch (projectType == null ? props.get("project_type") : projectType) {
            case "TA":
                return getFileTypeExtStr(FileType.TRUSS_MODEL);
            case "PF":
                return getFileTypeExtStr(FileType.FRAME_MODEL);
            case "BD":
                return getFileTypeExtStr(FileType.BEAM_MODEL);
            case "CBD":
                return getFileTypeExtStr(FileType.CONCRETE_BEAM_MODEL);
                case "CCD":
                return getFileTypeExtStr(FileType.CONCRETE_BEAM_MODEL);
            case "CSLB":
                return getFileTypeExtStr(FileType.CONCRETE_SLAB);
            case "CD":
                return getFileTypeExtStr(FileType.COLUMN_MODEL);
            default:
                return null;
        }
    }

    public static String getFileTypeExtStr(FileType ft) {
        switch (ft) {
            case TRUSS_MODEL:
                return ".stm";
            case FRAME_MODEL:
                return ".sfm";
            case FRAME_RESULT:
                return ".sfr";
            case TRUSS_RESULT:
                return ".str";
            case BEAM_MODEL:
                return ".sbm";
            case CONCRETE_BEAM_MODEL:
                return ".scbm";
            case CONCRETE_COLUMN_MODEL:
                return ".sccm";
            case CONCRETE_SLAB:
                return ".scsm";
            case COLUMN_MODEL:
                return ".scm";
            case PROJECT_FILE:
                return ".sdsproj";
            default:
                return null;
        }
    }

    public static void getRecentProjects() {
        //still working on it..really low on time
    }

    public enum Type {
        TA, BD, CBD, CD, PF, CSLB, CCD
    };

    public enum FileType {
        TRUSS_MODEL, FRAME_MODEL, FRAME_RESULT,
        TRUSS_RESULT, PROJECT_FILE, BEAM_MODEL,
        CONCRETE_BEAM_MODEL, COLUMN_MODEL, CONCRETE_SLAB, CONCRETE_COLUMN_MODEL
    };

    private String projectName, projectType;
    public File projectConfig = new File(""), projectPath = new File("");
    public boolean isNew = false;
    public final HashMap<String, String> props;

}

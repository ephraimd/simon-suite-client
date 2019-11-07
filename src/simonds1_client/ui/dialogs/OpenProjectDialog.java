/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.dialogs;

import java.io.File;
import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import simonds1.core.SimonUtil;

/**
 * Uniformly handles all project opening operations
 *
 * @author Olagoke Adedamola Farouq
 */
public class OpenProjectDialog {

    public OpenProjectDialog() {
        this.dialog = new Dialog<>();
        this.setupDialog();
        this.setupContent();
        this.setupButtons();
        this.setupEvents();
    }

    private void setupDialog() {
        dialog.setGraphic(new ImageView(SimonUtil.resPath + "image/new.png"));
        dialog.setTitle("Simon Design Suite");
        dialog.setHeaderText("Open Project");
        dialog.setWidth(700);
        dialog.setHeight(400);
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(this.grid);
    }

    private void setupContent() {
        Label name = new Label("Project File");
        name.setLabelFor(this.projectFile);
        this.projectFile = new TextField(System.getProperty("user.home"));
        this.projectFile.setPromptText("Click to locate project file");
        GridPane.setHgrow(name, Priority.NEVER);
        GridPane.setHgrow(this.projectFile, Priority.ALWAYS);
        this.grid.add(name, 0, 0);
        this.grid.add(this.projectFile, 1, 0); //col,row
        
        this.notif.setStyle("-fx-color: red;"); 
        this.grid.add(notif, 0, 1); //col,row
    }

    private void setupButtons() {
        this.okBut = new ButtonType("Open", ButtonBar.ButtonData.FINISH);
        dialog.getDialogPane().getButtonTypes().addAll(this.okBut, ButtonType.CANCEL);
    }

    private void setupEvents() {
        Node okButn = this.dialog.getDialogPane().lookupButton(this.okBut);
        okButn.setDisable(!new File(this.projectFile.getText()).isFile());
        this.projectFile.setOnMouseClicked(e -> {
            FileChooser fc = SimonUtil.getFileDialog("Open File", System.getProperty("user.home"),
                    new FileChooser.ExtensionFilter[]{
                        new FileChooser.ExtensionFilter("SDS Project File", "*.sdsproj")
                    });
            File res = fc.showOpenDialog(this.dialog.getOwner());
            if (res != null) {
                projectFile.setText(res.toPath().toAbsolutePath().toString() + "\\");
                okButn.setDisable(false);
            } else okButn.setDisable(true);
        });
        this.dialog.setResultConverter(dBut -> {
            if (dBut == this.okBut) {
                HashMap<String, String> tmp = new HashMap<>();
                tmp.put("project_path", projectFile.getText());
                return tmp;
            }
            return new HashMap<>();
        });
    }
    public Dialog<HashMap<String, String>> dialog;
    private final Label notif = new Label();
    private final GridPane grid = new GridPane();
    public ButtonType okBut;
    private TextField projectFile;
}

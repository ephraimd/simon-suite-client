package simonds1_client.ui.dialogs;

import java.io.File;
import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import simonds1.core.Resources;
import simonds1.core.SimonUtil;

/**
 * A generic startup dialog
 *
 * @author ADEDAMOLA
 */
public class StartupDialog {

    public StartupDialog(HashMap<String, String> options) {
        this.optionsMap = options;
        dialog = new Dialog<>();
        setupButtons();
        setupContent();
        setupDialog();
    }

    private void setupDialog() {
        dialog.setGraphic(Resources.getImage("imgProject"));
        dialog.setTitle("Simon Design Suite");
        dialog.setHeaderText(optionsMap.get("module_new_heading"));
        dialog.setWidth(700);
        dialog.setHeight(400);
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(contentPane);
    }

    private void setupButtons() {
        this.okBut = new ButtonType("Create", ButtonBar.ButtonData.FINISH);
        dialog.getDialogPane().getButtonTypes().addAll(this.okBut, ButtonType.CANCEL);
    }

    private void setupContent() {
        contentPane.setHgap(6);
        contentPane.setHgap(4);
        contentPane.setPadding(new Insets(10, 5, 4, 8));
        setupContent1();
        buildEvents();
    }

    private void setupContent1() {
        {
            Label name = new Label("Project Name");
            name.setLabelFor(this.nameField);
            this.nameField = new TextField("New Project");
            this.nameField.setPromptText("e.g Dammy's Project");
            GridPane.setHgrow(name, Priority.NEVER);
            GridPane.setHgrow(this.nameField, Priority.ALWAYS);
            contentPane.add(name, 0, 0);
            contentPane.add(this.nameField, 1, 0); //col,row
        }
        {
            Label name = new Label("Project Location");
            name.setLabelFor(this.projectFolder);
            this.projectFolder = new TextField(System.getProperty("user.home"));
            this.projectFolder.setPromptText("Click to enter folder path");
            GridPane.setHgrow(name, Priority.NEVER);
            GridPane.setHgrow(this.projectFolder, Priority.ALWAYS);
            contentPane.add(name, 0, 1);
            contentPane.add(this.projectFolder, 1, 1); //col,row
        }
        if (this.optionsMap.get("ui").equals("Canvas2DPane")) {
            {
                Label name = new Label("Author Email");
                name.setLabelFor(this.emailField);
                this.emailField = new TextField();
                this.emailField.setPromptText("eg. user@consultant.com");
                GridPane.setHgrow(name, Priority.NEVER);
                GridPane.setHgrow(this.emailField, Priority.ALWAYS);
                contentPane.add(name, 0, 2);
                contentPane.add(this.emailField, 1, 2); //col,row
            }
            {
                Label name = new Label("Scale (m)");
                HBox htmp = new HBox();
                name.setLabelFor(htmp);
                GridPane.setHgrow(name, Priority.NEVER);
                GridPane.setHgrow(htmp, Priority.ALWAYS);

                this.insituScale = new TextField("1");
                this.insituScale.setPromptText("On Site");
                this.insituScale.setPrefColumnCount(3);
                this.modelScale = new TextField("5");
                this.modelScale.setPromptText("On Model");
                this.modelScale.setPrefColumnCount(3);
                htmp.getChildren().addAll(new Label("(insitu)"), this.insituScale, new Label("  (model)"), this.modelScale);
                contentPane.add(name, 0, 4);
                contentPane.add(htmp, 1, 4); //col,row
            }
        }
    }

    public void buildEvents() {
        Node okButn = dialog.getDialogPane().lookupButton(okBut);
        okButn.setDisable(false);

        ChangeListener ev = (ChangeListener<String>) (ob, ov, nv) -> {
            okButn.setDisable(this.nameField.getText().isEmpty() || (this.emailField != null && this.emailField.getText().isEmpty()));
        };
        this.nameField.textProperty().addListener(ev);
        if (this.optionsMap.get("ui").equals("Canvas2DPane")) {
            this.emailField.textProperty().addListener(ev);
        }

        this.projectFolder.setOnMouseClicked(e -> {
            DirectoryChooser fc = new DirectoryChooser();
            fc.setInitialDirectory(new File(this.projectFolder.getText()));
            fc.setTitle("Select Project Folder");
            File res = fc.showDialog(SimonUtil.primaryStage);
            if (res != null) {
                projectFolder.setText(res.toPath().toAbsolutePath().toString() + "\\");
            }
        });

        this.dialog.setResultConverter(dBut -> {
            if (dBut == okBut) {
                HashMap<String, String> tmp = new HashMap<>();
                tmp.put("project_name", nameField.getText());
                tmp.put("project_path", projectFolder.getText());
                tmp.put("project_type", optionsMap.get("project_type"));
                if (this.optionsMap.get("ui").equals("Canvas2DPane")) {
                    tmp.put("project_email", emailField.getText());
                    tmp.put("model_scale", modelScale.getText());
                    tmp.put("insitu_scale", insituScale.getText());
                }
                return tmp;
            }
            return null;
        });

        this.nameField.selectAll();
    }

    private final HashMap<String, String> optionsMap;
    public final Dialog<HashMap<String, String>> dialog;
    public ButtonType okBut;
    public GridPane contentPane = new GridPane();
    private TextField nameField,
            emailField, projectFolder,
            paperSize, modelScale, insituScale;
    private final Label notif = new Label();
    public CheckBox autoTruss;

}

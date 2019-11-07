
package simonds1_client.ui.dialogs;

import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.ImageView;
import simonds1.core.SimonUtil;

/**
 *
 * @author ADEDAMOLA
 */
public class ControlDialog {
    public ControlDialog(Node control, String title){
        this.dialog = new Dialog<>();
        this.setupDialog();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(control);
        this.setupButtons();
    }
    public ControlDialog(String title){
        this.dialog = new Dialog<>();
        this.setupDialog();
        dialog.setTitle(title);
        this.setupButtons();
    }

    private void setupDialog() {
        dialog.setGraphic(new ImageView(SimonUtil.resPath + "image/tmp.png"));
        dialog.setTitle("Simon Design Suite");
        dialog.setResizable(false);
    }

    private void setupButtons() {
        this.okBut = new ButtonType("Ok", ButtonBar.ButtonData.FINISH);
        dialog.getDialogPane().getButtonTypes().addAll(this.okBut);
    }

    public Dialog<HashMap<String, String>> dialog;
    public ButtonType okBut;
}

/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.dialogs;

import javafx.scene.control.Alert;

/**
 *
 * @author ADEDAMOLA
 */
public class SimpleAlert extends Alert{
    /**
     * Simple Alert Dialog Builder
     * @param title
     * @param header
     * @param content
     * @param type 0=error, 1=warning, 2=info
     */
    public SimpleAlert(String title, String header, String content, int type){ //0-error,1-warning,2-info
        super((type == 0 )? AlertType.ERROR : ((type == 2) ? AlertType.INFORMATION : AlertType.WARNING ));
        this.setup(title, header, content); 
    }
    private void setup(String title, String header, String content){
        this.setTitle(title);
        this.setHeaderText(header);
        this.setContentText(content);
    }
    public void exec(){
        this.showAndWait();
    }
}

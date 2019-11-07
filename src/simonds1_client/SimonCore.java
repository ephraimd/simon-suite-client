/* 
    Simon Design Suite version  1.0 (Simon DS1)
    Final year project being supervised by Dr Olawale Simon
 */
package simonds1_client;

import simonds1.core.SimonUtil;
import javafx.application.Application;
import javafx.stage.Stage;
import simonds1.core.Settings;
import simonds1_client.ui.MainUI;
import simonds1.core.Resources;

/**
 *
 * @author Olagoke Adedamola Farouq
 */
public class SimonCore extends Application {

    public static String[] sargs;

    @Override
    public void start(Stage primaryStage) {
        SimonUtil.init(primaryStage); //we need this every where 
        Settings.init();
        Resources.init();
        if (sargs.length != 0) {
            SimonUtil.parseCMDArgs(sargs);
        } else {
            MainUI app = new MainUI(); //47059510CD
            primaryStage.setTitle(app.getAppTitle());
            primaryStage.setScene(app.getUiContext());
            primaryStage.show();
        }

    }

    public static void main(String[] args) {
        sargs = args;
        launch(args);

    }

}

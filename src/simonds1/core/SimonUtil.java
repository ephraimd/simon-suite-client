/* 
    Simon Design Suite version  1.0 
 */
package simonds1.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import simonds1_client.modules.ModuleEngine;

/**
 *
 * @author Olagoke Adedamola Farouq
 */
public class SimonUtil {

    public static void init(Stage primaryStage) {
        //APP_ID = UUID.randomUUID().toString();
        SimonUtil.primaryStage = primaryStage;
        SimonUtil.primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
            if (Network.connected.get()) 
                Network.SDisconnect();
        });
    }

    public static FileChooser getFileDialog(String title, String initialFile, FileChooser.ExtensionFilter[] filters) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.setInitialDirectory(new File(initialFile));
        //fc.getExtensionFilters().addAll(Arrays.asList(filters));
        return fc;
    }
    public static int getRandomInts(int range){
        if(range == 0)
            return -1;
        
        int tmp = SimonUtil.ArrayToInt(SimonUtil.RANDOM.ints(range, 1, 9).toArray());
        return (tmp < 0)? -tmp : tmp;
    }
    /**
     * I'll prefer to use an in-built java library here
     * @param range 
     * @return RANDOM number string
     */
    public static String getRandomString(int range){
        return String.format("%d", getRandomInts(range));
    }
    public static int ArrayToInt(int[] arr){
        int multiplier = (int) Math.pow(10, arr.length-1); //in tens
        int sum = 0;
        for(int i=0;i<arr.length;i++){
            sum += arr[i] * multiplier;
            multiplier /= 10; //reduce by tens
        }
        return sum;
    }
    
    public static double round(double number, int places){
        double th = Math.pow(10, places);
        return Math.round(number *th)/th;
    }

    /**
     * for(int i=startPoint; i < endPoint; i++) Not considering value limitshere 
     * @param <T>
     * @param startPoint
     * @param endPoint
     * @return T[] if range is built or null if any error occurs
     */
    public static <T> T[] range(int startPoint, int endPoint) {
        return range(startPoint, endPoint, 1);
    }

    /**
     * for(int i=startPoint; i < endPoint; i++) Not considering value limits here
     * @param <T>
     * @param startPoint
     * @param endPoint
     * @param step
     * @return T[] if range is built or null if any error occurs
     */
    public static <T> T[] range(int startPoint, int endPoint, int step) {
        if (endPoint == startPoint) {
            return null;
        }
        ArrayList arr = new ArrayList();
        for (int i = 0, j = startPoint; i < endPoint; i += step, j += step) {
            arr.add(j);
        }
        return (T[]) arr.toArray();
    }
    /**
     * 
     * @param array the Array to convert to string
     * @param from if not null, the array will be stringified from this index
     * @param to if not null, the array will be stringified till this index
     * @return the stringified result
     */
    public static String ArrayToString(String[] array, Integer from, Integer to){
        if(array.length == 0)
            return null;
        StringBuilder buf = new StringBuilder();
        int count = 0;
        for(String tok: array){
            if(from != null && count < from){
                ++count;
                continue;
            }
            if(to != null && count > to)
                break;
            buf.append(tok);
        }
        return buf.toString();
    }

    /**
     * Converts a string to camel case
     *
     * @param str the string to be converted
     * @return {@code String}
     */
    public static String toCamelCase(String str) {
        if (null == str || str.isEmpty()) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        boolean isNextUpper = true;
        for (char s : str.toCharArray()) {
            if (isNextUpper && !Character.isSpaceChar(s)) {
                buf.append(Character.toUpperCase(s));
            } else if (!Character.isSpaceChar(s)) {
                buf.append(Character.toLowerCase(s));
            }
            isNextUpper = Character.isSpaceChar(s);
        }
        return buf.toString();
    }

    public static Label decorateLabel(Label label, String content) {
        if (label == null) {
            label = new Label();
        }
        label.setStyle("-fx-font-size:15px; -fx-text-alignment:center;");
        if(content != null)
            label.setText(content);
        return label;
    }
    public static void parseCMDArgs(String[] args){
        SimonUtil.CMD_ARG = args[0];
        File tmp = new File(args[0]);
        if(!args[0].endsWith(".sdsproj") || !tmp.exists()){
            Logger.displayAlert(args[0]+" is an invalid project file path", true);
            return;
        }
        HashMap<String, String> prop = new HashMap<>();
        prop.put("project_path", tmp.toPath().toAbsolutePath().toString() + "\\");
        Logger.displayAlert("Working Stealth!!", false);
        System.out.println(tmp.toPath().toAbsolutePath().toString());
        ProjectManager.route(prop);
    }

    public static Stage primaryStage = null;
    public static String resPath = "/simonds1_client/res/";
    public static String APP_ID = "";
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static String CMD_ARG = null;
}

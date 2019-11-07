/* 
    Simon Design Suite version  1.0 
*/
package simonds1.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import simonds1_client.ui.dialogs.SimpleAlert;

/**
 * The Logger class is a uniform logging utility that is accessible throughout 
 * the software system.
 * @author Olagoke Adedamola Farouq
 */
public class Logger {
    public static boolean addError(String error){
        ERROR_LIST.add(error);
        return false;
    }
    public static boolean addResponse(String response){
        return RESPONSES.add(response); 
    }
    public static void clearErrorLog(){
        ERROR_LIST.clear();
    }
    public static void clearResponseLog(){
        RESPONSES.clear();
    }
    public static String getLastError(){
        return ERROR_LIST.get(ERROR_LIST.size()-1);
    }
    public static String getLastResponse(){
        return ERROR_LIST.get(RESPONSES.size()-1);
    }
    public static int error_count(){
        return ERROR_LIST.size();
    }
    public static int response_count(){
        return RESPONSES.size();
    }
    public static boolean displayAlert(String content, boolean isError){
        if(isError)
            addError(content);
        else
            addResponse(content);
        SimpleAlert dialog = new SimpleAlert("Simon Design Suite",
                "Notice @ "+date.format(new Date()) , content, isError ? 0 : 2);
        dialog.showAndWait();
        return false;
    }
    
    private static final ArrayList<String> ERROR_LIST = new ArrayList<>();
    private static final ArrayList<String> RESPONSES = new ArrayList<>();
    public static SimpleDateFormat date = new SimpleDateFormat("hh:mm a");
}

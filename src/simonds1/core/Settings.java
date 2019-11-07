/* 
    Simon Design Suite version  1.0 
 */
package simonds1.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author ADEDAMOLA
 */
public class Settings {

    public static void init() {
        try {
            setEnvVars();
            setup();

            confHandle = new Props(appDataPath + "\\conf\\conf.ini");
            confHandle.load();
        } catch (IOException ex) {
            Logger.displayAlert("While Setting things up: " + ex.getMessage(), true);
        }
    }

    public static void resetPropHandle(String confpath) {
        try {
            //1 =set, 0=default
            confHandle = (null == confpath) ? new Props(appDataPath + "\\conf\\conf.ini") : new Props(confpath);
            confHandle.load();
        } catch (IOException ex) {
            Logger.displayAlert("Props Reset Error: " + ex.getMessage(), true);
        }
    }

    public static boolean requestFolder(String folder) {
        File tmp = new File(appDataPath + folder);
        tmp.mkdir();
        return true;
    }

    private static void setup() throws IOException {
        File parent = new File(appDataPath);
        if (!parent.exists()) { //improve this block of code please
            Logger.displayAlert("First time Use! All configurations will be set to default values. \n"
                    + "Application Data is located at " + appDataPath,
                    false);
            parent.mkdirs();
            new File(appDataPath + "\\conf").mkdir(); //make configuration folder
            new File(appDataPath + "\\data").mkdir(); //make server data folder
            new File(appDataPath + "\\INSTALLED").createNewFile(); //make installation flag file
        }
    }

    private static void setEnvVars() {
        isOnMac = System.getProperty("os.name").toLowerCase().contains("mac");
        isOnWindows = System.getProperty("os.name").toLowerCase().contains("win");
        isOnLinux = System.getProperty("os.name").toLowerCase().contains("linux");

        appDataPath = (isOnWindows) ? System.getProperty("user.home") : 
                ((isOnMac) ? System.getProperty("user.home") + "\\Library\\Application Support" 
                : System.getProperty("user.home"));
        appDataPath += "\\.simon-design-suite\\client";
    }
    
    public static String getConf(String conf) {
        return confHandle.getProperty(conf);
    }
    public static Set<Map.Entry<Object, Object>> getAllConf() {
        return confHandle.entrySet();
    }

    public static void setConf(String conf, String value) {
        confHandle.setProperty(conf, value);
    }

    public static void saveConf() {
        confHandle.save();
    }

    public static boolean isOnMac = false,
            isOnWindows = false,
            isOnLinux = false;
    public static String appDataPath = null;
    private static Props confHandle;
}

class Props extends Properties {

    public Props(String confFilePath) throws IOException {
        super();
        this.conFile = confFilePath;
        this.setup();
    }

    private void setup() throws IOException {
        File file = new File(this.conFile);
        if (!file.exists()) {
            file.createNewFile();
            this.setProperty("port", "6677"); //Settings.setConf("", "");
            this.setProperty("host", "127.0.0.1");
            this.setProperty("max_thread_count", "10");
            this.setProperty("data_path", Settings.appDataPath + "\\data");
            this.save();
        }
    }

    public void load() {
        try {
            this.load(new FileInputStream(this.conFile));
        } catch (IOException ex) {
            Logger.displayAlert("While loading config " + ex.getMessage() +" -"+this.conFile, true);
        }
    }

    public void save() {
        try {
            this.store(new FileOutputStream(this.conFile), "Preferences Updated");
        } catch (IOException ex) {
            Logger.displayAlert("While saving config " + ex.getMessage(), true);
        }
    }

    private final String conFile;
}

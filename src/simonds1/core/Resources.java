/* 
    Simon Design Suite version  1.0 
 */
package simonds1.core;

import java.util.HashMap;
import javafx.scene.image.ImageView;

/**
 * Loads and caches all required pics, vids, e.t.c has conf too
 * @author ADEDAMOLA
 */
public class Resources {
    public static void init(){
        //img.put("imgNetwork", new ImageView(SimonUtil.resPath + "image/network.png"));
        img.put("imgNetwork", SimonUtil.resPath + "image/network.png");
        img.put("imgNoNetwork", SimonUtil.resPath + "image/nonetwork.png");
        img.put("imgProcess", SimonUtil.resPath + "image/process.png"); //new res
        img.put("imgNewRes", SimonUtil.resPath + "image/newRes.png");
        img.put("imgNewProject", SimonUtil.resPath + "image/newProject.png");
        img.put("imgProject", SimonUtil.resPath + "image/fileProject.png");
        img.put("imgExpand", SimonUtil.resPath + "image/treeExpand.png");
        img.put("imgModelFile", SimonUtil.resPath + "image/modelFile.png");
        img.put("imgLineTool", SimonUtil.resPath + "image/lineTool.png");
        img.put("imgNodeTool", SimonUtil.resPath + "image/rectTool.png");
        img.put("imgLoadTool", SimonUtil.resPath + "image/loadTool.png");
        img.put("imgSelectTool", SimonUtil.resPath + "image/selectTool.png");
        img.put("imgGroupTool", SimonUtil.resPath + "image/group.png");
        img.put("imgUngroupTool", SimonUtil.resPath + "image/ungroup.png");
        img.put("imgCopyTool", SimonUtil.resPath + "image/copy.png");
        img.put("imgCutTool", SimonUtil.resPath + "image/cut.png");
        img.put("imgPasteTool", SimonUtil.resPath + "image/paste.png");
        img.put("imgUndoTool", SimonUtil.resPath + "image/undo.png");
        img.put("imgRedoTool", SimonUtil.resPath + "image/redo.png");
        img.put("imgOpenFileTool", SimonUtil.resPath + "image/openfile.png");
        img.put("imgSaveTool", SimonUtil.resPath + "image/savetool.png");
        img.put("imgSaveAsTool", SimonUtil.resPath + "image/saveastool.png");
        img.put("imgZoomIn", SimonUtil.resPath + "image/zoom_in.png");
        img.put("imgZoomOut", SimonUtil.resPath + "image/zoom_out.png");
        img.put("imgDeleteFile", SimonUtil.resPath + "image/delete_file.png");
        img.put("imgAbout", SimonUtil.resPath + "image/about.png");
        img.put("imgArrowDown", SimonUtil.resPath + "image/arrow_down.png");
        img.put("imgArrowRight", SimonUtil.resPath + "image/arrow_right.png");
        img.put("imgArrowDownLight", SimonUtil.resPath + "image/arrow_down_light.png");
        img.put("imgArrowRightLight", SimonUtil.resPath + "image/arrow_right_light.png");
        img.put("imgUDLLight", SimonUtil.resPath + "image/udllight.png");
        img.put("imgUDLDark", SimonUtil.resPath + "image/udldark.png");
        img.put("imgTmp", SimonUtil.resPath + "image/tmp.png");
        img.put("FixedControl", SimonUtil.resPath + "image/fixedControl.png");
        img.put("PinnedControl", SimonUtil.resPath + "image/hingeControl.png");
        img.put("RollerControl", SimonUtil.resPath + "image/rollerControl.png");//UDLLight
    }
    public static ImageView getImage(String imgStr){
        if(!img.containsKey(imgStr))
            imgStr = "imgTmp";
        return new ImageView(img.get(imgStr)); 
        
    }
    
    public static HashMap<String, String> img = new HashMap<>();
}

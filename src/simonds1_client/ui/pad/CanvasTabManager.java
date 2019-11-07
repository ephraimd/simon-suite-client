/* 
    Simon Design Suite version  1.0 
 */
package simonds1_client.ui.pad;

import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import simonds1.core.SignalSlot;
import simonds1.core.transport.DataBoxPayload;
import simonds1.core.transport.SDSModel;

/**
 *
 * @author Olagoke Adedamola Farouq
 */
public final class CanvasTabManager extends StackPane {

    private final double insituScale;
    private final double modelScale;
    private final String fileExt, windowID;

    public CanvasTabManager(double insituScale, double modelScale, String ext, String windowID) {
        super();
        fileExt = ext;
        this.insituScale = insituScale;
        this.modelScale = modelScale;
        this.windowID = windowID;
        this.getChildren().add(this.tabPane);
    }
    /**
     * When we add tabs, we use each tab's hash code to id it.
     * @param tabTitle 
     * @return  {@code String} i use this value to unique id each tab for object list mapping
     */
    public String addTab(String tabTitle) {
        String title = null == tabTitle ? "New Model"+tabPane.getTabs().size() : tabTitle;
        title += fileExt;
        Canvas2D tmp = new Canvas2D(insituScale, modelScale, windowID);
        tmp.canvasTitle.set(title);
        return addTab(tmp);
    }
    public String addTabFromData(DataBoxPayload data){
        Canvas2D tmp = new Canvas2D(windowID);//canvas will change it
        tmp.applyGridlines();//draw the gridlines
        tmp.importModel((SDSModel) data, true); //we applied gridlines before
        return addTab(tmp);
    }
    private String addTab(Canvas2D tmp){
        Tab tab = new Tab();
        tab.textProperty().bind(tmp.canvasTitle);
        tab.setContent(tmp);
        tab.setOnClosed(e -> SignalSlot.emitSignal(this.windowID, "ShapeDestroyed", null));
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectLast();
        return String.valueOf(tab.hashCode());
    }

    /**
     * Returns the current canvas on active tab if null is passed
     * @param index tabpane index to get canvas from
     * @return {@code Canvas2D} object
     */
    public Canvas2D getCanvasAt(Integer index) {
        return (Canvas2D) this.tabPane.getTabs().get(null == index ? getCurrentIndex(): index).getContent();
    }
    /**
     * Returns the current tab if null is passed else, the tab at the specified index
     * @param index tabpane index to get tab from
     * @return {@code Tab} object
     */
    public Tab getTabAt(Integer index) {
        return tabPane.getTabs().get(null == index ? getCurrentIndex(): index);
    }

    public int getCurrentIndex() {
        return tabPane.getSelectionModel().selectedIndexProperty().get();
    }
    public String getTabIdAt(Integer index){
        return String.valueOf(getTabAt(index).hashCode());
    }

    public TabPane tabPane = new TabPane();
    public int tabCount = 0;
}

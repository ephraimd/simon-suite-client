/*
 A Pretty cool Source File on a God Blessed day!
*/

package simonds1_client.ui.pad;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Ephrahim Adedamola <olagokedammy@gmail.com>
 */
public class WorkPane extends ScrollPane {

    private final String title;
    public WorkPane(String windowID, String title){
        this.windowID = windowID;
        this.title = title;
        setupUI();
    }
    private void setupUI(){
        buildScroll();
        buildLeftPane();
        buildRightPane();
        buildContentPane();
    }
    private void buildScroll() {
        this.setContent(root);
        this.setPannable(false);
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }
    protected void buildLeftPane(){
        leftPane.setPadding(new Insets(5));
        leftPaneWrapper = new ScrollPane(leftPane);
        leftPaneWrapper.setFitToHeight(true);
        leftPaneWrapper.setFitToWidth(true);
        leftPaneWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftPaneWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        //implement yours
    }
    protected void buildRightPane(){
        rightPane.setPadding(new Insets(4));
        rightPaneWrapper = new ScrollPane(rightPane);
        rightPaneWrapper.setFitToHeight(true);
        rightPaneWrapper.setFitToWidth(true);
        rightPaneWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightPaneWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }
    private void buildContentPane(){
        SplitPane.setResizableWithParent(leftPaneWrapper, false);
        contentPane = new SplitPane(leftPaneWrapper, rightPaneWrapper);
        contentPane.setDividerPositions(0.4f, 6f);
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        root.getChildren().add(contentPane);
        
        Label lab = new Label(title);
        lab.setStyle("-fx-text-align:center;-fx-font-size:30px;-fx-padding: 12px 12px;");
        root.getChildren().add(lab);
    }
    
    protected VBox root = new VBox();
    protected VBox leftPane = new VBox(),
            rightPane = new VBox();
    protected SplitPane contentPane;
    public final String windowID;
    private ScrollPane leftPaneWrapper,
            rightPaneWrapper;
}

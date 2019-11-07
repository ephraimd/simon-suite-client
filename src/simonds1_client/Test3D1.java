package simonds1_client;


import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.stage.Stage;

/* 
    Simon Design Suite version  1.0 
 */

/**
 *
 * @author ADEDAMOLA
 */
public class Test3D1 extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        Group layout = new Group();
        Scene scene = new Scene(layout);
        scene.setFill(Color.WHITE);
        
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(1.0);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1000);
        
        scene.setCamera(camera);
        
        final Cylinder cylinder = new Cylinder(50, 100);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
        cylinder.setMaterial(blueMaterial);
        
        layout.getChildren().add(cylinder);
        
        primaryStage.setTitle("Test App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args){
        launch(args);
    }
}

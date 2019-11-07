package simonds1_client.ui;

import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

/**
 * TextField for number based Entries
 * @author ADEDAMOLA
 */
public class NumberField extends TextField{
    public NumberField(){
        super();
        this.setText("0");
        initUI();
    }
    public NumberField(String text){
        super(text);
        initUI();
    }
    public NumberField(String text, String promptText){
        super(text);
        this.setPromptText(promptText);
        this.setTooltip(new Tooltip(promptText));
        initUI();
    }
    private void initUI(){
        this.textProperty().addListener((ob, ov, nv)->{
            try{
                Float.parseFloat(nv);
            } catch(NumberFormatException ex){
                setText(ov);
            }
        });
    }
}

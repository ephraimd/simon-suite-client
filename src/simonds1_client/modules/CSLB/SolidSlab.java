/*
 A Pretty cool Source File on a God Blessed day!.
 */
package simonds1_client.modules.CSLB;

import java.util.HashMap;
import simonds1.core.transport.DataBox;
import simonds1.core.transport.TabledModel;
import simonds1_client.ui.shapes.CSlab;

/**
 *
 * @author Ephrahim Adedamola <olagokedammy@gmail.com>
 */
public final class SolidSlab {

    private double Fcu, Fy;
    private int bar;
    private double cover;
    private String boundary;
    
    private double longSpan, shortSpan;
    private String slabType; //type of slab
    
    private double moment, gk, qk, uls, sls;
    
    private double depth;
    private double constt;
    private double thickness = Double.NaN;
    

    private void addResult(String key, String value, String comment) {
        sresult.addColumnCell("Result", key);
        sresult.addColumnCell("Value", value);
        sresult.addColumnCell("Comment", comment);
    }

    private void addResult(String key, double value, String comment) {
        sresult.addColumnCell("Result", key);
        sresult.addColumnCell("Value", String.format("%.6s", value));
        sresult.addColumnCell("Comment", comment);
    }

    public DataBox buildResults() {
        String restr = String.format("Designed Slab of depth %smm.", depth);
        restr += "The Results are as follows";
        DataBox payLoad = new DataBox();
        payLoad.payload.put("slab_table", sresult);
        payLoad.stringPayload.put("result_notif", restr);
        
        return payLoad;
    }
    public SolidSlab(CSlab node) {
        getData(node);
        design();
    }

    public void getData(CSlab Node) {
        sresult.addColumnKey("Result");
        sresult.addColumnKey("Value");
        sresult.addColumnKey("Comment");
        //put error reporting mechanism in place please
        try {
            HashMap<String, String> stringProps = Node.stringProps;
            HashMap<String, Number> numberProps = Node.numberProps;
            longSpan = numberProps.get("longspan").doubleValue();
            shortSpan = numberProps.get("shortspan").doubleValue();
            bar = numberProps.get("bar").intValue();
            Fcu = numberProps.get("fcu").doubleValue();
            Fy = Integer.valueOf(stringProps.get("fy").replace("S",""));
            gk = numberProps.get("gk").doubleValue();
            qk = numberProps.get("qk").doubleValue();
            cover = numberProps.get("cover").doubleValue();
            slabType = stringProps.get("slabtype").toLowerCase();
            boundary = stringProps.get("boundary").toLowerCase();
            //System.out.printf("fcu:%s, fy:%s, bc:%s, lx:%s, ly:%s, gk:%s, qk:%s, tita:%s \n",
            //        fcu, fy, bc, lx, ly, gk, qk, tita);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            //data.setError("A serious issue occurred while processing your request. Please Contact Developer.", Flags.MODULE_ERROR);
            //throw new SimonException("Failed to recieve valid Beam Design parameters!");
        }
    }

    public void design(){
        //check long span and short span
        if(longSpan < shortSpan){
            addResult("Span Length", "", "Long span entered is shorter than short span entered. Lengths swapped :)");
            double temp = shortSpan; //switcing span lengths
            shortSpan = longSpan;
            longSpan = temp;
        }
        //estimate loads
        uls = 1.4*gk + 1.6*qk;
        sls = gk + qk;
        
        double ratio = longSpan / shortSpan;
        if(ratio > 2){
            addResult("Slab type", "One way slab", "");
            designOneWay();
        }else{           
            addResult("Slab type", "Two way slab", "");
            designTwoWay();
        }
    }
    
    public void designOneWay(){
        double constant;
        
        switch (slabType){
            case "simply supported":
                moment = uls*Math.pow((shortSpan/1000),2)/8;
                constant = qk < 10 ? 27 : 24;
                break;
            case "cantilever":
                moment = uls*Math.pow((shortSpan/1000), 2)/2;
                constant = qk < 10 ? 11 : 10;
                break;
            default:
                moment = uls*Math.pow((shortSpan/1000),2)/8;
                constant = qk < 10 ? 27 : 24;
                break;               
        }
        depth = shortSpan/constant;
        thickness = depth + cover + bar/2; //incase of increased thickness which would exist
        
        double k = (moment * Math.pow(10,6))/(1000*Math.pow(depth, 2)*Fcu);
        while(k >=  0.156){
            depth = depth + 0.1*depth;
            k = moment * Math.pow(10, 6)/(1000 * Math.pow(depth, 2)*Fcu);
            addResult("K", k, "");
        }
        if(k < 0.156){
            double la = 0.5 + Math.sqrt(0.25 - k/0.9);
            double areaRequired = (moment * Math.pow(10,6))/(0.87*Fy*la*depth);
            //checking reinforcement percentage
            areaRequired = Math.max(1.3*thickness, areaRequired);
            areaRequired = Math.min(40*thickness, areaRequired);
            
            double oneReBar = (Math.PI*Math.pow(bar, 2))/4;
            double spacing = (1000*oneReBar)/(areaRequired);
            spacing = Math.min(3*depth, spacing);
            spacing = Math.min(spacing, 300);
            
            addResult("Area of reinforcement required (mm^2)", areaRequired, "");
            addResult("Bar size (mm)", bar, "");
            addResult("Spacing required (mm)", spacing, "");
            addResult("Suitable Depth (mm)", depth, "");
            addResult("Suitable Cover (mm)", cover, "");
            addResult("Suitable Thickness (mm)", thickness, "");
            
        }else{
            //can we increase bar size and cover?
            cover += 10;
            bar += 2;
            designOneWay();
        }
    }
    
    //two way slab 
    
    
    public void designTwoWay(){
        double[] beta;
        double Msxplus, Msxminus, Msyplus, Msyminus;
        
        beta = betaFunc(boundary, shortSpan, longSpan);
        Msxminus = beta[0]*uls*Math.pow(shortSpan/1000, 2);
        Msxplus = beta[1]*uls*Math.pow(shortSpan/1000, 2);
        Msyminus = beta[2]*uls*Math.pow(shortSpan/1000, 2);
        Msyplus = beta[3]*uls*Math.pow(shortSpan/1000, 2);
        switch (slabType){
            case "simply supported":  
                constt = qk < 10 ? 30 : 28;
                break;
            case "continous":
                constt = qk < 10 ? 40 : 39;
                break;
            default:
                constt = 30;
                break;          
        }
        depth = shortSpan/constt;
        //System.out.println( depth);
        thickness = depth + cover + bar/2;
        //negative msx moment
        double k = Msxminus * Math.pow(10, 6)/(1000 * Math.pow(depth, 2)*Fcu);
        while(k >=  0.156){
            depth = depth + 0.1*depth;
            k = Msxminus * Math.pow(10, 6)/(1000 * Math.pow(depth, 2)*Fcu);
        }
        if(k < 0.156){
            double z = 0.5 + Math.sqrt(0.25 - k/0.9);
            z = Math.min(z, 0.95);
            double Asxminus = Msxminus * Math.pow(10, 6)/(0.87*Fy*z*depth);
            Asxminus = Math.max(1.3*thickness, Asxminus);
            Asxminus = Math.min(40*thickness, Asxminus);
            
            double oneReBar = (Math.PI*Math.pow(bar, 2))/4;
            double spacing = (1000*oneReBar)/(Asxminus);
            spacing = Math.min(3*depth, spacing);
            spacing = Math.min(spacing, 300);
            addResult("Area of reinforcement required Asx negative (mm)", Asxminus, "");
            addResult("Spacing for Asx negative (mm)", spacing, "");
        }else{
            //can we increase bar size and cover?
            cover += 10;
            bar += 2;
            designTwoWay();
            //keeps iterating until it passes, we should report to the user on iteration though
        }
        
        //positive msx moment
        k = Msxplus * Math.pow(10, 6)/(1000 * Math.pow(depth, 2)*Fcu);
        while(k >=  0.156){
            depth =+ 0.1*depth;
            k = Msxminus * Math.pow(10, 6)/(1000 * Math.pow(depth, 2)*Fcu);
        }
        if(k < 0.156){
            double z = 0.5 + Math.sqrt(0.25 - k/0.9);
            z = Math.min(z, 0.95);
            double Asxplus = Msxplus * Math.pow(10, 6)/(0.87*Fy*z*depth);
            Asxplus = Math.max(1.3*thickness, Asxplus);
            Asxplus = Math.min(40*thickness, Asxplus);
            
            double oneReBar = (Math.PI*Math.pow(bar, 2))/4;
            double spacing = (1000*oneReBar)/(Asxplus);
            spacing = Math.min(3*depth, spacing);
            spacing = Math.min(spacing, 300);
            addResult("Area of reinforcement required Asx Positive (mm)", Asxplus, "");
            addResult("Spacing for Asx Positive (mm)", spacing, "");
        }else{
            //can we increase bar size and cover?
            cover += 10;
            bar += 2;
            designTwoWay();
            //keeps iterating until it passes, we should report to the user on iteration though
        }
        //negative msy moment
        k = Msyminus * Math.pow(10, 6)/(1000 * Math.pow(depth, 2)*Fcu);
        while(k >=  0.156){
            depth =+ 0.1*depth;
            k = Msxminus * Math.pow(10, 6)/(1000 * Math.pow(depth, 2)*Fcu);
        }
        if(k < 0.156){
            double z = 0.5 + Math.sqrt(0.25 - k/0.9);
            z = Math.min(z, 0.95);
            double Asyminus = Msyminus * Math.pow(10, 6)/(0.87*Fy*z*depth);
            Asyminus = Math.max(1.3*thickness, Asyminus);
            Asyminus = Math.min(40*thickness, Asyminus);
            
            double oneReBar = (Math.PI*Math.pow(bar, 2))/4;
            double spacing = (1000*oneReBar)/(Asyminus);
            spacing = Math.min(3*depth, spacing);
            spacing = Math.min(spacing, 300);
            addResult("Area of reinforcement required Asy negative (mm)", Asyminus, "");
            addResult("Spacing for Asy negative (mm)", spacing, "");
        }else{
            //can we increase bar size and cover?
            cover += 10;
            bar += 2;
            designTwoWay();
            //keeps iterating until it passes, we should report to the user on iteration though
        }   
        //positive msy moment
        k = Msyplus * Math.pow(10, 6)/(1000 * Math.pow(depth, 2)*Fcu);
        while(k >=  0.156){
            depth =+ 0.1*depth;
            k = Msxminus * Math.pow(10, 6)/(1000 * Math.pow(depth, 2)*Fcu);
        }
        if(k < 0.156){
            double z = 0.5 + Math.sqrt(0.25 - k/0.9);
            z = Math.min(z, 0.95);
            double Asyplus = Msyplus * Math.pow(10, 6)/(0.87*Fy*z*depth);
            Asyplus = Math.max(1.3*thickness, Asyplus);
            Asyplus = Math.min(40*thickness, Asyplus);
            
            double oneReBar = (Math.PI*Math.pow(bar, 2))/4;
            double spacing = (1000*oneReBar)/(Asyplus);
            spacing = Math.min(3*depth, spacing);
            spacing = Math.min(spacing, 300);
            addResult("Area of reinforcement required Asy Positive (mm)", Asyplus, "");
            addResult("Spacing for Asy Positive (mm)", spacing, "");
            addResult("Suitable Thickness (mm)", thickness, "");
            addResult("Suitable Depth (mm)", depth, "");
        }else{
            //can we increase bar size and cover?
            cover += 10;
            bar += 2;
            designTwoWay();
            //keeps iterating until it passes, we should report to the user on iteration though
        }
    }
    
    double[] betaFunc(String type, double lx, double ly){
        double[] beta = new double[]{0.0d,0.0d,0.0d,0.0d};
        double r = ly/lx;
        switch(type){
            
            case "interior":
                beta[0] = -0.0083*Math.pow(r,4)+0.0616*Math.pow(r,3)-0.1875*Math.pow(r,2)+0.2873*r-0.1221;
                beta[1] = 0.0038*Math.pow(r,4)-0.008*Math.pow(r,3)-0.0305*Math.pow(r,2)+0.1152*r-0.0565;
                beta[2] = 0.032;
                beta[3] = 0.024;
                break;
            case "one short discontinous":
                beta[0]=0.0089*Math.pow(r,4)-0.0425*Math.pow(r,3)+0.0491*Math.pow(r,2)+0.044*r-0.0209;
                beta[1]=-0.0117*Math.pow(r,4)+0.0813*Math.pow(r,3)-0.02192*Math.pow(r,2)+0.02849*r-0.1063;
                beta[2]=0.037;
                beta[3]=0.028;
                break;
            case "one long discontinous":
                beta[0]=-0.0259*Math.pow(r,4)+0.1785*Math.pow(r,3)-0.482*Math.pow(r,2)+0.634*r-0.2654;
                beta[1]=0.0101*Math.pow(r,4)-0.05*Math.pow(r,3)+0.0606*Math.pow(r,2)-0.0543*r-0.045;
                beta[2]=0.037;
                beta[3]=0.028;
                break;
            case "two adjacent discontinous":
                beta[0]=-0.0405*Math.pow(r,4)+0.2666*Math.pow(r,3)-0.675*Math.pow(r,2)+0.8128*r-0.3168;
                beta[1]=0.0042*Math.pow(r,4)-0.0144*Math.pow(r,3)-0.0146*Math.pow(r,2)+0.1161*r-0.0551;
                beta[2]=0.045;
                beta[3]=0.034;
                break;
            case "two short discountinous":
                beta[0]=-0.0063*Math.pow(r,4)+0.0444*Math.pow(r,3)-0.1298*Math.pow(r,2)+0.1975*r-0.0599;
                beta[1]=0.0078*Math.pow(r,4)-0.035*Math.pow(r,3)+0.0376*Math.pow(r,2)+0.0345*r-0.0107;
                beta[2]=0;
                beta[3]=0.034;
                break;
            case "two long discountinous":
                beta[0]=0;
                beta[1]=0.0297*Math.pow(r,4)+0.2069*Math.pow(r,3)-0.5682*Math.pow(r,2)+0.7674*r-0.3424;
                beta[2]=0.045;
                beta[3]=0.034;
                break;
            case "one long continous":
                beta[0]=-0.0276*Math.pow(r,4)+0.1903*Math.pow(r,3)-0.5034*Math.pow(r,2)+0.6334*r-0.2357;
                beta[1]=-0.018*Math.pow(r,4)+0.0267*Math.pow(r,3)-0.11*Math.pow(r,2)+0.2129*r-0.081;
                beta[2]=0;
                beta[3]=0.044;
                break;
            case "one short continous":
                beta[0]=0;
                beta[1]=-0.0356*Math.pow(r,4)+0.0242*Math.pow(r,3)-0.6434*Math.pow(r,2)+0.8291*r-0.3505;
                beta[2]=0.058;
                beta[3]=0.044;
                break;
            default:  //default is four edges discontinuos
                beta[0]=0;
                beta[1]=-0.0307*Math.pow(r,4)+0.2121*Math.pow(r,3)-0.5699*Math.pow(r,2)+0.7408*r-0.2975;
                beta[2]=0;
                beta[3]=0.056;
                break;
                
        }
        return beta;
}
    
    TabledModel<String> sresult = new TabledModel<>();
}

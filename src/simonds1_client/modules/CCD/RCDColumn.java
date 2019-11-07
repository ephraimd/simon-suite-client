/*
 A Pretty cool Source File on a God Blessed day!.
 */
package simonds1_client.modules.CCD;

import simonds1_client.modules.CSLB.*;
import java.util.HashMap;
import simonds1.core.transport.DataBox;
import simonds1.core.transport.TabledModel;
import simonds1_client.ui.shapes.CSlab;

/**
 *
 * @author Ephrahim Adedamola <olagokedammy@gmail.com>
 */
public final class RCDColumn {

    private double load; //axial load on column (kN)
    private double momentX; //moment about the x axis (kNm)
    private double momentY; //moment about the y axis (kNm)
    private double length; //length of the column along x axis (mm)
    private double breadth; //breadth of the column along the y axis (mm)
    private double Fcu; //concrete strength (N/mm2)
    private double Fy; //steel yield strength (N/mm2)
    private int bar; //preferred diameter of reBar (mm)
    private int cover; //provides cover to the column reinforcement (mm)
    private boolean isBraced; //stores the bracing condition for the column 
    private double height;  //clear height of the column (mm)
    private int topCondition; //the top end condition of the column (1,2,3,4)
    private int bottomCondition; //the bottom end condition of the column (1,2,3)
    
    private double momentXBottom; //stores bottom moment when dealing with slenderness (kNm)
    private double momentYBottom; //same as above (kNm)
    
    //other variables
    private double effectiveHeight;
    private boolean isShort;
    

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
        String restr = "The Results are as follows";
        DataBox payLoad = new DataBox();
        payLoad.payload.put("slab_table", sresult);
        payLoad.stringPayload.put("result_notif", restr);
        
        return payLoad;
    }
    public RCDColumn(CSlab node) {
        getData(node);
        design();
    }

    public void getData(CSlab Node) {
        sresult.addColumnKey("Result");
        sresult.addColumnKey("Value");
        sresult.addColumnKey("Comment");
        
        try {
            HashMap<String, String> stringProps = Node.stringProps;
            HashMap<String, Number> numberProps = Node.numberProps;
            load = numberProps.get("load").doubleValue();
            Fy = Integer.valueOf(stringProps.get("fy").replace("S",""));
            length = numberProps.get("length").doubleValue();
            breadth = numberProps.get("breadth").doubleValue();
            momentX = numberProps.get("momentx").doubleValue();
            momentY = numberProps.get("momenty").doubleValue();
            isBraced = stringProps.get("isbraced").toLowerCase().equals("true");
            height = numberProps.get("height").doubleValue();
            topCondition = numberProps.get("tc").intValue();
            bottomCondition = numberProps.get("bc").intValue();
            Fcu = numberProps.get("fcu").doubleValue();
            cover = numberProps.get("cover").intValue();
            bar = numberProps.get("bar").intValue();
            momentXBottom = numberProps.get("mxb").doubleValue();
            momentYBottom = numberProps.get("myb").doubleValue();
            //System.out.printf("fcu:%s, fy:%s, bc:%s, lx:%s, ly:%s, gk:%s, qk:%s, tita:%s \n",
            //        fcu, fy, bc, lx, ly, gk, qk, tita);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            //data.setError("A serious issue occurred while processing your request. Please Contact Developer.", Flags.MODULE_ERROR);
            //throw new SimonException("Failed to recieve valid Beam Design parameters!");
        }
    }

    public void design(){
        //column sizing
        double b = Math.min(length, breadth);
        double h = Math.max(length, breadth);
        //addResult("reinforcement bar size", bar, "");
        if(height > 60 * b){
//            soln = true;
            b = height / 60;
            if(b < 150)
                b = 150;
            h = 1.2 * b;         
        }
        if(isBraced == false && topCondition == 4){
            double limit = 100 * Math.pow(b, 2) / h;
            if(height > limit){
//                soln = true;
                b = Math.sqrt(h * height /100);
                if(b < 150)
                    b = 150;
                h = 1.2*b;
            }
        }
        length = h;
        breadth = b;
        
        //calculate effective height
        double beta = getBeta();
        effectiveHeight = beta * height;
        
        //classifying column to short or slender
        double ratio = effectiveHeight / Math.min(length, breadth);
        isShort = false;
        if(isBraced && ratio < 15){
            isShort = true;
        }
        else if(!isBraced && ratio < 10){
            isShort = true;
        }       
        if(isShort && momentX == 0 && momentY == 0){
            //designShortAxial();
            addResult("Column Type", "SHORT Axially loaded", "");
            double concreteArea = length * breadth;
            double area = (load*Math.pow(10, 3) - 0.4 * Fcu * concreteArea)/(0.7*Fy - 0.4 * Fcu);

            //checking percentage reinforcement
            double minArea = 0.4*length*breadth/100;
            double maximumArea = 6*length*breadth/100;
            area = Math.max(minArea, area);
            area = Math.min(area, maximumArea);

            addResult("Area of reinforcement required", area, "");

            //calculate number of bars
            double oneReBar = (Math.PI*Math.pow(this.bar, 2))/4;
            double number = area/oneReBar;
            int num = (int) Math.ceil(number);
            num = Math.max(num, 4);
            addResult("No. of reinforcement bars required", num, "");
        }else if(isShort && (momentX == 0 || momentY == 0)){
            //designShortUniAxial();
            double moment;
            
            if(momentX == 0){
                moment = momentY;
                b = length;
                h = breadth;
            }else{
                moment = momentX;
                b = breadth;
                h = length;
            }
            
            double momentAxis = (moment *Math.pow(10, 6)) / (b * Math.pow(h, 2));
            double depth = h - cover - bar/2;
            double depthRatio = depth / h;
            double loadAxis = load / (b * h);
            
            addResult("Column Type", "SHORT Unaxially loaded", "");
            addResult("Moment Axis (M / bh^2)", momentAxis, "");
            addResult("Depth Ratio (d/h)", depthRatio, "");
            addResult("Load Axis (N/bh)", loadAxis, "");
            
            //calculating area of steel required
//            double k = (moment *Math.pow(10, 6))/b*Math.pow(depth,2)*Fcu;
//            double z = depth*(0.5+sqrt(0.25-k/0.9));
//            double area = (load - 0.25*Fcu*length*breadth)/0.87*Fy + (moment *Math.pow(10, 6))/(0.87*Fy*z);
            //calc area
            double area = 0.4*h*b/100;
            double Nu = 0.45*Fcu*b*depth/2 + Fy*area;
            double Mu = 0.45*Fcu*b*(depth/2)*((h/2)-(depth/4))+Fy*(area/2)*((h/2)-cover) + Fy*(area/2)*(depth - (h/2));
            double check = (load*Math.pow(10, 3)/Nu) + ((moment*Math.pow(10, 6))/Mu);
            
            while(check >= 1){
                area = area * 1.1;
                Nu = 0.45*Fcu*b*depth/2 + Fy*area;
                Mu = 0.45*Fcu*b*(depth/2)*((h/2)-(depth/4))+Fy*(area/2)*((h/2)-cover) + Fy*(area/2)*(depth - (h/2));
                check = (load*Math.pow(10, 3)/Nu) + ((moment*Math.pow(10, 6))/Mu);
            }
            
            //checking percentage reinforcement
            double minArea = 0.4*length*breadth/100;
            double maximumArea = 6*length*breadth/100;
            area = Math.max(minArea, area);
            area = Math.min(area, maximumArea);
            
            //calculate number of bars
            double oneReBar = (Math.PI*Math.pow(bar, 2))/4;
            double number = area/oneReBar;
            int num = (int) Math.ceil(number);
                        
            addResult("Area of reinforcement required", area, "");
            addResult("No. of reinforcement bars required", num, "");
        }else if(isShort && momentX != 0 && momentY != 0){
//            designShortBiAxial();
            double hprime = length - cover - bar/2;
            double bprime = breadth - cover - bar/2;
            double momentXratio = momentX / hprime;
            double momentYratio = momentY / bprime;
            double moment;


            //increasing moment about one axis
            double loadModifier = load / (breadth * length * Fcu);            
            if(momentXratio > momentYratio){
                double beta2 = getBeta2(loadModifier);                
                h = length;
                b = breadth;
                moment = momentX + beta2 * momentY * (b/h);
    //                System.out.println("momentXratio bigger");
            }else{
                double beta2 = getBeta2(loadModifier);
                h = breadth;
                b = length;
                moment = momentY + beta2 * momentX *(b/h);

            }

            double momentAxis = (moment *Math.pow(10, 6)) / (b * Math.pow(h, 2));
            double depth = h - cover - bar/2;
            double depthRatio = depth/h;         
            double loadAxis = (load * 1000) / (b * h);

            addResult("Column Type", "SHORT Biaxial", "");
            addResult("Moment Axis (M / bh^2)", momentAxis, "");
            addResult("Depth Ratio (d/h)", depthRatio, "");
            addResult("Load Axis (N/bh)", loadAxis, "");

            //get reinforcement area required
//            double k = (moment *Math.pow(10, 6))/b*Math.pow(depth,2)*Fcu;
//            double z = depth*(0.5+sqrt(0.25-k/0.9));
//            double area = (load - 0.25*Fcu*length*breadth)/0.87*Fy + (moment *Math.pow(10, 6))/(0.87*Fy*z);
            
            //calc area
            double area = 0.4*h*b/100;
            double Nu = 0.45*Fcu*b*depth/2 + Fy*area;
            double Mu = 0.45*Fcu*b*(depth/2)*((h/2)-(depth/4))+Fy*(area/2)*((h/2)-cover) + Fy*(area/2)*(depth - (h/2));
            double check = (load*Math.pow(10, 3)/Nu) + ((moment*Math.pow(10, 6))/Mu);
            
            while(check >= 1){
                area = area * 1.1;
                Nu = 0.45*Fcu*b*depth/2 + Fy*area;
                Mu = 0.45*Fcu*b*(depth/2)*((h/2)-(depth/4))+Fy*(area/2)*((h/2)-cover) + Fy*(area/2)*(depth - (h/2));
                check = (load*Math.pow(10, 3)/Nu) + ((moment*Math.pow(10, 6))/Mu);
            }

            //checking percentage reinforcement
            double minArea = 0.4*length*breadth/100;
            double maximumArea = 6*length*breadth/100;
            area = Math.max(minArea, area);
            area = Math.min(area, maximumArea);

            //calculate number of bars
            double oneReBar = (Math.PI*Math.pow(this.bar, 2))/4;
            double number = 1.1*area/oneReBar;
            int num = (int) Math.ceil(number);
            num = Math.max(4, num);

            addResult("Area of reinforcement required", area, "");
            addResult("No. of reinforcement bars required", num, "");
        }
        else if(!isShort){ //design for slender columns
//            designSlender();
            //get the design moments
            //moment due to eccentricity
            double eMoment = load * Math.min((0.05*length), 0.02);

            //moment due to slenderness
            double bprime = breadth - cover - bar/2;
            double beta_ = (1/2000) * Math.pow((effectiveHeight/bprime),2);
            double Nuz = 0.45*Fcu*breadth*height + 0.95*Fy*2*breadth*height/100;
            double Nbal = 0.25*Fcu*breadth*height;
            double K = (Nuz - load)/(Nuz - Nbal);
            K = Math.min(1, K);
            double alpha = beta_ * K * (height/1000);
            double addMoment = load*alpha;

            //moments about x axis
            double m1 = Math.min(momentX, momentXBottom);
            double m2 = Math.max(momentX, momentXBottom);
            double mi = 0.4*m1+0.6*m2;
            mi = Math.max(0.4*m2, mi);

            //select maximum moment about x axis
            double mx = Math.max(m2, (mi + addMoment));
            mx = Math.max((m1 + (addMoment/2)), mx);
            mx = Math.max(mx, eMoment);

            //moments about y axis
            m1 = Math.min(momentY, momentYBottom);
            m2 = Math.max(momentY, momentYBottom);        
            mi = 0.4*m1+0.6*m2;
            mi = Math.max(0.4*m2, mi);

            //select maximum moment about y axis
            double my = Math.max(m2, (mi + addMoment));
            my= Math.max((m1 + (addMoment/2)), my);
            my = Math.max(my, eMoment);


            double hprime = length - cover - bar/2;
            double momentXratio = mx / hprime;
            double momentYratio = my / bprime;
            double moment;

            //increasing moment about one axis
            double loadModifier = load / (breadth * length * Fcu);            
            if(momentXratio > momentYratio){
                double beta2 = getBeta2(loadModifier);                
                h = length;
                b = breadth;
                moment = mx + beta2 * my * (b/h);
            }else{
                double beta2 = getBeta2(loadModifier);
                h = breadth;
                b = length;
                moment = my + beta2 * mx *(b/h);
            }
            double momentAxis = (moment * Math.pow(10, 6)) / (b * Math.pow(h, 2));
            double depth = h - cover - bar/2;
            double depthRatio = depth / h;
            double loadAxis = load / (b * h);

            addResult("Column Type", "SLENDER", "");
            addResult("Moment Axis (M / bh^2)", momentAxis, "");
            addResult("Depth Ratio (d/h)", depthRatio, "");
            addResult("Load Axis (N/bh)", loadAxis, "");

//            double k = (moment * Math.pow(10, 6))/(b*Math.pow(depth,2)*Fcu);
//            double z = 0.5 + Math.sqrt(0.25 - k/0.9);
//            z = Math.min(z, 0.95);
//            double area = (load - 0.25*Fcu*length*breadth)/(0.87*Fy) + (moment * Math.pow(10, 6)) / (0.87*Fy*z*depth);
            
            //calc area
            double area = 0.4*h*b/100;
            double Nu = 0.45*Fcu*b*depth/2 + Fy*area;
            //double Mu = 0.45*Fcu*b*(depth/2)*((h/2)-(depth/4))+Fy*(area/2)*((h/2)-cover);
            double Mu = 0.45*Fcu*b*(depth/2)*((h/2)-(depth/4))+Fy*(area/2)*((h/2)-cover) + Fy*(area/2)*(depth - (h/2));
            double check = (load*Math.pow(10, 3)/Nu) + ((moment*Math.pow(10, 6))/Mu);
            
            while(check >= 1){
                area = area * 1.000001;
                Nu = 0.45*Fcu*b*depth/2 + Fy*area;
                Mu = 0.45*Fcu*b*(depth/2)*((h/2)-(depth/4))+Fy*(area/2)*((h/2)-cover) + Fy*(area/2)*(depth - (h/2));
                check = (load*Math.pow(10, 3)/Nu) + ((moment*Math.pow(10, 6))/Mu);
            }

            //checking percentage reinforcement
            double minArea = 0.4*length*breadth/100;
            double maximumArea = 6*length*breadth/100;
            area = Math.max(minArea, area);
            area = Math.min(area, maximumArea);

            //calculate number of bars
            double oneReBar = (Math.PI*Math.pow(this.bar, 2))/4;
            double number = area/oneReBar;
            int num = (int) Math.ceil(number);

            addResult("Area of reinforcement required", area, "");
            addResult("No. of reinforcement bars required", num, "");
        }
    }     
   
    public double getBeta2(double loadModifier){
        
        double beta = 1;
        if(loadModifier < 0.1){
            beta = 1;
        }
        else if(loadModifier < 0.2){
            beta = 0.88;
        }
        else if(loadModifier < 0.3){
            beta = 0.77;
        }
        else if(loadModifier < 0.4){
            beta = 0.65;
        }
        else if(loadModifier < 0.5){
            beta = 0.53;
        }
        else if(loadModifier < 0.6){
            beta = 0.42;
        }
        else if(loadModifier > 0.6){
            beta = 0.65;
        }
        return beta;
    }
    
    //calculates beta for effective height 
    public double getBeta(){
        double value = 0;
        if(isBraced){
            if(topCondition == 1){
                if(bottomCondition == 1)
                    value = 0.75;
                else if(bottomCondition == 2)
                    value = 0.8;
                else if(bottomCondition == 3)
                    value = 0.9;
            }
            else if(topCondition == 2){
                if(bottomCondition == 1)
                    value = 0.80;
                else if(bottomCondition == 2)
                    value = 0.85;
                else if(bottomCondition == 3)
                    value = 0.95;
            }
            else if(topCondition == 3){
                if(bottomCondition == 1)
                    value = 0.90;
                else if(bottomCondition == 2)
                    value = 0.95;
                else if(bottomCondition == 3)
                    value = 1.0;
            }
        }else{
            if(topCondition == 1){
                if(bottomCondition == 1)
                    value = 1.2;
                else if(bottomCondition == 2)
                    value = 1.3;
                else if(bottomCondition == 3)
                    value = 1.6;
            }
            else if(topCondition == 2){
                if(bottomCondition == 1)
                    value = 1.3;
                else if(bottomCondition == 2)
                    value = 1.5;
                else if(bottomCondition == 3)
                    value = 1.8;
            }
            else if(topCondition == 3){
                if(bottomCondition == 1)
                    value = 1.6;
                else if(bottomCondition == 2)
                    value = 1.8;
                else if(bottomCondition == 3)
                    value = 0;
            }
            else if(topCondition == 4){
                if(bottomCondition == 1)
                    value = 2.2;
                else if(bottomCondition == 2)
                    value = 0;
                else if(bottomCondition == 3)
                    value = 0;
            }
            
        }
        return value;
    }
    
    TabledModel<String> sresult = new TabledModel<>();
}

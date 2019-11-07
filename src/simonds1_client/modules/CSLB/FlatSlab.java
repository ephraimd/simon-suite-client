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
public final class FlatSlab {

    public double fcu, //Concrete Grade (N/mm^2)
            fy, //Steel Grade (N/mm^2)
            bc, //Breadth of Column (mm)
            ly, //long span of slab (m)
            lx, //slab short span (m)
            gk,// dead load (KN/m^2)
            qk,//live load (KN/m^2)
            b, //breadth of beam (mm)
            tita, //reinforcement area
            uls, sls,
            d, v, dPrime, h, l,f,
            csw, msw, pm, nm, csm, msm, csmPrime, msmPrime;
    

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
        String restr = String.format("Designed Slab of depth %smm.", d);
        restr += "The Results are as follows";
        DataBox payLoad = new DataBox();
        payLoad.payload.put("slab_table", sresult);
        payLoad.stringPayload.put("result_notif", restr);
        
        return payLoad;
    }
    public FlatSlab(CSlab node) {
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
            fcu = numberProps.get("fcu").doubleValue();
            fy = Integer.valueOf(stringProps.get("fy").replace("S",""));
            bc = numberProps.get("bc").doubleValue();
            lx = numberProps.get("lx").doubleValue();
            ly = numberProps.get("ly").doubleValue();
            gk = numberProps.get("gk").doubleValue();
            qk = numberProps.get("qk").doubleValue();
            tita = numberProps.get("tita").doubleValue();
            //System.out.printf("fcu:%s, fy:%s, bc:%s, lx:%s, ly:%s, gk:%s, qk:%s, tita:%s \n",
            //        fcu, fy, bc, lx, ly, gk, qk, tita);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            //data.setError("A serious issue occurred while processing your request. Please Contact Developer.", Flags.MODULE_ERROR);
            //throw new SimonException("Failed to recieve valid Beam Design parameters!");
        }
    }

    public void design() {
        b = (lx*1000)/2;
        uls = (1.4 * gk) + (1.6 * qk);
        sls = (1.0 * gk) + (1.0 * qk);
        f = uls*ly*lx;
        double constt = (qk < 10) ? 36 : 33;

        d = (lx *1000)/ constt;
        v = 0.6 * f;
        dPrime = (((-8 * bc) / 24) + Math.sqrt(((64 * Math.pow(bc, 2)) / Math.pow(24, 2)) + ((4 * v) / 24))) / 2;
        d = Math.max(d, dPrime);
        h = d + 50 + (tita / 2);
        bc = bc/1000; //convert to m
        l = lx - (bc / 3);
        csw = l / 4;
        msw = l - csw;
        pm = 0.083 * f * l; // positive
        nm = 0.02 * f * l;
        csm = 0.55 * pm;
        msm = 0.45 * pm;
        csmPrime = nm * 0.75;
        msmPrime = nm * 0.25;
        

        addResult("Slab Depth (mm)", d, "");
        //addResult("Beam width (mm)", b, "");
        designParts("+CSM", csm, false);
        designParts("+MSM", msm, true);
        designParts("-CSM", csmPrime, false);
        designParts("-MSM", msmPrime, false);

    }

    public void designParts(String title, double M, boolean checkDefl) {
        title = String.format("[%s] ", title);
        M = M * Math.pow(10, 6); //convert to Nmm
        
        double k = M / (b * Math.pow(d, 2) * fcu);
        double mf, As, AsProv;
//        System.out.println("before loop is moment: "+ M +" depth: "+d +" k: "+ k);
        while (k > 0.156) { //wait till k is satisfactory
            d = 1.1 * d;
            k = M / (b * Math.pow(d, 2) * fcu);
        }

        //singly reinforced
        //lever arm
        double la = 0.5 + Math.sqrt(0.25 - (k / 0.9));
        la = Math.min(0.95, la);

//        System.out.println("after loop moment: "+ M +" depth: "+d +" k: "+ k + "la: "+la);
        
        As = M / (0.95 * fy * la * d);
        As = Math.max(0.0013 * b * h, As);
        As = Math.min(0.04 * h * b, As);
        addResult(title + "Area of Steel Required (mm^2)", As, "");

        //provide reinforcement
        double reBarArea = (Math.PI * Math.pow(tita, 2)) / 4;
        long noBars = Math.round((1.1 * As) / reBarArea);
        noBars = noBars % 2 != 0 ? (noBars + 1) : noBars;
        AsProv = noBars * reBarArea;
        addResult(title + "(T) Area of Steel Provided (mm^2)", AsProv, "");
        addResult(title + "(T) Number of Reinforcement", String.format("%sY%s", noBars, tita), "");

        if (checkDefl) {
            //deflection
            double fs = (2 * fy * As) / (3 * AsProv);
            mf = 0.55 + ((477 - fs) / (120 * (0.9 + (M / (b * Math.pow(d, 2)))))); //yet again, whats b? bf?
            mf = Math.min(mf, 2.0);
            //basic ratio
            double br = 23;
            double l_d = mf * br;

            if ((l / d) > l_d) {
                addResult(title + "Deflection Check", "Failed", "Lx/d > Limiting l/d");
            } else {
                addResult(title + "Deflection Check", "Passed", "");

            }
        }
    }
    
    TabledModel<String> sresult = new TabledModel<>();
}

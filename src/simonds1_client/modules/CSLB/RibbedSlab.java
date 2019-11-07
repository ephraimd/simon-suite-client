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
public final class RibbedSlab {

    public double Ly, Lx, Th, Rw, Rd, LL, Bw, Ladd, tita, C, Fcu, Fy;
    private String beamType; //cns or ss or ctl

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
        String restr = "The Design Results are as follows:";
        DataBox payLoad = new DataBox();
        payLoad.payload.put("slab_table", sresult);
        payLoad.stringPayload.put("result_notif", restr);

        return payLoad;
    }

    public RibbedSlab(CSlab node) {
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
            Fcu = numberProps.get("fcu").doubleValue();
            Fy = Integer.valueOf(stringProps.get("fy").replace("S", ""));
            Th = numberProps.get("th").doubleValue();
            Lx = numberProps.get("lx").doubleValue();
            Ly = numberProps.get("ly").doubleValue();
            Rw = numberProps.get("rw").doubleValue();
            Rd = numberProps.get("rd").doubleValue();
            tita = numberProps.get("tita").doubleValue();
            LL = numberProps.get("ll").doubleValue();
            Bw = numberProps.get("bw").doubleValue();
            Ladd = numberProps.get("ladd").doubleValue();
            C = numberProps.get("c").doubleValue();
            beamType = stringProps.get("beam-type");
            //System.out.printf("fcu:%s, fy:%s, bc:%s, lx:%s, ly:%s, gk:%s, qk:%s, tita:%s \n",
            //        fcu, fy, bc, lx, ly, gk, qk, tita);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            //data.setError("A serious issue occurred while processing your request. Please Contact Developer.", Flags.MODULE_ERROR);
            //throw new SimonException("Failed to recieve valid Beam Design parameters!");
        }
    }

    public void design() {
        //load calc
        double Dr = Rw + Bw;
        /*                      */
        double Bl = 0;//?? where did you get to put Bl below?
        double udl = 1.4 * (Th * Dr * 24 + Rw * Rd * 24 + Dr * Ladd + Bl) + 1.6 * Dr * LL; //KN/m^2)
        //moment calc
        double Sp = Lx + 0.225;
        double M = 0.125 * Math.pow(Sp, 2) * udl; //KNm
        M *= Math.pow(10, 6); //convert to N/mm
        //parameters calc
        double Bf = Math.min(Bw, Rw + 0.2 * Lx); //i put Bl in place of Bf...Whats Bf here??
        double d = Rd + Th - C - tita;
        double k = (M * Math.pow(10, 6)) / (Bf * Math.pow(d, 2) * Fcu);
        if (k < 0.156) {
            double la = 0.5 + Math.sqrt(0.25 - (k / 0.9));
            la = Math.min(0.95, la);
            //area of reinforcement
            double As = M / (0.95 * Fy * la * d);
            As = Math.max(0.0013 * Rw * Rd, As);
            As = Math.min(0.04 * Rw * Rd, As);
            addResult("Area of Reinfrocement Required", String.format("%s", As), "");
            //provide Reinforcement
            double reBarArea = (Math.PI * Math.pow(tita, 2)) / 4;
            int noBars = (int) Math.ceil((1.1 * As) / reBarArea);
            double AsProv = noBars * reBarArea;
            addResult("Number of Reinforcement ", String.format("%s", noBars), "");
            addResult("Bar Diameter Size ", String.format("%s", reBarArea), "");
            addResult("Area of Steel Provided ", String.format("%s", AsProv), "");

            //deflection check
            double Fs = (2 * Fy * As) / (3 * AsProv);
            double mf = 0.55 + ((477 - Fs) / (120 * (0.9 + (M / (Bf * Math.pow(d, 2)))))); //yet again, whats b? bf?
            mf = Math.min(mf, 2.0);
            //basic ratio
            double br = beamType.equals("Continous") ? 26 : beamType.equals("Simply Supported") ? 20 : 7;
            double l_d = mf * br;

            if (Lx / d > l_d) {
                addResult("Deflection", "Failed ", "Lx/d > Limiting l/d. Try again and Modifiy your inputs");
                //Util.printDesignData(results);
                //return;
            } else {
                addResult("Deflection", "Passed", "");
                //shear check
                double vf = 0.5 * udl * Sp; //shearing force
                double vs = vf / (Bf * d); // shearing stress
                if (vf > 0.8 * Math.sqrt(Fcu)) {
                    addResult("Shear", "Failed ", "Consider increasing your depth");
                    //Util.printDesignData(results);
                } else {
                    double q = Math.min(100 * As / Bf * d, 3.0);
                    double r = Math.max(400 / d, 1.0);
                    double vc = 0.79 * Math.pow(q, 0.3333334) * r / 1.25;
                    if (vc < 0.5 * vc) {
                        addResult("Shear Links", "None", "No Shear Links Required");
                        //Util.printDesignData(results);
                    } else {
                        double Asv, Sv;
                        if ((0.5 * vc) < vf && vf < (vc + 0.4)) {
                            Asv = 2 * (Math.PI * Math.pow(tita, 2)) / 4;  //there is no tita3 so i used tita
                            Sv = Asv * 0.95 * Fy / 0.4 * Bf;
                        } else {
                            Asv = 2 * (Math.PI * Math.pow(tita, 2)) / 4;  //there is no tita3 so i used tita
                            Sv = Asv * 0.95 * Fy / Bf * (vf - vc);
                        }
                        addResult("Area of Steel Stirrup", String.format("%s", Asv), "");
                        addResult("Link Spacing", String.format("%s", Sv), "");
                        //Util.printDesignData(results);
                    }
                    //double vc = 0.79
                }
            }
        } else {
            addResult("K", String.format("%s", k), "K > 0.156. Consider increasing Rib Depth");
            //Util.printDesignData(results);
        }
    }

    TabledModel<String> sresult = new TabledModel<>();
}

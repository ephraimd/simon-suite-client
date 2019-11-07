
package simonds1_client.modules;

/**
 *
 * @author ADEDAMOLA
 */
public final class ElementProfile {

    public String name = "Profile 1";
    public double youngModulus = 0.0,
            mmi = 0.0, //moment of inertia
            crsArea = 0.0; //cross sectional area

    public ElementProfile(String name) {
        this.name = name;
    }

    public ElementProfile(String name, double youngModulus, double mmi, double crsArea) {
        this.name = name;
        this.setProfileVars(youngModulus, mmi, crsArea);
    }

    public boolean setProfileVars(double youngModulus, double mmi, double crsArea) {
        this.youngModulus = youngModulus;
        this.mmi = mmi;
        this.crsArea = crsArea;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}

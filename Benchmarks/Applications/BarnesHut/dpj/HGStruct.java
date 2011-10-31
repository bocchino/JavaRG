/**
 * Class for temporary storage of computed and required fields for hackgrav
 * @author Rakesh Komuravelli
 */

public class HGStruct {
    
    /* Node to skip in force evaluation */
    Node pskip in R;
    
    /* point at which to evaluate field */
    Vector pos0 in R;
    
    /* computed potential at pos0       */
    double phi0 in R;
    
    /* computed acceleration at pos0    */
    Vector acc0 in R;
    
    /* intermediate computation for gravsub */
    Vector ai in R;

    /* intermediate computation for gravsub */
    Vector dr in R;
    
    /**
     * Constructor
     */
    public HGStruct() {
        this.pskip = null;
        this.pos0  = new Vector();
        this.phi0  = 0;
        this.acc0  = new Vector();
        this.dr    = new Vector();
        this.ai    = new Vector();
    }
}

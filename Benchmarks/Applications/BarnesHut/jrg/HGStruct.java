/**
 * Class for temporary storage of computed and required fields for hackgrav
 * @author Rakesh Komuravelli
 */

public class HGStruct {
    
    /* Node to skip in force evaluation */
    Node pskip;
    
    /* point at which to evaluate field */
    Vector pos0;
    
    /* computed potential at pos0       */
    double phi0;
    
    /* computed acceleration at pos0    */
    Vector acc0;
    
    /* intermediate computation for gravsub */
    Vector ai;

    /* intermediate computation for gravsub */
    Vector dr;
    
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

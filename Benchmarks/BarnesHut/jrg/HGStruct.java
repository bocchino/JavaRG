/**
 * Class for temporary storage of computed and required fields for hackgrav
 * @author Rakesh Komuravelli
 */

public class HGStruct<region R> {

    /* Node to skip in force evaluation */
    Node pskip in R;
    
    /* point at which to evaluate field */
    final unique Vector<R> pos0 in R;
    
    /* computed potential at pos0       */
    double phi0 in R;
    
    /* computed acceleration at pos0    */
    final unique Vector<R> acc0 in R;
    
    /* intermediate computation for gravsub */
    final unique Vector<R> ai in R;

    /* intermediate computation for gravsub */
    final unique Vector<R> dr in R;
    
    /**
     * Constructor
     */
    public HGStruct() pure {
        this.pskip = null;
        this.pos0  = new Vector<R>();
        this.phi0  = 0;
        this.acc0  = new Vector<R>();
        this.dr    = new Vector<R>();
        this.ai    = new Vector<R>();
    }
}

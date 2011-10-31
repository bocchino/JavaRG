import java.util.Formatter;

/**
 * Vector operations
 */
public class Vector {

    public final double[] elts in R = 
	new double[Constants.NDIM];

    public void CLRV() {
	for (int i = 0; i < Constants.NDIM; ++i)
	    elts[i] = 0.0;
    }

    public void UNITV(int coord) {
	for (int i = 0; i < Constants.NDIM; ++i)
	    elts[i] = (coord == i) ? 1.0 : 0.0;
    }

    public void SETV(Vector u) {
	for (int i = 0; i < Constants.NDIM; i++) 					
	    elts[i] = u.elts[i]; 						
    }
    
    public void ADDV(Vector u, Vector w) {
	for (int i = 0; i < Constants.NDIM; ++i)
	    elts[i] = u.elts[i] + w.elts[i];					
    }

    public void SUBV(Vector u, Vector w) {
	for (int i = 0; i < Constants.NDIM; i++)					
	    elts[i] = u.elts[i] - w.elts[i];					
    }

    /**
     *  MULtiply Vector by Scalar 
     */
    public void MULVS(Vector u, double s) {
	int i;							
	for (i = 0; i < Constants.NDIM; i++)					
	    elts[i] = u.elts[i] * s;					
    }

    public void DIVVS(Vector u, double s) {
	int i;					       	
	for (i = 0; i < Constants.NDIM; i++)					
	    elts[i] = u.elts[i] / s;					
    }


    public double DOTVP(Vector u) {
	int i;							
	double s = 0.0;								
	for (i = 0; i < Constants.NDIM; i++)					
	    s += elts[i] * u.elts[i];					
	return s;
    }
     
    public void ABSV(double s) {
	double tmp;                                                
	int i;							
	tmp = 0.0;								
	for (i = 0; i < Constants.NDIM; i++)					
	    tmp += elts[i] * elts[i];					
	s = Math.sqrt(tmp);                                                   
    }
    
    public void DISTV(double s, Vector u) {
	double tmp;                                                
	int i;							
	tmp = 0.0;								
	for (i = 0; i < Constants.NDIM; i++)					
	    tmp += (u.elts[i]-elts[i]) * (u.elts[i]-elts[i]);		        
	s = Math.sqrt(tmp);                                                   
    }

    public void CROSSVP(Vector u, Vector w) {
	elts[0] = u.elts[1]*w.elts[2] - u.elts[2]*w.elts[1];				
	elts[1] = u.elts[2]*w.elts[0] - u.elts[0]*w.elts[2];				
	elts[2] = u.elts[0]*w.elts[1] - u.elts[1]*w.elts[0];				
    }

    public void INCADDV(Vector u) {
	int i;
	for (i = 0; i < Constants.NDIM; i++)
	    elts[i] += u.elts[i];                                             
    }

    public void INCSUBV(Vector u) {
	int i;                                                    
	for (i = 0; i < Constants.NDIM; i++)                                       
	    elts[i] -= u.elts[i];                                             
    }
    
    public void INCMULVS(double s) {
	int i;                                                    
	for (i = 0; i < Constants.NDIM; i++)                                       
	    elts[i] *= s;                                                 
    }

    public void INCDIVVS(double s) {
	int i;                                                    
	for (i = 0; i < Constants.NDIM; i++)                                       
	    elts[i] /= s;                                                 
    }

    public void SETVS(double s) {
	int i;							
	for (i = 0; i < Constants.NDIM; i++)					
	    elts[i] = s;							
    }

    public void ADDVS(Vector u, double s) {
	int i;
	for (i = 0; i < Constants.NDIM; i++)			
	    elts[i] = u.elts[i] + s;					
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	Formatter f = new Formatter(sb);
	sb.append("<");
	f.format("%f", elts[0]);
	for (int i = 1; i < elts.length; ++i) {
	    f.format(",%f", elts[i]);
	}
	sb.append(">");
	return sb.toString();
    }

    public boolean EQUAL(Vector v) {
	for (int i = 0; i < Constants.NDIM; ++i)
	    if (elts[i] != v.elts[i]) return false;
	return true;
    }
}

import java.util.Formatter;

/**
 * Vector operations
 */
public class Vector<region R> {

    public final unique DoubleArray<R> elts in R = 
	new DoubleArray<R>(Constants.NDIM);

    public void CLRV() 
	writes R via this 
    {
	for (int i = 0; i < Constants.NDIM; ++i)
	    elts[i] = 0.0;
    }

    public void UNITV(int coord) 
        writes R via this
    {
	for (int i = 0; i < Constants.NDIM; ++i)
	    elts[i] = (coord == i) ? 1.0 : 0.0;
    }

    public <region Ru>void SETV(Vector<Ru> u) 
	reads Ru via u writes R via this
    {
	for (int i = 0; i < Constants.NDIM; i++) 					
	    elts[i] = u.elts[i]; 						
    }
    
    public <region Ru,Rw>void ADDV(Vector<Ru> u, Vector<Rw> w) 
	reads Ru via u, Rw via w
	writes R via this
    {
	for (int i = 0; i < Constants.NDIM; ++i)
	    elts[i] = u.elts[i] + w.elts[i];					
    }

    public <region Ru,Rw>void SUBV(Vector<Ru> u, Vector<Rw> w) 
	reads Ru via u, Rw via w
	writes R via this
    {
	for (int i = 0; i < Constants.NDIM; i++)					
	    elts[i] = u.elts[i] - w.elts[i];					
    }

    /**
     *  MULtiply Vector by Scalar 
     */
    public <region Ru>void MULVS(Vector<Ru> u, double s) 
	reads Ru via u
	writes R via this
    {
	int i;							
	for (i = 0; i < Constants.NDIM; i++)					
	    elts[i] = u.elts[i] * s;					
    }

    public <region Ru>void DIVVS(Vector<Ru> u, double s) 
	reads Ru via u
	writes R via this
    {
	int i;					       	
	for (i = 0; i < Constants.NDIM; i++)					
	    elts[i] = u.elts[i] / s;					
    }


    public <region Ru>double DOTVP(Vector<Ru> u) 
	reads Ru via u
	writes R via this
    {
	int i;							
	double s = 0.0;								
	for (i = 0; i < Constants.NDIM; i++)					
	    s += elts[i] * u.elts[i];					
	return s;
    }
     
    public void ABSV(double s) 
	reads R via this
    {
	double tmp;                                                
	int i;							
	tmp = 0.0;								
	for (i = 0; i < Constants.NDIM; i++)					
	    tmp += elts[i] * elts[i];					
	s = Math.sqrt(tmp);                                                   
    }
    
    public <region Ru>void DISTV(double s, Vector<Ru> u) 
	reads R via this, Ru via this
    {
	double tmp;                                                
	int i;							
	tmp = 0.0;								
	for (i = 0; i < Constants.NDIM; i++)					
	    tmp += (u.elts[i]-elts[i]) * (u.elts[i]-elts[i]);		        
	s = Math.sqrt(tmp);                                                   
    }

    public <region Ru,Rw>void CROSSVP(Vector<Ru> u, Vector<Rw> w) 
	reads Ru via u, Rw via w
	writes R via this
    {
	elts[0] = u.elts[1]*w.elts[2] - u.elts[2]*w.elts[1];				
	elts[1] = u.elts[2]*w.elts[0] - u.elts[0]*w.elts[2];				
	elts[2] = u.elts[0]*w.elts[1] - u.elts[1]*w.elts[0];				
    }

    public <region Ru>void INCADDV(Vector<Ru> u) 
	reads Ru via u
	writes R via this
    {
	int i;
	for (i = 0; i < Constants.NDIM; i++)
	    elts[i] += u.elts[i];                                             
    }

    public <region Ru>void INCSUBV(Vector<Ru> u) 
	reads Ru via u
	writes R via this
    {
	int i;                                                    
	for (i = 0; i < Constants.NDIM; i++)                                       
	    elts[i] -= u.elts[i];                                             
    }
    
    public void INCMULVS(double s) 
	reads R via this
    {
	int i;                                                    
	for (i = 0; i < Constants.NDIM; i++)                                       
	    elts[i] *= s;                                                 
    }

    public void INCDIVVS(double s) 
	reads R via this
    {
	int i;                                                    
	for (i = 0; i < Constants.NDIM; i++)                                       
	    elts[i] /= s;                                                 
    }

    public void SETVS(double s) 
	writes R via this
    {
	int i;							
	for (i = 0; i < Constants.NDIM; i++)					
	    elts[i] = s;							
    }

    public <region Ru>void ADDVS(Vector<Ru> u, double s) 
	reads Ru via u
	writes R via this
    {
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

    public <region Rv>boolean EQUAL(Vector<Rv> v) 
	reads Rv via v
	writes R via this
    {
	for (int i = 0; i < Constants.NDIM; ++i)
	    if (elts[i] != v.elts[i]) return false;
	return true;
    }
}


/**
  * Class for defining the results of a task.  Currently, this is simply
  * the Monte Carlo generate rate path.
  *
  * @author H W Yau
  * @version $Revision: 1.8 $ $Date: 1999/02/16 18:53:07 $
  */
public class ToResult implements java.io.Serializable {
  private final String header;
  private final double expectedReturnRate;
  private final double volatility;
  private final double volatility2;
  private final double finalStockPrice;
  private final PathValue pathValue;

  /**
    * Constructor, for the results from a computation.
    *
    * @param header Simple header string.
    * @param pathValue Data computed by the Monte Carlo generator.
    */
  public ToResult(String header, double expectedReturnRate, double volatility, 
		  double volatility2, double finalStockPrice, PathValue pathValue) 
    pure
  {
    this.header=header;
    this.expectedReturnRate = expectedReturnRate;
    this.volatility = volatility;
    this.volatility2 = volatility2;
    this.finalStockPrice = finalStockPrice;
    this.pathValue = pathValue;
  }
  /**
    * Gives a simple string representation of this object.
    *
    * @return String representation of this object.
    */
  public String toString() 
      pure
  {
    return(header);
  }
  //------------------------------------------------------------------------
  // Accessor methods for class ToResult.
  // Generated by 'makeJavaAccessor.pl' script.  HWY.  20th January 1999.
  //------------------------------------------------------------------------
  /**
    * Accessor method for private instance variable <code>header</code>.
    *
    * @return Value of instance variable <code>header</code>.
    */
  public String get_header() 
      pure
  {
    return(this.header);
  }
  /**
    * Accessor method for private instance variable <code>expectedReturnRate</code>.
    *
    * @return Value of instance variable <code>expectedReturnRate</code>.
    */
  public double get_expectedReturnRate()
      pure
  {
    return(this.expectedReturnRate);
  }
  /**
    * Accessor method for private instance variable <code>volatility</code>.
    *
    * @return Value of instance variable <code>volatility</code>.
    */
  public double get_volatility() 
      pure
  {
    return(this.volatility);
  }
  /**
    * Accessor method for private instance variable <code>volatility2</code>.
    *
    * @return Value of instance variable <code>volatility2</code>.
    */
  public double get_volatility2() 
      pure
  {
    return(this.volatility2);
  }
  /**
    * Accessor method for private instance variable <code>finalStockPrice</code>.
    *
    * @return Value of instance variable <code>finalStockPrice</code>.
    */
  public double get_finalStockPrice() 
      pure
  {
    return(this.finalStockPrice);
  }
  /**
    * Accessor method for private instance variable <code>pathValue</code>.
    *
    * @return Value of instance variable <code>pathValue</code>.
    */
  public PathValue get_pathValue() 
      pure
  {
    return(this.pathValue);
  }
  //------------------------------------------------------------------------
}



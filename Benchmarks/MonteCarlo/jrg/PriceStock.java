
/**
  * Class to do the work in the Application demonstrator, in particular
  * the pricing of the stock path generated by Monte Carlo.  The run
  * method will generate a single sequence with the required statistics,
  * estimate its volatility, expected return rate and final stock price
  * value.
  *
  * @author H W Yau
  * @version $Revision: 1.5 $ $Date: 1999/02/16 18:52:15 $
  */
public class PriceStock<region P> extends Universal<P> {

    //------------------------------------------------------------------------
  // Class variables.
  //------------------------------------------------------------------------
  /**
    * Class variable for determining whether to switch on debug output or
    * not.
    */
  public static boolean DEBUG=true;
  /**
    * Class variable for defining the debug message prompt.
    */
  protected static String prompt="PriceStock> ";

  //------------------------------------------------------------------------
  // Instance variables.
  //------------------------------------------------------------------------
  /**
    * The Monte Carlo path to be generated.
    */
  // 
  // put MonteCarloPath object under the region for PriceStock
  // necessary for parallel processing of the outer foreach
  //
  private MonteCarloPath<P> mcPath in P;
  /**
    * String identifier for a given task.
    */
  private String taskHeader in P;
  /**
    * Random seed from which the Monte Carlo sequence is started.
    */
  private long randomSeed in P;
  /**
    * Initial stock price value.
    */
  private double pathStartValue in P;
  /**
    * Object which represents the results from a given computation task.
    */
  private ToResult<P> result in P;
  private double expectedReturnRate in P;
  private double volatility in P;
  private double volatility2 in P;
  private double finalStockPrice in P;
  private double[] pathValue in P;

  //------------------------------------------------------------------------
  // Constructors.
  //------------------------------------------------------------------------
  /**
    * Default constructor.
    */
  public PriceStock() {
    super();
    
    mcPath = new MonteCarloPath<P>();
    set_prompt(prompt);
    set_DEBUG(DEBUG);
  }
  //------------------------------------------------------------------------
  // Methods.
  //------------------------------------------------------------------------
  //------------------------------------------------------------------------
  // Methods which implement the Slaveable interface.
  //------------------------------------------------------------------------
  /**
    * Method which is passed in the initialisation data common to all tasks,
    * and then unpacks them for use by this object.
    *
    * @param obj Object representing data which are common to all tasks.
    */
   public void setInitAllTasks(ToInitAllTasks initAllTasks) {
    mcPath.set_name(initAllTasks.get_name());
    mcPath.set_startDate(initAllTasks.get_startDate());
    mcPath.set_endDate(initAllTasks.get_endDate());
    mcPath.set_dTime(initAllTasks.get_dTime());
    mcPath.set_returnDefinition(initAllTasks.get_returnDefinition());
    mcPath.set_expectedReturnRate(initAllTasks.get_expectedReturnRate());
    mcPath.set_volatility(initAllTasks.get_volatility());
    int nTimeSteps = initAllTasks.get_nTimeSteps();
    mcPath.set_nTimeSteps(nTimeSteps);
    this.pathStartValue = initAllTasks.get_pathStartValue();
    mcPath.set_pathStartValue(pathStartValue);
    mcPath.set_pathValue(new double[nTimeSteps]);
    mcPath.set_fluctuations(new double[nTimeSteps]);
  }
  /**
    * Method which is passed in the data representing each task, which then
    * unpacks it for use by this object.
    *
    * @param obj Object representing the data which defines a given task.
    */
  public <region R>void setTask(ToTask<R> obj) {
      ToTask<R> task = obj;
    this.taskHeader     = task.get_header();
    this.randomSeed     = task.get_randomSeed();
  }
  /**
    * The business end.  Invokes the necessary computation routine, for a
    * a given task.
    */
  // TODO major computation
  // TODO potentially parallelizable
  public void run() {
    try{
      mcPath.computeFluctuationsGaussian(randomSeed);
      mcPath.computePathValue(pathStartValue);
      
      RatePath<P> rateP = new RatePath<P>(mcPath);
      ReturnPath<P> returnP = rateP.getReturnCompounded();
      returnP.estimatePath();
      expectedReturnRate = returnP.get_expectedReturnRate();
      volatility = returnP.get_volatility();
      volatility2 = returnP.get_volatility2();
      
      finalStockPrice = rateP.getEndPathValue();
      pathValue = mcPath.get_pathValue();
    } catch( DemoException demoEx ) {
      errPrintln(demoEx.toString());
    }
  }
  /*
   * Method which returns the results of a computation back to the caller.
   *
   * @return An object representing the computed results.
   */
  public ToResult<P> getResult() {
    String resultHeader = "Result of task with Header="+taskHeader+": randomSeed="+randomSeed+": pathStartValue="+pathStartValue;
    ToResult<P> res = new ToResult<P>(resultHeader,expectedReturnRate,volatility,volatility2,finalStockPrice,pathValue);
    return res;
  }
}

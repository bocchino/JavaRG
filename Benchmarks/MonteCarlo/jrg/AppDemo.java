import java.util.*;
import java.awt.*;
import java.util.concurrent.locks.ReentrantLock;

/**
  * Code, a test-harness for invoking and driving the Applications
  * Demonstrator classes.
  *
  * <p>To do:
  * <ol>
  *   <li>Very long delay prior to connecting to the server.</li>
  *   <li>Some text output seem to struggle to get out, without
  *       the user tapping ENTER on the keyboard!</li>
  * </ol>
  *
  * @author H W Yau
  * @version $Revision: 1.12 $ $Date: 1999/02/16 19:13:38 $
  */
public class AppDemo extends Universal {
  //------------------------------------------------------------------------
  // Class variables.
  //------------------------------------------------------------------------

    region Arrays, Results, TaskR, reductionR;

  public static double JGFavgExpectedReturnRateMC =0.0;
  /**
    * A class variable.
    */
  public static boolean DEBUG=true;
  /**
    * The prompt to write before any debug messages.
    */
  protected static String prompt="AppDemo> ";

  public static final int Serial=1;
  //------------------------------------------------------------------------
  // Instance variables.
  //------------------------------------------------------------------------
  /**
    * Directory in which to find the historical rates.
    */
  private String dataDirname;
  /**
    * Name of the historical rate to model.
    */
  private String dataFilename;
  /**
    * The number of time-steps which the Monte Carlo simulation should
    * run for.
    */
  private int nTimeStepsMC=0;
  /**
    * The number of Monte Carlo simulations to run.
    */
  private int nRunsMC=0;
  /**
    * The default duration between time-steps, in units of a year.
    */
  private double dTime = 1.0/365.0;
  /**
    * Flag to determine whether initialisation has already taken place.
    */
  private boolean initialised=false;
  /**
    * Variable to determine which deployment scenario to run.
    */
  private int runMode;

    private ToTask[] tasks in TaskR;

    private ResultArray results in Results;

  public AppDemo(String dataDirname, String dataFilename, int nTimeStepsMC, int nRunsMC) {
    this.dataDirname    = dataDirname;
    this.dataFilename   = dataFilename;
    this.nTimeStepsMC   = nTimeStepsMC;
    this.nRunsMC        = nRunsMC;
    this.initialised    = false;
    set_prompt(prompt);
    set_DEBUG(DEBUG);
  }
  /**
    * Single point of contact for running this increasingly bloated
    * class.  Other run modes can later be defined for whether a new rate
    * should be loaded in, etc.
    * Note that if the <code>hostname</code> is set to the string "none",
    * then the demonstrator runs in purely serial mode.
    */

  /**
    * Initialisation and Run methods.
    */
    double pathStartValue = 100.0;
    double avgExpectedReturnRateMC in reductionR = 0.0;
    double avgVolatilityMC in reductionR = 0.0;

    ToInitAllTasks initAllTasks in TaskR = null;

    public void initParallel() {
    
      try{
      //
      // Measure the requested path rate.
      // read from the file and store the data in an array
      // rateP contains the entire data as an array
      RatePath rateP = new RatePath(dataDirname, dataFilename);
      // for debugging purpose
      rateP.dbgDumpFields();
      
      ReturnPath returnP = rateP.getReturnCompounded();
   
      returnP.estimatePath();
      returnP.dbgDumpFields();
      
      // get expected return rate and volatility
      double expectedReturnRate = returnP.get_expectedReturnRate();
      double volatility         = returnP.get_volatility();
      
      // Now prepare for MC runs.
      // initialize basic task information with the computed values in ReturnPath    
      initAllTasks = new ToInitAllTasks(returnP, nTimeStepsMC, pathStartValue);
      String slaveClassName = "MonteCarlo.PriceStock";
      //
      // Now create the tasks.
      initTasks(nRunsMC);
      //
    } catch( DemoException demoEx ) {
      dbgPrintln(demoEx.toString());
      System.exit(-1);
    }
  }
    
  //------------------------------------------------------------------------
  /**
    * Generates the parameters for the given Monte Carlo simulation.
    *
    * @param nRunsMC the number of tasks, and hence Monte Carlo paths to
    *        produce.
    */
    private void initTasks(int nRunsMC) {
        
      // initialize task array
	tasks = new ToTask[nRunsMC];
	
      // for each task, parallel init
      for(int i = 0; i < nRunsMC; ++i) {
        String header = "MC run "+String.valueOf(i);
        ToTask task = new ToTask(header, (long)i*11);
        tasks[i] = task;
      }
   }

    /**
     * Main runParallel implementation; uses uniqueness
     */
    public void runParallel() {
	results = new ResultArray(nRunsMC);

	if (true) {
	    runParallelMapping();
	    return;
	}

	for each i in results pardo {
		results[i] = 
		    computeResult(new PriceStock<Results>(), i);
	    }
    }

    public ToResult<Results> computeResult(unique PriceStock<Results> ps, int i)
	reads TaskR writes Results via ps 
    {
	ps.setInitAllTasks(initAllTasks);
	// read the corresponding task and copy its value to PriceStock
	ps.setTask(tasks[i]);
	
	// ****************************************************
	// main Monte Carlo computation
	ps.run();
	// ****************************************************
	return ps.getResult();
    }

    public void runParallelMapping() {
	refgroup g1,g2;
	DisjointArray<Results,g1> psArray = 
	    new DisjointArray<Results,g1>(nRunsMC);
	for (int i = 0; i < nRunsMC; ++i) {
	    psArray.set(new PriceStock<Results>(),i);
	}
	DisjointArray<Results,g2> resultArray =
	    psArray.<region TaskR,refgroup g2>withParallelMapping(new RunMapping<g2>());
	for (int i = 0; i < nRunsMC; ++i) {
	    results[i] = (ToResult<Results>) resultArray.getShared(i);
	}
    }

    public class RunMapping<refgroup G>
	implements DisjointArray.ParallelMapping<TaskR,G>
    {
	public <region R | R # TaskR>
	    unique(G) Data<R> map(Data<R> ps, int i)
	    reads TaskR writes R via ps
	{
	    switch (ps) instanceof {
		case PriceStock<R>:
		    ps.setInitAllTasks(initAllTasks);
		    // read the corresponding task and copy its value to PriceStock
		    ps.setTask(tasks[i]);
		    
		    // ****************************************************
		    // main Monte Carlo computation
		    ps.run();
		    // ****************************************************
		    return ps.<refgroup G>getResult();
		}

	    return null;
	}

    }

    /**
     * Alternate implementation of runParallel using local regions;
     * not called by test harness.
     */
    public void runParallelLocalRegions() {
	results = new ResultArray(nRunsMC);
	
	for each i in results pardo {
		region Loop;
		
		PriceStock<Loop> ps = new PriceStock<Loop>();
		ps.setInitAllTasks(initAllTasks);
		// read the corresponding task and copy its value to PriceStock
		ps.setTask(tasks[i]);
		
		// ****************************************************
		// main Monte Carlo computation
		ps.run();
		// ****************************************************
		ToResult<Loop> result = ps.getResult();
		results[i] = (ToResult<Results>) result;
	    }
    }

  
  public void processParallel() {
      //
      // Process the results.
    try {
      processResults();
    } catch( DemoException demoEx ) {
      dbgPrintln(demoEx.toString());
      System.exit(-1);
    }
  }
 
  /**
    * Method for doing something with the Monte Carlo simulations.
    * It's probably not mathematically correct, but shall take an average over
    * all the simulated rate paths.
    *
    * @exception DemoException thrown if there is a problem with reading in
    *            any values.
    */

 RatePath<Results> avgMCrate;
 ReentrantLock lock = new ReentrantLock();
 RatePath<Results>[] localAvgMCrate;

    void sumReduction(final int index, double localAvgExpectedReturnRateMC, 
		      double localAvgVolatilityMC) 
    {
	lock.lock();
	avgExpectedReturnRateMC += localAvgExpectedReturnRateMC;
	avgVolatilityMC += localAvgVolatilityMC;
	avgMCrate.inc_pathValue2(localAvgMCrate[index].get_pathValue());
	lock.unlock();
 }

  private void processResults() throws DemoException {	  
	avgExpectedReturnRateMC = 0.0;
	avgVolatilityMC = 0.0;

	double runAvgExpectedReturnRateMC = 0.0;
    double runAvgVolatilityMC = 0.0;
    //ToResult returnMC;
    //if( nRunsMC != results.size() ) {
    if (nRunsMC != results.length) {
    	errPrintln("Fatal: TaskRunner managed to finish with no all the results gathered in!");
      System.exit(-1);
    }
    //
    // Create an instance of a RatePath, for accumulating the results of the
    // Monte Carlo simulations.
    avgMCrate = new RatePath<Results>(nTimeStepsMC, "MC", 19990109, 19991231, dTime);
      
    // parallelize the reduction using local and tiling
    int tileSize = 100;
    localAvgMCrate = new RatePath<Results>[nRunsMC/tileSize];  

    for (int p = 0; p < nRunsMC/tileSize; ++p) {

    	int start = p * tileSize;
    	int end = (p+1) * tileSize;
    	double localAvgExpectedReturnRateMC = 0.0;
        double localAvgVolatilityMC = 0.0;

	localAvgMCrate[p] = new RatePath<Results>(nTimeStepsMC, "MC", 19990109, 19991231, dTime);

    	for (int i=start;i<end;i++) {
	    ToResult<Results> returnMC = results[i];
	    PathValue<Results> pathValue = returnMC.get_pathValue();
	    localAvgMCrate[p].inc_pathValue(pathValue);

    	    // reductions (sum)
    	    localAvgExpectedReturnRateMC += returnMC.get_expectedReturnRate();
    	    localAvgVolatilityMC         += returnMC.get_volatility();
       	}
    	
    	// update global sum
	sumReduction(p, localAvgExpectedReturnRateMC, localAvgVolatilityMC);

    }
    
    // ********************************************************************
    // final result
    avgMCrate.inc_pathValue((double)1.0/((double)nRunsMC));
	avgExpectedReturnRateMC /= nRunsMC;
	avgVolatilityMC /= nRunsMC;
    // ********************************************************************
    
	JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;

//    dbgPrintln("Average over "+nRunsMC+": expectedReturnRate="+
//    avgExpectedReturnRateMC+" volatility="+avgVolatilityMC + JGFavgExpectedReturnRateMC);
  }

}

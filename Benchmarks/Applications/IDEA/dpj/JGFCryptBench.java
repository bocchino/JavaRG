/**************************************************************************
*                                                                         *
*         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         * 
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


//package crypt;
import jgfutil.*; 


public class JGFCryptBench extends IDEATest implements JGFSection2{ 

  private int size; 
  private int datasizes[]={3000000,20000000,35000000};
  public static int nthreads;

  public JGFCryptBench(int nthreads){
    this.nthreads=nthreads;
  }


  public void JGFsetsize(int size){
    this.size = size;
  }

  public void JGFinitialise(){
    array_rows = datasizes[size];
    //nthreads = array_rows/64;
    //nthreads = array_rows/8192;
    buildTestData();
  }
 
  public void JGFkernel(){
    Do(); 
  }

  public void JGFvalidate(){
    boolean error;

    error = false; 
    for (int i = 0; i < array_rows; i++){
      error = (plain1.get(i) != plain2.get(i)); 
      if (error){
	System.out.println("Validation failed");
	System.out.println("Original Byte " + i + " = " + plain1.get(i)); 
	System.out.println("Encrypted Byte " + i + " = " + crypt1.get(i)); 
	System.out.println("Decrypted Byte " + i + " = " + plain2.get(i)); 
	//break;
      }
    }
System.out.println("Validation Success!");
  }


  public void JGFtidyup(){
    freeTestData(); 
  }  



  public void JGFrun(int size){


    JGFInstrumentor.addTimer("Section2:Crypt:Kernel", "Kbyte",size);

    JGFsetsize(size); 
    JGFinitialise(); 
    JGFkernel(); 
    JGFvalidate(); 
    JGFtidyup(); 

     
    JGFInstrumentor.addOpsToTimer("Section2:Crypt:Kernel", (2*array_rows)/1000.); 
    JGFInstrumentor.printTimer("Section2:Crypt:Kernel"); 
  } 
}

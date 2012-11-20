import DPJRuntime.*;
import java.util.Random;

/**
 * Sample sort program adapted from a demo in
 * <A href="http://supertech.lcs.mit.edu/cilk/"> Cilk</A> and
 * <A href="http://www.cs.utexas.edu/users/hood/"> Hood</A>.
 *
 * There are two versions of MergeSort here: One that splits the array
 * into four pieces at each recursive step, and one that splits the
 * array into eight pieces.  This abstract class represents the common
 * elements of both versions.
 * 
 **/

public abstract class MergeSort extends Harness {

    region Input, Result;

    protected ArrayInt<Input> input;
    protected ArrayInt<Result> result;

    public MergeSort(String name, String[] args) {
        super(name, args);
    }

    @Override
    public void initialize() {
	input = new ArrayInt<Input>(size);
	result = new ArrayInt<Result>(size);
	for (int i = 0; i < input.length; ++i) {
	    input[i] = i;
	}
	DPJUtils.permuteInt(input);
    }

    @Override
    public void runWork() {
	refgroup G1, G2;
	unique(G1) ArraySliceInt<Input,G1> inputSlice = 
	    new ArraySliceInt<Input,G1>(this.input);
	unique(G2) ArraySliceInt<Result,G2> resultSlice =
	    new ArraySliceInt<Result,G2>(this.result);
	this.<region Input,Result,refgroup G1,G2>sort(inputSlice,resultSlice);
    }

    @Override
    public void runTest() {
	checkSorted(input,input.length);
    }

    public abstract <region R1,R2, refgroup G1,G2 | R1 # R2>
	void sort(ArraySliceInt<R1,G1> A, ArraySliceInt<R2,G2> B);

    protected static <region R>
	void checkSorted (ArrayInt<R> array, int n)  
    {
	for (int i = 0; i < n - 1; i++) {
	    if (array[i] > array[i+1]) {
		throw new Error("Unsorted at " + i + ": " + 
				array[i] + " / " + array[i+1]);
	    }
	}
    }
    
    protected static <region R1,R2,refgroup G1,G2 | R1 # R2>
	void merge(ArraySliceInt<R1,G1> A, 
		   ArraySliceInt<R1,G1> B, 
		   ArraySliceInt<R2,G2> out) 
        // FIXME
	writes R1 via A...G1, R1 via B...G1,
	       R2 via out...G2
    {
	
	if (A.length <= MERGE_SIZE) {
	    MergeSort.<region R1,R2,refgroup G1,G2>
		sequentialMerge(A, B, out);
	} else {
	    int aHalf = A.length >>> 1; /*l33t shifting h4x!!!*/
	    int bSplit = MergeSort.<region R1,refgroup G1>findSplit(A.get(aHalf), B);

	    A.partition(aHalf);
	    B.partition(bSplit);
	    out.partition(aHalf + bSplit);

	    pardo {
		merge(A.segs[0],B.segs[0],out.segs[0]);
		merge(A.segs[1],B.segs[1],out.segs[1]);
	    }
	}
    }

    /** A standard sequential merge **/
    protected static <region R1,R2,
	refgroup G1,G2 | R1#R2>
	void sequentialMerge(ArraySliceInt<R1,G1> A, 
			     ArraySliceInt<R1,G1> B, 
			     ArraySliceInt<R2,G2> out) 
	reads R1 via A...G1, R1 via B...G1 
	writes R2 via out...G2
    {
	int a = 0;
	int aFence = A.length;
	int b = 0;
	int bFence = B.length;
	int k = 0;
	
	while (a < aFence && b < bFence) {
	    if (A.get(a) < B.get(b)) 
		out.put(k++, A.get(a++));
	    else 
		out.put(k++, B.get(b++));
	}
	
	while (a < aFence) out.put(k++, A.get(a++));
	while (b < bFence) out.put(k++, B.get(b++));
    }
    
    protected static <region R,refgroup G>int 
	findSplit(int value, ArraySliceInt<R,G> B) 
	reads R via B...G 
    {
	int low = 0;
	int high = B.length;
	while (low < high) {
	    int middle = low + ((high - low) >>> 1);
	    if (value <= B.get(middle))
		high = middle;
	    else
		low = middle + 1;
	}
	return high;
    }

    
  
    /* Threshold values */
    
    // Cutoff for when to do sequential versus parallel merges 
    public static final int MERGE_SIZE = 2048;
    
    // Cutoff for when to do sequential quicksort versus parallel mergesort
    public static final int QUICK_SIZE = 2048;
    
    // Cutoff for when to use insertion-sort instead of quicksort
    public static final int INSERTION_SIZE = 2000;
    
    
    
    /** A standard sequential quicksort **/
    protected static <region R,refgroup G>
	void quickSort(ArraySliceInt<R,G> arr) 
	writes R via arr...G
    {
	int lo = 0;
	int hi = arr.length-1;
	// If under threshold, use insertion sort
	if (hi-lo+1l <= INSERTION_SIZE) {
	    for (int i = lo + 1; i <= hi; i++) {
		int t = arr.get(i);
		int j = i - 1;
		while (j >= lo && arr.get(j) > t) {
		    arr.put(j+1, arr.get(j));
		    --j;
		}
		arr.put(j+1, t);
	    }
	    return;
	}
	
	//  Use median-of-three(lo, mid, hi) to pick a partition. 
	//  Also swap them into relative order while we are at it.
	
	int mid = (lo + hi) >>> 1;
	
	if (arr.get(lo) > arr.get(mid)) {
	    int t = arr.get(lo); arr.put(lo, arr.get(mid)); 
	    arr.put(lo, arr.get(mid)); arr.put(mid, t);
	}
	if (arr.get(mid) > arr.get(hi)) {
	    int t = arr.get(mid); arr.put(mid, arr.get(hi)); 
	    arr.put(hi, t);
	    
	    if (arr.get(lo) > arr.get(mid)) {
		t = arr.get(lo); arr.put(lo, arr.get(mid));
		arr.put(mid, t);
	    }
	    
	}
	
	int left = lo+1;           // start one past lo since already handled lo
	int right = hi-1;          // similarly
	
	int partition = arr.get(mid);
	
	for (;;) {
	    
	    while (arr.get(right) > partition)
		--right;
	    
	    while (left < right && arr.get(left) <= partition) 
		++left;
	    
	    if (left < right) {
		int t = arr.get(left); 
		arr.put(left, arr.get(right));
		arr.put(right, t);
		--right;
	    }
	    else break;
	    
	}

	arr.partition(left+1);
	MergeSort.<region R,refgroup G>quickSort(arr.segs[0]);
	MergeSort.<region R,refgroup G>quickSort(arr.segs[1]);
	
    }
    
}


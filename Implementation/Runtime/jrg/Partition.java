package DPJRuntime;

/**
 * <p>The {@code Partition} class represents an array of {@link
 * ArraySlice} objects that partition another {@code ArraySlice}
 * (called the <i>root array</i>).  Each {@code ArraySlice} in the
 * partition is a contiguous subsection of the root array, and all are
 * mutually disjoint.  The {@code ArraySlice}s in the partition are
 * called <i>segments</i> of the root array.
 *
 * <p>For example, if the root array has index space {@code [0,10]},
 * then a partition might have segments with index spaces {@code
 * [0,5]} and {@code [6,10]}.
 * 
 * @author Rob Bocchino
 *
 * @param <T> The type of an element of an array in the partition
 * @param <R> The region of a cell of an array in the partition
 */
public class Partition<type T,region R,refgroup G> {

    private arrayclass Segs<region R1>
    {
	unique(G) ArraySlice<T,R> in R1;
    }

    /**
     * The array being partitioned
     */
    private final ArraySlice<T,R> A;

    /**
     * The segments of the partition
     */
    public final unique(G) Segs<R> segs;

    /**
     * Number of segments in the partition
     */
    public final int length;

    /**
     * Stride of the segments, for a strided partition.
     */
    private final int stride;

    /**
     * Partitions an array {@code A} into two segments at index {@code
     * idx}.  If {@code A} has {@code n} elements, then the first
     * segment consists of elements {@code 0} through {@code idx-1},
     * and the second segment consists of elements {@code idx} through
     * {@code n-1}.
     *
     * <p>Throws {@code ArrayIndexOutOfBoundsException} if
     * {@code idx} is not in {@code [0,n-1]}.
     *
     * @param A   The array to partition
     * @param idx The partition index
     */
    public Partition(ArraySlice<T,R> A, final int idx) 
	fresh G
	pure
	switches G
    {
	this.A = A;
	this.length = 2;
	this.stride = 0;
	region Local;
	unique(G) Segs<Local> localSegs = ArrayCreator.create(length);
	localSegs[0] = A.subarray(0, idx);
	localSegs[1] = A.subarray(idx, A.length - idx);
	this.segs = (Segs<R>) localSegs;
    }

    /**
     * Partitions an array {@code A} into two segments at {@code idx},
     * optionally excluding the element at {@code idx}.  If {@code A}
     * has {@code n} elements, then the first segment is always {@code
     * [0,idx-1]}.  The second segment is either {@code [idx,n-1]} (if
     * {@code exclude} is false) or {@code [idx+1,n-1]} (if {@code
     * exclude} is true).
     *
     * <p>Throws {@code ArrayIndexOutOfBoundsException} if
     * {@code idx} is not in {@code [0,n-1]}.
     *
     * @param A       The array to partition
     * @param idx     The partition index
     * @param exclude Whether to exclude the element at
     *                {@code idx} from the segments
     */
    public Partition(ArraySlice<T,R> A, final int idx, boolean exclude) 
	fresh G
	pure
	switches G
    {
	this.A = A;
	this.length = 2;
	this.stride = 0;
	region Local;
	unique(G) Segs<Local> localSegs = ArrayCreator.create(length);
	localSegs[0] = A.subarray(0, idx);
	if (exclude) {
	    localSegs[1] = A.subarray(idx + 1, A.length - idx - 1);
	} else {
            localSegs[1] = A.subarray(idx, A.length - idx);
	}
	this.segs = (Segs<R>) localSegs;
    }

    /**
     * Private constructor.  {@code strided} is a dead arg here: we
     * use it to disambiguate this constructor from the other one that
     * takes an array and a stride.
     */
    private Partition(ArraySlice<T,R> A, int stride, double strided) 
	fresh G
	pure
	switches G
    {
    	this.A = A;
        this.stride = stride;
	this.length = (A.length / stride) + ((A.length % stride == 0) ? 0 : 1);
	region Local;
	unique(G) Segs<Local> localSegs = ArrayCreator.create(length);
	for (int idx = 0; idx < length; ++idx) {
	    int start = idx * stride;
	    int segLength = (start + stride > A.length) ? (A.length - start) : stride;
	    localSegs[idx] = A.subarray(start, segLength);
	}
	this.segs = (Segs<R>) localSegs;
    }

    /**
     * Creates a partition using stride {@code stride}.  For example,
     * partitioning a 10-element array with stride 2 creates a
     * partition with 5 segments, each of length 2.
     *
     * @param <T> The type of the array to partition
     * @param <R> The region of the array to partition
     * @param A  The array to partition
     * @param stride The stride at which to partition
     * @return A partition of {@code A} with stride {@code stride}
     */
    public static <type T,region R,refgroup G>Partition<T,R,G> 
      stridedPartition(ArraySlice<T,R> A, int stride) 
	fresh G
	pure
	switches G
    {
    	return new Partition<T,R,G>(A, stride, 0.0);
    }

    /**
     * Partitions an array {@code A} into
     * {@code idxs.length+1} segments using the indices in
     * {@code idxs} as the split points.  If {@code A} has
     * index space {@code [0,n-1]}, then the segments are
     * {@code [0,idxs[0]-1]}, {@code [idxs[0],idxs[1]-1]},
     * ..., {@code [idxs[idxs.length-1]-1,n-1]}.
     *
     * Throws {@code ArrayOutOfBoundsException} if the indices
     * are not monotonically increasing, or if any index is out of
     * bounds for {@code A}.
     *
     * @param <RI> The region of array {@code idxs}
     * @param A    The array to partition
     * @param idxs The split points    
     * @return A partition of {@code A} using {@code idxs} as the
     * split points.
     */
    public <region RI>Partition(ArraySlice<T,R> A, IntArray<RI> idxs) 
	fresh G
	reads RI writes R 
	switches G
    {
	this.A = A;
	this.length = idxs.length+1;
	this.stride = 0;
	region Local;
	unique(G) Segs<Local> localSegs = ArrayCreator.create(length);
	if (length == 1)
	    localSegs[0] = A;
	else {
	    int i = 0, len = 0;
	    localSegs[0] = A.subarray(0, idxs[0]);
	    for (i = 1; i < idxs.length; ++i) {
		len = idxs[i] - idxs[i-1];
		if (len < 0) 
		    throw new ArrayIndexOutOfBoundsException();
	    	final int j = i;
		localSegs[j] = A.subarray(idxs[j-1], len);	    
	    }
            i = idxs[idxs.length - 1];
            len = A.length - i;
	    final int length = idxs.length;
            localSegs[length] = A.subarray(i, len);
	}
	this.segs = (Segs<R>) localSegs;
    }

    /**
     * Returns segment {@code idx} of the partition.
     *
     * <p> Throws {@code ArrayIndexOutOfBoundsException} if {@code
     * idx} is not a valid segment index.
     *
     * @param idx  Index of the segment to get
     * @return Segment {@code idx} of the partition
     */
    public unique(G) ArraySlice<T,R> get(final int idx) 
	reads R 
    {
	if (idx < 0 || idx > length-1) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	return !segs[idx];
    }

}

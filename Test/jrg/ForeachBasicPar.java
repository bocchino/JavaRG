/**
 * Basic test of parallel 'for each' statement
 */

class ForeachBasicPar extends Harness {

    region Data, Cells;

    class Cell {
	int data in Data;
    }

    arrayclass CellArray<refgroup G> {
	unique(G) Cell in Cells;
    }

    class Work<refgroup G> {
	unique(G) CellArray<G> cellArray;
    }

    // Can't annotate the harness methods with permissions
    @Override
    public void initialize() {}
    @Override
    public void runTest() {}

    @Override public void runWork() {
	refgroup g;

	// Switch g to updating
	// Call 'init' which updates g
	final unique(g) CellArray<g> cellArray = 
	    this.<refgroup g>init();

	// Switch g to preserving
	// Get permission 'writes Data via cellArray...g'
	// Call 'work' which needs the permission (implies preserving)
	this.<refgroup g>work(cellArray);

	// Call test
	this.<refgroup g>test(cellArray);
    }

    private <refgroup G>unique(G) CellArray<G> init() 
	updates G 
    {
	unique(G) CellArray<G> cellArray = 
	    new CellArray<G>(size);
	for each i in cellArray {
	     cellArray[i] = new Cell();
	}
	return cellArray;
    }

    private <refgroup G>void work(final CellArray<G> cellArray) 
	writes Data via cellArray...G 
    {
	for each i in cellArray pardo {	     	
	     cellArray[i].data = i;
	}
    }

    private <refgroup G>void test(CellArray<G> cellArray) {
	for (int i = 0; i < cellArray.length; ++i)
	    assert(cellArray[i].data == i);
    }

    public ForeachBasicPar(String[] args) {
	super("ForeachBasicPar", args);
    }

    public static void main(String[] args) {
	ForeachBasicPar test = new ForeachBasicPar(args);
	test.run();
    }
}

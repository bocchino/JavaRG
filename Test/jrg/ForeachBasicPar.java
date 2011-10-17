/**
 * Basic test of parallel 'for each' statement
 */

class ForeachBasicPar<refgroup G> extends Harness {

    region Data, Cells;

    class Cell {
	int data in Data;
    }

    arrayclass CellArray<refgroup G> {
	unique(G) Cell in Cells;
    }

    // unique(G)
    CellArray cellArray;
    
    @Override
    public void initialize() {
	cellArray = new CellArray(size);
	for each i in cellArray {
	     cellArray[i] = new Cell();
	}
    }

    @Override
    public void runWork() {
	//unique(G) 
	CellArray cellArray = !this.cellArray;
	for each i in cellArray pardo {	     	
	     cellArray[i].data = i;
	}
	this.cellArray = cellArray;
    }

    @Override
    public void runTest() {
	for (int i = 0; i < cellArray.length; ++i)
	    assert(cellArray[i].data == i);
    }

    public ForeachBasicPar(String[] args) {
	super("ForeachBasicPar", args);
    }

    public static void main(String[] args) {
	refgroup g;
	ForeachBasicPar<g> test = new ForeachBasicPar<g>(args);
	test.run();
    }
}

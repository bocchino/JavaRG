/**
 * Test coarsening of updated groups
 */
class WarnCoarsenUpdatedGroup {
    class Data {
	public Data() pure {}
 	region r;
 	int d in r;
     }
    class Cell<refgroup G> {
	region r1,r2;
	unique(G) Data d1 in r1;
	unique(G) Data d2 in r1;
	unique(G) Data d3 in r2;
    }
    // Should warn
     void m1() {
	refgroup g;
	unique(g) Cell<g> cell = new Cell<g>();
	cell.d1=new Data();
	cell.d2=new Data();
 	pardo {
 	    cell.d1.d=0;
 	    cell.d2.d=1;
 	    cell.d3 = new Data(); // Updates g
 	}
     }
     // Should not warn
     void m2() {
	refgroup g;
	unique(g) Cell<g> cell = new Cell<g>();
	cell.d1=new Data();
	cell.d2=new Data();
 	pardo {
 	    cell.d1.d=0;
 	    cell.d2.d=1;
 	}
     }

 }
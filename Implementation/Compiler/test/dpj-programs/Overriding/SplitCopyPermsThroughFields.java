/**
 * Test splitting of copy perms
 */
class Data {}

class List<refgroup G> {
    unique(G) Data data;
    unique(G) List list;    
}

abstract class A<refgroup GA1,GA2> {
    abstract void m(unique(GA1) List<GA1> xA) 
    	fresh GA2 preserves GA1;
}

abstract class B<refgroup GB1,GB2> extends A<GB1,GB2> {
    abstract void m(unique(GB1) List<GB1> xB) 
    	copies xB...GB1 to GB2; 
}

abstract class C<refgroup GC1,GC2> extends B<GC1,GC2> {
    abstract void m(unique(GC1) List<GC1> xC) 
    	copies xC to GC2, xC.data to GC2, xC.list...GC1 to GC2  
    	preserves GC1;
}

abstract class D<refgroup GD1,GD2> extends C<GD1,GD2> {
    abstract void m(unique(GD1) List<GD1> xD)
    	copies xD.list.list...GD1 to GD2;
}

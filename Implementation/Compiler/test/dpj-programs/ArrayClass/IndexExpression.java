/**
 * Test of index expression with array class type
 */

class Data {}

arrayclass DataArray<region R1, refgroup G1> { 
    unique(G1) Data in R1; 
}

class IndexExpression<region R2, refgroup G2> {
    unique(G2) Data m(DataArray<R2,G2> a) { return a[0]; }
}
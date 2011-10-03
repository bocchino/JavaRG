/**
 * Test of index expression with array class type
 */

class Data {}

arrayclass DataArray<region R, refgroup G> { 
    unique(G) Data in R; 
}

class IndexExpression {
    unique(G) Data m(DataArray a) { return a[0]; }
}
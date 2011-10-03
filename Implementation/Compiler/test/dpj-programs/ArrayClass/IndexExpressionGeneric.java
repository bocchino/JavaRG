/**
 * Test of index expression with generic type
 */

arrayclass DataArray<type T1, region R1, refgroup G1> { 
    unique(G1) T1 in R1; 
}

class IndexExpression<type T2, region R2, refgroup G2> {
    unique(G2) T2 m(DataArray<T2,R2,G2> a) { return a[0]; }
}
/**
 * Test array class creation with generic parameter
 */

arrayclass Array<type T1, region R1, refgroup G1> { 
    unique(G1) T1 in R1; 
}

class ArrayCreation<type T2, region R2, refgroup G2> {
    unique(G2) T2 m1(Array<T2,R2,G2> a) { return !a[0]; }
    unique(G2) T2 m2() {
        Array<T2,R2,G2> a = new Array<T2,R2,G2>(10);
        return m1(a);
    }

}
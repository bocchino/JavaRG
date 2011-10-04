/**
 * Test array class creation
 */

class Data {}

arrayclass DataArray<region R1, refgroup G1> { 
    unique(G1) Data in R1; 
}

class ArrayCreation<region R2, refgroup G2> {
    unique(G2) Data m1(DataArray<R2,G2> a) { return a[0]; }
    unique(G2) Data m2() {
        DataArray<R2,G2> a = new DataArray<R2,G2>(10);
        return m1(a);
    }

}
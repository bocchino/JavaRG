/**
 * Test array class access
 */

class Data {}

arrayclass DataArray<region R1, refgroup G1> { 
    unique(G1) Data in R1; 
}

class ArrayAccess<region R2, refgroup G2> {
    DataArray<R2,G2> m() updates G2 {
        DataArray<R2,G2> a = new DataArray<R2,G2>(10);
        for (int i = 0; i < a.length; ++i)
            a[i] = new Data();
        return a;
    }
}
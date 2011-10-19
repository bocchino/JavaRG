/**
 * Test array class access with generic type param
 */

class Data {}

arrayclass Array<type T1, region R1, refgroup G1> { 
    unique(G1) T1 in R1; 
}

class ArrayCreation<region R2, refgroup G2> {
    Array<Data, R2,G2> m() {
        Array<Data, R2,G2> a = new Array<Data, R2,G2>(10);
        for (int i = 0; i < a.length; ++i)
            a[i] = new Data();
        return a;
    }
}
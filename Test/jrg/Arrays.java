/**
 * Array classes
 */

arrayclass IntArray<region R> { int in R; }

arrayclass IntIntArray<region R> { IntArray<R> in R; }

arrayclass DataArray<region R> { 
    Data in R; 
}

arrayclass Array<type T, region R> { 
    T in R; 
}


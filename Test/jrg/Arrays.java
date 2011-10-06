/**
 * Array classes
 */

arrayclass IntArray<region R> { int in R; }

arrayclass IntIntArray<region R> { IntArray<R> in R; }

arrayclass DataArray<region R, refgroup G> { 
    unique(G) Data in R; 
}

arrayclass GenericArray<type T, region R, refgroup G> { 
    unique(G) T in R; 
}


/**
 * Array classes for Barnes-Hut
 */

arrayclass BodyArray<refgroup A> {
    /*unique(A)*/ Body in BarnesHut.Links;
}

arrayclass NodeArray<refgroup G> {
    unique(G) Node in BarnesHut.Links;
}

arrayclass DoubleArray<region R> {
    double in R;
}



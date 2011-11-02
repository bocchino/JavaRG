/**
 * Array classes for Barnes-Hut
 */

arrayclass BodyArray<refgroup T,A> {
    /*unique(A)*/ Body<T,A> in BarnesHut.Links;
}

arrayclass NodeArray<refgroup G> {
    /*unique(G)*/ Node<G> in BarnesHut.Links;
}

arrayclass DoubleArray<region R> {
    double in R;
}



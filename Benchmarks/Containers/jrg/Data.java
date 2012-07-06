/**
 * A data interface for the containers
 */
public abstract class Data<region R> {
    public abstract <refgroup G>void updateParallel() 
	writes R via this...G;
    public abstract void updateSequential();
}

arrayclass DataArray<region R> {
    Data<R> in R;
}


arrayclass UniqueDataArray<region R, refgroup G> {
    unique(G) Data<R> in R;
}

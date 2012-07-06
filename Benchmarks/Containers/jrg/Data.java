/**
 * A data interface for the containers
 */
public interface Data<region R> {}

arrayclass DataArray<region R> {
    Data<R> in R;
}

arrayclass UniqueDataArray<region R, refgroup G> {
    unique(G) Data<R> in R;
}



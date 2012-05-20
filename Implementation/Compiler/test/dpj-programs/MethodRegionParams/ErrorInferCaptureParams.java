abstract class InferCaptureParams {
    class Data<region R> {}
    abstract <region R>void m1(Data<R> x, Data<R> y) writes R;
    void m2() {
	region A,B;
	Data<*> x = new Data<A>();
	Data<*> y = new Data<B>();
	// Should not be allowed: implies R=A and R=B in m1
	this.m1(x,y);
	// OK
	Data<A> z = new Data<A>();
	this.m1(z, z);
    }
}
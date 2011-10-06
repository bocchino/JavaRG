/**
 * Test of group name bound to array class param
 */

class Data {}

arrayclass DataArray<region R, refgroup G> {
    unique(G) Data in R;
}

class ArrayClassParamArg<region R, refgroup G> {
    DataArray<R,G> dataArray;
    static void main() {
	region r;
	refgroup g;
	ArrayClassParamArg<r,g> acpa = new ArrayClassParamArg<r,g>();
	acpa.dataArray = new DataArray<r,g>(10);
	for (int i = 0; i < acpa.dataArray.length; ++i)
	    acpa.dataArray[i] = new Data();
    }
}
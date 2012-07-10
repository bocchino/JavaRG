/**
 * Test error case:  Don't mask final unique field
 */
class ErrorDontMaskFinalUniqueField {
    region r;
    final unique Data data = new Data();
    class Data { 
	public Data() pure {}
	int x in r; 
    }
    void m() unique
    	pure // error
    {
	data.x = 1; // write to r is not masked
    }
    

}
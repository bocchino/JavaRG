/**
 * Include effect on final unique field
 */
class IncludeEffectOnFinalUniqueField {
    region r;
    class Data { 
	public Data() pure {}
	int f in r; 
    }
    final unique Data data = new Data();
    void m()
     	writes r via this
    {
	// f is final unique ==>
	// write to this.data.f covered by write to this.data
	this.data.f = 1;
    }
}
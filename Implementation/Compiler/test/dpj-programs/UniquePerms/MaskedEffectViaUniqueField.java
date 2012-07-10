class MaskedEffectViaUniqueField {
    region r1, r2;
    unique Data data in r1;
    class Data { 
	public Data() pure {}
	int x in r2; 
    }
    void m() unique
    	writes r1
    {
	data = new Data();
	data.x = 1; // write to r2 is masked
    }
    

}
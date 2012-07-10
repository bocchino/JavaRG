class MaskedEffectViaUniqueVar {
    class Data { 
	public Data() pure {}
	int x; 
    }
    void m()
    	pure
    {
	unique Data data = new Data();
	data.x = 1;
    }    
}
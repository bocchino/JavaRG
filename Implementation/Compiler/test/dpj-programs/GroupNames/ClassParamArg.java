/**
 * Test of group name bound to class param
 */

class Data {}

class ClassParamArg<refgroup G> {
    unique(G) Data field;
    void method() {
	refgroup g;
	unique(g) ClassParamArg<g> cpa = 
		new ClassParamArg<g>();
	unique(g) Data data = new Data();
	cpa.field = data;
    }
}
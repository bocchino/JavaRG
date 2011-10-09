/**
 * Basic test of pardo statement
 */
class PardoBasic {
    region r1, r2;
    int x1 in r1, x2 in r2;
    void m() {
	pardo {
	    x1 = 1;
	    x2 = 2;
	}
    }
}
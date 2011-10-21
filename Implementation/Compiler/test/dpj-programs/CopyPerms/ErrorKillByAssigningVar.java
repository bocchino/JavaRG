/**
 * Test kill of copy perm by assigning to var
 */
class KillByAssigningVar {
    class Data{}
    <refgroup G>void m(Data x, Data y) copies x to G {
	x = y;           // Should kill copy perm
	unique(G) Data z = x; // Should fail
    }
}
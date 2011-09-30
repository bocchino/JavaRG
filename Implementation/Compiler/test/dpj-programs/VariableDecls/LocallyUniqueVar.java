/** Test of locally unique variable syntax
 */

class LocallyUniqueVar<type T, refgroup G> {
    void m() {
	unique(G) T x = null;
    }
}
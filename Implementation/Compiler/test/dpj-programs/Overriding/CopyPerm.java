/**
 * Test valid overriding of copy perms
 */
class CopyPerm {
    class Data{}
    class A<refgroup GA1, GA2> {
	void m(Data x) copies x...GA1 to GA2 {}
    }
    class B<refgroup GB1, GB2> extends A<GB1,GB2> {
	void m(Data x) copies x...GB1 to GB2 {}
    }
}
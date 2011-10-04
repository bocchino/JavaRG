/**
 * Test of index expression with int cell type
 */

arrayclass IntArray { int; }

class IndexExpression {
    int m(IntArray a) { return a[0]; }
}
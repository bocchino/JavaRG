/** Test of locally unique field syntax with region specifier
 */

class LocallyUniqueField<type T, region R, refgroup G> {
    unique(G) T f in R;
}
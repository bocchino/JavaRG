/** Test of reference permission in return type
 */
class ReturnType<type T, refgroup G> {
    unique(G) T m() { return null; }
}
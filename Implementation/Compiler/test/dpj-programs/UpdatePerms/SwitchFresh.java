/**
 * Can switch and preserve if fresh
 */
interface SwitchFresh<refgroup G> {
    void m() fresh G switches G;
}
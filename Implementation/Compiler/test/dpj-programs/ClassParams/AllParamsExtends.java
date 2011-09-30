/**
 * Test of all class parameters with 'extends' clause
 *
 * @author Rob Bocchino
 */

class Super<type T1, T2, region R1, R2, refgroup G1, G2> {}

class Sub<type T1, T2, region R1, R2, refgroup G1, G2> extends Super<T1,T2,R1,R2,G1,G2> {}
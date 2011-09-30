/**
 * Test of all class parameters with 'implements' clause
 *
 * @author Rob Bocchino
 */

interface Interface<type T1, T2, region R1, R2, refgroup G1, G2> {}

class Class<type T1, T2, region R1, R2, refgroup G1, G2> implements Interface<T1,T2,R1,R2,G1,G2> {}
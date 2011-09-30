/**
 * Test group params with 'extends' clause
 *
 * @author Rob Bocchino
 */

class Super<refgroup G1, G2> {}

class Sub<refgroup G1, G2> extends Super<G1, G2> {}

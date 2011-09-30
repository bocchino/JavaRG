/**
 * Test group params with 'implements' clause
 *
 * @author Rob Bocchino
 */

interface Interface<refgroup G1, G2> {}

class Class<refgroup G1, G2> implements Interface<G1, G2> {}

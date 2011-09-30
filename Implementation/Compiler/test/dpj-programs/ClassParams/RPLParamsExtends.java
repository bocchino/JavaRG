/**
 * Test RPL params with 'extends' clause
 *
 * @author Rob Bocchino
 */

class Super<region R1, R2> {}

class Sub<region R1, R2> extends Super<R1, R2> {}

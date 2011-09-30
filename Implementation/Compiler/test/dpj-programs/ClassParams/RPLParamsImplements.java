/**
 * Test RPL params with 'interface' clause
 *
 * @author Rob Bocchino
 */

interface Interface<region R1, R2> {}

class Class<region R1, R2> implements Interface<R1, R2> {}

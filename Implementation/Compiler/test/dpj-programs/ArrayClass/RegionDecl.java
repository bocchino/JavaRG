/** 
 * Test of array class with region declaration
 */

class Body {}

arrayclass BodyArray<refgroup G> { 
    region r;
    unique(G) Body in r; 
}
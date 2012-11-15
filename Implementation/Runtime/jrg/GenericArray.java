/**
 * Wrapper class to encapsulate the hideous nonsense we have to do in
 * Java to create a generic array.
 */
package DPJRuntime;

class GenericArray<type T> {

    /**
     * Create a generic array of type T and size size.
     *
     * @param T    - Must be an array type
     * @param size - the size of the array
     */
    public static <type T>T create(int size) 
	pure 
    {
	return (T) ((Object) new Object[size]);
    }

}
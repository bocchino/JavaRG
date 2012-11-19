/**
 * Wrapper for the nonsense we have to do to get a generic array in
 * Java.
 */
package DPJRuntime;

class ArrayCreator {

    /**
     * Create a generic array of type T and size size.
     *
     * @param T    - Must be an array type
     * @param size - the size of the array
     */
    public static <type T>unique T create(int size) 
	pure 
    {
	return (T) ((Object) new Object[size]);
    }

}
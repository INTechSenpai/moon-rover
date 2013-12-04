/** ============================================================================
 * 	                           Abstract Factory class
 *  ============================================================================
 * 
 * You are intended to implement FactoryProduct if you want a factory to clone 
 * your class.
 * Author : Martial
 */
package factories;
// All classes that could be learned by factories have to implement this interface.
// So we ensure that we can rely on class name as string, and its cloning method.
public interface FactoryProduct
{
	// Returns an independent copy of the calling object
	// It is your job to define what is a clone according to your context.
	// All attributes may or may not be copied, and (not recommanded) extra work can also be done here
	// It's all up to you
	public FactoryProduct Clone();

	// Gives the class's name in a string
	public String TypeName();
}
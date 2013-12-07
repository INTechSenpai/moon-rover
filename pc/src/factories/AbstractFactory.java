/** ============================================================================
 * 	                           Abstract Factory class
 *  ============================================================================
 * 
 * You are intented to inherit from AbstractFactory to build your concrete factory
 * that suits your needs. Please do not implement hybrid classes that act as managers
 * or something else. 
 * Note : 	Factories are not necessarily singletons. For instance you wanna two factories
 * 			if you have 2 presets for a single desired Type.
 *
 * Two ways to use this pattern : 
 * 1) You just want to clone an initial object : the LearnToClone method is recommanded.
 * 			Please make sure your product is a Factory Product 
 * 			(see interface FactoryProduct)	
 *			Also, please lookup the string passed in MakeFromString in knownTypes
 * 2) You have some extra work to do : modify your LearnToClone method so the desired type
 *  		cannot be learned that way, because you need something more powerfull.
 *			Then, 3 levels of complexity : 
 * 			1) 	If you don't need to pass parameters, specify in your MakeFromString the 
 *				special treatment according to your need. See MakeFromString exemple 
 *				implementation for details.
 *			2)	If want to pass parameters, it is recommanded to override Make() instead. 
 *				Parameter's	types will automatically forward calls to correct Make definition.
 *			3)	If you have reaaaaly complex things to do (like creating many object at once),
 *				Define a specific function in your factory.
 *				Keep in mind that this method ties the utilisation and the factory together,
 *				since you dont't rely on standard interface. on the other hand, you'll end up
 *				with explicit name for this manufacturing method. Use it wisely.
 *			
 *
 * Author : Martial
 */

package factories;
import java.util.Hashtable;

public abstract class AbstractFactory 
{

	// ===================  Cloning Learning methods ============================

	// Tells the factory to learn to make this kind of object. So you're able to modify this factory
	// at runtime.
	// The factory will clone the passed object into its internal memory, 
	// and will use this internal model to build other clones each time a creation is needed
	// Here you wanna reject types that requires more than a simple cloning to be built
	// Returns True only if Model was sucessfully learned, and False if type is rejected
	public abstract boolean LearnToClone(FactoryProduct model); // Exemple implementation
/*	{			
 *		// here A, B and C are the only ones to need a special treatment, in Make or MakeFromString
 *		// so you wanna reject them from the cloning mechanism
 *		// Please make sure you reject all classes that needs a special treatment
 *		if 			(toCheck instanceof A)	
 *			return false;
 *		else if 	(toCheck instanceof B)
 *			return false;
 *		else if 	(toCheck instanceof C)
 *			return false;
 *		else
 * 		{
 *			// Here only valid types arrives, so we register them.
 *			RegisterNewProduct(model);
 *			return true;
 * 		}
 *	}
 */	

	// Forget a learned model.
	// returns True only if className was previously learned.
	public final boolean ForgetHowToClone(String className)
	{
		if(IsRegistered(className))
		{
			productsModels.remove(className);
			return true;
		}
		else
			return false;
	}


	// ===================  Capabilities methods ============================

	// Tells whether Make is overrided whith that type
	// you have to write down here each class that is compatible.
	public abstract boolean CanMake(Object toCheck); // Exemple implementation
/*	{			
 *		// here A, B and C needs special treatment and are hardcoded in Make
 *		if 			(toCheck instanceof A)	
 *			return true;
 *		else if 	(toCheck instanceof B)
 *			return true;
 *		else if 	(toCheck instanceof C)
 *			return true;
 *		else	
 *			return false;
 *	}
 */


	// Verifies the avariability of provided class though its name as String.
	// you have to write down here each class that is compatible.
	public abstract boolean CanMakeFromString(String className); // Exemple implementation
/*	{			
 *		// here A, B and C needs special treatment and are hardcoded in MakeFromString
 *		// they are'nt necessarily the same as the ones of CanMake
 *		if 			(className == "A")	
 *			return true;
 *		else if 	(className == "B")
 *			return true;
 *		else if 	(className == "C")
 *			return true;
 *		else	
 *			return IsRegistered(className);
 *	}
 */


	// ===================  Manufacturing methods ============================

	// Cloning have to be made through MakeFromString. And parametrized or sophisticated
	// things should be done through Make overrides.
	// Of course you can define  other methods in your factories that will build a specific object

	// You'll override this method many times with different parameters which were, typically,
	// parameters for your constructor.
	// The arguments types will determine which method definition is called.
	// Note : if you have 2 differents objects with same argument types, add one that differs,
	//			 like a reference to an instance of you class.
	public Object Make() 
	{
		// you don't want to end up here, override if needed 
		return null;
	}
	

	// Takes a string and parses it to instantiate the corresponding class
	// Prefer Make override, so you can detach the usage of your objectr from its implementation
	public abstract Object MakeFromString(String className);// Exemple implementation
/*	{	
 *		//here there is A, B and C that needs special treatment
 *
 *		if 			(ClassName == "A")	{ ... } // special treatment for A
 *		else if 	(ClassName == "B")	{ ... } // special treatment for B
 *		else if 	(ClassName == "C")	{ ... } // special treatment for C
 *		else
 *		{
 *			// We know deal with 
 *			Object potentialClone = Clone(className);		
 *			if (potentialClone == null) 	
 *			{
 *				// Unsupported type, you must log an error here
 *			}
 *			return potentialClone; 
 *		}
 *	}
 */
	

	// Clones the Learned model and returns it.
	// returns null if className isn't founded
	protected final FactoryProduct Clone(String className)
	{
		if(IsRegistered(className))
			return productsModels.get(className).Clone();
		else
			return null;
	}
	

	// ============== Product's models memorization ====================

	// Registers the provided element in productsModels
	// You wanna call this when learning new object to clone
	protected final void RegisterNewProduct(FactoryProduct model)
	{
		// add a copy of provided model so client can modify passed object afterwards without
		// influencing this factory's behavior.

		// TODO : check that Model implements FactoryProduct
		// TODO : check if model type already exists
		productsModels.put(model.TypeName(), model.Clone()); 
	}

	// lookup a particular entry in this factory's models
	// returns true if founded, false otherwise.
	protected final boolean IsRegistered(String className)
	{
		return productsModels.containsKey(className);
	}

	// Map of supported types 
	protected Hashtable<String, FactoryProduct> productsModels;
}

/* ============================================================================
 * 								Example Factory 
 * ============================================================================
 * 
 * Just an example of what can been  done with the Factory interface.
 * This example is a singleton, because most factories are in real world 
 * applications.
 * Author : Martial

 */

/*
 *	TODO : 	See how to transmit constructors arguments trough MakeFrom methods 
 *			(templates ??)
 */
package Factories;

import java.util.Hashtable;


public class ExempleFactory implements AbstractFactory
{
	// List of class that this factory can instantiate.
	// It converts a CLassID integer to a String containing the class name.
	// This mechanism is strongly recommended
	private Hashtable<Integer, String> knownTypes;
	
	// ========================
	// Singleton matters :
	// ========================
	
	// Define the only instance of this factory
	private static final ExempleFactory INSTANCE = new ExempleFactory();
	 
	// Factory's getter 
    public static ExempleFactory getInstance() 
    {
        return INSTANCE;
    }
    
    
	// ========================
	// Factory matters :
	// ========================
    

	// Important: private constructor
    // called at first use of this factory
	private ExempleFactory() 
	{
		// List of all types that can be built
		knownTypes.put(1,"Pizza");
		knownTypes.put(2,"Trompet");
		knownTypes.put(3,"Mamoth");
	}
    
	// Takes the ClassID to instantiate this class
	public Object MakeFromID(int ClassID)
	{
		return MakeFromString(knownTypes.get(ClassID));
	}

	// Takes a string and parse it to instantiate the corresponding class
	public Object MakeFromString(String ObjDescription)
	{
		if (ObjDescription == "Pizza")
			return MakePizza();
		else if (ObjDescription == "Trompet")
			return MakeTrompet();
		else if (ObjDescription == "Mamoth")
			return MakeMamoth();
		else 
		{
			// Here, do log an error in log files, providing ObjDescription and 
			// a dump of knownTypes
			return null;
		}
			
		
	}

	// Verify the avariability of provided class (either ID or Class Name)
	public boolean canMake(int ClassID)
	{
		return knownTypes.containsKey(ClassID);
	}
	public boolean canMake(String ClassName)
	{
		return knownTypes.containsValue(ClassName);
	}
	
	

	// ========================
	// Building matters :
	// ========================

	// Theses class descriptions are just here to avoid Type error
	// You can use any existing type without defining it here
	public class Pizza {}
	public class Trompet {}
	public class Mamoth {}
	
	
	// Constructors of known types
    private Pizza MakePizza()
    {
    	// Delicious pizza delivered directly to INTech
		return null;
    }
    

    private Trompet MakeTrompet()
    {
    	// Remplace ronald troll when INTech's amplifier is HS
		return null;
    }
    

    private Mamoth MakeMamoth()
    {
    	// Particularly well known target for ping pong balls 
		return null;
    }
    
    
}

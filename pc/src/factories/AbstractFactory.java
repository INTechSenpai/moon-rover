/** ============================================================================
 * 							Abstract Factory interface
 * ============================================================================
 * 
 * This interface should be implemented by all factories throughout the robot.
 * Author : Martial
 */
package factories;

public interface AbstractFactory 
{
	// Takes a string and parse it to instantiate the corresponding class
	public Object MakeFromString(String ClassName);
	
	// Takes the ClassID to instantiate this class
	public Object MakeFromID(int ClassID);
		
	// Verify the avariability of provided class
	public boolean canMake(String ClassName);
	public boolean canMake(int ClassID);
	
	
		
	// Map of supported types
	//private Hashtable<Integer, String> knownTypes; // Cannot enforce this mechanism in an interface.
}

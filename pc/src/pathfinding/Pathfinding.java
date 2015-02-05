package pathfinding;
import java.util.ArrayList;

import smartMath.Vec2;

/**
 * Classe encapsulant les calculs de pathfinding
 * @author Marsya
 *
 */
public class Pathfinding
{
	/**
	 * Constructor
	 */
	public Pathfinding()
	{
		// TODO
	}
	
	public ArrayList<Vec2> computePath(Vec2 start, Vec2 end)
	{
		
		// TODO
		// voici un pathfinding mathématiquement démontré comme correct
		// correct au sens d'un chemin partant du départ et allant a l'arrivée
		
		// bon après si vous chipottez pour les obstacles en chemin aussi...
		
		ArrayList<Vec2> out = new ArrayList<Vec2>();
		out.add(end);
		return out;
	}
}

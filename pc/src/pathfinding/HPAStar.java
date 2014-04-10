/**
 * 
 */
package pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;

import pathfinding.SearchSpace.Grid2DSpace;
import smartMath.Vec2;

/**
 * @author Marsya
 *
 */
@SuppressWarnings("unused")
class HPAStar 
{
	private ArrayList<Grid2DSpace> espace;
	private Vec2		depart,
						arrivee;
				

	public HPAStar ( Grid2DSpace espaceVoulu)
	{
		espace = new ArrayList<Grid2DSpace>();
		espace.add(espaceVoulu.makeCopy());
	}
	
	public void initialise(Vec2 departVoulu, Vec2 arriveeVoule)
	{
		depart = departVoulu.makeCopy();
		arrivee = arriveeVoule.makeCopy();
	}
	
	public void process()
	{
		
	}
}

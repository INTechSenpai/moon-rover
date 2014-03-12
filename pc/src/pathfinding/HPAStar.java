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
import smartMath.IntPair;

/**
 * @author Marsya
 *
 */
@SuppressWarnings("unused")
public class HPAStar 
{
	private ArrayList<Grid2DSpace> espace;
	private IntPair		depart,
						arrivee;
				

	public HPAStar ( Grid2DSpace espaceVoulu)
	{
		espace = new ArrayList<Grid2DSpace>();
		espace.add(espaceVoulu.makeCopy());
	}
	
	public void initialise(IntPair departVoulu, IntPair arriveeVoule)
	{
		depart = departVoulu.makeCopy();
		arrivee = arriveeVoule.makeCopy();
	}
	
	public void process()
	{
		
	}
}

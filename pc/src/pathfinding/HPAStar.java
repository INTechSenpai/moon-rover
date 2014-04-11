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
class HPAStar implements PathfindingAlgo
{
	private ArrayList<Grid2DSpace> espace;
	private Vec2		depart,
						arrivee;
	private AStar solver;
				

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
	
	@Override
	public void process()
	{
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<Vec2> getChemin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setArrivee(Vec2 arrivee) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDepart(Vec2 depart) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}
}

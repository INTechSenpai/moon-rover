package pathfinding;

import java.util.ArrayList;

import smartMath.Vec2;

interface PathfindingAlgo {

	public void cleanup();
	public void process();
	public ArrayList<Vec2> getChemin();
	
	public void setArrivee(Vec2 arrivee);
	public void setDepart(Vec2 depart);
	public boolean isValid();

}

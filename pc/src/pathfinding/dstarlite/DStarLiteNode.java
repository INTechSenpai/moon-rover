package pathfinding.dstarlite;

/**
 * Un nœud du pathfinding
 * @author pf
 *
 */

public class DStarLiteNode {

	public final int gridpoint;
	public Cle cle = new Cle();
	public int g = Integer.MAX_VALUE, rhs = Integer.MAX_VALUE;
	public boolean done = false;
	public long nbPF = 1;
	
	public DStarLiteNode(int gridpoint)
	{
		this.gridpoint = gridpoint;
	}
	
	@Override
	public int hashCode()
	{
		return gridpoint;
	}
	
	@Override
	public boolean equals(Object o)
	{
		return gridpoint == o.hashCode();
	}
	
	@Override
	public String toString()
	{
		int x = gridpoint & (GridSpace.NB_POINTS_POUR_TROIS_METRES - 1);
		int y = gridpoint >> GridSpace.PRECISION;
		return x+" "+y+" ("+cle+")";
	}
	
}
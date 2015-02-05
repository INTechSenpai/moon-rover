package table.obstacles;

import smartMath.Vec2;

/**
 * Obstacle circulaire
 * @author pf
 *
 */
public class ObstacleCirculaire extends Obstacle
{
	// le Vec2 "position" indique le centre de l'obstacle
	
	// rayon de cet obstacle
	protected int radius;
	
	public ObstacleCirculaire(Vec2 position, int rad)
	{
		super(position);
		this.radius = rad;
	}
	
	public ObstacleCirculaire clone()
	{
		return new ObstacleCirculaire(position.clone(), radius);
	}

	// Copie this dans oc, sans modifier this
	public void clone(ObstacleCirculaire oc)
	{
		oc.position = position;
		oc.radius = radius;
	}

	public int getRadius()
	{
		return radius;
	}
	
	public String toString()
	{
		return super.toString()+", rayon: "+radius;
	}
}

package table;

import smartMath.Vec2;

public class ObstacleCirculaire extends Obstacle
{

	protected float radius;
	
	public ObstacleCirculaire(Vec2 position, float rad)
	{
		super(position);
		this.radius = rad;
	}
	
	public ObstacleCirculaire clone()
	{
		return new ObstacleCirculaire(position.clone(), radius);
	}
	public float getRadius()
	{
		return radius;
	}
	public String toString()
	{
		return super.toString()+", rayon: "+radius;
	}
}

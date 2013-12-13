package table;

import smartMath.Vec2;

class ObstacleCirculaire extends Obstacle
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

}

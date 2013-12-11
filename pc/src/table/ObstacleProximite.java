package table;

import smartMath.Vec2;

public class ObstacleProximite extends Obstacle {

	public long death_date;
	
	public ObstacleProximite (Vec2 position, float rad, long death_date)
	{
		super(position,rad);
		this.death_date = death_date;
	}
	
	
}

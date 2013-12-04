package table;

import smartMath.Vec2;

public class ObstacleProximite extends Obstacle {

	private int death_date;
	
	public ObstacleProximite (Vec2 position, float rad, int death_date)
	{
		super(position,rad);
		this.death_date = death_date;
	}
	
	
}

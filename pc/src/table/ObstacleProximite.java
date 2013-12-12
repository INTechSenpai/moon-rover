package table;

import smartMath.Vec2;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf
 *
 */
public class ObstacleProximite extends Obstacle {

	public long death_date;
	
	public ObstacleProximite (Vec2 position, float rad, long death_date)
	{
		super(position,rad);
		this.death_date = death_date;
	}
	
	public ObstacleProximite clone()
	{
		return new ObstacleProximite(position, radius, death_date);
	}
	
}

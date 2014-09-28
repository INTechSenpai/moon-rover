package table.obstacles;

import smartMath.Vec2;

/**
 * Obstacles détectés par capteurs de proximité (ultrasons et infrarouges)
 * @author pf, marsu
 */
class ObstacleProximite extends ObstacleCirculaire
{
	public ObstacleProximite (Vec2 position, int rad)
	{
		super(position,rad);
	}
	
	public ObstacleProximite clone()
	{
		return new ObstacleProximite(position.clone(), getRadius());
	}
}

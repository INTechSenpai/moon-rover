package table;
import smartMath.Vec2;

/**
 * Obstacles détectés par balise. On connaît leur vitesse.
 * @author pf
 *
 */
class ObstacleBalise extends ObstacleCirculaire {

	private Vec2 speed;
	
	public ObstacleBalise (Vec2 position, float rad, Vec2 spe)
	{
		super(position,rad);
		this.speed = spe;
	}
	
	public ObstacleBalise clone()
	{
		return new ObstacleBalise(position.clone(), radius, speed);
	}
}

package table;
import smartMath.Vec2;

/**
 * Obstacles détectés par balise. On connaît leur vitesse.
 * @author pf
 *
 */
public class ObstacleBalise extends Obstacle {

	private Vec2 speed;
	
	public ObstacleBalise (Vec2 position, float rad, Vec2 spe)
	{
		super(position,rad);
		this.speed = spe;
	}
}

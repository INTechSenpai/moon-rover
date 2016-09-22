package capteurs;

import robot.Cinematique;
import utils.Vec2RO;

public class CapteurImmobile extends Capteur
{

	public CapteurImmobile(Vec2RO positionRelative, double orientationRelative, double angleCone, int portee) {
		super(positionRelative, orientationRelative, angleCone, portee);
	}

	@Override
	public double getOrientationRelative(Cinematique c)
	{
		return orientationRelative;
	}

}

/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package capteurs;

import robot.Cinematique;
import utils.Vec2RO;

/**
 * Capteur qui regarde dans le sens des roues
 * Si les roues tournent, le capteur tourne aussi
 * @author pf
 *
 */

public class CapteurMobile extends Capteur
{
	private boolean droite;

	/**
	 * L'orientation relative à donner est celle du capteur lorsque les roues sont droites (courbure nulle)
	 * @param positionRelative
	 * @param orientationRelative
	 * @param angleCone
	 * @param portee
	 */
	public CapteurMobile(Vec2RO positionRelative, double orientationRelative, double angleCone, int portee, boolean droite)
	{
		super(positionRelative, orientationRelative, angleCone, portee);
		this.droite = droite;
	}

	@Override
	public double getOrientationRelative(Cinematique c)
	{
		if(Math.abs(c.courbure) < 0.01)
			return orientationRelative;
		double R = 1000 / c.courbure; // le rayon de courbure
		if(droite)
			return orientationRelative + Math.atan2(L, d+R);
		return orientationRelative + Math.atan2(L, d-R);
	}

}

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

/**
 * Les différents types de capteurs
 * @author pf
 *
 */

public enum TypeCapteur
{
	// IR : cône de 5°, horizon à 80mm, distance min 20mm
	// ToF : cône de 0.1°, horizon à 254mm, distance min 0mm
	// ToF longue portée : cône de 0.1°, horizon à 2m

	ToF_COURT(0.1, 1, 254),
	ToF_LONG(0.1, 1, 2000),
	IR(5. / 180 * Math.PI, 100, 630);
	
	public final double angleCone; // ne sert qu'à l'affichage
	public final int distanceMin, portee;
	
	private TypeCapteur(double angleCone, int distanceMin, int portee)
	{
		this.angleCone = angleCone;
		this.distanceMin = distanceMin;
		this.portee = portee;
	}
}

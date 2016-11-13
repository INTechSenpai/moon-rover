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

import graphic.printable.Couleur;

import java.awt.Color;

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

	ToF_COURT(0.01, 1, 254, Couleur.ToF_COURT),
	ToF_LONG(0.01, 1, 2000, Couleur.ToF_LONG),
	IR(5. / 180 * Math.PI, 100, 630, Couleur.IR);
	
	public final double angleCone; // ne sert qu'à l'affichage
	public final int distanceMin, portee;
	public Color couleur, couleurTransparente;
	
	private TypeCapteur(double angleCone, int distanceMin, int portee, Couleur c)
	{
		couleur = c.couleur;
		couleurTransparente = new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 100);
		this.angleCone = angleCone;
		this.distanceMin = distanceMin;
		this.portee = portee;
	}
}

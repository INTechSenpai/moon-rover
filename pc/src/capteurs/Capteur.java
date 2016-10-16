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

import java.awt.Color;
import java.awt.Graphics;

import config.Config;
import config.ConfigInfo;
import graphic.Fenetre;
import graphic.printable.Layer;
import graphic.printable.Printable;
import robot.Cinematique;
import robot.RobotReal;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Un capteur de proximité du robot
 * @author pf
 *
 */

public abstract class Capteur implements Printable
{
	public boolean sureleve;
	public final Vec2RO positionRelative;
	protected final double orientationRelative;
	public final double angleCone; // angle du cône (en radians)
	public final int portee;
	public final int distanceMin;
	protected static int L, d;
	
	public Capteur(Vec2RO positionRelative, double orientationRelative, TypeCapteur type, boolean sureleve)
	{
		this.positionRelative = positionRelative;
		this.orientationRelative = orientationRelative;
		this.angleCone = type.angleCone;
		this.distanceMin = type.distanceMin;
		this.portee = type.portee;
		this.sureleve = sureleve;
	}
	
	public static void useConfig(Config config)
	{
		L = config.getInt(ConfigInfo.DISTANCE_ROUES_AVANT_ET_ARRIERE);
		d = config.getInt(ConfigInfo.DISTANCE_ROUES_GAUCHE_ET_DROITE) / 2;
	}

	public abstract double getOrientationRelative(Cinematique c);
	
	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		double orientation = robot.getCinematique().orientationReelle;
		Vec2RW p1 = positionRelative.clone();
		p1.rotate(orientation);
		p1.plus(robot.getCinematique().getPosition());
		Vec2RW p2 = positionRelative.clone();
		p2.plus(new Vec2RO(portee, angleCone + getOrientationRelative(robot.getCinematique()), false));
		p2.rotate(orientation);
		p2.plus(robot.getCinematique().getPosition());
		Vec2RW p3 = positionRelative.clone();
		p3.plus(new Vec2RO(portee, - angleCone + getOrientationRelative(robot.getCinematique()), false));
		p3.rotate(orientation);
		p3.plus(robot.getCinematique().getPosition());
		int[] x = new int[3];
		x[0] = f.XtoWindow(p1.getX());
		x[1] = f.XtoWindow(p2.getX());
		x[2] = f.XtoWindow(p3.getX());
		int[] y = new int[3];
		y[0] = f.YtoWindow(p1.getY());
		y[1] = f.YtoWindow(p2.getY());
		y[2] = f.YtoWindow(p3.getY());
		g.setColor(new Color(0, 130, 0, 50));
		g.fillPolygon(x, y, 3);
		g.setColor(new Color(0, 130, 0, 255));
		g.drawPolygon(x, y, 3);
	}

	@Override
	public Layer getLayer() {
		return Layer.FOREGROUND;
	}

}

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

package robot;

import java.awt.Graphics;

import obstacles.types.ObstacleRectangular;
import config.Config;
import config.ConfigInfo;
import container.Service;
import utils.Log;
import graphic.Fenetre;
import graphic.PrintBuffer;
import graphic.printable.Layer;
import graphic.printable.Printable;

/**
 * Effectue le lien entre le code et la réalité (permet de parler à la carte bas niveau, d'interroger les capteurs, etc.)
 * @author pf
 *
 */

public class RobotReal extends Robot implements Service, Printable
{
	protected volatile boolean matchDemarre = false;
    protected volatile long dateDebutMatch;
	private boolean print;
	private PrintBuffer buffer;
	
	// Constructeur
	public RobotReal(Log log, PrintBuffer buffer)
 	{
		super(log);
		this.buffer = buffer;
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */

	@Override
	public synchronized void updateConfig(Config config)
	{
		super.updateConfig(config);
		dateDebutMatch = config.getLong(ConfigInfo.DATE_DEBUT_MATCH);
		matchDemarre = config.getBoolean(ConfigInfo.MATCH_DEMARRE);
	}
	
	@Override
	public void useConfig(Config config)
	{
		super.useConfig(config);
		cinematique = new Cinematique(0, 300, 0, true, 3, Speed.STANDARD);
		print = config.getBoolean(ConfigInfo.GRAPHIC_ROBOT_AND_SENSORS);
		if(print)
			buffer.add(this);
	}
			
	public void setEnMarcheAvance(boolean enMarcheAvant)
	{
		cinematique.enMarcheAvant = enMarcheAvant;
	}
	
	@Override
    public long getTempsDepuisDebutMatch()
    {
		if(!matchDemarre)
			return 0;
		return System.currentTimeMillis() - dateDebutMatch;
    }

	@Override
	public void setCinematique(Cinematique cinematique)
	{
		super.setCinematique(cinematique);
		synchronized(buffer)
		{
			buffer.notify();
		}
	}

	public Cinematique getCinematique()
	{
		return cinematique;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		// affichage rudimentaire
		new ObstacleRectangular(cinematique.getPosition(), longueurNonDeploye, largeurNonDeploye, cinematique.orientationGeometrique).print(g, f, robot);
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

}

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
import container.Service;
import utils.Log;
import utils.Vec2RO;
import utils.Config;
import utils.ConfigInfo;
import serie.Ticket;
import exceptions.UnableToMoveException;
import exceptions.UnexpectedObstacleOnPathException;
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
		cinematique = new Cinematique(0, 0, 0, true, 0, 0, 0, Speed.STANDARD);
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

    /**
     * Bloque jusqu'à ce que la carte donne un status.
     * - Soit le robot a fini le mouvement, et la méthode se termine
     * - Soit le robot est bloqué, et la méthod lève une exception
     * ATTENTION ! Il faut que cette méthode soit appelée dans un synchronized(requete)
     * @throws UnableToMoveException
     * @throws InterruptedException 
     */
    public void attendStatus(Ticket t) throws UnableToMoveException, UnexpectedObstacleOnPathException, InterruptedException
    {
		Ticket.State o;
		do {
			if(t.isEmpty())
			{
				// Si au bout de 3s le robot n'a toujours rien répondu,
				// on suppose un blocage mécanique
				t.set(Ticket.State.KO);
				t.wait(15000);
			}

			o = t.getAndClear();
			if(o == Ticket.State.KO)
				throw new UnableToMoveException("Le bas niveau prévenu d'un problème mécanique");
/*				else if(o == SerialProtocol.ENNEMI_SUR_CHEMIN)
				{
					log.critical("Ennemi sur le chemin !");
					serialOutput.immobilise();
					Sleep.sleep(4000);
					throw new UnexpectedObstacleOnPathException();
				}*/
		} while(o != Ticket.State.OK);

    }

	public void setCinematique(Cinematique cinematique)
	{
		cinematique.copy(this.cinematique);
	}

	public Vec2RO getPosition()
	{
		return cinematique.getPosition();
	}

	public double getOrientation()
	{
		return cinematique.orientation;
	}

	public Cinematique getCinematique()
	{
		return cinematique;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		// affichage rudimentaire
		new ObstacleRectangular(cinematique.getPosition(), longueurNonDeploye, largeurNonDeploye, cinematique.orientation).print(g, f, robot);;
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

}

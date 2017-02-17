/*
Copyright (C) 2013-2017 Pierre-François Gimenez

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
import java.lang.reflect.InvocationTargetException;

import obstacles.types.ObstacleRobot;
import serie.BufferOutgoingOrder;
import serie.Ticket;
import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import container.dependances.CoreClass;
import exceptions.UnableToMoveException;
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

public class RobotReal extends Robot implements Service, Printable, Configurable, CoreClass
{
	protected volatile boolean matchDemarre = false;
    protected volatile long dateDebutMatch;
    private int demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant;
    private int nbRetente;
	private boolean print;
	private PrintBuffer buffer;
	private BufferOutgoingOrder out;
    private boolean cinematiqueInitialised = false;

	// Constructeur
	public RobotReal(Log log, BufferOutgoingOrder out, PrintBuffer buffer)
 	{
		super(log);
		this.buffer = buffer;
		this.out = out;
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
		cinematique = new Cinematique(0, 300, 0, true, 3, Speed.STANDARD.translationalSpeed);
		print = config.getBoolean(ConfigInfo.GRAPHIC_ROBOT_AND_SENSORS);
		demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		nbRetente = config.getInt(ConfigInfo.NB_TENTATIVES_ACTIONNEURS);
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

	public boolean isCinematiqueInitialised()
	{
		return cinematiqueInitialised;
	}
	
	@Override
	public synchronized void setCinematique(Cinematique cinematique)
	{
		super.setCinematique(cinematique);
		/*
		 * On vient juste de récupérer la position initiale
		 */
		if(!cinematiqueInitialised)
		{
			cinematiqueInitialised = true;
			notifyAll();
		}
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
		ObstacleRobot o = new ObstacleRobot(robot);
		o.update(cinematique.getPosition(), cinematique.orientationReelle);
		o.print(g, f, robot);
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

	public int getDemieLargeurGauche()
	{
		return demieLargeurNonDeploye;
	}

	public int getDemieLargeurDroite()
	{
		return demieLargeurNonDeploye;
	}

	public int getDemieLongueurAvant()
	{
		return demieLongueurAvant;
	}

	public int getDemieLongueurArriere()
	{
		return demieLongueurArriere;
	}
	
	/*
	 * DÉPLACEMENTS
	 */
	
	@Override
	public void avance(double distance) throws UnableToMoveException
	{
		// TODO
	}
	
	/*
	 * ACTIONNEURS
	 */
	
	/**
	 * Rend bloquant l'appel d'une méthode
	 * @param m
	 * @throws InterruptedException
	 */
	@Override
	protected void bloque(String nom, Object... param) throws InterruptedException
	{
		Ticket.State etat;
		Ticket t = null;
		Class<?>[] paramClasses = new Class[param.length];
		for(int i = 0; i < param.length; i++)
			paramClasses[i] = param[i].getClass();
		int nbEssai = nbRetente;
		do {
			try {
				t = (Ticket) BufferOutgoingOrder.class.getMethod(nom, paramClasses.length == 0 ? null : paramClasses).invoke(out, param.length == 0 ? null : param);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			synchronized(t)
			{
				if(t.isEmpty())
					t.wait();
			}
			etat = t.getAndClear();
			nbEssai--;
			if(etat == Ticket.State.KO)
				log.warning("Problème pour l'actionneur "+nom+" : on "+(nbEssai >= 0 ? "retente." : "abandonne."));
		} while(nbEssai >= 0 && etat == Ticket.State.KO);
	}

}

/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

import capteurs.SensorMode;
import config.Config;
import config.ConfigInfo;
import container.Container;
import exceptions.MemoryManagerException;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import pathfinding.KeyPathCache;
import pathfinding.PathCache;
import pathfinding.RealGameState;
import robot.Cinematique;
import robot.RobotReal;
import scripts.ScriptsSymetrises;
import serie.BufferOutgoingOrder;
import serie.SerialProtocol;
import serie.Ticket;
import utils.Log;

/**
 * WOLOLOOOOOGATION
 * 
 * @author pf
 *
 */

public class Homologation
{

	/**
	 * Position de départ : (550, 1905), -Math.PI/2
	 * 
	 * @param args
	 */

	public static void main(String[] args)
	{
		Container container = null;
		Ticket ticketFinMatch = null;
		Log log = null;
		long dateDebutMatch = System.currentTimeMillis();
		try
		{
			container = new Container();
			log = container.getService(Log.class);
			Config config = container.getService(Config.class);
			BufferOutgoingOrder data = container.getService(BufferOutgoingOrder.class);
			RobotReal robot = container.getService(RobotReal.class);
			PathCache path = container.getService(PathCache.class);
			RealGameState state = container.getService(RealGameState.class);
			boolean simuleSerie = config.getBoolean(ConfigInfo.SIMULE_SERIE);

			log.debug("Initialisation des actionneurs…");

			/*
			 * Initialise les actionneurs
			 */
			robot.initActionneurs();

			log.debug("Actionneurs initialisés");

			robot.setSensorMode(SensorMode.ALL);

			log.debug("Attente de la couleur…");

			/*
			 * Demande de la couleur
			 */
			if(!simuleSerie)
			{
				SerialProtocol.State etat;
				do
				{
					Ticket t = data.demandeCouleur();
					etat = t.attendStatus().etat;
				} while(etat != SerialProtocol.State.OK);
			}
			log.debug("Couleur récupérée");
			
			Thread.sleep(500);
			boolean sym = config.getSymmetry();
			
			/*
			 * La couleur est connue : on commence le stream de position
			 */
			data.startStream();

			log.debug("Stream des positions et des capteurs lancé");

			/*
			 * On attend d'avoir l'info de position
			 */
			if(simuleSerie)
				robot.setCinematique(new Cinematique(550, 1905, -Math.PI / 2, true, 0));
			else
			{
				synchronized(robot)
				{
					if(!robot.isCinematiqueInitialised())
						robot.wait();
				}
			}

			log.debug("Cinématique initialisée : " + robot.getCinematique());

			KeyPathCache k = new KeyPathCache(state);
			k.shoot = false;
			k.s = ScriptsSymetrises.SCRIPT_HOMOLO_A_NOUS.getScript(sym);
			path.prepareNewPath(k);
			
			log.debug("Attente du jumper…");

			/*
			 * Attente du jumper
			 */
			if(!simuleSerie)
			{
				SerialProtocol.State etat;
				do
				{
					Ticket t = data.waitForJumper();
					etat = t.attendStatus().etat;
				} while(etat != SerialProtocol.State.OK);
			}

			log.debug("LE MATCH COMMENCE !");

			/*
			 * Le match a commencé !
			 */
			ticketFinMatch = data.startMatchChrono();
			dateDebutMatch = System.currentTimeMillis();
			log.debug("Chrono démarré");

			try
			{
				path.computeAndFollow(k);
				k.s.s.execute(state);
			}
			catch(PathfindingException | UnableToMoveException | MemoryManagerException e)
			{
				e.printStackTrace();
				e.printStackTrace(log.getPrintWriter());
			}
		}
		catch(Exception e)
		{
			ticketFinMatch = null; // pour arrêter proprement
			e.printStackTrace();
			if(log != null)
				e.printStackTrace(log.getPrintWriter());
		}
		finally
		{
			try
			{
				if(ticketFinMatch != null)
					ticketFinMatch.attendStatus(95000 - (System.currentTimeMillis() - dateDebutMatch));
				System.exit(container.destructor().code);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				if(log != null)
					e.printStackTrace(log.getPrintWriter());
			}
		}
	}

}

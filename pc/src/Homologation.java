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

import capteurs.SensorMode;
import config.Config;
import config.ConfigInfo;
import container.Container;
import exceptions.ContainerException;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import pathfinding.KeyPathCache;
import pathfinding.PathCache;
import pathfinding.RealGameState;
import robot.Cinematique;
import robot.RobotReal;
import scripts.ScriptNames;
import serie.BufferOutgoingOrder;
import serie.SerialProtocol;
import serie.Ticket;
import utils.Log;

/**
 * WOLOLOOOOOGATION
 * @author pf
 *
 */

public class Homologation {

	/**
	 * Position de départ : (550, 1905), -Math.PI/2
	 * @param args
	 */
	
	public static void main(String[] args)
	{
		Container container = null;
		try {
			container = new Container();
			Log log = container.getService(Log.class);
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
				do {
					Ticket t = data.demandeCouleur();
					etat = t.attendStatus().etat;
				} while(etat != SerialProtocol.State.OK);
			}			
			log.debug("Couleur récupérée");
			
			/*
			 * La couleur est connue : on commence le stream de position
			 */
			data.startStream();
			
			log.debug("Stream des positions et des capteurs lancé");
			
			/*
			 * On attend d'avoir l'info de position
			 */
			if(simuleSerie)
				robot.setCinematique(new Cinematique(550, 1905, -Math.PI/2, true, 0));
			else
			{
				synchronized(robot)
				{
					if(!robot.isCinematiqueInitialised())
						robot.wait();
				}
			}
			
			log.debug("Cinématique initialisée : "+robot.getCinematique());
			
			log.debug("Attente du jumper…");
			
			/*
			 * Attente du jumper
			 */
			if(!simuleSerie)
			{
				SerialProtocol.State etat;
				do {
					Ticket t = data.waitForJumper();
					etat = t.attendStatus().etat;
				} while(etat != SerialProtocol.State.OK);
			}
			
			log.debug("LE MATCH COMMENCE !");
			
			/*
			 * Le match a commencé !
			 */
			data.startMatchChrono();
			
			log.debug("Chrono démarré");
			
			KeyPathCache k = new KeyPathCache(state);
			k.shoot = false;
			k.s = ScriptNames.SCRIPT_CRATERE_HAUT_DROITE;
			try {
				path.computeAndFollow(k);
				k.s.s.execute(state);
				
				k.s = ScriptNames.SCRIPT_DEPOSE_MINERAI;
				path.computeAndFollow(k);
				k.s.s.execute(state);
			} catch (PathfindingException e) {
				e.printStackTrace();
				e.printStackTrace(log.getPrintWriter());
			} catch (UnableToMoveException e) {
				e.printStackTrace();
				e.printStackTrace(log.getPrintWriter());
			}
		} catch (ContainerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
		finally
		{
			if(container != null)
				try {
					container.destructor();
				} catch (ContainerException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}
	
}

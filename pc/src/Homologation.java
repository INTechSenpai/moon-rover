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

import container.Container;
import exceptions.ContainerException;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import pathfinding.KeyPathCache;
import pathfinding.PathCache;
import pathfinding.RealGameState;
import robot.RobotReal;
import robot.Speed;
import scripts.ScriptManager;
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
	 * Position de départ : (700, 1800), Math.PI
	 * @param args
	 */
	
	public static void main(String[] args)
	{
		Container container = null;
		try {
			container = new Container();
			Log log = container.getService(Log.class);
			BufferOutgoingOrder data = container.getService(BufferOutgoingOrder.class);
			RobotReal robot = container.getService(RobotReal.class);
			PathCache path = container.getService(PathCache.class);
			RealGameState state = container.getService(RealGameState.class);
			ScriptManager sm = container.getService(ScriptManager.class);
			
			log.debug("Initialisation des actionneurs…");
			
			/*
			 * Initialise les actionneurs
			 */
			robot.initActionneurs();
			
			log.debug("Actionneurs initialisés");
			
			log.debug("Attente de la couleur…");
			
			/*
			 * Demande de la couleur
			 */
			SerialProtocol.State etat;
			do {
				Ticket t = data.demandeCouleur();
				etat = t.attendStatus().etat;
			} while(etat != SerialProtocol.State.OK);
			
			log.debug("Couleur récupérée");
			
			/*
			 * La couleur est connue : on commence le stream de position
			 */
			data.startStream();
			
			log.debug("Stream des positions et des capteurs lancé");
			
			/*
			 * On attend d'avoir l'info de position
			 */
			synchronized(robot)
			{
				if(!robot.isCinematiqueInitialised())
					robot.wait();
			}
			
			log.debug("Cinématique initialisée : "+robot.getCinematique());
			
			log.debug("Attente du jumper…");
			
			/*
			 * Attente du jumper
			 */
			do {
				Ticket t = data.waitForJumper();
				etat = t.attendStatus().etat;
			} while(etat != SerialProtocol.State.OK);

			log.debug("LE MATCH COMMENCE !");
			
			/*
			 * Le match a commencé !
			 */
			data.startMatchChrono();
			
			log.debug("Chrono démarré");
			
			KeyPathCache k = new KeyPathCache(state);
			k.shoot = false;
			k.s = sm.getScripts().get("MINERAI_CRATERE_HAUT_DROITE");
			try {
				path.prepareNewPathToScript(k);
				path.sendPreparedPath();
				robot.followTrajectory(Speed.TEST);
				k.s.execute(state);
				
				k.s = sm.getScripts().get("DEPOSE");
				path.prepareNewPathToScript(k);
				path.sendPreparedPath();
				robot.followTrajectory(Speed.TEST);
				k.s.execute(state);
			} catch (PathfindingException e) {
				e.printStackTrace();
			} catch (UnableToMoveException e) {
				e.printStackTrace();
			}
		} catch (ContainerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
		finally
		{
			if(container != null)
				try {
					container.destructor(false);
				} catch (ContainerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
}

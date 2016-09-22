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

package threads.serie;

import capteurs.SensorsData;
import capteurs.SensorsDataBuffer;
import container.Container;
import exceptions.ContainerException;
import robot.Cinematique;
import robot.RobotColor;
import robot.RobotReal;
import robot.Speed;
import serie.BufferIncomingOrder;
import serie.Ticket;
import serie.SerialProtocol.InOrder;
import serie.SerialProtocol.OutOrder;
import serie.trame.Paquet;
import threads.ThreadShutdown;
import threads.ThreadService;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2RO;
import pathfinding.astarCourbe.arcs.ClothoidesComputer;
import pathfinding.chemin.CheminPathfinding;

/**
 * Thread qui écoute la série et appelle qui il faut.
 * @author pf
 *
 */

public class ThreadSerialInputCoucheOrdre extends ThreadService
{
	protected Log log;
	protected Config config;
	private BufferIncomingOrder serie;
	private SensorsDataBuffer buffer;
	private RobotReal robot;
	private CheminPathfinding chemin;
	private Container container;
	
	private boolean capteursOn = false;
	private boolean matchDemarre = false;
	private boolean debugSerie;
	private int nbCapteurs;
	
	public ThreadSerialInputCoucheOrdre(Log log, Config config, BufferIncomingOrder serie, SensorsDataBuffer buffer, RobotReal robot, CheminPathfinding chemin, Container container)
	{
		this.container = container;
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		this.robot = robot;
		this.chemin = chemin;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			while(true)
			{
				synchronized(serie)
				{
					if(serie.isEmpty())
						serie.wait();

					Paquet paquet = serie.poll();
					int[] data = paquet.message;
					
					/**
					 * Couleur du robot
					 */
					if(paquet.origine == OutOrder.ASK_COLOR)
					{
						if(!matchDemarre)
						{
							if(data[0] == InOrder.COULEUR_ROBOT_DROITE.codeInt || data[0] == InOrder.COULEUR_ROBOT_GAUCHE.codeInt)
							{
								paquet.ticket.set(Ticket.State.OK);
								config.set(ConfigInfo.COULEUR, RobotColor.getCouleur(data[0] == InOrder.COULEUR_ROBOT_GAUCHE.codeInt));
							}
							else
							{
								paquet.ticket.set(Ticket.State.KO);
								if(data[0] != InOrder.COULEUR_ROBOT_INCONNU.codeInt)
									log.critical("Code couleur inconnu : "+data[0]);
							}
						}
						else
							log.critical("Le bas niveau a signalé un changement de couleur en plein match : "+data[0]);
					}
					
					/**
					 * Capteurs
					 */
					else if(paquet.origine == OutOrder.START_STREAM_ALL)
					{
						/**
						 * Récupération de la position et de l'orientation
						 */
						int xRobot = data[0] << 4;
						xRobot += data[1] >> 4;
						xRobot -= 1500;
						int yRobot = (data[1] & 0x0F) << 8;
						yRobot = yRobot + data[2];
						Vec2RO positionRobot = new Vec2RO(xRobot, yRobot);
		
						double orientationRobot = ((data[3] << 8) + data[4]) / 1000.;
						int indexTrajectory = data[5];
						chemin.setCurrentIndex(indexTrajectory / ClothoidesComputer.NB_POINTS);
						
						if(debugSerie)
							log.debug("Le robot est en "+positionRobot+", orientation : "+orientationRobot);
		
						// TODO récupérer à partir de l'index trajectory les info de cinématique
						Cinematique c = new Cinematique(xRobot, yRobot, orientationRobot, true, 0, 0, 0, Speed.STANDARD);
						robot.setCinematique(c);
						
						if(data.length > 6) // la présence de ces infos n'est pas systématiques
						{
							/**
							 * Acquiert ce que voit les capteurs
						 	 */
							int[] mesures = new int[nbCapteurs];
							for(int i = 0; i < nbCapteurs / 2; i++)
							{
								mesures[2*i] = (data[10+3*i] << 4) + (data[10+3*i+1] >> 4);
								mesures[2*i+1] = ((data[10+3*i+1] & 0x0F) << 8) + data[10+3*i+2];
							}
							if(capteursOn)
								buffer.add(new SensorsData(mesures, c));
						}

					}
		
					/**
					 * Démarrage du match
					 */
					else if(paquet.origine == OutOrder.WAIT_FOR_JUMPER)
					{
						capteursOn = true;
						synchronized(config)
						{
							config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
							config.set(ConfigInfo.MATCH_DEMARRE, true);
							matchDemarre = true;
							paquet.ticket.set(Ticket.State.OK);
						}
					}
					
					/**
					 * Fin du match, on coupe la série et on arrête ce thread
					 */
					else if(paquet.origine == OutOrder.START_MATCH_CHRONO)
					{
						log.debug("Fin du Match !");
						
						// On lance manuellement le thread d'arrêt
						ThreadShutdown t;
						try {
							t = container.getService(ThreadShutdown.class);
							Runtime.getRuntime().removeShutdownHook(t);
							t.start();
						} catch (ContainerException e) {
							log.critical(e);
						}
						
						// On attend d'être arrêté
						while(true)
							Thread.sleep(1000);
					}
							
					/**
					 * Le robot est arrivé après un arrêt demandé par le haut niveau
					 */
					else if(paquet.origine == OutOrder.FOLLOW_TRAJECTORY)
					{

						if(data[0] == InOrder.ROBOT_ARRIVE.codeInt)
							paquet.ticket.set(Ticket.State.OK);
						else
						{
							paquet.ticket.set(Ticket.State.KO);
							if(data[0] != InOrder.ROBOT_BLOQUE.codeInt && data[0] != InOrder.PLUS_DE_POINTS.codeInt)
								log.critical("Code fin mouvement inconnu : "+data[0]);
						}
					}
					
					else if(data.length != 0)
						log.critical("On a ignoré un paquet d'origine "+paquet.origine+" (taille : "+data.length+")");

				}
			}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		debugSerie = config.getBoolean(ConfigInfo.DEBUG_SERIE);
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS);
	}

}

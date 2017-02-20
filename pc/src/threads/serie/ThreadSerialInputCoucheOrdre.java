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

package threads.serie;

import capteurs.CapteursRobot;
import capteurs.SensorsData;
import capteurs.SensorsDataBuffer;
import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Container;
import container.dependances.SerialClass;
import exceptions.ContainerException;
import robot.Cinematique;
import robot.RobotColor;
import robot.RobotReal;
import serie.BufferIncomingOrder;
import serie.BufferOutgoingOrder;
import serie.Ticket;
import serie.SerialProtocol.InOrder;
import serie.SerialProtocol.OutOrder;
import serie.trame.Paquet;
import threads.ThreadShutdown;
import threads.ThreadService;
import utils.Log;
import pathfinding.chemin.CheminPathfinding;

/**
 * Thread qui écoute la série et appelle qui il faut.
 * @author pf
 *
 */

public class ThreadSerialInputCoucheOrdre extends ThreadService implements Configurable, SerialClass
{
	protected Log log;
	protected Config config;
	private BufferIncomingOrder serie;
	private SensorsDataBuffer buffer;
	private RobotReal robot;
	private CheminPathfinding chemin;
	private Container container;
	private BufferOutgoingOrder out;
	
	private boolean capteursOn = false;
	private boolean matchDemarre = false;
	private double lastVitesse = -1;
	private boolean debugSerie;
	private int nbCapteurs;
	
	public ThreadSerialInputCoucheOrdre(Log log, Config config, BufferIncomingOrder serie, SensorsDataBuffer buffer, RobotReal robot, CheminPathfinding chemin, Container container, BufferOutgoingOrder out)
	{
		this.container = container;
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.buffer = buffer;
		this.robot = robot;
		this.chemin = chemin;
		this.out = out;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de "+Thread.currentThread().getName());
		
		nbCapteurs = CapteursRobot.values().length;
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

						// On ne récupère pas toutes les infos mécaniques (la courbure manque, marche avant, …)
						// Du coup, on récupère les infos théoriques (à partir du chemin) qu'on complète
						double orientationRobot = ((data[3] << 8) + data[4]) / 1000.;
						int indexTrajectory = data[5];
						Cinematique current = chemin.setCurrentIndex(indexTrajectory);
						current.getPositionEcriture().setX(xRobot);
						current.getPositionEcriture().setY(yRobot);
						current.orientationReelle = orientationRobot;
						robot.setCinematique(current);
						
						// la vitesse de planification est gérée directement dans le pathfinding
						double tmpVitesse = current.vitesseMax;
						
						if(!current.enMarcheAvant) // la vitesse doit être signée
							tmpVitesse = -tmpVitesse;
						
						if(tmpVitesse != lastVitesse) // la vitesse a changé : on la renvoie
						{
							out.setMaxSpeed(tmpVitesse);
							lastVitesse = tmpVitesse;
						}

						if(debugSerie)
							log.debug("Le robot est en "+current.getPosition()+", orientation : "+orientationRobot);
		
						if(data.length > 6) // la présence de ces infos n'est pas systématique
						{
							/**
							 * Acquiert ce que voit les capteurs
						 	 */
							int[] mesures = new int[nbCapteurs];
							for(int i = 0; i < nbCapteurs; i++)
							{
								mesures[i] = data[6+i];
								if(debugSerie)
									log.debug("Capteurs "+CapteursRobot.values[i].name()+" : "+mesures[i]);
							}
							if(capteursOn)
								buffer.add(new SensorsData(mesures, current));
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
						
						if(data[0] == InOrder.ARRET_URGENCE.codeInt)
						{
							log.critical("Arrêt d'urgence provenant du bas niveau !");
							paquet.ticket.set(Ticket.State.KO);
						}
						else
							paquet.ticket.set(Ticket.State.OK);

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
							if(data[0] != InOrder.ROBOT_BLOCAGE_INTERIEUR.codeInt && data[0] != InOrder.ROBOT_BLOCAGE_EXTERIEUR.codeInt && data[0] != InOrder.PLUS_DE_POINTS.codeInt)
								log.critical("Code fin mouvement inconnu : "+data[0]);
						}
					}
					
					/*
					 * ACTIONNEURS
					 */

					/**
					 * Filet en bas
					 */
					else if(paquet.origine == OutOrder.PULL_DOWN_NET)
						paquet.ticket.set(Ticket.State.OK);
					
					/**
					 * Filet en haut
					 */
					else if(paquet.origine == OutOrder.PULL_UP_NET)
						paquet.ticket.set(Ticket.State.OK);
					
					/**
					 * Filet à mi-chemin
					 */
					else if(paquet.origine == OutOrder.PUT_NET_HALFWAY)
						paquet.ticket.set(Ticket.State.OK);
					
					/**
					 * Le filet est ouvert
					 */
					else if(paquet.origine == OutOrder.OPEN_NET)
						paquet.ticket.set(Ticket.State.OK);

					/**
					 * Le filet est fermé
					 */
					else if(paquet.origine == OutOrder.CLOSE_NET)
						paquet.ticket.set(Ticket.State.OK);

					/**
					 * Bascule abaissée
					 */
					else if(paquet.origine == OutOrder.CROSS_FLIP_FLOP)
					{
						if(data[0] == InOrder.ACT_SUCCESS.codeInt)
							paquet.ticket.set(Ticket.State.OK);
						else
							paquet.ticket.set(Ticket.State.KO);
					}

					/**
					 * Balles éjectées côté gauche
					 */
					else if(paquet.origine == OutOrder.EJECT_LEFT_SIDE)
					{
						if(data[0] == InOrder.ACT_SUCCESS.codeInt)
							paquet.ticket.set(Ticket.State.OK);
						else
							paquet.ticket.set(Ticket.State.KO);
					}

					/**
					 * Balles éjectées côté droit
					 */
					else if(paquet.origine == OutOrder.EJECT_RIGHT_SIDE)
					{
						if(data[0] == InOrder.ACT_SUCCESS.codeInt)
							paquet.ticket.set(Ticket.State.OK);
						else
							paquet.ticket.set(Ticket.State.KO);
					}
					
					/**
					 * Côté gauche réarmé
					 */
					else if(paquet.origine == OutOrder.REARM_LEFT_SIDE)
					{
						if(data[0] == InOrder.ACT_SUCCESS.codeInt)
							paquet.ticket.set(Ticket.State.OK);
						else
							paquet.ticket.set(Ticket.State.KO);
					}
					
					/**
					 * Côté droit réarmé
					 */
					else if(paquet.origine == OutOrder.REARM_RIGHT_SIDE)
					{
						if(data[0] == InOrder.ACT_SUCCESS.codeInt)
							paquet.ticket.set(Ticket.State.OK);
						else
							paquet.ticket.set(Ticket.State.KO);
					}
					
					/**
					 * Les paquets dont l'état n'importe pas et sans donnée (par exemple PING ou STOP) n'ont pas besoin d'être traités
					 */
					else if(data.length != 0)
						log.critical("On a ignoré un paquet d'origine "+paquet.origine+" (taille : "+data.length+")");

				}
			}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}
	
	@Override
	public void useConfig(Config config)
	{
		debugSerie = config.getBoolean(ConfigInfo.DEBUG_SERIE);
	}

}

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

package serie;

import pathfinding.astarCourbe.arcs.ArcCourbe;

import java.util.LinkedList;

import robot.Speed;
import serie.SerialProtocol.OutOrder;
import serie.trame.Order;
import container.Service;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;

/**
 * Classe qui contient les ordres à envoyer à la série
 * Il y a trois priorité
 * - la plus haute, l'arrêt
 * - ensuite, la trajectoire courbe
 * - enfin, tout le reste
 * @author pf
 *
 */

public class BufferOutgoingOrder implements Service
{
	protected Log log;
	private int prescaler, sendPeriod;
	
	public BufferOutgoingOrder(Log log)
	{
		this.log = log;
	}
		
	private volatile LinkedList<Order> bufferBassePriorite = new LinkedList<Order>();
	private volatile LinkedList<Order> bufferTrajectoireCourbe = new LinkedList<Order>();
	private volatile boolean stop = false;
	private boolean debugSerie;
	
	/**
	 * Le buffer est-il vide?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return bufferBassePriorite.isEmpty() && bufferTrajectoireCourbe.isEmpty() && !stop;
	}

	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized Order poll()
	{
		if(bufferTrajectoireCourbe.size() + bufferBassePriorite.size() > 10)
			log.warning("On n'arrive pas à envoyer les ordres assez vites (ordres TC en attente : "+bufferTrajectoireCourbe.size()+", autres en attente : "+bufferBassePriorite.size()+")");
		
		if(stop)
		{
			stop = false;
			bufferTrajectoireCourbe.clear(); // on annule tout mouvement
			return new Order(OutOrder.STOP);
		}
		else if(!bufferTrajectoireCourbe.isEmpty())
			return bufferTrajectoireCourbe.poll();
		else
			return bufferBassePriorite.poll();
	}
	
	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * @0 elem
	 */
	public synchronized Ticket followTrajectory(Speed vitesse)
	{
		byte[] data = new byte[4];
		data[0] = (byte) (0); // TODO vitesse
		data[1] = (byte) (0);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(data, OutOrder.FOLLOW_TRAJECTORY, t));
		notify();
		return t;
	}
	
	/**
	 * Ajout d'une demande d'ordre de s'arrêter
	 */
	public synchronized void immobilise()
	{
		if(debugSerie)
			log.debug("Stop !");
		stop = true;
		notify();
	}
	
	/**
	 * Demande la couleur au bas niveau
	 */
	public synchronized Ticket demandeCouleur()
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.ASK_COLOR, t));
		notify();
		return t;
	}

	/**
	 * Demande à être notifié du début du match
	 */
	public synchronized void demandeNotifDebutMatch()
	{
		bufferBassePriorite.add(new Order(OutOrder.WAIT_FOR_JUMPER));
		notify();
	}

	/**
	 * Demande à être notifié de la fin du match
	 */
	public synchronized void demandeNotifFinMatch()
	{
		bufferBassePriorite.add(new Order(OutOrder.START_MATCH_CHRONO));
		notify();
	}

	/**
	 * Démarre le stream
	 */
	public synchronized void startStream()
	{
		byte[] data = new byte[3];
		data[0] = (byte) (sendPeriod >> 8);
		data[1] = (byte) (sendPeriod & 0xFF);
		data[2] = (byte) (prescaler);
		bufferBassePriorite.add(new Order(data, OutOrder.START_STREAM_ALL));
		notify();
	}

	/**
	 * Met à jour la vitesse max
	 */
	public synchronized void setMaxSpeed()
	{
		byte[] data = new byte[2];
		data[0] = (byte) (0); // TODO
		data[1] = (byte) (0);
		bufferBassePriorite.add(new Order(data, OutOrder.SET_MAX_SPEED));
		notify();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		sendPeriod = config.getInt(ConfigInfo.SENSORS_SEND_PERIOD);
		prescaler = config.getInt(ConfigInfo.SENSORS_PRESCALER);
		debugSerie = config.getBoolean(ConfigInfo.DEBUG_SERIE);
	}
	
	/**
	 * Envoi de tous les arcs élémentaires d'un arc courbe
	 * @0 arc
	 */
	public synchronized void envoieArcCourbe(ArcCourbe arc, int indexTrajectory)
	{
		if(debugSerie)
			log.debug("Envoi d'un arc "+arc.getPoint(0));

		byte[] data = new byte[1+7*arc.getNbPoints()];
		data[0] = (byte) indexTrajectory;
		
		for(int i = 0; i < arc.getNbPoints(); i++)
		{
			data[7*i+1] = (byte) (((int)(arc.getPoint(i).getPosition().getX())+1500) >> 4);
			data[7*i+2] = (byte) ((((int)(arc.getPoint(i).getPosition().getX())+1500) << 4) + ((int)(arc.getPoint(i).getPosition().getY()) >> 8));
			data[7*i+3] = (byte) ((int)(arc.getPoint(i).getPosition().getY()));
			double angle = arc.getPoint(i).orientationReelle;
			if(!arc.getPoint(0).enMarcheAvant)
				angle += Math.PI;
		
			angle %= 2*Math.PI;
			if(angle < 0)
				angle += 2*Math.PI; // il faut toujours envoyer des nombres positifs

			int theta = (int) Math.round(angle*1000);

			data[7*i+4] = (byte) (theta >> 8);
			data[7*i+5] = (byte) theta;
			
			// TODO : corriger en se basant sur le protocole
			data[7*i+6] = (byte) (((Math.round(arc.getPoint(i).courbureReelle+20)*1000) >> 8) & 0xEF);

			if(i != 0 && arc.getPoint(i).enMarcheAvant != arc.getPoint(i-1).enMarcheAvant)
				data[6] |= 0x70; // en cas de marche arrière
			
			data[7*i+7] = (byte) ((Math.round(arc.getPoint(i).courbureReelle+20)*1000) & 0xFF);

		}
		bufferTrajectoireCourbe.add(new Order(data, OutOrder.SEND_ARC));
		notify();			
	}

}

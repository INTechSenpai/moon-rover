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

import java.nio.ByteBuffer;
import java.util.LinkedList;

import robot.Speed;
import serie.SerialProtocol.OutOrder;
import serie.trame.Order;
import container.Service;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2RO;

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
	private byte prescaler;
	private short sendPeriod;
	
	public BufferOutgoingOrder(Log log)
	{
		this.log = log;
	}
		
	private volatile LinkedList<Order> bufferBassePriorite = new LinkedList<Order>();
	private volatile LinkedList<Order> bufferTrajectoireCourbe = new LinkedList<Order>();
	private volatile Ticket stop = null;
	private boolean debugSerie;
	
	/**
	 * Le buffer est-il vide?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return bufferBassePriorite.isEmpty() && bufferTrajectoireCourbe.isEmpty() && stop == null;
	}

	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized Order poll()
	{
		if(bufferTrajectoireCourbe.size() + bufferBassePriorite.size() > 10)
			log.warning("On n'arrive pas à envoyer les ordres assez vites (ordres TC en attente : "+bufferTrajectoireCourbe.size()+", autres en attente : "+bufferBassePriorite.size()+")");
		
		if(stop != null)
		{
			bufferTrajectoireCourbe.clear(); // on annule tout mouvement
			Order out = new Order(OutOrder.STOP, stop);
			stop = null;
			return out;
		}
		else if(!bufferTrajectoireCourbe.isEmpty())
			return bufferTrajectoireCourbe.poll();
		else
			return bufferBassePriorite.poll();
	}
	
	/**
	 * Signale la vitesse max au bas niveau
	 * @param vitesse
	 * @return
	 */
	public synchronized void setMaxSpeed(Speed vitesse, boolean marcheAvant)
	{
		short vitesseTr; // vitesse signée
		if(marcheAvant)
			vitesseTr = (short)(vitesse.translationalSpeed*1000);
		else
			vitesseTr = (short)(- vitesse.translationalSpeed*1000);

		ByteBuffer data = ByteBuffer.allocate(2);
		data.putShort(vitesseTr);

		bufferBassePriorite.add(new Order(data, OutOrder.SET_MAX_SPEED));
		notify();
	}
	
	/**
	 * Ordre long de suivi de trajectoire
	 * @param vitesseInitiale
	 * @param marcheAvant
	 * @return
	 */
	public synchronized Ticket beginFollowTrajectory(Speed vitesseInitiale, boolean marcheAvant)
	{
		short vitesseTr; // vitesse signée
		if(marcheAvant)
			vitesseTr = (short)(vitesseInitiale.translationalSpeed*1000);
		else
			vitesseTr = (short)(- vitesseInitiale.translationalSpeed*1000);

		ByteBuffer data = ByteBuffer.allocate(2);
		data.putShort(vitesseTr);

		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(data, OutOrder.SET_MAX_SPEED, t));
		notify();
		return t;
	}
	
	/**
	 * Ajout d'une demande d'ordre de s'arrêter
	 */
	public synchronized Ticket immobilise()
	{
		if(debugSerie)
			log.debug("Stop !");
		stop = new Ticket();
		notify();
		return stop;
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
	 * Ajoute une position et un angle.
	 * Occupe 5 octets.
	 * @param data
	 * @param pos
	 * @param angle
	 */
	private void addXYO(ByteBuffer data, Vec2RO pos, double angle)
	{
		data.put((byte) (((int)(pos.getX())+1500) >> 4));
		data.put((byte) ((((int)(pos.getX())+1500) << 4) + ((int)(pos.getY()) >> 8)));
		data.put((byte) ((int)(pos.getY())));
		short theta = (short) Math.round((angle % 2*Math.PI)*1000);
		data.putShort(theta);		
	}
	
	/**
	 * Corrige la position du bas niveau
	 */
	public synchronized void correctPosition(Vec2RO deltaPos, double deltaOrientation)
	{
		ByteBuffer data = ByteBuffer.allocate(4);
		addXYO(data, deltaPos, deltaOrientation);
		bufferBassePriorite.add(new Order(data, OutOrder.EDIT_POSITION));
		notify();
	}
	
	/**
	 * Demande à être notifié du début du match
	 */
	public synchronized void waitForJumper()
	{
		bufferBassePriorite.add(new Order(OutOrder.WAIT_FOR_JUMPER));
		notify();
	}

	/**
	 * Demande à être notifié de la fin du match
	 */
	public synchronized void startMatchChrono()
	{
		bufferBassePriorite.add(new Order(OutOrder.START_MATCH_CHRONO));
		notify();
	}

	/**
	 * Démarre le stream
	 */
	public synchronized void startStream()
	{
		ByteBuffer data = ByteBuffer.allocate(3);
		data.putShort(sendPeriod);
		data.put(prescaler);
		bufferBassePriorite.add(new Order(data, OutOrder.START_STREAM_ALL));
		notify();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		sendPeriod = config.getShort(ConfigInfo.SENSORS_SEND_PERIOD);
		prescaler = config.getByte(ConfigInfo.SENSORS_PRESCALER);
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

		if(arc.getNbPoints() >= 30) // ne devrait pas arriver…
			log.critical("Envoi d'un arc avec trop de points !");
		
		ByteBuffer data = ByteBuffer.allocate(1+7*arc.getNbPoints());
		data.put((byte)indexTrajectory);
		
		for(int i = 0; i < arc.getNbPoints(); i++)
		{
			addXYO(data, arc.getPoint(i).getPosition(), arc.getPoint(i).orientationGeometrique);
			short courbure = (short) ((Math.round(arc.getPoint(i).courbureReelle)*10) & 0xEFFF);

			if(i != 0 && arc.getPoint(i).enMarcheAvant != arc.getPoint(i-1).enMarcheAvant)
				courbure |= 0x8000; // en cas de marche arrière
			
			data.putShort(courbure);

		}
		bufferTrajectoireCourbe.add(new Order(data, OutOrder.SEND_ARC));
		notify();			
	}

}

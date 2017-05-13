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

package serie;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import capteurs.SensorMode;
import robot.CinematiqueObs;
import robot.Speed;
import serie.SerialProtocol.OutOrder;
import serie.trame.Order;
import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.SerialClass;
import utils.Log;
import utils.Log.Verbose;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Classe qui contient les ordres à envoyer à la série
 * Il y a trois priorité
 * - la plus haute, l'arrêt
 * - ensuite, la trajectoire courbe
 * - enfin, tout le reste
 * @author pf
 *
 */

public class BufferOutgoingOrder implements Service, SerialClass
{
	protected Log log;
	private byte prescaler;
	private short sendPeriod;
	private boolean streamStarted = false;
	
	public BufferOutgoingOrder(Log log, Config config)
	{
		this.log = log;
		sendPeriod = config.getShort(ConfigInfo.SENSORS_SEND_PERIOD);
		prescaler = config.getByte(ConfigInfo.SENSORS_PRESCALER);
	}
		
	private volatile LinkedList<Order> bufferBassePriorite = new LinkedList<Order>();
	private volatile LinkedList<Order> bufferTrajectoireCourbe = new LinkedList<Order>();
	private volatile boolean sendStop = false;
	private volatile Ticket stop = null;
	
	/**
	 * Le buffer est-il vide?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return bufferBassePriorite.isEmpty() && bufferTrajectoireCourbe.isEmpty() && !sendStop;
	}

	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized Order poll()
	{
		if(bufferTrajectoireCourbe.size() + bufferBassePriorite.size() > 10)
			log.warning("On n'arrive pas à envoyer les ordres assez vites (ordres TC en attente : "+bufferTrajectoireCourbe.size()+", autres en attente : "+bufferBassePriorite.size()+")");
		
		if(sendStop)
		{
			bufferTrajectoireCourbe.clear(); // on annule tout mouvement
			Order out = new Order(OutOrder.STOP, stop);
			sendStop = false;
			return out;
		}
		else if(!bufferTrajectoireCourbe.isEmpty())
			return bufferTrajectoireCourbe.poll();
		else
			return bufferBassePriorite.poll();
	}
	
	/**
	 * Signale la vitesse max au bas niveau
	 * @param vitesse signée
	 * @return
	 */
	public synchronized void setMaxSpeed(double vitesse)
	{
		log.debug("Envoi d'un ordre de vitesse max : "+vitesse, Verbose.SERIE.masque | Verbose.PF.masque);

		short vitesseTr; // vitesse signée
		vitesseTr = (short)(vitesse*1000);

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
	public synchronized Ticket followTrajectory(Speed vitesseInitiale, boolean marcheAvant)
	{
		log.debug("On commence à suivre la trajectoire", Verbose.PF.masque);
		short vitesseTr; // vitesse signée
		if(marcheAvant)
			vitesseTr = (short)(vitesseInitiale.translationalSpeed*1000);
		else
			vitesseTr = (short)(- vitesseInitiale.translationalSpeed*1000);

		ByteBuffer data = ByteBuffer.allocate(2);
		data.putShort(vitesseTr);

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
		log.warning("Stop !");
		sendStop = true;
		stop = new Ticket();
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
	 * Lève le filet
	 */
	public synchronized Ticket leveFilet()
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.PULL_UP_NET, t));
		notify();
		return t;
	}
	
	/**
	 * Baisse le filet
	 */
	public synchronized Ticket baisseFilet()
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.PULL_DOWN_NET, t));
		notify();
		return t;
	}
	
	/**
	 * Met le filet en position intermédiaire
	 */
	public synchronized Ticket bougeFiletMiChemin()
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.PUT_NET_HALFWAY, t));
		notify();
		return t;
	}

	/**
	 * Abaisse la bascule avec le filet
	 */
	public synchronized Ticket traverseBascule()
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.CROSS_FLIP_FLOP, t));
		notify();
		return t;
	}
	
	/**
	 * Ouvre le filet
	 */
	public synchronized Ticket ouvreFilet()
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.OPEN_NET, t));
		notify();
		return t;
	}
	
	/**
	 * Active la funny action
	 * @return
	 */
	public synchronized Ticket funnyAction() {
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.FUNNY_ACTION, t));
		notify();
		return t;
	}

	/**
	 * Verrouille le filet
	 */
	public synchronized Ticket verrouilleFilet()
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.LOCK_NET, t));
		notify();
		return t;
	}
	
	/**
	 * Ferme le filet
	 */
	public synchronized Ticket fermeFilet()
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.CLOSE_NET, t));
		notify();
		return t;
	}
	
	/**
	 * Ejecte les balles
	 */
	public synchronized Ticket ejecteBalles(Boolean droite) // la réflectivité demande d'utiliser Boolean et pas boolean
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(droite ? OutOrder.EJECT_RIGHT_SIDE : OutOrder.EJECT_LEFT_SIDE, t));
		notify();
		return t;
	}

	/**
	 * Réarme le filet
	 */
	public synchronized Ticket rearme(Boolean droite)
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(droite ? OutOrder.REARM_RIGHT_SIDE : OutOrder.REARM_LEFT_SIDE, t));
		notify();
		return t;
	}
	
	Vec2RW tmp = new Vec2RW();

	/**
	 * Ajoute une position et un angle.
	 * Occupe 5 octets.
	 * @param data
	 * @param pos
	 * @param angle
	 */
	private void addXYO(ByteBuffer data, Vec2RO pos, double angle)
	{
		double x = pos.getX();
		double y = pos.getY();
		x = (x < -1500 ? -1500 : x > 1500 ? 1500 : x);
		y = (y < 0 ? 0 : y > 2000 ? 2000 : y);
		data.put((byte) (((int)(x)+1500) >> 4));
		data.put((byte) ((((int)(x)+1500) << 4) + ((int)(y) >> 8)));
		data.put((byte) ((int)(y)));
		short theta = (short) Math.round((angle % (2*Math.PI))*1000);
		if(theta < 0)
			theta += (short) Math.round(2000 * Math.PI);
		data.putShort(theta);		
	}
	
	/**
	 * Corrige la position du bas niveau
	 */
	public synchronized void setPosition(Vec2RO pos, double orientation)
	{
		ByteBuffer data = ByteBuffer.allocate(5);
		addXYO(data, pos, orientation);
		bufferBassePriorite.add(new Order(data, OutOrder.SET_POSITION));
		notify();
	}
	
	/**
	 * Corrige la position du bas niveau
	 */
	public synchronized void correctPosition(Vec2RO deltaPos, double deltaOrientation)
	{
		ByteBuffer data = ByteBuffer.allocate(5);
		addXYO(data, deltaPos, deltaOrientation);
		bufferBassePriorite.add(new Order(data, OutOrder.EDIT_POSITION));
		notify();
	}
	
	/**
	 * Demande à être notifié du début du match
	 */
	public synchronized Ticket waitForJumper()
	{
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(OutOrder.WAIT_FOR_JUMPER, t));
		notify();
		return t;
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
	 * Demande d'utiliser un certain SensorMode
	 * @param mode
	 */
	public synchronized void setSensorMode(SensorMode mode)
	{
		ByteBuffer data = ByteBuffer.allocate(1);
		data.put(mode.code);
		bufferBassePriorite.add(new Order(data, OutOrder.SET_SENSOR_MODE));
		notify();
	}

	/**
	 * Démarre le stream
	 */
	public synchronized void startStream()
	{
		if(streamStarted)
			log.warning("Le stream est déjà lancé !");
		else
		{
			streamStarted = true;
			ByteBuffer data = ByteBuffer.allocate(3);
			data.putShort(sendPeriod);
			data.put(prescaler);
			bufferBassePriorite.add(new Order(data, OutOrder.START_STREAM_ALL));
			notify();
		}
	}

	/**
	 * Arrête le stream
	 */
	public synchronized void stopStream()
	{
		if(streamStarted)
		{
			streamStarted = false;
			bufferBassePriorite.add(new Order(OutOrder.STOP_STREAM_ALL));
			notify();
		}
	}

	/**
	 * Envoi un seul arc sans stop. Permet d'avoir une erreur NO_MORE_POINTS avec le point d'après
	 * @param point
	 * @param indexTrajectory
	 * @return
	 */
	public synchronized void makeNextObsolete(CinematiqueObs c, int indexTrajectory)
	{
		log.debug("Envoi d'un arc d'obsolescence", Verbose.SERIE.masque);
			
		ByteBuffer data = ByteBuffer.allocate(8);
		data.put((byte)indexTrajectory);
		addXYO(data, c.getPosition(), c.orientationReelle);
		short courbure = (short) ((Math.round(Math.abs(c.courbureReelle)*100)) & 0x7FFF);

		// stop
		courbure |= 0x8000;
		if(c.courbureReelle < 0) // bit de signe
			courbure |= 0x4000;

		data.putShort(courbure);
		
		bufferTrajectoireCourbe.add(new Order(data, OutOrder.SEND_ARC));
	}
	
	/**
	 * Envoi de tous les arcs élémentaires d'un arc courbe
	 * @0 arc
	 */
	public synchronized Ticket[] envoieArcCourbe(List<CinematiqueObs> points, int indexTrajectory)
	{
		log.debug("Envoi de "+points.size()+" points à partir de l'index "+indexTrajectory, Verbose.SERIE.masque | Verbose.PF.masque);

		int index = indexTrajectory;
		int nbEnvoi = (points.size() >> 5) + 1;
		int modulo = (points.size() & 31); // pour le dernier envoi
		
		Ticket[] t = new Ticket[nbEnvoi];
		
		for(int i = 0; i < nbEnvoi; i++)
		{
			int nbArc = 32;
			if(i == nbEnvoi - 1) // dernier envoi
				nbArc = modulo;
			ByteBuffer data = ByteBuffer.allocate(1+7*nbArc);
			data.put((byte)index);
			t[i] = new Ticket();
			for(int j = 0; j < nbArc; j++)
			{
				double prochaineCourbure; // pour gérer les arrêts qui font arrêter le robot
				CinematiqueObs c = points.get((i<<5)+j);
				
				if((i<<5)+j+1 < points.size())
					prochaineCourbure = points.get((i<<5)+j+1).courbureReelle;
				else
					prochaineCourbure = c.courbureReelle; // c'est le dernier point, de toute façon ce sera un STOP_POINT
				
				log.debug("Point "+((i<<5)+j)+" : "+c, Verbose.PF.masque);
				addXYO(data, c.getPosition(), c.orientationReelle);
				short courbure = (short) ((Math.round(Math.abs(c.courbureReelle)*100)) & 0x7FFF);

				// on vérifie si on va dans le même sens que le prochain point
				// le dernier point est forcément un point d'arrêt
				// de plus, si le changement de courbure est trop grand, on impose un arrêt
				if((i<<5)+j+1 == points.size() || c.enMarcheAvant != points.get((i<<5)+j+1).enMarcheAvant || Math.abs(c.courbureReelle - prochaineCourbure) > 0.5)
					courbure |= 0x8000; // en cas de rebroussement

				if(c.courbureReelle < 0) // bit de signe
					courbure |= 0x4000;
	
				data.putShort(courbure);
			}
			bufferTrajectoireCourbe.add(new Order(data, OutOrder.SEND_ARC, t[i]));
			index += nbArc;
		}
		notify();			
		return t;
	}
	
	public void waitStop() throws InterruptedException
	{
		log.debug("Attente de la réception de la réponse au stop", Verbose.REPLANIF.masque);
		if(stop != null)
		{
			stop.attendStatus();
			stop = null;
		}
	}

}

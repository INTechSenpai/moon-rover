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
	 * Démarre le stream
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
	public synchronized void envoieArcCourbe(ArcCourbe arc)
	{
		if(debugSerie)
			log.debug("Envoi d'un arc "+arc.getPoint(0));

		for(int i = 0; i < arc.getNbPoints(); i++)
		{
			byte[] data = new byte[7];
			data[0] = (byte) (((int)(arc.getPoint(i).getPosition().x)+1500) >> 4);
			data[1] = (byte) ((((int)(arc.getPoint(i).getPosition().x)+1500) << 4) + ((int)(arc.getPoint(i).getPosition().y) >> 8));
			data[2] = (byte) ((int)(arc.getPoint(i).getPosition().y));
			double angle = arc.getPoint(i).orientation;
			if(!arc.getPoint(0).enMarcheAvant)
				angle += Math.PI;
		
			angle %= 2*Math.PI;
			if(angle < 0)
				angle += 2*Math.PI; // il faut toujours envoyer des nombres positifs

			long theta = (long) (angle*1000);

			data[3] = (byte) (theta >> 8);
			data[4] = (byte) theta;
			
			data[5] = (byte) arc.indexTrajectory;
			
			data[6] = (byte) (((Math.round(arc.getPoint(i).courbure+20)*1000) >> 8) & 0xEF);

			if(i != 0 && arc.getPoint(i).enMarcheAvant != arc.getPoint(i-1).enMarcheAvant)
				data[6] |= 0x70; // en cas de marche arrière
			
			data[7] = (byte) ((Math.round(arc.getPoint(i).courbure+20)*1000) & 0xFF);

			bufferTrajectoireCourbe.add(new Order(data, OutOrder.SEND_ARC));
		}
		notify();			
	}

}

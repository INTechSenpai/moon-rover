package serie;

import pathfinding.astarCourbe.arcs.ArcCourbe;

import java.util.LinkedList;

import robot.Speed;
import robot.actuator.ActuatorOrder;
import serie.trame.Order;
import container.Service;
import enums.SerialProtocol.OutOrder;
import utils.Config;
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
	
	public BufferOutgoingOrder(Log log)
	{
		this.log = log;
	}
		
	private boolean symetrie;
	
	private volatile LinkedList<Order> bufferBassePriorite = new LinkedList<Order>();
	private volatile LinkedList<Order> bufferTrajectoireCourbe = new LinkedList<Order>();
	private volatile boolean stop = false;
	
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
	public synchronized Ticket avancer(int distance, Speed vitesse)
	{
		OutOrder ordre;
		if(Config.debugSerie)
			log.debug("Avance de "+distance);
		byte[] data = new byte[4];
		if(distance >= 0)
			ordre = OutOrder.AVANCER;
		else
		{
			ordre = OutOrder.AVANCER_NEG;
			distance = -distance;
		}
		data[0] = (byte) (distance >> 8);
		data[1] = (byte) (distance);
		data[2] = (byte) ((int)(vitesse.translationalSpeed*1000) >> 8);
		data[3] = (byte) ((int)(vitesse.translationalSpeed*1000) & 0xFF);
//		log.debug("Vitesse : "+vitesse.translationalSpeed*1000);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(data, ordre, t));
		notify();
		return t;
	}

	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * Si on avançait précédement, on va avancer. Si on reculait, on va reculer.
	 * @0 elem
	 */
	public synchronized Ticket avancerMemeSens(int distance, Speed vitesse)
	{
		if(Config.debugSerie)
			log.debug("Avance (même sens) de "+distance);
		byte[] data = new byte[4];
		OutOrder order;
		if(distance >= 0)
			order = OutOrder.AVANCER_IDEM;
		else
		{
			order = OutOrder.AVANCER_REVERSE;
			distance = -distance;
		}
		data[0] = (byte) (distance >> 8);
		data[1] = (byte) (distance);
		data[2] = (byte) ((int)(vitesse.translationalSpeed*1000) >> 8);
		data[3] = (byte) ((int)(vitesse.translationalSpeed*1000) & 0xFF);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(data, order, t));
		notify();
		return t;
	}

	/**
	 * Ajout d'une demande d'ordre de s'arrêter
	 */
	public synchronized void immobilise()
	{
		if(Config.debugSerie)
			log.debug("Stop !");
		stop = true;
		notify();
	}
	
	/**
	 * Ajout d'une demande d'ordre d'actionneurs pour la série
	 * @0 elem
	 */
	public synchronized Ticket utiliseActionneurs(ActuatorOrder elem)
	{
		ActuatorOrder elem2 = elem.getSymetrie(symetrie);
		byte[] data = new byte[3];
		data[0] = (byte) (elem2.id);
		data[1] = (byte) (elem2.angle >> 8);
		data[2] = (byte) (elem2.angle & 0xFF);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(data, OutOrder.ACTIONNEUR, t));
		notify();
		return t;
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
		bufferBassePriorite.add(new Order(OutOrder.MATCH_BEGIN));
		notify();
	}

	/**
	 * Demande à être notifié de la fin du match
	 */
	public synchronized void demandeNotifFinMatch()
	{
		bufferBassePriorite.add(new Order(OutOrder.MATCH_END));
		notify();
	}

	/**
	 * Désasservit le robot
	 */
	public synchronized void asserOff()
	{
		bufferBassePriorite.add(new Order(OutOrder.ASSER_OFF));
		notify();
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		symetrie = config.getSymmetry();
	}
	
	/**
	 * Envoi de tous les arcs élémentaires d'un arc courbe
	 * @0 arc
	 */
	public synchronized void envoieArcCourbe(ArcCourbe arc)
	{
		if(Config.debugSerie)
			log.debug("Envoi d'un arc "+arc.getPoint(0));

		for(int i = 0; i < arc.getNbPoints(); i++)
		{
			OutOrder order;
//			log.debug(i);
			byte[] data = new byte[9];
			if(i != 0 && arc.getPoint(i).enMarcheAvant != arc.getPoint(i-1).enMarcheAvant)
			{
//				log.debug("ARC ARRET");
				order = OutOrder.SEND_ARC_ARRET;
			}
			else
			{
//				log.debug("ARC");
				order = OutOrder.SEND_ARC;
			}
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
			
			data[5] = (byte) ((Math.round(arc.getPoint(i).courbure+20)*1000) >> 8);
			data[6] = (byte) ((Math.round(arc.getPoint(i).courbure+20)*1000) & 0xFF);
			
			data[7] = (byte) ((int)(arc.getPoint(i).vitesseTranslation*1000) >> 8);
			data[8] = (byte) ((int)(arc.getPoint(i).vitesseTranslation*1000) & 0xFF);
			
			bufferTrajectoireCourbe.add(new Order(data, order));
		}
		notify();			
	}

}

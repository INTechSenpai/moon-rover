package serie;

import pathfinding.astarCourbe.arcs.ArcCourbe;

import java.util.LinkedList;

import robot.Speed;
import robot.actuator.ActuatorOrder;
import serie.trame.Order;
import container.Service;
import enums.SerialProtocol;
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
	
	// priorité 0 = priorité minimale
	private volatile LinkedList<Order> bufferBassePriorite = new LinkedList<Order>();
	private volatile LinkedList<Order> bufferTrajectoireCourbe = new LinkedList<Order>();
	private volatile boolean stop = false;
	private final static int PARAM = 1;
	
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
		
		byte[] out;
		if(stop)
		{
			stop = false;
			bufferTrajectoireCourbe.clear(); // on annule tout mouvement
			out = new byte[1];
			return new Order(out, SerialProtocol.OutOrder.STOP);
		}
		else if(!bufferTrajectoireCourbe.isEmpty())
		{
			return bufferTrajectoireCourbe.poll();
		}
		else
		{
			return bufferBassePriorite.poll();
		}
	}
	
	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * @param elem
	 */
	public synchronized Ticket avancer(int distance, Speed vitesse)
	{
		SerialProtocol.OutOrder ordre;
		if(Config.debugSerie)
			log.debug("Avance de "+distance);
		byte[] out = new byte[5];
		if(distance >= 0)
			ordre = SerialProtocol.OutOrder.AVANCER;
		else
		{
			ordre = SerialProtocol.OutOrder.AVANCER_NEG;
			distance = -distance;
		}
		out[PARAM] = (byte) (distance >> 8);
		out[PARAM+1] = (byte) (distance);
		out[PARAM+2] = (byte) ((int)(vitesse.translationalSpeed*1000) >> 8);
		out[PARAM+3] = (byte) ((int)(vitesse.translationalSpeed*1000) & 0xFF);
//		log.debug("Vitesse : "+vitesse.translationalSpeed*1000);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(out, ordre, t));
		notify();
		return t;
	}

	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * Si on avançait précédement, on va avancer. Si on reculait, on va reculer.
	 * @param elem
	 */
	public synchronized Ticket avancerMemeSens(int distance, Speed vitesse)
	{
		if(Config.debugSerie)
			log.debug("Avance (même sens) de "+distance);
		byte[] out = new byte[5];
		SerialProtocol.OutOrder order;
		if(distance >= 0)
			order = SerialProtocol.OutOrder.AVANCER_IDEM;
		else
		{
			order = SerialProtocol.OutOrder.AVANCER_REVERSE;
			distance = -distance;
		}
		out[PARAM] = (byte) (distance >> 8);
		out[PARAM+1] = (byte) (distance);
		out[PARAM+2] = (byte) ((int)(vitesse.translationalSpeed*1000) >> 8);
		out[PARAM+3] = (byte) ((int)(vitesse.translationalSpeed*1000) & 0xFF);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(out, order, t));
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
	 * @param elem
	 */
	public synchronized Ticket utiliseActionneurs(ActuatorOrder elem)
	{
		ActuatorOrder elem2 = elem.getSymetrie(symetrie);
		byte[] out = new byte[4];
		out[PARAM] = (byte) (elem2.id);
		out[PARAM + 1] = (byte) (elem2.angle >> 8);
		out[PARAM + 2] = (byte) (elem2.angle & 0xFF);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(out, SerialProtocol.OutOrder.ACTIONNEUR, t));
		notify();
		return t;
	}
	
	/**
	 * Demande la couleur au bas niveau
	 */
	public synchronized Ticket demandeCouleur()
	{
		byte[] out = new byte[1];
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(out, SerialProtocol.OutOrder.ASK_COLOR, t));
		notify();
		return t;
	}

	/**
	 * Demande à être notifié du début du match
	 */
	public synchronized void demandeNotifDebutMatch()
	{
		byte[] out = new byte[1];
		bufferBassePriorite.add(new Order(out, SerialProtocol.OutOrder.MATCH_BEGIN));
		notify();
	}

	/**
	 * Demande à être notifié de la fin du match
	 */
	public synchronized void demandeNotifFinMatch()
	{
		byte[] out = new byte[1];
		bufferBassePriorite.add(new Order(out, SerialProtocol.OutOrder.MATCH_END));
		notify();
	}

	/**
	 * Désasservit le robot
	 */
	public synchronized void asserOff()
	{
		byte[] out = new byte[1];
		bufferBassePriorite.add(new Order(out, SerialProtocol.OutOrder.ASSER_OFF));
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
	 * @param arc
	 */
	public synchronized void envoieArcCourbe(ArcCourbe arc)
	{
		if(Config.debugSerie)
			log.debug("Envoi d'un arc "+arc.getPoint(0));

		for(int i = 0; i < arc.getNbPoints(); i++)
		{
			SerialProtocol.OutOrder order;
//			log.debug(i);
			byte[] out = new byte[10];
			if(i != 0 && arc.getPoint(i).enMarcheAvant != arc.getPoint(i-1).enMarcheAvant)
			{
//				log.debug("ARC ARRET");
				order = SerialProtocol.OutOrder.SEND_ARC_ARRET;
			}
			else
			{
//				log.debug("ARC");
				order = SerialProtocol.OutOrder.SEND_ARC;
			}
			out[PARAM] = (byte) (((int)(arc.getPoint(i).getPosition().x)+1500) >> 4);
			out[PARAM+1] = (byte) ((((int)(arc.getPoint(i).getPosition().x)+1500) << 4) + ((int)(arc.getPoint(i).getPosition().y) >> 8));
			out[PARAM+2] = (byte) ((int)(arc.getPoint(i).getPosition().y));
			double angle = arc.getPoint(i).orientation;
			if(!arc.getPoint(0).enMarcheAvant)
				angle += Math.PI;
		
			angle %= 2*Math.PI;
			if(angle < 0)
				angle += 2*Math.PI; // il faut toujours envoyer des nombres positifs

			long theta = (long) (angle*1000);

			out[PARAM+3] = (byte) (theta >> 8);
			out[PARAM+4] = (byte) theta;
			
			out[PARAM+5] = (byte) ((Math.round(arc.getPoint(i).courbure+20)*1000) >> 8);
			out[PARAM+6] = (byte) ((Math.round(arc.getPoint(i).courbure+20)*1000) & 0xFF);
			
			out[PARAM+7] = (byte) ((int)(arc.getPoint(i).vitesseTranslation*1000) >> 8);
			out[PARAM+8] = (byte) ((int)(arc.getPoint(i).vitesseTranslation*1000) & 0xFF);
			
			bufferTrajectoireCourbe.add(new Order(out, order));
		}
		notify();			
	}

	/**
	 * Renvoie un ping
	 * @return
	 */
	public Order getPing()
	{
		byte[] out = new byte[1];
		Order message = new Order(out, SerialProtocol.OutOrder.PING);
		return message;
	}

}

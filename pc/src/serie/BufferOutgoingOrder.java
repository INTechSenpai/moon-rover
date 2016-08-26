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
	private final static int COMMANDE = 0;
	
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
		byte[] out;
		if(stop)
		{
			stop = false;
			bufferTrajectoireCourbe.clear(); // on annule tout mouvement
			out = new byte[1];
			out[COMMANDE] = SerialProtocol.OUT_STOP.code;
			return new Order(out, Order.Type.SHORT);
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
		if(Config.debugSerie)
			log.debug("Avance de "+distance);
		byte[] out = new byte[5];
		if(distance >= 0)
			out[COMMANDE] = SerialProtocol.OUT_AVANCER.code;
		else
		{
			out[COMMANDE] = SerialProtocol.OUT_AVANCER_NEG.code;
			distance = -distance;
		}
		out[PARAM] = (byte) (distance >> 8);
		out[PARAM+1] = (byte) (distance);
		out[PARAM+2] = (byte) ((int)(vitesse.translationalSpeed*1000) >> 8);
		out[PARAM+3] = (byte) ((int)(vitesse.translationalSpeed*1000) & 0xFF);
//		log.debug("Vitesse : "+vitesse.translationalSpeed*1000);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(out, Order.Type.LONG, t));
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
		if(distance >= 0)
			out[COMMANDE] = SerialProtocol.OUT_AVANCER_IDEM.code;
		else
		{
			out[COMMANDE] = SerialProtocol.OUT_AVANCER_REVERSE.code;
			distance = -distance;
		}
		out[PARAM] = (byte) (distance >> 8);
		out[PARAM+1] = (byte) (distance);
		out[PARAM+2] = (byte) ((int)(vitesse.translationalSpeed*1000) >> 8);
		out[PARAM+3] = (byte) ((int)(vitesse.translationalSpeed*1000) & 0xFF);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(out, Order.Type.LONG, t));
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
		out[COMMANDE] = SerialProtocol.OUT_ACTIONNEUR.code;
		out[PARAM] = (byte) (elem2.id);
		out[PARAM + 1] = (byte) (elem2.angle >> 8);
		out[PARAM + 2] = (byte) (elem2.angle & 0xFF);
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(out, Order.Type.LONG, t));
		notify();
		return t;
	}
	
	/**
	 * Demande la couleur au bas niveau
	 */
	public synchronized Ticket demandeCouleur()
	{
		byte[] out = new byte[1];
		out[COMMANDE] = SerialProtocol.OUT_ASK_COLOR.code;
		Ticket t = new Ticket();
		bufferBassePriorite.add(new Order(out, Order.Type.SHORT, t));
		notify();
		return t;
	}

	/**
	 * Demande à être notifié du début du match
	 */
	public synchronized void demandeNotifDebutMatch()
	{
		byte[] out = new byte[1];
		out[COMMANDE] = SerialProtocol.OUT_MATCH_BEGIN.code;
		bufferBassePriorite.add(new Order(out, Order.Type.SHORT));
		notify();
	}

	/**
	 * Demande à être notifié de la fin du match
	 */
	public synchronized void demandeNotifFinMatch()
	{
		byte[] out = new byte[1];
		out[COMMANDE] = SerialProtocol.OUT_MATCH_END.code;
		bufferBassePriorite.add(new Order(out, Order.Type.SHORT));
		notify();
	}

	/**
	 * Désasservit le robot
	 */
	public synchronized void asserOff()
	{
		byte[] out = new byte[1];
		out[COMMANDE] = SerialProtocol.OUT_ASSER_OFF.code;
		bufferBassePriorite.add(new Order(out, Order.Type.SHORT));
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
//			log.debug(i);
			byte[] out = new byte[10];
			if(i != 0 && arc.getPoint(i).enMarcheAvant != arc.getPoint(i-1).enMarcheAvant)
			{
//				log.debug("ARC ARRET");
				out[COMMANDE] = SerialProtocol.OUT_SEND_ARC_ARRET.code;
			}
			else
			{
//				log.debug("ARC");
				out[COMMANDE] = SerialProtocol.OUT_SEND_ARC.code;
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
			
			bufferTrajectoireCourbe.add(new Order(out, Order.Type.SHORT));
		}
		notify();			
	}

	/**
	 * Retourne le ping initial dans un ordre long
	 */
	public synchronized Order getInitialLongPing()
	{
		byte[] out = new byte[1];
		out[COMMANDE] = SerialProtocol.OUT_PING.code;
		return new Order(out, Order.Type.LONG);
	}
	
	/**
	 * Renvoie un ping
	 * @return
	 */
	public Order getPing()
	{
		byte[] out = new byte[1];
		out[0] = SerialProtocol.OUT_PING.code;
		Order message = new Order(out, Order.Type.SHORT);
		return message;
	}

}

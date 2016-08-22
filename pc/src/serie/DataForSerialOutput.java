package serie;

import pathfinding.astarCourbe.arcs.ArcCourbe;

import java.util.LinkedList;

import robot.Speed;
import robot.actuator.ActuatorOrder;
import container.Service;
import enums.SerialProtocol;
import utils.Config;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Classe qui contient les ordres à envoyer à la série
 * Il y a trois priorité
 * - la plus haute, l'arrêt
 * - ensuite, la trajectoire courbe
 * - enfin, tout le reste
 * @author pf
 *
 */

public class DataForSerialOutput implements Service
{
	protected Log log;
	
	public DataForSerialOutput(Log log)
	{
		this.log = log;
	}
		
	private boolean symetrie;
	
	private int nbPaquet = 0; // numéro du prochain paquet
	private static final int NB_BUFFER_SAUVEGARDE = 50; // on a de la place de toute façon…
	private volatile byte[][] derniersEnvois = new byte[NB_BUFFER_SAUVEGARDE][];
	private volatile boolean[] derniersEnvoisPriority = new boolean[NB_BUFFER_SAUVEGARDE];
	
	// priorité 0 = priorité minimale
	private volatile LinkedList<byte[]> bufferBassePriorite = new LinkedList<byte[]>();
	private volatile LinkedList<byte[]> bufferTrajectoireCourbe = new LinkedList<byte[]>();
	private volatile boolean stop = false;
	
	private final static int ID_FORT = 0;
	private final static int ID_FAIBLE = 1;
	private final static int COMMANDE = 2;
	private final static int PARAM = 3;

	/**
	 * Le buffer est-il vide?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return bufferBassePriorite.isEmpty() && bufferTrajectoireCourbe.isEmpty() && !stop;
	}

	private synchronized void completePaquet(byte[] out)
	{
		out[ID_FORT] = (byte) ((nbPaquet>>8) & 0xFF);
		out[ID_FAIBLE] = (byte) (nbPaquet & 0xFF);
		nbPaquet++;
	}
	
	/**
	 * Retire un élément du buffer
	 * @return
	 */
	public synchronized byte[] poll()
	{
		byte[] out;
		if(stop)
		{
			stop = false;
			bufferTrajectoireCourbe.clear(); // on annule tout mouvement
			out = new byte[2+1];
			out[COMMANDE] = SerialProtocol.OUT_STOP.code;
			completePaquet(out);
			return out;
		}
		else if(!bufferTrajectoireCourbe.isEmpty())
		{
			out = bufferTrajectoireCourbe.poll();
			derniersEnvois[nbPaquet % NB_BUFFER_SAUVEGARDE] = out;
			derniersEnvoisPriority[nbPaquet % NB_BUFFER_SAUVEGARDE] = true;
			completePaquet(out);
			return out;
		}
		else
		{
			out = bufferBassePriorite.poll();
			derniersEnvois[nbPaquet % NB_BUFFER_SAUVEGARDE] = out;
			derniersEnvoisPriority[nbPaquet % NB_BUFFER_SAUVEGARDE] = false;
			completePaquet(out);
			return out;
		}
	}
	
	/**
	 * Réenvoie un paquet à partir de son id
	 * Comme on ne conserve pas tous les précédents paquets, on n'est pas sûr de l'avoir encore…
	 * La priorité est rétablie et le message est envoyé aussi tôt que possible afin de bousculer le moins possible l'ordre
	 * @param id
	 */
	public synchronized void resend(int id)
	{
		if(id <= nbPaquet - NB_BUFFER_SAUVEGARDE)
			log.critical("Réenvoie de message impossible : message trop vieux");
		else if(id >= nbPaquet)
			log.critical("Réenvoie de message impossible : message demandé pas encore envoyé");
		else
		{
			if(derniersEnvoisPriority[id % NB_BUFFER_SAUVEGARDE]) // on redonne la bonne priorité
				bufferTrajectoireCourbe.addFirst(derniersEnvois[id % NB_BUFFER_SAUVEGARDE]);
			else
				bufferBassePriorite.addFirst(derniersEnvois[id % NB_BUFFER_SAUVEGARDE]);
			notify();
		}
	}
	
	public synchronized void askResend(int id)
	{
		byte[] out = new byte[2+3];
		out[COMMANDE] = SerialProtocol.OUT_RESEND_PACKET.code;
		out[PARAM] = (byte) (id >> 8);
		out[PARAM+1] = (byte) (id & 0xFF);
		bufferBassePriorite.addFirst(out);
		notify();
	}
	
	public synchronized void initOdoSTM(Vec2<ReadOnly> pos, double angle)
	{
		int x = (int)pos.x;
		int y = (int)pos.y;
		if(Config.debugSerie)
			log.debug("Initialisation de l'odométrie à "+pos+", angle "+angle);
		byte[] out = new byte[2+6];
		out[COMMANDE] = SerialProtocol.OUT_INIT_ODO.code;
		out[PARAM] = (byte) ((x+1500) >> 4);
		out[PARAM+1] = (byte) (((x+1500) << 4) + (y >> 8));
		out[PARAM+2] = (byte) (y);
		out[PARAM+3] = (byte) (Math.round(angle*1000) >> 8);
		out[PARAM+4] = (byte) (Math.round(angle*1000) & 0xFF);
		bufferBassePriorite.add(out);
		notify();
	}

	public synchronized void vaAuPoint(Vec2<ReadOnly> pos, Speed vitesse)
	{
		int x = (int)pos.x;
		int y = (int)pos.y;
		if(Config.debugSerie)
			log.debug("Va au point "+pos);
		byte[] out = new byte[2+6];
		out[COMMANDE] = SerialProtocol.OUT_VA_AU_POINT.code;
		out[PARAM] = (byte) ((x+1500) >> 4);
		out[PARAM+1] = (byte) (((x+1500) << 4) + (y >> 8));
		out[PARAM+2] = (byte) (y);
		out[PARAM+3] = (byte) ((int)(vitesse.translationalSpeed*1000) >> 8);
		out[PARAM+4] = (byte) ((int)(vitesse.translationalSpeed*1000) & 0xFF);
		bufferBassePriorite.add(out);
		notify();
	}
	
	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * @param elem
	 */
	public synchronized void avancer(int distance, Speed vitesse)
	{
		if(Config.debugSerie)
			log.debug("Avance de "+distance);
		byte[] out = new byte[2+5];
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
		bufferBassePriorite.add(out);
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre d'avancer pour la série
	 * Si on avançait précédement, on va avancer. Si on reculait, on va reculer.
	 * @param elem
	 */
	public synchronized void avancerMemeSens(int distance, Speed vitesse)
	{
		if(Config.debugSerie)
			log.debug("Avance (même sens) de "+distance);
		byte[] out = new byte[2+5];
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
		bufferBassePriorite.add(out);
		notify();
	}
	
	public synchronized void reprendMouvement()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_RESUME_MOVE.code;
		bufferBassePriorite.add(out);
		notify();
	}
	
	/**
	 * Ajout d'une demande d'ordre de tourner pour la série
	 * @param elem
	 */
	public synchronized void turn(double angle, Speed vitesse)
	{
		if(Config.debugSerie)
			log.debug("Tourne à "+angle);
		byte[] out = new byte[2+5];
//		log.debug("Vitesse : "+vitesse.rotationalSpeed*1000*1000);
		angle %= 2*Math.PI;
		if(angle < 0)
			angle += 2*Math.PI; // il faut toujours envoyer des nombres positifs

		out[COMMANDE] = SerialProtocol.OUT_TOURNER.code;
		out[PARAM] = (byte) (Math.round(angle*1000) >> 8);
		out[PARAM+1] = (byte) (Math.round(angle*1000));
		out[PARAM+2] = (byte) ((int)(vitesse.rotationalSpeed*1000*1000) >> 8);
		out[PARAM+3] = (byte) ((int)(vitesse.rotationalSpeed*1000*1000) & 0xFF);
		bufferBassePriorite.add(out);
		notify();
	}


	public synchronized void suspendMouvement()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_SUSPEND_MOVE.code;
		bufferBassePriorite.add(out);
		notify();
	}
	
	/**
	 * Ajout d'une demande d'ordre de s'arrêter
	 */
	public synchronized void immobilise()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_ASSER_POS_ACTUELLE.code;
		bufferBassePriorite.add(out);
		notify();
	}

	/**
	 * Ajout d'une demande d'ordre de s'arrêter
	 */
	public synchronized void immobiliseUrgence()
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
	public synchronized void utiliseActionneurs(ActuatorOrder elem)
	{
		ActuatorOrder elem2 = elem.getSymetrie(symetrie);
		byte[] out = new byte[2+4];
		out[COMMANDE] = SerialProtocol.OUT_ACTIONNEUR.code;
		out[PARAM] = (byte) (elem2.id);
		out[PARAM + 1] = (byte) (elem2.angle >> 8);
		out[PARAM + 2] = (byte) (elem2.angle & 0xFF);
		bufferBassePriorite.add(out);
		notify();
	}
	
/*
	public synchronized void asserVitesse(int vitesseMax)
	{
		byte[] out = new byte[2+3];
		out[COMMANDE] = SerialProtocol.OUT_VITESSE.code;
		out[PARAM] = (byte) (vitesseMax >> 8);
		out[PARAM + 1] = (byte) (vitesseMax & 0xFF);
		bufferBassePriorite.add(out);
		notify();
	}*/
	
	/**
	 * Désasservit le robot
	 */
	public synchronized void asserOff()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_ASSER_OFF.code;
		bufferBassePriorite.add(out);
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
			byte[] out = new byte[2+10];
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
			
			bufferTrajectoireCourbe.add(out);
		}
		notify();			
	}

	public synchronized void activeDebugMode()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_DEBUG_MODE.code;
		bufferBassePriorite.add(out);
		notify();
	}

	public synchronized void sendPong()
	{
		byte[] out = new byte[2+2];
		out[COMMANDE] = SerialProtocol.OUT_PONG1.code;
		out[COMMANDE+1] = SerialProtocol.OUT_PONG2.code;
		bufferBassePriorite.add(out);
		notify();
	}

	public synchronized void addPing()
	{
		byte[] out = new byte[2+1];
		out[COMMANDE] = SerialProtocol.OUT_PING.code;
		bufferBassePriorite.add(out);
	}

}

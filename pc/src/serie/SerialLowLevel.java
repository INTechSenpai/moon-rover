package serie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import container.Service;
import exceptions.IncorrectChecksumException;
import exceptions.MissingCharacterException;
import exceptions.ProtocolException;
import serie.trame.Conversation;
import serie.trame.Frame.IncomingCode;
import serie.trame.IncomingFrame;
import serie.trame.Order;
import serie.trame.OutgoingFrame;
import serie.trame.Paquet;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;

/**
 * Implémentation du protocole bas niveau série
 * @author pf
 *
 */

public class SerialLowLevel implements Service
{	
	/**
	 * Liste des trames dont on attend un acquittement
	 * Les trames de cette liste sont toujours triées par date de mort (de la plus proche à la plus éloignée)
	 */
	private LinkedList<Conversation> waitingFrames = new LinkedList<Conversation>();
	
	/**
	 * Liste des trames d'ordre long acquittées dont on attend la fin
	 */
	private ArrayList<Conversation> pendingLongFrames = new ArrayList<Conversation>();

	/**
	 * Liste des trames d'ordre dont on a reçu la fin (EXECUTION_END ou REQUEST_ANSWER)
	 */
	private LinkedList<Conversation> closedFrames = new LinkedList<Conversation>();

	private int timeout;
	private byte dernierIDutilise = 0; // dernier ID utilisé
	
	private Log log;
	private SerialInterface serie;
	
	public SerialLowLevel(Log log, SerialInterface serie)
	{
		this.log = log;
		this.serie = serie;
	}
	
	/**
	 * Renvoie le prochain ID disponible
	 * @return
	 */
	private synchronized byte getNextAvailableID()
	{
		boolean ok;
		byte initialID = dernierIDutilise;
		dernierIDutilise++;
		do {
			if(initialID == dernierIDutilise) // on a fait un tour complet…
			{
				log.critical("Aucun ID disponible : attente");
				Sleep.sleep(1);
			}
			ok = true;
			
			for(Conversation c : waitingFrames)
				if(c.firstFrame.compteur == dernierIDutilise)
				{
					ok = false;
					dernierIDutilise++;
					break;
				}
			
			if(!ok)
				continue;

			for(Conversation c : pendingLongFrames)
				if(c.firstFrame.compteur == dernierIDutilise)
				{
					ok = false;
					dernierIDutilise++;
					break;
				}
			
			if(!ok)
				continue;
			
			for(Conversation c : closedFrames)
				if(c.firstFrame.compteur == dernierIDutilise)
				{
					ok = false;
					dernierIDutilise++;
					break;
				}
		} while(!ok);
		return dernierIDutilise;
	}

	/**
	 * Demande l'envoi d'un ordre
	 * @param o
	 */
	public synchronized void sendOrder(Order o)
	{
		Conversation f = new Conversation(o, getNextAvailableID());

		if(Config.debugSerie)
			log.warning("Envoi d'une nouvelle trame");

		serie.communiquer(f.firstFrame);
		waitingFrames.add(f);
	}

	/**
	 * Renvoie les données de la couche ordre (haut niveau)
	 * @return
	 */
	public Paquet readData()
	{
		IncomingFrame f = null;
		Ticket t = null;
		boolean restart;
		do {
			restart = false;
			try {
				f = readFrame();
				t = processFrame(f);
				if(t == null) // c'est une trame de signalisation
					restart = true;
			} catch (Exception e) {
				log.warning(e);
				restart = true;
			}
		} while(restart);
		return new Paquet(f.message, t);
	}
	
	/**
	 * S'occupe du protocole : répond si besoin est, vérifie la cohérence, etc.
	 * Renvoie le ticket associé à la conversation
	 * @param f
	 */
	public synchronized Ticket processFrame(IncomingFrame f) throws ProtocolException
	{
		Iterator<Conversation> it = waitingFrames.iterator();
		while(it.hasNext())
		{
			Conversation waitingC = it.next();
			OutgoingFrame waitingF = waitingC.firstFrame;
			if(waitingF.compteur == f.compteur)
			{
				// On a le EXECUTION_BEGIN d'une frame qui l'attendait
				if(f.code == IncomingCode.EXECUTION_BEGIN)
				{
					if(waitingF.type == Order.Type.LONG)
					{
						if(Config.debugSerie)
							log.debug("EXECUTION_BEGIN reçu");
						it.remove();
						pendingLongFrames.add(waitingC);
						return null;
					}
					else
						throw new ProtocolException(f.code+" reçu pour un ordre "+waitingF.type);
				}
				else if(f.code == IncomingCode.VALUE_ANSWER)
				{
					if(waitingF.type == Order.Type.SHORT)
					{
						if(Config.debugSerie)
							log.debug("VALUE_ANSWER reçu");

						// L'ordre court a reçu un acquittement et ne passe pas par la case "pending"
						it.remove();
						waitingC.setDeathDate(); // tes jours sont comptés…
						closedFrames.add(waitingC);
						return waitingC.ticket;
					}
					else
						throw new ProtocolException(f.code+" reçu pour un ordre "+waitingF.type);
				}
				else
					throw new ProtocolException(f.code+" reçu à la place de EXECUTION_BEGIN ou VALUE_ANSWER !");
			}
		}
		
		// Cette valeur n'a pas été trouvée dans les trames en attente
		// On va donc chercher dans les trames en cours
		
		it = pendingLongFrames.iterator();
		while(it.hasNext())
		{
			Conversation pendingC = it.next();
			OutgoingFrame pendingF = pendingC.firstFrame;
			if(pendingF.compteur == f.compteur)
			{
				// On a le EXECUTION_END d'une frame
				if(f.code == IncomingCode.EXECUTION_END)
				{
					if(Config.debugSerie)
						log.debug("EXECUTION_END reçu. On répond par un END_ORDER.");

					pendingC.setDeathDate(); // tes jours sont comptés…
					// on envoie un END_ORDER
					serie.communiquer(new OutgoingFrame(f.compteur));
					// et on retire la trame des trames en cours
					it.remove();
					closedFrames.add(pendingC);
					return pendingC.ticket;
				}
				else if(f.code == IncomingCode.STATUS_UPDATE)
				{
					if(Config.debugSerie)
						log.debug("STATUS_UPDATE reçu");
					
					return pendingC.ticket;
				}
				else
					throw new ProtocolException(f.code+" reçu à la place de EXECUTION_END ou STATUS_UPDATE !");
			}
		}
		
		// On cherche parmi les trames récemment fermées
		
		it = closedFrames.iterator();
		while(it.hasNext())
		{
			OutgoingFrame closed = it.next().firstFrame;
			if(closed.compteur == f.compteur)
			{
				// On avait déjà reçu l'EXECUTION_END. On ignore ce message
				if(f.code == IncomingCode.EXECUTION_END && closed.type == Order.Type.LONG)
				{
					if(Config.debugSerie)
						log.warning("EXECUTION_END déjà reçu");
					return null;
				}
				// on ne peut pas recevoir de VALUE_ANSWER
				else
					throw new ProtocolException(f.code+" reçu pour une trame "+closed.type+" finie !");
			}
		}
		
		throw new ProtocolException("ID conversation inconnu : "+f.compteur);
	}
	
	/**
	 * Lit une frame depuis la série
	 * Cette méthode est bloquante
	 * @return
	 * @throws MissingCharacterException
	 * @throws IncorrectChecksumException
	 */
	private IncomingFrame readFrame() throws MissingCharacterException, IncorrectChecksumException
	{
		synchronized(serie)
		{
			// Attente des données…
			if(!serie.available())
				try {
					serie.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			
			notify(); // on reçoit un truc
			int code = serie.read();
			int longueur = serie.read();

			if(longueur < 4 || longueur > 255)
				throw new IllegalArgumentException("Mauvaise longueur : "+longueur);
			else if(longueur > 4 && code == IncomingCode.EXECUTION_BEGIN.codeInt)
				throw new IllegalArgumentException("Trame EXECUTION_BEGIN de longueur incorrecte ("+longueur+")");
				

			int compteur = serie.read();
			int[] message = new int[longueur-4];
			for(int i = 0; i < message.length; i++)
				message[i] = serie.read();
			int checksum = serie.read();
			return new IncomingFrame(code, compteur, checksum, longueur, message);
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		timeout = config.getInt(ConfigInfo.SERIAL_TIMEOUT);
		Conversation.setTimeout(timeout);
	}
	
	/**
	 * Fermeture de la série
	 */
	public synchronized void close()
	{
		serie.close();
	}

	/**
	 * Renvoie le temps avant qu'une trame doive être renvoyée (timeout sinon)
	 * @return
	 */
	public synchronized int timeBeforeResend()
	{
		int out;
		if(!waitingFrames.isEmpty())
			out = (int) (waitingFrames.getFirst().timeBeforeResend());
		else
			out = timeout;
		return Math.max(out,0); // il faut envoyer un temps positif
	}
	
	/**
	 * Renvoie le temps avant qu'une trame fermée soit vraiment détruite
	 * @return
	 */
	public synchronized int timeBeforeDeath()
	{
		int out;
		if(!closedFrames.isEmpty())
			out = (int) (closedFrames.getFirst().timeBeforeDeath());
		else
			out = 2*timeout;
		return Math.max(out,0); // il faut envoyer un temps positif
	}

	/**
	 * Renvoie la trame la plus vieille qui en a besoin (possiblement aucune)
	 */
	public synchronized void resend()
	{
		if(!waitingFrames.isEmpty() && waitingFrames.getFirst().needResend())
		{
			Conversation trame = waitingFrames.poll();
			// On remet à la fin
			waitingFrames.add(trame);

			if(Config.debugSerie)
				log.warning("Une trame est renvoyée");

			serie.communiquer(trame.firstFrame);
			trame.updateResendDate(); // on remet la date de renvoi à plus tard
		}
	}

	/**
	 * Tue les vieilles trames
	 */
	public synchronized void kill()
	{
		while(!closedFrames.isEmpty() && closedFrames.getFirst().needDeath())
			closedFrames.removeFirst();
	}
}

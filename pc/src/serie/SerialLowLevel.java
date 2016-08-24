package serie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import container.Service;
import exceptions.IncorrectChecksumException;
import exceptions.MissingCharacterException;
import exceptions.ProtocolException;
import serie.trame.Frame.IncomingCode;
import serie.trame.Frame.OutgoingCode;
import serie.trame.IncomingFrame;
import serie.trame.Order;
import serie.trame.OutgoingFrame;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;

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
	private LinkedList<OutgoingFrame> waitingFrames = new LinkedList<OutgoingFrame>();
	
	/**
	 * Liste des trames d'ordre long acquittées dont on attend la fin
	 */
	private ArrayList<OutgoingFrame> pendingLongFrames = new ArrayList<OutgoingFrame>();
	
	private int timeout;
	
	private Log log;
	private SerialInterface serie;
	
	public SerialLowLevel(Log log, SerialInterface serie)
	{
		this.log = log;
		this.serie = serie;
	}

	/**
	 * Demande l'envoi d'un ordre
	 * @param o
	 */
	public void sendOrder(Order o)
	{
		OutgoingFrame f = new OutgoingFrame(o);
		serie.communiquer(f);
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
	public Ticket processFrame(IncomingFrame f) throws ProtocolException
	{
		Iterator<OutgoingFrame> it = waitingFrames.iterator();
		while(it.hasNext())
		{
			OutgoingFrame waiting = it.next();
			if(waiting.compteur == f.compteur)
			{
				// On a le EXECUTION_BEGIN d'une frame qui l'attendait
				if(f.code == IncomingCode.EXECUTION_BEGIN)
				{
					if(waiting.code == OutgoingCode.NEW_ORDER)
					{
						it.remove();
						pendingLongFrames.add(waiting);
						return waiting.ticket;
					}
					else
						throw new ProtocolException("EXECUTION_BEGIN pour un trame originale de type "+waiting.code);
				}
				else if(f.code == IncomingCode.VALUE_ANSWER)
				{
					if(waiting.code == OutgoingCode.VALUE_REQUEST)
					{
						// L'ordre court a reçu un acquittement et ne passe pas par la case "pending"
						it.remove();
						return waiting.ticket;
					}
					else
						throw new ProtocolException("VALUE_ANSWER pour un trame originale de type "+waiting.code);
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
			OutgoingFrame pending = it.next();
			if(pending.compteur == f.compteur)
			{
				// On a le EXECUTION_END d'une frame
				if(f.code == IncomingCode.EXECUTION_END)
				{
					// on envoie un END_ORDER
					serie.communiquer(new OutgoingFrame(f.compteur));
					// et on retire la trame des trames en cours
					it.remove();
					return pending.ticket;
				}
				else if(f.code == IncomingCode.STATUS_UPDATE)
					return pending.ticket;
				else
					throw new ProtocolException(f.code+" reçu à la place de EXECUTION_END ou STATUS_UPDATE !");
			}
		}
		
		throw new ProtocolException("Compteur inconnu : "+f.compteur);
	}
	
	/**
	 * Lit une frame depuis la série
	 * @return
	 * @throws MissingCharacterException
	 * @throws IncorrectChecksumException
	 */
	private IncomingFrame readFrame() throws MissingCharacterException, IncorrectChecksumException
	{
		synchronized(serie)
		{
			// TODO vérifier l'ordre
			int code = serie.read();
			int compteur = serie.read();
			int checksum = serie.read();
			int longueur = serie.read();
			int[] message = new int[longueur-5];
			for(int i = 0; i < message.length; i++)
				message[i] = serie.read();
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
		OutgoingFrame.setTimeout(timeout);
	}

	/**
	 * Un message est-il disponible ?
	 * @return
	 */
	public boolean available()
	{
		return false;
	}
	
	/**
	 * Fermeture de la série
	 */
	public void close()
	{
		serie.close();
	}

	/**
	 * Renvoie le temps avant qu'une trame doive être renvoyée (timeout sinon)
	 * @return
	 */
	public int timeBeforeRetry()
	{
		int out;
		if(!waitingFrames.isEmpty())
			out = (int) (waitingFrames.getFirst().timeBeforeDeath());
		else
			out = timeout;
		return Math.max(out,0); // il faut envoyer un temps positif
	}

	/**
	 * Renvoie la trame la plus vieille qui en a besoin
	 */
	public void retry()
	{
		while(!waitingFrames.isEmpty() && waitingFrames.getFirst().needResend())
		{
			OutgoingFrame trame = waitingFrames.poll();
			// On remet à la fin
			waitingFrames.add(trame);
			serie.communiquer(trame);
		}
	}

}

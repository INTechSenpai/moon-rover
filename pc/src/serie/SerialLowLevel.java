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
		Conversation f = new Conversation(o);
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
	public Ticket processFrame(IncomingFrame f) throws ProtocolException
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
					pendingC.setDeathDate(); // tes jours sont comptés…
					// on envoie un END_ORDER
					serie.communiquer(new OutgoingFrame(f.compteur));
					// et on retire la trame des trames en cours
					it.remove();
					closedFrames.add(pendingC);
					return pendingC.ticket;
				}
				else if(f.code == IncomingCode.STATUS_UPDATE)
					return pendingC.ticket;
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
					return null;
				// on ne peut pas recevoir de VALUE_ANSWER
				else
					throw new ProtocolException(f.code+" reçu pour une trame "+closed.type+" finie !");
			}
		}
		
		throw new ProtocolException("ID conversation inconnu : "+f.compteur);
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
			int code = serie.read();
			int longueur = serie.read();
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
	public int timeBeforeResend()
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
	public int timeBeforeDeath()
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
	public void resend()
	{
		if(!waitingFrames.isEmpty() && waitingFrames.getFirst().needResend())
		{
			Conversation trame = waitingFrames.poll();
			// On remet à la fin
			waitingFrames.add(trame);
			serie.communiquer(trame.firstFrame);
			trame.updateResendDate(); // on remet la date de renvoi à plus tard
		}
	}

	/**
	 * Tue les vieilles trames
	 */
	public void kill()
	{
		while(!closedFrames.isEmpty() && closedFrames.getFirst().needDeath())
			closedFrames.removeFirst();
	}
}

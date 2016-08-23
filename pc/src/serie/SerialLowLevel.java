package serie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import container.Service;
import exceptions.MissingCharacterException;
import serie.trame.Frame.IncomingCode;
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
	private ArrayList<OutgoingFrame> waitingFrames = new ArrayList<OutgoingFrame>();
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
		serie.communiquer(o.message);
		waitingFrames.add(new OutgoingFrame(o));
	}
	
	/**
	 * Renvoie les données de la couche ordre (haut niveau)
	 * @return
	 */
	public int[] readData()
	{
		IncomingFrame f = null;
		boolean restart;
		do {
			restart = false;
			try {
				f = readFrame();
				OutgoingFrame original = retrieveOriginalFrame(f);

			} catch (MissingCharacterException e) {
				System.out.println(e);
				restart = true;
			}
		} while(restart);
		return f.message;
	}
	
	public OutgoingFrame retrieveOriginalFrame(IncomingFrame f)
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
					it.remove();
					pendingLongFrames.add(waiting);
					return waiting;
				}
				else
				{
					log.warning(f.code+" reçu à la place de EXECUTION_BEGIN !");
					return null;
				}
			}
		}
		
		it = pendingLongFrames.iterator();
		while(it.hasNext())
		{
			OutgoingFrame pending = it.next();
			if(pending.compteur == f.compteur)
			{
				// On a le EXECUTION_END d'une frame
				if(f.code == IncomingCode.EXECUTION_END)
				{
					it.remove();
					return pending;
				}
				else if(f.code == IncomingCode.STATUS_UPDATE)
				{
					return pending;
				}
				else
				{
					log.warning(f.code+" reçu à la place de EXECUTION_END ou STATUS_UPDATE !");
					return null;
				}
			}
		}
		
		log.warning("Compteur inconnu : "+f.compteur);
		return null;
		
	}

	/**
	 * Lit une frame depuis la série
	 * @return
	 * @throws MissingCharacterException 
	 * @throws IOException 
	 */
	private IncomingFrame readFrame() throws MissingCharacterException
	{
		synchronized(serie)
		{
			int[] message = new int[0];
			int code = serie.read();
			int id = serie.read();
			int compteur = serie.read();
			int checksum = serie.read();
			// TODO lire le reste !
			return new IncomingFrame(code, id, compteur, checksum, message);
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
		
	}

	/**
	 * Renvoie le temps avant qu'une trame doive être renvoyée (timeout sinon)
	 * @return
	 */
	public int timeBeforeRetry()
	{
		int out = timeout;
		if(!waitingFrames.isEmpty())
			out = (int) (waitingFrames.get(0).timeBeforeDeath());
		return out;
	}

	/**
	 * Renvoie la trame la plus vieille qui n'a pas été 
	 */
	public void retry()
	{
		if(!waitingFrames.isEmpty() && waitingFrames.get(0).needResend())
			serie.communiquer(waitingFrames.get(0).message);
	}
	
	
}

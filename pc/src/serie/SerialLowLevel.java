package serie;

import java.io.IOException;
import java.util.ArrayList;

import container.Service;
import exceptions.MissingCharacterException;
import serie.trame.Frame;
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
		
	public void sendOrder(Order o)
	{
		waitingFrames.add(new OutgoingFrame(o));
	}
	
	/**
	 * Renvoie les données de la couche ordre (haut niveau)
	 * @return
	 */
	public int[] readData()
	{
		try {
			readFrame();
		} catch (MissingCharacterException e) {
			System.out.println(e);
		}
		return null;
	}
	
	/**
	 * Lit une frame depuis la série
	 * @return
	 * @throws MissingCharacterException 
	 * @throws IOException 
	 */
	private Frame readFrame() throws MissingCharacterException
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
		Frame.setTimeout(timeout);
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
			out = (int) (waitingFrames.get(0).deathDate - System.currentTimeMillis());
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

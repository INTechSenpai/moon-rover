package serie;

import container.Service;
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
	private int timeout;
	private Log log;
	private SerialInterface serie;
	
	public SerialLowLevel(Log log, SerialInterface serie)
	{
		this.log = log;
		this.serie = serie;
	}
	
	/**
	 * Cette méthode est synchronized car deux threads peuvent l'utiliser : ThreadSerialOutput et ThreadSerialOutputTimeout
	 * @param message
	 */
	public synchronized void sendOrder(Order message)
	{}
	
	private byte[] addChecksum(byte[] msg)
	{
		int c = 0;
		for(int i = 0; i < msg.length; i++)
			c += msg[i];
		return msg;
	}
	
	public int[] readData()
	{
		return null;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		timeout = config.getInt(ConfigInfo.SERIAL_TIMEOUT);
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

	public int timeBeforeRetry()
	{
		return 10;
	}

	public void retry() {
		// TODO Auto-generated method stub
		
	}
	
	
}

package serie;

import container.Service;
import exceptions.MissingCharacterException;
import serie.trame.OutgoingFrame;
import utils.Config;
import utils.Log;

/**
 * Série simulée.
 * @author pf
 *
 */

public class SerialSimulation implements SerialInterface, Service {

	private Log log;
	public SerialSimulation(Log log)
	{
		this.log = log;
	}

	public boolean ping()
	{
		return true;
	}
	
	protected void estimeLatence()
	{}

	protected void afficheMessage(byte[] out)
	{
		String m = "";
		for(int i = 0; i < out.length; i++)
		{
			String s = Integer.toHexString(out[i]).toUpperCase();
			if(s.length() == 1)
				m += "0"+s+" ";
			else
				m += s.substring(s.length()-2, s.length())+" ";
		}
		log.debug(m);
	}
	
	public synchronized void communiquer(OutgoingFrame out)
	{
		if(Config.debugSerie)
			out.afficheMessage();
	}

	@Override
	public void close()
	{}

	@Override
	public boolean available()
	{
		return false;
	}

	@Override
	public int read() throws MissingCharacterException
	{
		return 0;
	}

	@Override
	public void useConfig(Config config)
	{}
	
	@Override
	public void updateConfig(Config config)
	{}

}

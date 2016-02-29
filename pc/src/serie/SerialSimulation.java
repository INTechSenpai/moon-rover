package serie;

import java.io.IOException;

import container.Service;
import exceptions.MissingCharacterException;
import utils.Config;
import utils.Log;

/**
 * Série STM simulée.
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
	
	public synchronized void communiquer(byte[] out)
	{
		if(Config.debugSerie)
			afficheMessage(out);
	}

	@Override
	public void close()
	{}

	@Override
	public boolean available() throws IOException
	{
		return false;
	}

	@Override
	public byte read() throws IOException, MissingCharacterException
	{
		return 0;
	}

	@Override
	public void useConfig(Config config)
	{}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public int getFirstID()
	{
		return 0;
	}

}

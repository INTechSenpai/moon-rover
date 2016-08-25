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

	@Override
	public synchronized void communiquer(OutgoingFrame out)
	{
		if(Config.debugSerie)
			log.debug(out);
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

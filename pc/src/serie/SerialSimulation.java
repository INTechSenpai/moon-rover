package serie;

import container.Service;
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
		log.warning("LA SÉRIE EST SIMULÉE !");
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
	public void useConfig(Config config)
	{}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void init()
	{}

}

package threads;

import robot.Strategie;
import container.Service;

/**
 * Thread qui calculera en continu la stratégie à adopter
 * @author pf
 *
 */

public class ThreadStrategie extends AbstractThread {

	private Strategie strategie;
	
	ThreadStrategie(Service config, Service log, Service strategie)
	{
		super(config, log);
		this.strategie = (Strategie) strategie;
	}
	
	public void run()
	{
	}
	
}

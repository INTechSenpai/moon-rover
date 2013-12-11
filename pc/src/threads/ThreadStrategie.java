package threads;

import robot.Strategie;
import container.Service;

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

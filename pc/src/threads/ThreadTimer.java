package threads;

import container.Service;

public class ThreadTimer extends AbstractThread {

	public boolean match_demarre;
	
	ThreadTimer(Service config, Service log)
	{
		super(config, log);
	}
	public void run()
	{
		System.currentTimeMillis();
	}

	// TODO
	public boolean get_fin_match()
	{
		return false;
	}
	
}

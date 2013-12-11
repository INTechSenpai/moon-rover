package threads;

import container.Service;

/**
 * Thread qui s'occupe de la gestion du temps: début du match, péremption des obstacles
 * @author pf
 *
 */

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

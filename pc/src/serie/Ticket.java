package serie;

import enums.SerialProtocol;

/**
 * Un ticket. Tu tires un numéro et tu attends ton tour.
 * Utilisé par la série pour notifier des infos.
 * @author pf
 *
 */

public class Ticket
{
	private volatile SerialProtocol type;
	
	public synchronized SerialProtocol getAndClear()
	{
		SerialProtocol out = type;
		type = null;
		return out;
	}
	
	public synchronized boolean isEmpty()
	{
		return type == null;
	}
	
	public synchronized void set(SerialProtocol type)
	{
		this.type = type;
		notify();
	}
	
}

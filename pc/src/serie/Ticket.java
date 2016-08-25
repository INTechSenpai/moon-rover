package serie;

/**
 * Un ticket. Tu tires un numéro et tu attends ton tour.
 * Utilisé par la série pour notifier des infos.
 * @author pf
 *
 */

public class Ticket
{
	public enum State
	{
		OK, KO;
	}

	private volatile State type;
	
	public synchronized State getAndClear()
	{
		State out = type;
		type = null;
		return out;
	}
	
	public synchronized boolean isEmpty()
	{
		return type == null;
	}
	
	public synchronized void set(State type)
	{
		this.type = type;
		notify();
	}
	
}

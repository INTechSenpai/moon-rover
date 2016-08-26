package serie.trame;

import serie.Ticket;

public class Conversation
{
	private long deathDate; // date d'envoi + 2*timeout
	private long resendDate; // date d'envoi + timeout
	public final Ticket ticket;
	private OutgoingFrame firstFrame;
	public final Order.Type type;
	protected static int timeout;
	
	public Conversation(Order o, int id)
	{
		ticket = o.ticket;
		resendDate = System.currentTimeMillis() + timeout;
		type = o.orderType;
		firstFrame = new OutgoingFrame(o, id);
	}
	
	public void setDeathDate()
	{
		deathDate = System.currentTimeMillis() + 2*timeout;
	}
	
	public void updateResendDate()
	{
		resendDate = System.currentTimeMillis() + timeout;
	}
	
	/**
	 * Faut-il renvoyer cette trame ?
	 * @return
	 */
	public boolean needResend()
	{
		return resendDate < System.currentTimeMillis();
	}

	/**
	 * Faut-il supprimer cette conversation ?
	 * @return
	 */
	public boolean needDeath()
	{
		return deathDate < System.currentTimeMillis();
	}
	
	/**
	 * Récupère le temps restant avant son réenvoi
	 * @return
	 */
	public int timeBeforeResend()
	{
		return (int) (resendDate - System.currentTimeMillis());
	}
	
	/**
	 * Récupère le temps restant avant sa mort
	 * @return
	 */
	public int timeBeforeDeath()
	{
		return (int) (deathDate - System.currentTimeMillis());
	}

	public int getID()
	{
		return firstFrame.id;
	}
	
	public static void setTimeout(int timeout_p)
	{
		timeout = timeout_p;
	}

	public OutgoingFrame getFirstTrame()
	{
		return firstFrame;
	}
}

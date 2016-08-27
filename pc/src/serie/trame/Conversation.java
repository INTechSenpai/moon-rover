package serie.trame;

import enums.SerialProtocol;
import serie.Ticket;

public class Conversation
{
	private long deathDate; // date d'envoi + 2*timeout
	private long resendDate; // date d'envoi + timeout
	public Ticket ticket;
	public boolean libre = true;
	private OutgoingFrame firstFrame;
	protected static int timeout;
	public SerialProtocol.OutOrder origine;
	
	/**
	 * Construction d'une conversation
	 * @param id
	 */
	public Conversation(int id)
	{
		firstFrame = new OutgoingFrame(id);
	}
	
	/**
	 * Mise à mort !
	 */
	public void setDeathDate()
	{
		deathDate = System.currentTimeMillis() + 2*timeout;
	}
	
	/**
	 * Mise à jour de la date de renvoi.
	 * A chaque fois que la trame est renvoyée, on remet à jour cette date.
	 */
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

	public static void setTimeout(int timeout_p)
	{
		timeout = timeout_p;
	}

	public OutgoingFrame getFirstTrame()
	{
		return firstFrame;
	}

	public void update(Order o)
	{
		origine = o.ordre;
		ticket = o.ticket;
		firstFrame.update(o);
		resendDate = System.currentTimeMillis() + timeout;
	}
}

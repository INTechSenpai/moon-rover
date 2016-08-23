package serie;

/**
 * Paquet série haut niveau reçu
 * @author pf
 *
 */

public class Paquet
{
	public int[] message;
	public Ticket ticket;
	
	public Paquet(int[] message, Ticket ticket)
	{
		this.message = message;
		this.ticket = ticket;
	}
	
}

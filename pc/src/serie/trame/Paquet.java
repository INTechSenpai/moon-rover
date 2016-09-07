package serie.trame;

import serie.Ticket;
import serie.SerialProtocol.OutOrder;

/**
 * Paquet série haut niveau reçu
 * @author pf
 *
 */

public class Paquet
{
	public OutOrder origine;
	public int[] message;
	public Ticket ticket;
	
	public Paquet(int[] message, Ticket ticket, OutOrder origine)
	{
		this.origine = origine;
		this.message = message;
		this.ticket = ticket;
	}
	
}

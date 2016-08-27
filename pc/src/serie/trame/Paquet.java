package serie.trame;

import enums.SerialProtocol.OutOrder;
import serie.Ticket;

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

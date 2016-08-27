package serie.trame;

import enums.SerialProtocol;
import serie.Ticket;

/**
 * Paquet série haut niveau reçu
 * @author pf
 *
 */

public class Paquet
{
	public SerialProtocol.OutOrder origine;
	public int[] message;
	public Ticket ticket;
	
	public Paquet(int[] message, Ticket ticket, SerialProtocol.OutOrder origine)
	{
		this.origine = origine;
		this.message = message;
		this.ticket = ticket;
	}
	
}

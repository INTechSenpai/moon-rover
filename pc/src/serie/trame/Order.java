package serie.trame;

import enums.SerialProtocol.OutOrder;
import serie.Ticket;

/**
 * Un ordre à envoyer sur la série
 * @author pf
 *
 */

public class Order
{
	public enum Type
	{
		SHORT,
		LONG;
	}

	public byte[] message;
	public Ticket ticket;
	public OutOrder ordre;
	
	public Order(byte[] message, OutOrder ordre, Ticket ticket)
	{
		this.message = message;
		this.ticket = ticket;
		this.ordre = ordre;
	}

	public Order(byte[] message, OutOrder ordre)
	{
		this(message, ordre, new Ticket());
	}

	public Order(OutOrder ordre)
	{
		this(null, ordre, new Ticket());
	}

	public Order(OutOrder ordre, Ticket t)
	{
		this(null, ordre, t);
	}

}

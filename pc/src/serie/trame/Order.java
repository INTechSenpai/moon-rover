package serie.trame;

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
	public Type orderType;
	public Ticket ticket;
	
	public Order(byte[] message, Type orderType, Ticket ticket)
	{
		this.message = message;
		this.orderType = orderType;
		this.ticket = ticket;
	}
	
}

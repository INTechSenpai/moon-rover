package serie.trame;

import enums.SerialProtocol;
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
	public SerialProtocol.OutOrder ordre;
	
	public Order(byte[] message, SerialProtocol.OutOrder ordre, Ticket ticket)
	{
		message[0] = ordre.code;
		this.message = message;
		this.ticket = ticket;
		this.ordre = ordre;
	}

	public Order(byte[] message, SerialProtocol.OutOrder ordre)
	{
		this(message, ordre, new Ticket());
	}

}

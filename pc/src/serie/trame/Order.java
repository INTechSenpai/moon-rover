package serie.trame;

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

	
	public Order(byte[] message, Type orderType)
	{
		this.message = message;
		this.orderType = orderType;
	}
	
}

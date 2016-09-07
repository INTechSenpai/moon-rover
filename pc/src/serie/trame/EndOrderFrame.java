package serie.trame;

/**
 * Une trame sortant qui contient un END_ORDER
 * @author pf
 *
 */

public class EndOrderFrame extends OutgoingFrame
{
	private int base;
	/**
	 * Trame de END_ORDER
	 */
	public EndOrderFrame()
	{
		super();
		trame[0] = OutgoingCode.END_ORDER.code;
		trame[1] = 4; // longueur de la trame
		
		base = trame[0] + trame[1];
		
	}
	
	public void updateId(int id)
	{
		trame[2] = (byte) id;
		trame[3] = (byte) (base + id); // checksum
	}
}

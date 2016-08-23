package serie.trame;

/**
 * Une trame série
 * @author pf
 *
 */

public abstract class Frame
{
	public enum Code
	{
		NEW_ORDER(0xFF),
		END_ORDER(0xFE),
		VALUE_REQUEST(0xFD),
		EXECUTION_BEGIN(0xFC),
		EXECUTION_END(0xFB),
		STATUS_UPDATE(0xFA),
		VALUE_ANSWER(0xF9);
		
		public final byte code;
		public final int codeInt;
		
		private Code(int code)
		{
			this.code = (byte) code;
			codeInt = code;
		}
	}

	public byte id;
	public byte compteur;
	public long deathDate; // date d'envoi + timeout (toutes les trames provenant du haut niveau doivent être acquittées)
	public Code code;
	
	protected static byte compteurReference = 0;
	protected static int timeout;
	
	public static void setTimeout(int timeout_p)
	{
		timeout = timeout_p;
	}
	
	public boolean needResend()
	{
		return deathDate < System.currentTimeMillis();
	}
	
}
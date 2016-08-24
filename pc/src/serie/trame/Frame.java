package serie.trame;

/**
 * Une trame série
 * @author pf
 *
 */

public abstract class Frame
{
	public enum IncomingCode
	{
		EXECUTION_BEGIN(0xFC),
		EXECUTION_END(0xFB),
		STATUS_UPDATE(0xFA),
		VALUE_ANSWER(0xF9);
		
		public final byte code;
		public final int codeInt;
		
		private IncomingCode(int code)
		{
			this.code = (byte) code;
			codeInt = code;
		}
	}

	public enum OutgoingCode
	{
		NEW_ORDER(0xFF),
		END_ORDER(0xFE),
		VALUE_REQUEST(0xFD);
		
		public final byte code;
		public final int codeInt;
		
		private OutgoingCode(int code)
		{
			this.code = (byte) code;
			codeInt = code;
		}
	}

	public byte compteur;
	
	protected static byte compteurReference = 0;
			
	public abstract void afficheMessage();
	
}
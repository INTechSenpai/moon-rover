import gnu.io.*;

public class Serie extends Service {

	private static Serie INSTANCE = null;

	private Serie()
	{
	    try
	    {
	    	String path=getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
	    	path += "../lib/librxtxSerial.so";
	    	System.load(path);
	    }
	    catch (UnsatisfiedLinkError e)
	    {
	      System.err.println("Native code library failed to load.\n" + e);
	      System.exit(1);
	    }	
	
	}
	
	public static Serie initialiser()
	{
		if (INSTANCE == null)
		 	INSTANCE = new Serie();
		return INSTANCE;
	}

}

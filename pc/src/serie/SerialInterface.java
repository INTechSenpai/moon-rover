package serie;

import exceptions.MissingCharacterException;

/**
 * Interface série
 * @author pf
 *
 */

public interface SerialInterface
{
	public void communiquer(byte[] out);
	public void close();
	public boolean available();
	public int read() throws MissingCharacterException;
}

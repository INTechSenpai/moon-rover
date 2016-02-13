package serie;

import java.io.IOException;

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
	public boolean available() throws IOException;
	public byte read() throws IOException, MissingCharacterException;
}

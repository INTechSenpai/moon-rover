package serie;

import exceptions.MissingCharacterException;
import serie.trame.OutgoingFrame;

/**
 * Interface série
 * @author pf
 *
 */

public interface SerialInterface
{
	public void communiquer(OutgoingFrame out);
	public void close();
	public boolean available();
	public int read() throws MissingCharacterException;
}

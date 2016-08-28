package serie;

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
	public void init();
}

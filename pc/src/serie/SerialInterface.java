package serie;

import serie.trame.OutgoingFrame;

/**
 * Interface série
 * @author pf
 *
 */

public interface SerialInterface
{
	public void communiquer(OutgoingFrame out) throws InterruptedException;
	public void close();
	public void init() throws InterruptedException;
}

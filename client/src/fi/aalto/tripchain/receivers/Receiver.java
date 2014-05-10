package fi.aalto.tripchain.receivers;

/**
 * Method start() starts receiving and stop() end the reception.
 *
 */
public interface Receiver {
	public void start();
	public void stop();
}

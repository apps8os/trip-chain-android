package fi.aalto.tripchain.receivers;

import fi.aalto.tripchain.here.Address;
import fi.aalto.tripchain.route.Activity;
import android.location.Location;

/**
 * Used for subscribing to EventDispatcher's events.
 *
 */
public interface EventListener {
	/**
	 * Called when location is received.
	 */
	public void onLocation(Location location);
	
	/**
	 * Called when activity is received.
	 */
	public void onActivity(Activity activity);
	
	/**
	 * Called when address for location is received.
	 */
	public void onAddress(Address address);
}

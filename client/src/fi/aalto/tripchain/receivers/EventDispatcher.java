package fi.aalto.tripchain.receivers;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import fi.aalto.tripchain.here.Address;
import fi.aalto.tripchain.route.Activity;

/**
 * Static class used to dispatch events globally.
 * Currently events are dispatched only in service
 * context.
 *
 */
public class EventDispatcher {
	private static List<EventListener> listeners = new ArrayList<EventListener>();
	
	/**
	 * Subscribes events to listener.
	 * @param listener
	 */
	public synchronized static void subscribe(EventListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes listener's subscription.
	 */
	public synchronized void unsubscribe(EventListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Dispatches location event to listeners.
	 */
	public static synchronized void onLocation(Location location) {
		for (EventListener el : listeners) {
			el.onLocation(location);
		}
	}

	/**
	 * Dispatches activity event to listeners.
	 */
	public static synchronized void onActivity(Activity activity) {
		for (EventListener el : listeners) {
			el.onActivity(activity);
		}
	}
	
	/**
	 * Dispatches address event to listeners.
	 */
	public static synchronized void onAddress(Address address) {
		for (EventListener el : listeners) {
			el.onAddress(address);
		}
	}
}

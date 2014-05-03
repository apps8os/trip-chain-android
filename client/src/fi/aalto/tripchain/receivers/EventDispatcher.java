package fi.aalto.tripchain.receivers;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import fi.aalto.tripchain.here.Address;
import fi.aalto.tripchain.route.Activity;

public class EventDispatcher {
	private static List<EventListener> listeners = new ArrayList<EventListener>();
	
	public synchronized static void subscribe(EventListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void unsubscribe(EventListener listener) {
		listeners.remove(listener);
	}
	
	public static synchronized void onLocation(Location location) {
		for (EventListener el : listeners) {
			el.onLocation(location);
		}
	}
	
	public static synchronized void onActivity(Activity activity) {
		for (EventListener el : listeners) {
			el.onActivity(activity);
		}
	}
	
	public static synchronized void onAddress(Address address) {
		for (EventListener el : listeners) {
			el.onAddress(address);
		}
	}
}

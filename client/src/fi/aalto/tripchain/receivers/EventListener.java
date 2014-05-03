package fi.aalto.tripchain.receivers;

import fi.aalto.tripchain.here.Address;
import fi.aalto.tripchain.route.Activity;
import android.location.Location;

public interface EventListener {
	public void onLocation(Location location);
	public void onActivity(Activity activity);
	public void onAddress(Address address);
}

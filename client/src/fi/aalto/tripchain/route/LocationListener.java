package fi.aalto.tripchain.route;

import android.app.Service;
import android.location.Location;
import android.util.Log;
import fi.aalto.tripchain.receivers.LocationReceiver;

public class LocationListener extends LocationReceiver {
	private Route route;
	
	private static final String TAG = LocationListener.class.getSimpleName();

	public LocationListener(Service service, Route route) {
		super(service);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Provider: " + location.getProvider()  + 
				" Accuracy: " + location.getAccuracy() +
				" Latitude: " + location.getLatitude() + 
				" Longitude: " + location.getLongitude());
		
		this.route.onLocation(location);
	}
}

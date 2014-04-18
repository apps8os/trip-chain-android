package fi.aalto.tripchain.route;

import java.util.List;

import fi.aalto.tripchain.here.Address;
import fi.aalto.tripchain.here.ReverseGeocoder;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import fi.aalto.tripchain.receivers.LocationReceiver;

public class LocationListener extends LocationReceiver {
	private Trip trip;
	
	private static final String TAG = LocationListener.class.getSimpleName();

	public LocationListener(Context context, Trip trip) {
		super(context);
		this.trip = trip;
	}
	
	@Override
	public void onLocationChanged(final Location location) {
		Log.d(TAG, "Provider: " + location.getProvider()  + 
				" Accuracy: " + location.getAccuracy() +
				" Latitude: " + location.getLatitude() + 
				" Longitude: " + location.getLongitude());
		
		trip.onLocation(location);
	}
}

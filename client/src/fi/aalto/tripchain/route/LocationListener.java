package fi.aalto.tripchain.route;

import fi.aalto.tripchain.BackgroundService;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationListener implements android.location.LocationListener {
	private final static String TAG = LocationListener.class.getSimpleName();
	private BackgroundService service;
	private LocationManager locationManager;
	
	private final static int MINUMUM_POSITION_THRESHOLD = 50; // meters
	private final static int MINIMUM_POSITION_INTERVAL = 5000; // milliseconds	
	
	public LocationListener(BackgroundService service) {
		this.service = service;
		
		this.locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
		
		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
					MINIMUM_POSITION_INTERVAL, MINUMUM_POSITION_THRESHOLD, this);
		} catch (Exception ex) {
			Log.d(TAG, "failed requesting gps location updates", ex);
		}

		try {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
					MINIMUM_POSITION_INTERVAL, MINUMUM_POSITION_THRESHOLD, this);
		} catch (Exception ex) {
			Log.d(TAG, "failed requesting network location updates", ex);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Provider: " + location.getProvider()  + 
				" Accuracy: " + location.getAccuracy() +
				" Latitude: " + location.getLatitude() + 
				" Longitude: " + location.getLongitude());
		
		this.service.getRoute().onLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	public void stop() {
		locationManager.removeUpdates(this);
	}
	
}

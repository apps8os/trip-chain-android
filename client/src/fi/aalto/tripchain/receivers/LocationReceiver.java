package fi.aalto.tripchain.receivers;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public abstract class LocationReceiver implements 
		GooglePlayServicesClient.ConnectionCallbacks, 
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener,
		Receiver {
	private final static String TAG = LocationReceiver.class.getSimpleName();
	
	private LocationClient locationClient;
	
	private LocationRequest locationRequest;
	
	private Context context;
	
	public LocationReceiver(Context context) {
		this.context = context;
		
        locationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(5000)
				.setFastestInterval(1000)
		        .setSmallestDisplacement(10);
		
		locationClient = new LocationClient(context, this, this);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d(TAG, "Connection failed");
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "Connection succeeded");

    	locationClient.requestLocationUpdates(locationRequest, this);
    	Log.d(TAG, "Requested location updates");

	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "Disconnected");
	}
	
	public void stop() {
		locationClient.removeLocationUpdates(this);
		locationClient.disconnect();
	}
	
	public void start() {
		locationClient.connect();		
	}

	@Override
	public abstract void onLocationChanged(Location location);
}

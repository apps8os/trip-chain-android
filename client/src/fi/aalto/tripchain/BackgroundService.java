package fi.aalto.tripchain;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service  {
	private final static String TAG = BackgroundService.class.getSimpleName();
	
	private final static int MINUMUM_POSITION_THRESHOLD = 50; // meters
	private final static int MINIMUM_POSITION_INTERVAL = 5000; // milliseconds
	
	private LocationManager locationManager;
	
	private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ActivityRecognitionResult.hasResult(intent)) {
				ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
				DetectedActivity da = result.getMostProbableActivity();
			     
			    String activity = "";
			    switch (da.getType()) {
			    case DetectedActivity.IN_VEHICLE:
			    	activity = "In vehicle";
			    	break;
			    case DetectedActivity.ON_BICYCLE:
			    	activity = "On bicycle";
			    	break;
			    case DetectedActivity.ON_FOOT:
			    	activity = "On foot";
			    	break;
			    case DetectedActivity.STILL:
			    	activity = "Still";
			    	break;
			    case DetectedActivity.TILTING:
			    	activity = "Tilting";
			    	break;
			    case DetectedActivity.UNKNOWN:
			    	activity = "Unknown";
			    	break;
			    }
    
			    Log.i(TAG, "probably: " + activity);
			}
		}
	};
	
	
	private LocationListener locationListenener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "Provider: " + location.getProvider()  + 
					" Accuracy: " + location.getAccuracy() +
					" Latitude: " + location.getLatitude() + 
					" Longitude: " + location.getLongitude());
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
		
	};

	@Override
	public void onCreate() {
		IntentFilter intentFilter = new IntentFilter(Configuration.ACTIVITY_INTENT);
		this.registerReceiver(activityReceiver, intentFilter);
		
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
					MINIMUM_POSITION_INTERVAL, MINIMUM_POSITION_INTERVAL, locationListenener);
		} catch (Exception ex) {
			Log.d(TAG, "failed requesting gps location updates", ex);
		}

		try {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
					MINIMUM_POSITION_INTERVAL, MINIMUM_POSITION_INTERVAL, locationListenener);
		} catch (Exception ex) {
			Log.d(TAG, "failed requesting network location updates", ex);
		}
	}



	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

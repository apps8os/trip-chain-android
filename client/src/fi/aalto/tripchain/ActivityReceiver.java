package fi.aalto.tripchain;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

class ActivityReceiver extends BroadcastReceiver implements 
		GooglePlayServicesClient.ConnectionCallbacks, 
		GooglePlayServicesClient.OnConnectionFailedListener {
	private final static String TAG = ActivityReceiver.class.getSimpleName();
	
	private ActivityRecognitionClient activityRecognitionClient;
	
	private BackgroundService service;
	
	private boolean starting = true;
	
	private Intent activityIntent;
    private PendingIntent callbackIntent;
	
	public ActivityReceiver(BackgroundService service) {
		this.service = service;
		
		this.activityIntent = new Intent(Configuration.ACTIVITY_INTENT);
		this.callbackIntent = PendingIntent.getBroadcast(service, 0, activityIntent,            
	    		PendingIntent.FLAG_UPDATE_CURRENT);
		
		IntentFilter intentFilter = new IntentFilter(Configuration.ACTIVITY_INTENT);
		service.registerReceiver(this, intentFilter);
		
		activityRecognitionClient = new ActivityRecognitionClient(service, this, this);
		activityRecognitionClient.connect();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			DetectedActivity da = result.getMostProbableActivity();
			Activity activity = getActivityString(da);
			
			Log.d(TAG, "Probably: " + activity);
			
			this.service.getRoute().onActivity(activity);
		}
	}
	
	public static Activity getActivityString(DetectedActivity da) {
	    Activity activity = Activity.UNKNOWN;
	    switch (da.getType()) {
	    case DetectedActivity.IN_VEHICLE:
	    	activity = Activity.IN_VEHICLE;
	    	break;
	    case DetectedActivity.ON_BICYCLE:
	    	activity = Activity.ON_BICYCLE;
	    	break;
	    case DetectedActivity.ON_FOOT:
	    	activity = Activity.ON_FOOT;
	    	break;
	    case DetectedActivity.STILL:
	    	activity = Activity.STILL;
	    	break;
	    case DetectedActivity.TILTING:
	    	activity = Activity.TILTING;
	    	break;
	    }
	    
	    return activity;
	}
	

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d(TAG, "Connection failed");
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "Connection succeeded");
		
	    if (this.starting) {
	    	activityRecognitionClient.requestActivityUpdates(30000, callbackIntent);
	    	starting = false;
	    } else {
	    	activityRecognitionClient.removeActivityUpdates(callbackIntent);
	    }
	    
	    activityRecognitionClient.disconnect();
	    Log.d(TAG, "Requested and disconnected.");
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "Disconnected");
	}
	
	public void stop() {
		activityRecognitionClient.connect();
		service.unregisterReceiver(this);
	}

};
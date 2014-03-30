package fi.aalto.tripchain.receivers;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;

import fi.aalto.tripchain.Configuration;

public abstract class ActivityReceiver extends BroadcastReceiver implements 
		GooglePlayServicesClient.ConnectionCallbacks, 
		GooglePlayServicesClient.OnConnectionFailedListener {
	private final static String TAG = ActivityReceiver.class.getSimpleName();
	
	private ActivityRecognitionClient activityRecognitionClient;
	
	private Service service;
	
	private boolean starting = true;
	
	private Intent activityIntent;
    private PendingIntent callbackIntent;
	
	public ActivityReceiver(Service service) {
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
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			
			
			onActivityRecognitionResult(result);
		}		
	}
	
	public abstract void onActivityRecognitionResult(ActivityRecognitionResult activity);

}

package fi.aalto.tripchain;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

class ActivityReceiver extends BroadcastReceiver {
	private final static String TAG = ActivityReceiver.class.getSimpleName();
	
	BackgroundService service;
	
	public ActivityReceiver(BackgroundService service) {
		this.service = service;
		
		IntentFilter intentFilter = new IntentFilter(Configuration.ACTIVITY_INTENT);
		service.registerReceiver(this, intentFilter);
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

};
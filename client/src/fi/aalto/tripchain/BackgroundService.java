package fi.aalto.tripchain;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service  {
	private final static String TAG = BackgroundService.class.getSimpleName();
	
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

	@Override
	public void onCreate() {
		IntentFilter intentFilter = new IntentFilter(Configuration.ACTIVITY_INTENT);
		this.registerReceiver(activityReceiver, intentFilter);
		
		
	}



	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

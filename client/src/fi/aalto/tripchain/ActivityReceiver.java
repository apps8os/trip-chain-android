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

class ActivityReceiver extends BroadcastReceiver {
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
		}
	}
};
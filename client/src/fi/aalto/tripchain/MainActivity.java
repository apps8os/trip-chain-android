package fi.aalto.tripchain;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.internal.ac;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.LocationClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;

public class MainActivity extends Activity  {	
	private final static String TAG = MainActivity.class.getSimpleName();
	
	private Intent serviceIntent;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		serviceIntent = new Intent(this, BackgroundService.class);		
		startService(serviceIntent);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.d(TAG, "onDestroy");
		
		stopService(serviceIntent);
	}

}

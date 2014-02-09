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

public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, 
GooglePlayServicesClient.OnConnectionFailedListener {
	
	private final static String TAG = MainActivity.class.getSimpleName();
	private ActivityRecognitionClient activityRecognitionClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startService(new Intent(this, BackgroundService.class));
		
		activityRecognitionClient = new ActivityRecognitionClient(this, this, this);
		activityRecognitionClient.connect();
		
	}


	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "Connection failed");
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "Connection succeeded");
		
	    Intent intent = new Intent("fi.aalto.tripchain.activity.recognition");
	    //intent.putExtra("receiverTag", mReceiver);
	    PendingIntent callbackIntent = PendingIntent.getBroadcast(this, 0, intent,            
	    		PendingIntent.FLAG_UPDATE_CURRENT);
	    activityRecognitionClient.requestActivityUpdates(30000, callbackIntent);
	    activityRecognitionClient.disconnect();
	    Log.i(TAG, "Requested and disconnected.");
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "Disconnected");
	}

}

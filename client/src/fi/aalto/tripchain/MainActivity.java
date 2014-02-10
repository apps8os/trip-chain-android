package fi.aalto.tripchain;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

public class MainActivity extends Activity  {	
	private final static String TAG = MainActivity.class.getSimpleName();
	
	private Intent serviceIntent;
	private ServiceConnectionApi serviceConnectionApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startService();
	}
	
	private void startService() {
		serviceIntent = new Intent(this, BackgroundService.class);	
		startService(serviceIntent);
    	bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
	}
	
	private void stopService() {
		try {
			serviceConnectionApi.stop();
		} catch (Exception e) {
			Log.d(TAG, "stopping service failed", e);
		}
		
		try {
			unbindService(serviceConnection);
		} catch (Exception e) {
			Log.d(TAG, "Failed to unbind service", e);
		}
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() {		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "Service connection created " +name);			
			serviceConnectionApi = ServiceConnectionApi.Stub.asInterface(service);

		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "Service connection closed " +name);			
		}	
	};
	
	
	@Override
	protected void onStop() {
		super.onStop();
		
		Log.d(TAG, "onStop");
		
		stopService();
	}

}

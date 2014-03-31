package fi.aalto.tripchain;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MainActivity extends Activity {
	private final static String TAG = MainActivity.class.getSimpleName();

	private Intent serviceIntent;
	private ServiceConnectionApi serviceConnectionApi;

	private Button startButton;

	private boolean recording = false;
	
	SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService();
		
		preferences = getSharedPreferences(Configuration.SHARED_PREFERENCES, MODE_MULTI_PROCESS);
	}

	private void initUi() {
		setContentView(R.layout.activity_main);

		this.startButton = (Button) findViewById(R.id.button);
		this.startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!recording) {
					try {
						serviceConnectionApi.start();
						recording = true;
					} catch (RemoteException e) {
					}
				} else {
					try {
						serviceConnectionApi.stop();
						recording = false;
					} catch (RemoteException e) {
					}
				}

				initUi();
			}
		});

		this.startButton.setText(!recording ? "Start recording"
				: "Stop recording");
	}

	private void startService() {
		serviceIntent = new Intent(this, BackgroundService.class);
		startService(serviceIntent);
		bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
	}

	public void onStop() {
		super.onStop();

		if (!recording) {
			//stopService(serviceIntent);
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
			Log.i(TAG, "Service connection created " + name);
			serviceConnectionApi = ServiceConnectionApi.Stub
					.asInterface(service);

			try {
				recording = serviceConnectionApi.recording();
			} catch (RemoteException e) {
			}

			initUi();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "Service connection closed " + name);
		}
	};
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_logout:
	            logout();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void logout() {
		Editor e = preferences.edit();
		e.putString(Configuration.KEY_LOGIN_ID, null);
		e.commit();
		
		Intent i = new Intent(getApplicationContext(), LoginActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		finish();
	}
}

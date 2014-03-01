package fi.aalto.tripchain;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;

public class MainActivity extends Activity {
	private final static String TAG = MainActivity.class.getSimpleName();

	private Intent serviceIntent;
	private ServiceConnectionApi serviceConnectionApi;

	private static final int DIALOG_ACCOUNTS = 0;

	private Button startButton;

	private boolean recording = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService();
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
			stopService(serviceIntent);
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
}

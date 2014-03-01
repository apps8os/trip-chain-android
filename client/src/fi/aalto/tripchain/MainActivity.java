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
		
		chooseAccount();
	}
	
	private void chooseAccount() {
		// use https://github.com/frakbot/Android-AccountChooser for
		// compatibility with older devices
		Intent intent = AccountManager.newChooseAccountIntent(null, null,
		new String[] { "com.google" }, false, null, null, null, null);
		startActivityForResult(intent, 1237);
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

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ACCOUNTS:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select a Google account");
			AccountManager accountManager = AccountManager.get(this);
			final Account[] accounts = accountManager.getAccountsByType("com.google");
			final int size = accounts.length;
			String[] names = new String[size];
			for (int i = 0; i < size; i++) {
				names[i] = accounts[i].name;
			}
			builder.setItems(names, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Stuff to do when the account is selected by the user
					Log.d(TAG, "" + accounts[which]);
				}
			});
			return builder.create();
		}
		return null;
	}
}

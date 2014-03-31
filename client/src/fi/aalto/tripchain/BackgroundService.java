package fi.aalto.tripchain;

import java.util.Random;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import fi.aalto.tripchain.route.Trip;

public class BackgroundService extends Service  {
	private final static String TAG = BackgroundService.class.getSimpleName();
	
	private Handler handler;
	
	private volatile boolean recording = false;
	
	private Trip trip;
	
	private Starter starter;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		this.handler = new Handler();
		
		this.starter = new Starter(this);
		this.starter.start();
	}
	
	public void stop() {
		Log.d(TAG, "Stopping!");
		this.recording = false;
		
		this.trip.stop();
	}
	
	public void start() {
		Log.d(TAG, "Starting!");

		PendingIntent pe = PendingIntent.getActivity(this, 0, new Intent(this, LoginActivity.class), 0);
		
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(this)
			    .setSmallIcon(R.drawable.ic_launcher)
			    .setContentTitle("Tripchain")
			    .setContentText("Recording route")
			    .setContentIntent(pe);

		startForeground(new Random().nextInt(), mBuilder.build());
		
		this.recording = true;
		
		this.trip = new Trip(this);
		this.trip.start();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		this.starter.stop();
		
		Log.d(TAG, "onDestroy");
	}
	

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	private final ServiceConnectionApi.Stub mBinder = new ServiceConnectionApi.Stub() {
		@Override
		public void stop() throws RemoteException {
			handler.post(new Runnable() {
				@Override
				public void run() {
					BackgroundService.this.stop();
				}
			});
		}

		@Override
		public void start() throws RemoteException {
			handler.post(new Runnable() {
				@Override
				public void run() {
					BackgroundService.this.start();
				}
			});
		}

		@Override
		public boolean recording() throws RemoteException {
			return BackgroundService.this.recording;
		}
	};

}

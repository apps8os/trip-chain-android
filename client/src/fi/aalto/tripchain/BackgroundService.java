package fi.aalto.tripchain;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class BackgroundService extends Service  {
	private final static String TAG = BackgroundService.class.getSimpleName();
	
	private ActivityReceiver activityReceiver;
	private LocationListener locationListener;
	private Route route;
	
	private Handler handler;
	
	private volatile boolean recording = false;
	
	
	public synchronized Route getRoute() {
		return this.route;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		this.handler = new Handler();
	}
	
	public void stop() {
		Log.d(TAG, "Stopping!");
		this.activityReceiver.stop();
		this.locationListener.stop();
		this.recording = false;
		
		// TODO XXX Post json
		
		stopForeground(true);
	}
	
	public void start() {
		Log.d(TAG, "Starting!");
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(this)
			    .setSmallIcon(R.drawable.ic_launcher)
			    .setContentTitle("Tripchain")
			    .setContentText("Recording route");
		
		startForeground(12345, mBuilder.build());
		
		this.recording = true;
		this.route = new Route();
		this.activityReceiver = new ActivityReceiver(this);
		this.locationListener = new LocationListener(this);		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
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

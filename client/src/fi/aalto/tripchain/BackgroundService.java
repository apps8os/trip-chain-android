package fi.aalto.tripchain;

import android.app.Service;
import android.content.Intent;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class BackgroundService extends Service  {
	private final static String TAG = BackgroundService.class.getSimpleName();
	
	private ActivityReceiver activityReceiver;
	private LocationListener locationListener;
	private Route route;
	
	private Handler handler;
	
	
	public synchronized Route getRoute() {
		return this.route;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		this.handler = new Handler();
		this.route = new Route();
		this.activityReceiver = new ActivityReceiver(this);
		this.locationListener = new LocationListener(this);
	}
	
	public void stop() {
		Log.d(TAG, "Stopping!");
		this.activityReceiver.stop();
		this.locationListener.stop();
		
		// TODO XXX Post json
		
		stopSelf();
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
	};

}

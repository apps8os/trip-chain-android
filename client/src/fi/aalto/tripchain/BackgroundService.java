package fi.aalto.tripchain;

import android.app.Service;
import android.content.Intent;

import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service  {
	private final static String TAG = BackgroundService.class.getSimpleName();
	
	private ActivityReceiver activityReceiver;
	private LocationListener locationListener;
	private Route route;
	
	
	public synchronized Route getRoute() {
		return this.route;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		this.route = new Route();
		this.activityReceiver = new ActivityReceiver(this);
		this.locationListener = new LocationListener(this);
	}
	
	@Override
	public void onDestroy() {
		this.activityReceiver.stop();
		this.locationListener.stop();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

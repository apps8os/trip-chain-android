package fi.aalto.tripchain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import fi.aalto.tripchain.route.TripRecorder;

public class BackgroundService extends Service  {
	private final static String TAG = BackgroundService.class.getSimpleName();
	
	private Handler handler;
	
	private volatile boolean recording = false;
	
	private TripRecorder trip;
	
	private PowerManager.WakeLock wakeLock;
	
	List<Client> clients = new CopyOnWriteArrayList<Client>();
	private Map<Integer, Client> clientMap = new HashMap<Integer, Client>();

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		this.handler = new Handler();
	}
	
	private void aquireWakeLock() {
	    final PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
	    this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
	    this.wakeLock.acquire();
	}

	/**
	 * Release the wake lock.
	 */
	private void releaseWakeLock() {
	    if( this.wakeLock == null )
	        return;
	    this.wakeLock.release();
	    this.wakeLock = null;
	}
	
	public void stop() {
		Log.d(TAG, "Stopping!");
		this.recording = false;
		
		this.trip.stop();
		releaseWakeLock();
	}
	
	public void start() {
		Log.d(TAG, "Starting!");
		aquireWakeLock();

		PendingIntent pe = PendingIntent.getActivity(this, 0, new Intent(this, LoginActivity.class), 0);
		
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(this)
			    .setSmallIcon(R.drawable.ic_launcher)
			    .setContentTitle("Tripchain")
			    .setContentText("Recording route")
			    .setContentIntent(pe);

		startForeground(new Random().nextInt(), mBuilder.build());
		
		this.recording = true;
		
		this.trip = new TripRecorder(this, clients);
		this.trip.start();
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

		@Override
		public void subscribe(Client client, int hashCode) throws RemoteException {
			clients.add(client);
			clientMap.put(hashCode, client);
		}

		@Override
		public void unsubscribe(int hashCode) throws RemoteException {
			Client c = clientMap.get(hashCode);
			clients.remove(c);
			clientMap.remove(hashCode);
		}
	};

}

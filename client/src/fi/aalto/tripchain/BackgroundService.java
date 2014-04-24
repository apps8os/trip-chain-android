package fi.aalto.tripchain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import fi.aalto.tripchain.receivers.EventDispatcher;
import fi.aalto.tripchain.receivers.EventListener;
import fi.aalto.tripchain.route.Activity;
import fi.aalto.tripchain.route.Trip;

public class BackgroundService extends Service implements EventListener  {
	private final static String TAG = BackgroundService.class.getSimpleName();
	
	private Handler handler;
	
	private volatile boolean recording = false;
	
	private Trip trip;
	
	private final static UUID PEBBLE_APP_UUID = UUID.fromString("3b760d02-93f3-4c0d-aea3-687a466eaab3");
	
	
	List<Client> clients = new CopyOnWriteArrayList<Client>();
	private Map<Integer, Client> clientMap = new HashMap<Integer, Client>();

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");

		PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
  
		this.handler = new Handler();
		EventDispatcher.subscribe(this);
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
		
		this.trip = new Trip(this, clients);
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



	@Override
	public void onLocation(Location location) {
		// TODO Auto-generated method stub
		Log.i("Hello", "Hello");
		PebbleDictionary data = new PebbleDictionary();
		data.addString(0, String.format("%f N", location.getLatitude()));
		data.addString(1, String.format("%f E", location.getLongitude()));
		data.addString(3, String.format("%f E", location.getLongitude()));
		
		PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);
		
	}

	@Override
	public void onActivity(Activity activity) {
		// TODO Auto-generated method stub
		Log.i("HelloActivityRecon", "HelloHelloHello");
		PebbleDictionary data = new PebbleDictionary();
		data.addString(3, "default");
		//PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);
		
	}}

package fi.aalto.tripchain;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import fi.aalto.tripchain.route.ActivityReceiver;
import fi.aalto.tripchain.route.LocationListener;
import fi.aalto.tripchain.route.Route;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.AsyncTask;
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
	
	private long timestamp;
	
	private SharedPreferences preferences;
	
	
	public synchronized Route getRoute() {
		return this.route;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		this.handler = new Handler();
		
		preferences = getSharedPreferences(Configuration.SHARED_PREFERENCES, MODE_MULTI_PROCESS);
	}
	
	private void postTrip(JSONObject trip) throws ClientProtocolException, IOException {
	    HttpClient client = new DefaultHttpClient();
	    HttpPost httpPost = new HttpPost("http://tripchaingame.herokuapp.com/api/trip.json");
	    
	    httpPost.addHeader("Content-Type", "application/json");
	    httpPost.setEntity(new StringEntity(trip.toString()));
	    
	    HttpResponse response = client.execute(httpPost);
	    Log.d(TAG, "post status: " + response.getStatusLine());
	}
	
	public void stop() {
		Log.d(TAG, "Stopping!");
		this.activityReceiver.stop();
		this.locationListener.stop();
		this.recording = false;
		
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					JSONObject trip = new JSONObject();
					trip.put("clientId", preferences.getString("login_id", null));
					trip.put("trip", route.toJson());
					trip.put("startedAt", timestamp);
					postTrip(trip);
				} catch (Exception e) {
					Log.d(TAG, "Failed to post trip", e);
				}
				
				stopForeground(true);
				return null;
			}
		}.execute();
	}
	
	public void start() {
		Log.d(TAG, "Starting!");
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(this)
			    .setSmallIcon(R.drawable.ic_launcher)
			    .setContentTitle("Tripchain")
			    .setContentText("Recording route");

		startForeground(12345, mBuilder.build());
		
		this.timestamp = System.currentTimeMillis();
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

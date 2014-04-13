package fi.aalto.tripchain.route;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import fi.aalto.tripchain.Client;
import fi.aalto.tripchain.Configuration;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.util.Log;

public class TripRecorder {
	private final static String TAG = TripRecorder.class.getSimpleName();
	
	private ActivityListener activityListener;
	private LocationListener locationListener;
	private Trip trip;
	private long timestamp = 0;
	
	private Service context;
	
	private SharedPreferences preferences;
	
	public void stop() {
		this.activityListener.stop();
		this.locationListener.stop();
		
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				report();

				context.stopForeground(true);
				
				return null;
			}
		}.execute();
	}
	
	public void start() {
		this.timestamp = System.currentTimeMillis();
		this.activityListener.start();
		this.locationListener.start();
	}
	
	public TripRecorder(Service context, List<Client> clients) {
		this.context = context;
		this.trip = new Trip(clients);
		this.activityListener = new ActivityListener(context, trip);
		this.locationListener = new LocationListener(context, trip);		
		
		preferences = context.getSharedPreferences(Configuration.SHARED_PREFERENCES, Context.MODE_MULTI_PROCESS);
	}
	
	private void report() {
		try {
			JSONObject j = new JSONObject();
			j.put("userId", preferences.getString(Configuration.KEY_LOGIN_ID, null));
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			j.put("clientVersion", pInfo.versionName);
			
			j.put("trip", trip.toGeoJson());
			j.put("locations", trip.toLocations());
			j.put("activities", trip.toActivities());
			j.put("roads", trip.toRoads());
			j.put("startedAt", timestamp);
			
			Log.d(TAG, j.toString(2));
			postTrip(j);
		} catch (Exception e) {
			Log.d(TAG, "Failed to post trip", e);
		}
	}
	
	private void postTrip(JSONObject trip) throws ClientProtocolException, IOException {
	    HttpClient client = new DefaultHttpClient();
	    HttpPost httpPost = new HttpPost("http://tripchaingame.herokuapp.com/api/trip.json");
	    
	    httpPost.addHeader("Content-Type", "application/json");
	    httpPost.setEntity(new StringEntity(trip.toString()));
	    
	    HttpResponse response = client.execute(httpPost);
	    Log.d(TAG, "post status: " + response.getStatusLine());
	}
}

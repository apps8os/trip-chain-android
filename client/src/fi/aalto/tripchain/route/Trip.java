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

public class Trip {
	private final static String TAG = Trip.class.getSimpleName();
	
	private ActivityListener activityListener;
	private LocationListener locationListener;
	private Route route;
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
	
	public Trip(Service context, List<Client> clients) {
		this.context = context;
		this.route = new Route(clients);
		this.activityListener = new ActivityListener(context, route);
		this.locationListener = new LocationListener(context, route);		
		
		preferences = context.getSharedPreferences(Configuration.SHARED_PREFERENCES, Context.MODE_MULTI_PROCESS);
	}
	
	private void report() {
		try {
			JSONObject trip = new JSONObject();
			trip.put("userId", preferences.getString(Configuration.KEY_LOGIN_ID, null));
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			trip.put("clientVersion", pInfo.versionName);
			
			trip.put("trip", route.toFeatureCollection());
			trip.put("locations", route.toLocations());
			trip.put("activities", route.toActivities());
			trip.put("roads", route.toRoadSegments());
			trip.put("startedAt", timestamp);	
			
			Log.d(TAG, trip.toString(2));
			postTrip(trip);
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

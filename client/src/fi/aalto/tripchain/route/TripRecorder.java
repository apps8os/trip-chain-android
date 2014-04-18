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

	
	private Service context;
	
	private SharedPreferences preferences;
	
	public void stop() {
		this.activityListener.stop();
		this.locationListener.stop();
		
		this.trip.stop();
		
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
		this.activityListener.start();
		this.locationListener.start();
		this.trip.start();
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
			JSONObject j = trip.toJson();
			j.put("userId", preferences.getString(Configuration.KEY_LOGIN_ID, null));
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			j.put("clientVersion", pInfo.versionName);
			
			Log.d(TAG, j.toString(2));
			postTrip(j);
		} catch (Exception e) {
			Log.d(TAG, "Failed to post trip", e);
		}
	}
	
	private void postTrip(JSONObject trip) throws ClientProtocolException, IOException {
	    HttpClient client = new DefaultHttpClient();
	    client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
	    HttpPost httpPost = new HttpPost("http://192.168.100.101:5000/api/trip.json");//new HttpPost("http://tripchaingame.herokuapp.com/api/trip.json");
	    
	    httpPost.addHeader("Content-Type", "application/json");
	    httpPost.setEntity(new StringEntity(trip.toString(), "UTF-8"));
	    
	    HttpResponse response = client.execute(httpPost);
	    Log.d(TAG, "post status: " + response.getStatusLine());
	}
}

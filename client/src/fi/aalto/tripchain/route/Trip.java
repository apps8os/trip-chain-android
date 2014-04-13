package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.aalto.tripchain.Client;

import fi.aalto.tripchain.here.Address;
import android.location.Location;
import android.os.RemoteException;

public class Trip {	
	private List<Location> locations = new ArrayList<Location>();
	private List<ActivityModel> activities = new ArrayList<ActivityModel>();
	
	private List<Client> clients;
	
	private Roads roads = new Roads();
	private Route route = new Route();
	
	public Trip(List<Client> clients) {
		this.clients = clients;
	}

	private static class ActivityModel {
		final long timestamp;
		final Activity activity;

		public ActivityModel(Activity activity) {
			this.activity = activity;
			this.timestamp = System.currentTimeMillis();
		}
	}

	public void onActivity(Activity activity) {
		if (activity == Activity.TILTING) {
			return;
		}

		this.activities.add(new ActivityModel(activity));

		route.onActivity(activity);
	}

	public void onLocation(Location location) {
		this.locations.add(location);

		for (Client c : clients) {
			try {
				c.onLocation(locations);
			} catch (RemoteException e) {
			}
		}
		
		route.onLocation(location);
		roads.onLocation(location);
	}

	public JSONArray toLocations() throws JSONException {
		return toLocations(this.locations);
	}
	
	
	static JSONArray toLocations(List<Location> locations) throws JSONException {
		JSONArray locs = new JSONArray();

		for (Location l : locations) {
			JSONObject location = new JSONObject();
			location.put("time", l.getTime());
			location.put("longitude", l.getLongitude());
			location.put("latitude", l.getLatitude());
			location.put("speed", l.getSpeed());
			location.put("altitude", l.getAltitude());
			location.put("bearing", l.getBearing());
			location.put("accuracy", l.getAccuracy());

			locs.put(location);
		}

		return locs;
	}
	
	public JSONArray toRoads() throws JSONException {
		return this.roads.toJson();
	}
	
	public JSONObject toGeoJson() throws JSONException {
		return this.route.toJson();
	}

	public JSONArray toActivities() throws JSONException {
		JSONArray activities = new JSONArray();

		for (ActivityModel a : this.activities) {
			JSONObject activity = new JSONObject();
			activity.put("time", a.timestamp);
			activity.put("value", a.activity.toString());

			activities.put(activity);
		}

		return activities;
	}
}

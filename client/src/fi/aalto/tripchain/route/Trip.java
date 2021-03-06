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

/**
 * Models a trip as locations, activities, Roads and Route.
 *
 */
public class Trip {	
	private List<Location> locations = new ArrayList<Location>();
	private List<ActivityModel> activities = new ArrayList<ActivityModel>();
	
	private List<Client> clients;
	
	private Roads roads = new Roads();
	private Route route = new Route();
	
	private long startedAt;
	private long stoppedAt;
	
	public void start() {
		startedAt = System.currentTimeMillis();
		
		roads.start();
	}
	
	public void stop() {
		stoppedAt = System.currentTimeMillis();
		
		roads.stop();
	}
	
	
	public Trip(List<Client> clients) {
		this.clients = clients;
	}

	static class ActivityModel {
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

		ActivityModel am = new ActivityModel(activity);
		this.activities.add(am);

		route.onActivity(am);
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
	
	public JSONObject toJson() throws JSONException {
		JSONObject j = new JSONObject();
		j.put("trip", route.toJson());
		j.put("locations", toLocations());
		j.put("activities", toActivities());
		j.put("roads", roads.toJson());
		j.put("startedAt", startedAt);
		j.put("stoppedAt", stoppedAt);
		
		return j;
	}
}

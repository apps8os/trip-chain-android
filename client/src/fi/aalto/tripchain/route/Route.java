package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class Route {
	private List<RouteSegment> route = new ArrayList<RouteSegment>();
	private Location lastLocation;
	
	private List<Location> locations = new ArrayList<Location>();
	private List<ActivityModel> activities = new ArrayList<ActivityModel>();
	
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
		
	    if (route.size() == 0) {
	    	route.add(new RouteSegment(activity));
	    	if (lastLocation != null) {
	    		onLocation(lastLocation);
	    		lastLocation = null;
	    	}
	    } else {
	    	RouteSegment lastSegment = route.get(route.size() - 1);
	    	if (lastSegment.activity != activity) {
	    		// new segment should begin where old one ends
	    		RouteSegment newSegment = new RouteSegment(activity);
	    		newSegment.addLocation(lastSegment.getLastLocation());
	    		route.add(newSegment);
	    	}
	    }
	}
	
	public void onLocation(Location location) {
		this.locations.add(location);
		
	    if (route.size() == 0) {
	    	lastLocation = location;
	    	return;
	    }
	    
    	RouteSegment lastSegment = route.get(route.size() - 1);
    	lastSegment.addLocation(location);
	}
	
	public JSONObject toFeatureCollection() throws JSONException {		
		JSONObject featureCollection = new JSONObject();
		JSONArray features = new JSONArray();
		
		for (RouteSegment rs : route) {
			JSONObject j = rs.toJson();
			if (j != null) {
				features.put(j);
			}
		}
		
		featureCollection.put("type", "FeatureCollection");
		featureCollection.put("features", features);
		
		return featureCollection;
	}
	
	public JSONArray toLocations() throws JSONException {
		JSONArray locations = new JSONArray();
		
		for (Location l : this.locations) {
			JSONObject location = new JSONObject();
			location.put("time", l.getTime());
			location.put("longitude", l.getLongitude());
			location.put("latitude", l.getLatitude());
			location.put("speed", l.getSpeed());
			location.put("altitude", l.getAltitude());
			location.put("bearing", l.getBearing());
			location.put("accuracy", l.getAccuracy());
			
			locations.put(location);
		}
		
		return locations;
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

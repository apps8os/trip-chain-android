package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.aalto.tripchain.route.Trip.ActivityModel;

import android.location.Location;

/**
 * Represents a subpath of a trip as the mode of activity and coordinates.
 */
public class RouteSegment {
	final public Activity activity;
	List<Location> locations;
	final private long time;
	
	public RouteSegment(ActivityModel activityModel) {
		this.activity = activityModel.activity;
		this.time = activityModel.timestamp;
		this.locations = new ArrayList<Location>();
	}
	
	public void addLocation(Location location) {
		locations.add(location);
	}
	
	public Location getLastLocation() {
		return this.locations.get(this.locations.size() - 1);
	}
	
	public JSONObject toJson() throws JSONException {
		if (locations.size() == 0) {
			return null;
		}
		
		JSONObject feature = new JSONObject();
		JSONObject geometry = new JSONObject();
		
		JSONArray coordinates = new JSONArray();
		if (locations.size() > 1) {
			geometry.put("type", "LineString");
			
			for (Location l : locations) {
				JSONArray tuple = new JSONArray();
				tuple.put(l.getLongitude());
				tuple.put(l.getLatitude());
				coordinates.put(tuple);
			}

		} else {
			geometry.put("type", "Point");			
			
			coordinates.put(locations.get(0).getLongitude());
			coordinates.put(locations.get(0).getLatitude());
		}
		
		geometry.put("coordinates", coordinates);
		
		JSONObject properties = new JSONObject();
		properties.put("activity", activity.toString());
		properties.put("time", time);
		
		feature.put("geometry", geometry);
		feature.put("properties", properties);
		feature.put("type", "Feature");
		
		return feature;
	}
	
	
}

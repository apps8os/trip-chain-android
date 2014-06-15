package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.aalto.tripchain.route.Trip.ActivityModel;

import android.location.Location;

/**
 * Models trip as RouteSegments.
 * @author juke
 *
 */
public class Route {
	private List<RouteSegment> route = new ArrayList<RouteSegment>();
	private Location lastLocation;
	
	public void onActivity(ActivityModel am) {
		if (route.size() == 0) {
			route.add(new RouteSegment(am));
			if (lastLocation != null) {
				onLocation(lastLocation);
				lastLocation = null;
			}
		} else {
			RouteSegment lastSegment = route.get(route.size() - 1);
			if (lastSegment.activity != am.activity) {
				// new segment should begin where old one ends
				RouteSegment newSegment = new RouteSegment(am);
				newSegment.addLocation(lastSegment.getLastLocation());
				route.add(newSegment);
			}
		}
	}
	
	
	void onLocation(Location location) {
		if (route.size() == 0) {
			lastLocation = location;
		} else {
			RouteSegment lastRouteSegment = route.get(route.size() - 1);
			lastRouteSegment.addLocation(location);
		}		
	}
	
	public JSONObject toJson() throws JSONException {
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
}

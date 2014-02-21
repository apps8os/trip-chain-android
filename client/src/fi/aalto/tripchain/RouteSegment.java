package fi.aalto.tripchain;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.ge;
import com.google.android.gms.location.DetectedActivity;

import android.location.Location;

public class RouteSegment {
	final public Activity activity;
	List<Location> locations;
	
	public RouteSegment(Activity activity) {
		this.activity = activity;
		locations = new ArrayList<Location>();
	}
	
	public void addLocation(Location location) {
		locations.add(location);
	}
	
	public JSONObject toJson() throws JSONException {
		/*
		 * {
			    "type": "LineString", 
			    "coordinates": [
			        [30, 10], [10, 30], [40, 40]
			    ]
			}
			
			
			
			{
			    "type": "Feature",
			    "properties": {
			        "name": "Coors Field",
			        "amenity": "Baseball Stadium",
			        "popupContent": "This is where the Rockies play!"
			    },
			    "geometry": {
			        "type": "Point",
			        "coordinates": [-104.99404, 39.75621]
			    }
			}
		 */
		
		JSONObject feature = new JSONObject();
		JSONObject geometry = new JSONObject();
		JSONArray coordinates = new JSONArray();
		for (Location l : locations) {
			JSONArray tuple = new JSONArray();
			tuple.put(l.getLongitude());
			tuple.put(l.getLatitude());
			coordinates.put(tuple);
		}
		
		geometry.put("coordinates", coordinates);
		geometry.put("type", "LineString");
		
		JSONObject properties = new JSONObject();
		properties.put("activity", activity.toString());
		
		feature.put("geometry", geometry);
		feature.put("properties", properties);
		feature.put("type", "feature");
		
		return feature;
	}
}

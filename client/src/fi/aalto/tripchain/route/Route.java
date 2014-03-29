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
	
	public void onActivity(Activity activity) {
		if (activity == Activity.TILTING) {
			return;
		}
		
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
	    if (route.size() == 0) {
	    	lastLocation = location;
	    	return;
	    }
	    
    	RouteSegment lastSegment = route.get(route.size() - 1);
    	lastSegment.addLocation(location);
	}
	
	public JSONObject toJson() throws JSONException {
		/*
		  { "type": "FeatureCollection",
		    "features": [
		      { "type": "Feature",
		        "geometry": {"type": "Point", "coordinates": [102.0, 0.5]},
		        "properties": {"prop0": "value0"}
		        },
		      { "type": "Feature",
		        "geometry": {
		          "type": "LineString",
		          "coordinates": [
		            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]
		            ]
		          },
		        "properties": {
		          "prop0": "value0",
		          "prop1": 0.0
		          }
		        },
		      { "type": "Feature",
		         "geometry": {
		           "type": "Polygon",
		           "coordinates": [
		             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],
		               [100.0, 1.0], [100.0, 0.0] ]
		             ]
		         },
		         "properties": {
		           "prop0": "value0",
		           "prop1": {"this": "that"}
		           }
		         }
		       ]
		     }		
		 */
		
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

package fi.aalto.tripchain;

import java.util.ArrayList;
import java.util.List;

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
	    		route.add(new RouteSegment(activity));
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
}

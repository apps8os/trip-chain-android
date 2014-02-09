package fi.aalto.tripchain;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

public class Route {
	private List<RouteSegment> route = new ArrayList<RouteSegment>();
	
	public void onActivity(Activity activity) {
	    if (route.size() == 0) {
	    	route.add(new RouteSegment(activity));
	    } else {
	    	RouteSegment lastSegment = route.get(route.size() - 1);
	    	if (lastSegment.activity != activity) {
	    		route.add(new RouteSegment(activity));
	    	}
	    }
	}
	
	public void onLocation(Location location) {
	    if (route.size() == 0) {
	    	return;
	    }
	    
    	RouteSegment lastSegment = route.get(route.size() - 1);
    	lastSegment.addLocation(location);
	}
}

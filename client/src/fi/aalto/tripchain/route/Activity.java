package fi.aalto.tripchain.route;

import com.google.android.gms.location.DetectedActivity;


/**
 * Maps Play Service DetectedActivities to strings.
 * @author juke
 *
 */
public enum Activity {
	IN_VEHICLE("in-vehicle"), 
	ON_FOOT("on-foot"),
	STILL("still"), 
	UNKNOWN("unknown"), 
	ON_BICYCLE("on-bicycle"), 
	TILTING("tilting");
		
	private final String name;
	 
	private Activity(String name) {
		this.name = name;
	}
	
	@Override 
	public String toString() {
		return this.name;
	}
	
	public static Activity getActivity(DetectedActivity da) {
	    Activity activity = Activity.UNKNOWN;
	    switch (da.getType()) {
	    case DetectedActivity.IN_VEHICLE:
	    	activity = Activity.IN_VEHICLE;
	    	break;
	    case DetectedActivity.ON_BICYCLE:
	    	activity = Activity.ON_BICYCLE;
	    	break;
	    case DetectedActivity.ON_FOOT:
	    	activity = Activity.ON_FOOT;
	    	break;
	    case DetectedActivity.STILL:
	    	activity = Activity.STILL;
	    	break;
	    case DetectedActivity.TILTING:
	    	activity = Activity.TILTING;
	    	break;
	    }
	    
	    return activity;
	}
}

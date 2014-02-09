package fi.aalto.tripchain;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.location.DetectedActivity;

import android.location.Location;

public class RouteSegment {
	final public Activity activity;
	List<Location> coordinates;
	
	public RouteSegment(Activity activity) {
		this.activity = activity;
		coordinates = new ArrayList<Location>();
	}
	
	public void addLocation(Location location) {
		coordinates.add(location);
	}
	
	public JSONObject toJson() {
		return null;
	}
}

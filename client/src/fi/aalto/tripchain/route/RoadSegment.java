package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

public class RoadSegment {
	public final String street;
	public List<Location> locations = new ArrayList<Location>();
	
	RoadSegment(String street) {
		this.street = street;
	}
	
	void addLocation(Location loc) {
		this.locations.add(loc);
	}
	
	boolean match(String street) {
		return this.street.equals(street);
	}
}

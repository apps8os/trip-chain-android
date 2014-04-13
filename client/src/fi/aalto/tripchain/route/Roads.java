package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import fi.aalto.tripchain.here.Address;

public class Roads {
	private List<RoadSegment> roadSegments = new ArrayList<RoadSegment>();
	
	void onLocation(Location location, List<Address> addresses) {
		if (roadSegments.size() == 0) {
			String street;
			if (addresses.size() == 0) {
				street = "";
			} else {
				street = addresses.get(0).street;
			}
			
			RoadSegment rs = new RoadSegment(street);
			rs.addLocation(location);
			roadSegments.add(rs);
			return;
		}
		
		RoadSegment lastRoadSegment = roadSegments.get(roadSegments.size() - 1);
		if (addresses.size() > 0) {
			boolean stillOnTheSameRoad = false;
			for (Address address : addresses) {
				if (lastRoadSegment.match(address.street)) {
					stillOnTheSameRoad = true;
					break;
				}
			}
			
			if (stillOnTheSameRoad) {
				lastRoadSegment.addLocation(location);
			} else {
				roadSegments.add(new RoadSegment(addresses.get(0).street));
			}			
		} else if (lastRoadSegment.match("")) {
			lastRoadSegment.addLocation(location);
		} else {
			RoadSegment rs = new RoadSegment("");
			rs.addLocation(location);
			roadSegments.add(rs);
		}
	}
	
	public JSONArray toJson() throws JSONException {
		JSONArray roads = new JSONArray();
		
		for (RoadSegment rs : this.roadSegments) {
			JSONObject j = new JSONObject();
			j.put("locations", Trip.toLocations(rs.locations));
			j.put("street", rs.street);
			
			roads.put(j);
		}
		
		return roads;
	}
}

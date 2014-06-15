package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;
import fi.aalto.tripchain.here.Address;
import fi.aalto.tripchain.here.ReverseGeocoder;

/**
 * Models a trip as RoadSegments.
 *
 */
public class Roads {
	private static final String TAG = Roads.class.getSimpleName();
	
	private List<RoadSegment> roadSegments = new ArrayList<RoadSegment>();
	private RoadSegment lastRoadSegment = null;
	
	/**
	 * HandlerThread for network activity. Prevents blocking on main thread.
	 */
	private HandlerThread thread = new HandlerThread("reverseGeocodingHandlerThread");
	
	private Handler handler;
	
	void start() {
		thread.start();
		handler = new Handler(thread.getLooper());
	}
	
	void stop() {
		thread.quit();
	}
	
	/**
	 * Does reverse geocoding on coordinates.
	 * @param location
	 */
	private void addressQuery(final Location location) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				List<Address> addresses = ReverseGeocoder.query(location);
				if (addresses.size() > 0) {
					onAddress(location, addresses);
				}
			}
		});	 
	}
	
	void onLocation(Location location) {
		addressQuery(location);
	}
	
	/**
	 * Handles roadSegments list.
	 * @param location
	 * @param addresses
	 */
	private void onAddress(Location location, List<Address> addresses) {
		if (roadSegments.size() == 0) {
			lastRoadSegment = new RoadSegment(location, addresses);
			roadSegments.add(lastRoadSegment);
		} else if (lastRoadSegment.stillOnTheSameStreet(addresses)) {
			lastRoadSegment.addLocation(location, addresses);
		} else {
			lastRoadSegment = new RoadSegment(location, addresses);
			roadSegments.add(lastRoadSegment);
		}
	}
	
	public JSONArray toJson() throws JSONException {
		JSONArray roads = new JSONArray();
		
		for (RoadSegment rs : this.roadSegments) {
			JSONObject j = new JSONObject();
			j.put("locations", Trip.toLocations(rs.locations));
			j.put("street", rs.latestAddress.street);
			j.put("city", rs.latestAddress.city);
			j.put("country", rs.latestAddress.country);
			j.put("addresses", rs.toAddresses());
			
			roads.put(j);
		}
		
		return roads;
	}
}

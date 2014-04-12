package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.aalto.tripchain.Client;

import fi.aalto.tripchain.here.Address;
import android.location.Location;
import android.os.RemoteException;

public class Route {
	private List<RouteSegment> route = new ArrayList<RouteSegment>();
	private Location lastLocation;
	
	private List<RoadSegment> roadSegments = new ArrayList<RoadSegment>();

	private List<Location> locations = new ArrayList<Location>();
	private List<ActivityModel> activities = new ArrayList<ActivityModel>();
	
	private List<Client> clients;
	
	public Route(List<Client> clients) {
		this.clients = clients;
	}

	private static class ActivityModel {
		final long timestamp;
		final Activity activity;

		public ActivityModel(Activity activity) {
			this.activity = activity;
			this.timestamp = System.currentTimeMillis();
		}
	}

	public void onActivity(Activity activity) {
		if (activity == Activity.TILTING) {
			return;
		}

		this.activities.add(new ActivityModel(activity));

		if (route.size() == 0) {
			route.add(new RouteSegment(activity));
			if (lastLocation != null) {
				updateRoute(lastLocation);
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
	
	private void updateRoute(Location location) {
		if (route.size() == 0) {
			lastLocation = location;
		} else {
			RouteSegment lastRouteSegment = route.get(route.size() - 1);
			lastRouteSegment.addLocation(location);
		}		
	}
	
	private void updateRoads(Location location, List<Address> addresses) {
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

	public void onLocation(Location location, List<Address> addresses) {
		this.locations.add(location);

		for (Client c : clients) {
			try {
				c.onLocation(locations);
			} catch (RemoteException e) {
			}
		}
		
		updateRoute(location);
		updateRoads(location, addresses);
	}

	public JSONObject toFeatureCollection() throws JSONException {
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

	public JSONArray toLocations() throws JSONException {
		return toLocations(this.locations);
	}
	
	private JSONArray toLocations(List<Location> locations) throws JSONException {
		JSONArray locs = new JSONArray();

		for (Location l : locations) {
			JSONObject location = new JSONObject();
			location.put("time", l.getTime());
			location.put("longitude", l.getLongitude());
			location.put("latitude", l.getLatitude());
			location.put("speed", l.getSpeed());
			location.put("altitude", l.getAltitude());
			location.put("bearing", l.getBearing());
			location.put("accuracy", l.getAccuracy());

			locs.put(location);
		}

		return locs;
	}

	public JSONArray toActivities() throws JSONException {
		JSONArray activities = new JSONArray();

		for (ActivityModel a : this.activities) {
			JSONObject activity = new JSONObject();
			activity.put("time", a.timestamp);
			activity.put("value", a.activity.toString());

			activities.put(activity);
		}

		return activities;
	}
	
	public JSONArray toRoadSegments() throws JSONException {
		JSONArray roads = new JSONArray();
		
		for (RoadSegment rs : this.roadSegments) {
			JSONObject j = new JSONObject();
			j.put("locations", toLocations(rs.locations));
			j.put("street", rs.street);
			
			roads.put(j);
		}
		
		return roads;
	}
}

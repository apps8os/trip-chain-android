package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import fi.aalto.tripchain.here.Address;
import fi.aalto.tripchain.receivers.EventDispatcher;
import android.location.Location;
import android.util.Log;

/**
 * Represents a subpath of a trip as address-location pairs along a street.
 *
 */
public class RoadSegment {
	private final static String TAG = RoadSegment.class.getSimpleName();
	
	String currentStreetName;
	Address latestAddress;
	List<Location> locations = new ArrayList<Location>();
	List<List<Address>> addressLists = new ArrayList<List<Address>>();

	List<Address> currentStreetAddresses = new ArrayList<Address>();
	
	RoadSegment(Location location, List<Address> addresses) {
		stillOnTheSameStreet(addresses);
		addLocation(location, addresses);
	}
	
	private Map<String, Integer> calculateStreetFrequency(List<Address> newAddresses) {
		List<List<Address>> addresses = new ArrayList<List<Address>>();
		addresses.addAll(addressLists);
		addresses.add(newAddresses);
		
		Map<String, Integer> streetFrequency = new HashMap<String, Integer>();
		for (List<Address> addressList : addresses) {
			Set<String> streetSet = new HashSet<String>();
			for (Address a : addressList) {
				String street = a.street;
				
				// to not add streets multiple times per one location
				if (streetSet.contains(street)) {
					continue;
				}
				
				streetSet.add(street);
				
				if (streetFrequency.containsKey(street)) {
					int f = streetFrequency.get(street);
					streetFrequency.put(street, f + 1);
				} else {
					streetFrequency.put(street, 1);
				}
			}
		}
		
		return streetFrequency;
	}
	
	private String checkCommonStreet(Map<String, Integer> frequency, int locations) {
		for (String street : frequency.keySet()) {
			if (frequency.get(street) == locations) {
				return street;
			}
		}
		
		return null;
	}
	
	private void updateStreetAddressList() {
		if (this.currentStreetName == null) {
			this.currentStreetAddresses = null;
			return;
		}
		
		List<Address> streetAddresses = new ArrayList<Address>();
		for (List<Address> locationAddresses : addressLists) {
			for (Address a : locationAddresses) {
				if (a.street.equals(currentStreetName)) {
					streetAddresses.add(a);
					break;
				}
			}
		}
		
		this.currentStreetAddresses = streetAddresses;
		this.latestAddress = streetAddresses.get(streetAddresses.size() - 1);
		
		Log.d(TAG, "Current address: " + latestAddress.label);
		
		EventDispatcher.onAddress(latestAddress);
	}
	
	boolean stillOnTheSameStreet(List<Address> addresses) {
		Map<String, Integer> frequency = calculateStreetFrequency(addresses);
		String street = checkCommonStreet(frequency, this.locations.size() + 1);
		
		if (street != null) {
			this.currentStreetName = street;
			return true;
		}
		
		return false;
	}
	
	void addLocation(Location location, List<Address> addresses) {		
		this.locations.add(location);
		this.addressLists.add(addresses);
		
		this.updateStreetAddressList();
	}
	
	JSONArray toAddresses() throws JSONException {
		return Address.toJSONArray(this.currentStreetAddresses);
	}
}

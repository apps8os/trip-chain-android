package fi.aalto.tripchain.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.common.data.Freezable;

import fi.aalto.tripchain.here.Address;

import android.location.Location;

public class RoadSegment {
	public String street;
	List<Location> locations = new ArrayList<Location>();
	List<List<Address>> addressLists = new ArrayList<List<Address>>();
	
	RoadSegment(String street) {

	}
	
	void addLocation(Location loc) {
		this.locations.add(loc);
	}
	
	private Map<String, Integer> calculateStreetFrequency(List<Address> addresses) {
		Map<String, Integer> streetFrequency = new HashMap<String, Integer>();
		for (List<Address> addressList : addressLists) {
			for (Address a : addressList) {
				String street = a.street;
				if (streetFrequency.containsValue(street)) {
					int f = streetFrequency.get(street);
					streetFrequency.put(street, f + 1);
				} else {
					streetFrequency.put(street, 1);
				}
			}
		}
		
		for (Address a : addresses) {
			String street = a.street;
			if (streetFrequency.containsValue(street)) {
				int f = streetFrequency.get(street);
				streetFrequency.put(street, f + 1);
			} else {
				streetFrequency.put(street, 1);
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
	
	private boolean stillOnTheSameStreet(List<Address> addresses) {
		Map<String, Integer> frequency = calculateStreetFrequency(addresses);
		String street = checkCommonStreet(frequency, this.locations.size() + 1);
		if (street != null) {
			this.street = street;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns false when street has changed
	 * @param location
	 * @param addresses
	 * @return
	 */
	boolean onLocation(Location location, List<Address> addresses) {		
		if (stillOnTheSameStreet(addresses)) {
			this.locations.add(location);
			this.addressLists.add(addresses);
			
			return true;
		}
		
		
		return false;
	}
	
	boolean match(String street) {
		return this.street.equals(street);
	}
}

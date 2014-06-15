package fi.aalto.tripchain;

import android.location.Location;

interface Client {
	/**
	* Called when a new location is received
	* @param locations contains all the locations of current trip.
	*/
	void onLocation(in List<Location> locations);
}

package fi.aalto.tripchain;

import android.location.Location;

interface Client {
	void onLocation(in List<Location> locations);
}

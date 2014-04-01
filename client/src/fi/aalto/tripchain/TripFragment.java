package fi.aalto.tripchain;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TripFragment extends SupportMapFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		// Get a handle to the Map Fragment
		GoogleMap map = getMap();

		LatLng sydney = new LatLng(-33.867, 151.206);

		map.setMyLocationEnabled(true);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

		map.addMarker(new MarkerOptions().title("Sydney").snippet("The most populous city in Australia.")
				.position(sydney));
		
		return rootView;
	}
}

package fi.aalto.tripchain;

import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import fi.aalto.tripchain.route.Trip;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TripFragment extends SupportMapFragment {
	private final static String TAG = TripFragment.class.getSimpleName();

	MainActivity main;

	Polyline polyline;

	Handler handler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.handler = new Handler();

		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		GoogleMap map = getMap();
		map.setMyLocationEnabled(true);

		this.main = (MainActivity) getActivity();
		this.main.subscribe(clientConnection);

		return rootView;
	}

	Client.Stub clientConnection = new Client.Stub() {
		@Override
		public void onLocation(final List<Location> locations) throws RemoteException {
			handler.post(new Runnable() {
				public void run() {
					if (polyline != null) {
						polyline.remove();
					}

					Log.d(TAG, "onLocation");

					PolylineOptions rectOptions = new PolylineOptions();
					for (Location l : locations) {
						rectOptions.add(new LatLng(l.getLatitude(), l.getLongitude()));
					}

					polyline = getMap().addPolyline(rectOptions);
				}
			});
		}
	};
}

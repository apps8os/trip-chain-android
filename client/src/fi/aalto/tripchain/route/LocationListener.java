package fi.aalto.tripchain.route;

import java.util.List;

import fi.aalto.tripchain.here.Address;
import fi.aalto.tripchain.here.ReverseGeocoder;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import fi.aalto.tripchain.receivers.LocationReceiver;

public class LocationListener extends LocationReceiver {
	private Trip route;
	private Context context;
	private Handler handler;
	
	private static final String TAG = LocationListener.class.getSimpleName();

	public LocationListener(Context context, Trip route) {
		super(context);
		this.context = context;
		this.route = route;
		this.handler = new Handler();
	}
	
	@Override
	public void onLocationChanged(final Location location) {
		Log.d(TAG, "Provider: " + location.getProvider()  + 
				" Accuracy: " + location.getAccuracy() +
				" Latitude: " + location.getLatitude() + 
				" Longitude: " + location.getLongitude());
		
		ReverseGeocoder.Callback callback = new ReverseGeocoder.Callback() {
			@Override
			public void run(final List<Address> addresses) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (addresses.size() > 0) {
							Address address = addresses.get(0);
							Log.d(TAG, "got address! " + address.label);
							Toast.makeText(context, address.label, Toast.LENGTH_LONG).show();
						}

						route.onLocation(location, addresses);
					}
				});
			}
		};
		
		ReverseGeocoder.query(location, callback);
	}
}

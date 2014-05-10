package fi.aalto.tripchain.route;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import fi.aalto.tripchain.receivers.ActivityReceiver;
import fi.aalto.tripchain.receivers.EventDispatcher;

/**
 * Receives Play Services recognized activities and forwards them to Trip and EventDispatcher.
 *
 */
public class ActivityListener extends ActivityReceiver {
	private final static String TAG = ActivityListener.class.getSimpleName();
	
	private Trip trip;
	
	public ActivityListener(Context context, Trip trip) {
		super(context);
		
		this.trip = trip;
	}

	@Override
	public void onActivityRecognitionResult(ActivityRecognitionResult result) {
		DetectedActivity da = result.getMostProbableActivity();
		
		if (da.getConfidence() < 50) {
			// not confident enough
			return;
		}
		
		Activity activity = Activity.getActivity(da);
		
		if (activity == Activity.UNKNOWN) {
			// choosing second most probable
			for (DetectedActivity d : result.getProbableActivities()) {
				Activity tmp = Activity.getActivity(d);
				if (tmp != Activity.UNKNOWN) {
					activity = tmp;
					break;
				}
			}
		}
		
		Log.d(TAG, "Probably: " + activity);
		
		trip.onActivity(activity);
		EventDispatcher.onActivity(activity);
	}
}

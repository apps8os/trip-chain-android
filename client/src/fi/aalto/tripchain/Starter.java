package fi.aalto.tripchain;

import fi.aalto.tripchain.route.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import fi.aalto.tripchain.receivers.ActivityReceiver;

public class Starter extends ActivityReceiver {
	private final static String TAG = Starter.class.getSimpleName();
	
	private long onMoveSince = -1;
	private boolean onMove;
	
	private static final long ON_MOVE_THRESHOLD = 15000;
	
	private Context context;

	public Starter(Context context) {
		super(context);
		
		this.context = context;
	}

	@Override
	public void onActivityRecognitionResult(ActivityRecognitionResult activity) {
		String str = "";
		for (DetectedActivity da : activity.getProbableActivities()) {
			str += Activity.getActivity(da) + " " + da.getConfidence() + " %\n";
		}
		Log.d(TAG, str);
		
		DetectedActivity da = activity.getMostProbableActivity();
		if (da.getConfidence() < 50) {
			return;
		}
		
		Activity mode = Activity.getActivity(da);
		preTripHeuristics(mode);
		stopTripHeuristics(mode);
	}
	
	private void preTripHeuristics(Activity mode) {
		if (isMoving(mode)) {
			if (!onMove) {
				onMoveSince = System.currentTimeMillis();
				onMove = true;
			}
		} else {
			onMove = false;
		}
		
		if (onMove) {
			Log.d(TAG, "onMove");
			if (System.currentTimeMillis() - onMoveSince > ON_MOVE_THRESHOLD) {
				Log.d(TAG, "ON MOVE!");
				Toast.makeText(context, "YOU ARE ON MOVE!", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void stopTripHeuristics(Activity mode) {
		
	}
	
    private boolean isMoving(Activity mode) {
        switch (mode) {
            // These types mean that the user is probably not moving
            case STILL :
            case UNKNOWN :
                return false;
            default:
                return true;
        }
    } 
}

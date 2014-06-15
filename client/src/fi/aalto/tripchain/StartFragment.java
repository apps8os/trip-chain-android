package fi.aalto.tripchain;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Provides UI for starting and stopping trip recording.
 *
 */
public class StartFragment extends Fragment {
	
	private Button startButton;
	private MainActivity main;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.start_fragment, container, false);
        
        this.main = (MainActivity) getActivity();
        
        this.startButton = ((Button) rootView.findViewById(R.id.button));
		this.startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!main.recording()) {
					try {
						main.start();
					} catch (RemoteException e) {
					}
				} else {
					try {
						main.stop();
					} catch (RemoteException e) {
					}
				}
				
				startButton.setText(!main.recording() ? "Start recording"
						: "Stop recording");
			}
		});

		this.startButton.setText(!main.recording() ? "Start recording"
				: "Stop recording");
  
        return rootView;
	}
}

package fi.aalto.tripchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MainActivity extends FragmentActivity {
	private final static String TAG = MainActivity.class.getSimpleName();

	private Intent serviceIntent;
	
	ServiceConnectionApi serviceConnectionApi;

	boolean recording = false;
	
	SharedPreferences preferences;
	
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	
	private List<Client.Stub> clients = new ArrayList<Client.Stub>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		startService();
		
		preferences = getSharedPreferences(Configuration.SHARED_PREFERENCES, MODE_MULTI_PROCESS);
	}

	private void initUi() {
		setContentView(R.layout.activity_main);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	void startService() {
		serviceIntent = new Intent(this, BackgroundService.class);
		startService(serviceIntent);
		bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
	}
	
	void subscribe(Client.Stub client) {
		try {
			serviceConnectionApi.subscribe(client, client.hashCode());
			this.clients.add(client);
		} catch(RemoteException e) {
			Log.d(TAG, "Failed to subscribe", e);
		}
	}

	public void onDestroy() {
		super.onDestroy();

		if (!recording) {
			stopService(serviceIntent);
		}
		
		try {
			for (Client.Stub c : clients) {
				serviceConnectionApi.unsubscribe(c.hashCode());
			}
		} catch (Exception e) {
			Log.d(TAG, "Failed to unsubscribe from background service updates");
		}

		try {
			unbindService(serviceConnection);
		} catch (Exception e) {
			Log.d(TAG, "Failed to unbind service", e);
		}
	}

	ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "Service connection created " + name);
			serviceConnectionApi = ServiceConnectionApi.Stub
					.asInterface(service);

			try {
				recording = serviceConnectionApi.recording();
			} catch (RemoteException e) {
			}

			initUi();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "Service connection closed " + name);
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_logout:
	            logout();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void logout() {
		Editor e = preferences.edit();
		e.putString(Configuration.KEY_LOGIN_ID, null);
		e.commit();
		
		Intent i = new Intent(getApplicationContext(), LoginActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		finish();
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
		
			if (position == 1) {
				Fragment fragment = new TripFragment();
				Bundle args = new Bundle();
				//args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}
			
			Fragment fragment = new StartFragment();
			Bundle args = new Bundle();
			//args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;

		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
				case 0:
					return "Start";
				case 1:
					return "Trip";
				
			}
			return null;
		}
	}
}

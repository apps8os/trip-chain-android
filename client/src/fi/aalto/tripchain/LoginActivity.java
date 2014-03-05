package fi.aalto.tripchain;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;

import com.google.android.gms.plus.*;
import com.google.android.gms.plus.model.people.Person;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener,
		ConnectionCallbacks, OnConnectionFailedListener {

	/* Request code used to invoke sign in user interactions. */
	private static final int RC_SIGN_IN = 0;

	/* Client used to interact with Google APIs. */
	private GoogleApiClient mGoogleApiClient;

	private final Scope SCOPE = new Scope(Scopes.PROFILE);

	/*
	 * A flag indicating that a PendingIntent is in progress and prevents us
	 * from starting further intents.
	 */
	private boolean mIntentInProgress;

	private static final String TAG = LoginActivity.class.getSimpleName();

	private SharedPreferences preferences;

	/*
	 * Track whether the sign-in button has been clicked so that we know to
	 * resolve all issues preventing sign-in without waiting.
	 */
	private boolean mSignInClicked;

	/*
	 * Store the connection result from onConnectionFailed callbacks so that we
	 * can resolve them when the user clicks sign-in.
	 */
	private ConnectionResult mConnectionResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = getSharedPreferences(Configuration.SHARED_PREFERENCES,
				MODE_MULTI_PROCESS);
		String loginId = preferences
				.getString(Configuration.KEY_LOGIN_ID, null);
		if (loginId != null) {
			startMain();
			return;
		}

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API, null)
				.addScope(SCOPE).build();

		setContentView(R.layout.activity_login);
		findViewById(R.id.sign_in_button).setOnClickListener(this);
	}
	

	/* A helper method to resolve the current ConnectionResult error. */
	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
			} catch (SendIntentException e) {
				// The intent was canceled before it was sent. Return to the
				// default
				// state and attempt to connect to get an updated
				// ConnectionResult.
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			// Store the ConnectionResult so that we can use it later when the
			// user clicks
			// 'sign-in'.
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}
	}

	public void onClick(View view) {
		if (view.getId() == R.id.sign_in_button
				&& !mGoogleApiClient.isConnecting()) {
			mSignInClicked = true;
			resolveSignInError();
		}
	}

	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mSignInClicked = false;

		
		if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
			Log.d(TAG, "Got id with peopleApi");
			Person currentPerson = Plus.PeopleApi
					.getCurrentPerson(mGoogleApiClient);

			gotId(currentPerson.getId());
		} else {
			final String account = Plus.AccountApi
					.getAccountName(mGoogleApiClient);

			AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

				@Override
				protected String doInBackground(Void... params) {
					HttpURLConnection urlConnection = null;


					try {
						String sAccessToken = GoogleAuthUtil.getToken(LoginActivity.this, 
								account, "oauth2:" + Scopes.PROFILE);
						
						URL url = new URL("https://www.googleapis.com/plus/v1/people/me" 
											+ "?access_token=" + sAccessToken);
						
						urlConnection = (HttpURLConnection) url.openConnection();
						urlConnection.addRequestProperty("Authorization", sAccessToken);

						String content = new Scanner(
								urlConnection.getInputStream(), "UTF-8")
								.useDelimiter("\\A").next();

						JSONObject response = new JSONObject(content);
						return response.getString("id");

					} catch (UserRecoverableAuthException userAuthEx) { // Start
																		// the
																		// userrecoverable
																		// action
																		// using
																		// the
																		// intent
																		// //
																		// returned
																		// by //
																		// getIntent()
						startActivityForResult(userAuthEx.getIntent(), RC_SIGN_IN);
						return null;
					} catch (Exception e) { // Handle error
						e.printStackTrace(); // Uncomment if needed during //
												// debugging.

					} finally {
						if (urlConnection != null) {
							urlConnection.disconnect();
						}
					}

					return null;
				}

				@Override
				protected void onPostExecute(String id) {
					Log.d(TAG, "Got id with http get");
					gotId(id);
				}

			};

			task.execute();
		}
	}

	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	protected void onStop() {
		super.onStop();

		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	private void startMain() {
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		finish();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		mGoogleApiClient.connect();
	}

	private void gotId(String id) {
		Toast.makeText(getApplicationContext(), "USER ID: " + id,
				Toast.LENGTH_LONG).show();
		Editor e = preferences.edit();
		e.putString(Configuration.KEY_LOGIN_ID, id);
		e.commit();

		Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
		startMain();
	}
}

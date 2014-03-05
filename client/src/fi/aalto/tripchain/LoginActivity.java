package fi.aalto.tripchain;

import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import net.frakbot.accounts.chooser.AccountChooser;

import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private static final String TAG = LoginActivity.class.getSimpleName();

	private static final String SCOPE = Scopes.PROFILE;

	private SharedPreferences preferences;

	private static final int AUTHORIZATION_CODE = 1993;
	private static final int ACCOUNT_CODE = 1601;
	
	private volatile String accountName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = getSharedPreferences(Configuration.SHARED_PREFERENCES,
				MODE_MULTI_PROCESS);
		String loginId = preferences.getString(Configuration.KEY_LOGIN_ID, null);
		if (loginId != null) {
			startMain();
			return;
		}

		setContentView(R.layout.activity_login);
		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				chooseAccount(v);
			}
		});
	}

	public void chooseAccount(View _) {
		Intent intent = AccountChooser.newChooseAccountIntent(null, null, new String[] { "com.google" }, 
				false, null, null, null, null, this);
		startActivityForResult(intent, ACCOUNT_CODE);
	}

	private void getUserId() {
		final ProgressDialog dialog = ProgressDialog.show(this, "",
				"Loading. Please wait...", true);

		new AsyncTask<Void, Void, Boolean>() {
			protected Boolean doInBackground(Void... _) {
				try {
					String token = GoogleAuthUtil.getToken(LoginActivity.this,
							accountName, "oauth2:" + SCOPE);

					URL url = new URL(
							"https://www.googleapis.com/plus/v1/people/me?access_token=" + token);
					HttpURLConnection con = (HttpURLConnection) url
							.openConnection();
					int serverCode = con.getResponseCode();
					// successful query
					if (serverCode == 200) {
						InputStream is = con.getInputStream();

						String message = new Scanner(is, "UTF-8").useDelimiter(
								"\\A").next();
						JSONObject j = new JSONObject(message);
						is.close();

						Editor editor = preferences.edit();
						editor.putString(Configuration.KEY_LOGIN_ID, j.getString("id"));
						editor.commit();

						GoogleAuthUtil.invalidateToken(LoginActivity.this, token);

						return true;
						// bad token, invalidate and get a new one
					} else if (serverCode == 401) {
						GoogleAuthUtil.invalidateToken(LoginActivity.this, token);
						Log.e(TAG,
								"Server auth error: "
										+ new Scanner(con.getErrorStream(),
												"UTF-8").useDelimiter("\\A")
												.next());
						// unknown error, do something else
					} else {
						Log.e(TAG, "Server returned the following error code: "
								+ serverCode, null);
					}
				} catch (UserRecoverableAuthException e) {
					startActivityForResult(e.getIntent(), AUTHORIZATION_CODE);
				} catch (Exception e) {
					Log.d(TAG, "failed to get user id", e);
				}

				return false;
			}

			protected void onPostExecute(Boolean success) {
				Log.d(TAG, "Cancelling dialog.");
				dialog.cancel();

				if (success) {
					Toast.makeText(LoginActivity.this, 
							"USER: " + preferences.getString(Configuration.KEY_LOGIN_ID, null), 
							Toast.LENGTH_SHORT).show();
					startMain();
				}
			}
		}.execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");

		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == AUTHORIZATION_CODE) {
			Log.d(TAG, "AUTHORIZATION_CODE");
		} else if (requestCode == ACCOUNT_CODE) {
			accountName = data
					.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			
			Log.d(TAG, "ACCOUNT_CODE " + accountName);
		}

		getUserId();
	}

	private void startMain() {
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		finish();
	}
}

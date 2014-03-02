package fi.aalto.tripchain;

import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class LoginActivity extends Activity {

	private static final String TAG = LoginActivity.class.getSimpleName();

	private static final String KEY_USER = "auth_user";
	private static final String KEY_TOKEN = "auth_token";
	private static final String KEY_LOGIN_ID = "login_id";

	private static final String SCOPE = "profile";

	private SharedPreferences preferences;
	private AccountManager accountManager;

	private static final int AUTHORIZATION_CODE = 1993;
	private static final int ACCOUNT_CODE = 1601;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountManager = AccountManager.get(this);

		preferences = getSharedPreferences(Configuration.SHARED_PREFERENCES, MODE_MULTI_PROCESS);
		String loginId = preferences.getString(KEY_LOGIN_ID, null);
		if (loginId != null) {
			startMain();
			return;
		}

		setContentView(R.layout.activity_login);
	}

	public void chooseAccount(View _) {
		Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[] { "com.google" },
				false, null, null, null, null);
		startActivityForResult(intent, ACCOUNT_CODE);
	}

	private void getUserId() {
		Log.e(TAG, getToken());
		final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);

		new AsyncTask<Void, Void, Boolean>() {
			protected Boolean doInBackground(Void... _) {
				try {
					String token = getToken();

					URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					int serverCode = con.getResponseCode();
					// successful query
					if (serverCode == 200) {
						InputStream is = con.getInputStream();

						String message = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
						JSONObject j = new JSONObject(message);
						is.close();

						Editor editor = preferences.edit();
						editor.putString(KEY_LOGIN_ID, j.getString("id"));
						editor.commit();

						GoogleAuthUtil.invalidateToken(LoginActivity.this, token);

						return true;
						// bad token, invalidate and get a new one
					} else if (serverCode == 401) {
						GoogleAuthUtil.invalidateToken(LoginActivity.this, token);
						Log.e(TAG, "Server auth error: "
								+ new Scanner(con.getErrorStream(), "UTF-8").useDelimiter("\\A").next());
						// unknown error, do something else
					} else {
						Log.e(TAG, "Server returned the following error code: " + serverCode, null);
					}

				} catch (Exception e) {
					Log.d(TAG, "failed to get user id", e);
				}

				return false;
			}

			protected void onPostExecute(Boolean success) {
				Log.d(TAG, "Cancelling dialog.");
				dialog.cancel();

				if (success) {
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
			requestToken();
		} else if (requestCode == ACCOUNT_CODE) {

			String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			setUser(accountName);

			Log.d(TAG, "ACCOUNT_CODE " + accountName);

			// invalidate old tokens which might be cached. we want a fresh
			// one, which is guaranteed to work
			invalidateToken();

			requestToken();
		}
	}

	private void requestToken() {
		Account userAccount = null;
		String user = getUser();
		for (Account account : accountManager.getAccountsByType("com.google")) {
			if (account.name.equals(user)) {
				userAccount = account;

				break;
			}
		}

		Log.d(TAG, "Request token. " + (userAccount != null));

		accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, this, new OnTokenAcquired(), null);
	}

	private void invalidateToken() {
		accountManager.invalidateAuthToken("com.google", getToken());
		setToken(null);
	}

	public void setUser(String user) {
		Editor editor = preferences.edit();
		editor.putString(KEY_USER, user);
		editor.commit();
	}

	public void setToken(String password) {
		Editor editor = preferences.edit();
		editor.putString(KEY_TOKEN, password);
		editor.commit();
	}

	public String getUser() {
		return preferences.getString(KEY_USER, null);
	}

	public String getToken() {
		return preferences.getString(KEY_TOKEN, null);
	}

	private void startMain() {
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		finish();
	}

	private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

		@Override
		public void run(AccountManagerFuture<Bundle> result) {
			Log.d(TAG, "onTokenAcquired");

			try {
				Bundle bundle = result.getResult();

				Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
				if (launch != null) {
					startActivityForResult(launch, AUTHORIZATION_CODE);
				} else {
					String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

					setToken(token);

					getUserId();
				}
			} catch (Exception e) {

			}
		}
	}
}

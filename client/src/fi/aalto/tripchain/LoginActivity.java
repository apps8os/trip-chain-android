package fi.aalto.tripchain;

import java.util.UUID;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

public class LoginActivity extends Activity {
	
	private static final String TAG = LoginActivity.class.getSimpleName();

	private static final String KEY_USER = "auth_user";
	private static final String KEY_TOKEN = "auth_token";
	
	private static final String SCOPE = "profile";

	private SharedPreferences preferences;
	private AccountManager accountManager;

	private static final int AUTHORIZATION_CODE = 1993;
	private static final int ACCOUNT_CODE = 1601;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		accountManager = AccountManager.get(this);

		preferences = getSharedPreferences(Configuration.SHARED_PREFERENCES,
				MODE_MULTI_PROCESS);
		String loginId = preferences.getString("login_id", null);
		if (loginId != null) {
			startMain();
		} else {
			chooseAccount();
		}
	}

	private void chooseAccount() {
		// use https://github.com/frakbot/Android-AccountChooser for
		// compatibility with older devices
		Intent intent = AccountManager.newChooseAccountIntent(null, null,
				new String[] { "com.google" }, true, null, null, null, null);
		startActivityForResult(intent, ACCOUNT_CODE);
	}
	
	private void doCoolAuthenticatedStuff() {
		// TODO: insert cool stuff with authPreferences.getToken()
		 
		Log.e(TAG, getToken());
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

					doCoolAuthenticatedStuff();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}

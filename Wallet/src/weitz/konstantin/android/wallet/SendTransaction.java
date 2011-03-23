package weitz.konstantin.android.wallet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

// Code documentation
// http://blog.notdot.net/2010/05/Authenticating-against-App-Engine-from-an-Android-app

public class SendTransaction extends Activity
{	
	DefaultHttpClient http_client = new DefaultHttpClient();

	private static final String DOMAIN = "walletbank.appspot.com";
	private Bundle transaction;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.load);
		
		Log.e("created", "created");
		
		Bundle extra = getIntent().getExtras();
		transaction = (Bundle)extra.get("transaction");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		Account account = (Account)intent.getExtras().get("account");
		accountManager.getAuthToken(account, "ah", false, new GetAuthTokenCallback(), null);
	}

	private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
		public void run(AccountManagerFuture<Bundle> result) {
			Log.e("getting auth token", "getting auth token");
			
			Bundle bundle;
			try {
				bundle = result.getResult();
				Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
				if(intent != null) {
					// User input required
					startActivity(intent);
				} else {
					onGetAuthToken(bundle);
				}
			} catch (OperationCanceledException e) {
				// TODO Auto-generated catch block
				Log.e("error", "Mean Exception");
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				// TODO Auto-generated catch block
				Log.e("error", "Mean Exception");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("error", "Mean Exception");
				e.printStackTrace();
			}
		}
	};

	protected void onGetAuthToken(Bundle bundle) {
		String auth_token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
		new GetCookieTask().execute(auth_token);
		Log.e("not too bad", "Returning here");
	}

	private class GetCookieTask extends AsyncTask<String, Void, Boolean> {
		protected Boolean doInBackground(String... tokens) {
			try {
				Log.e("preparing to send", "preparing to send");
				
				// Don't follow redirects
				http_client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

				Log.e("a1", tokens[0]);
				
				
				HttpGet http_get = new HttpGet("https://"+DOMAIN+"/_ah/login?continue=http://localhost/&auth=" + tokens[0]);

				Log.e("a2", "a2");
				
				HttpResponse response;
				
				Log.e("a3", "a3");
				
				response = http_client.execute(http_get);

				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
				try{
					String line;
					while((line = reader.readLine()) != null) Log.e("scheiße",line);
				}catch(IOException e) {
					Log.e("scheiße", "error");
				}
				Log.e("a4", "a4");
				
				if(response.getStatusLine().getStatusCode() != 302){
					// Response should be a redirect

					Log.e("No fucking redirect","Fuck you");
					
					return false;
				}
					
				for(Cookie cookie : http_client.getCookieStore().getCookies()) {
					
					Log.e("Keks", cookie.getValue());
					
					Log.e("Is secure? ", cookie.isSecure()?"true":"false");
					
					if(cookie.getName().equals("SACSID")){
						Log.e("awesome", "Everythings awesome");
						return true;
					}
				}
			} catch (ClientProtocolException e) {
				Log.e("error", "Mean Exception");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("error", "Mean Exception");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				http_client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
			}
			

			Log.e("Bad", "Is pretty false");
			return false;
		}

		protected void onPostExecute(Boolean result) {
			if(!result){
				Log.e("not true", "This is bad");
			}
			
			new AuthenticatedRequestTask().execute("https://"+DOMAIN+"/json");
		}
	}

	private class AuthenticatedRequestTask extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... urls) {
			try {
				HttpPost post = new HttpPost(urls[0]);

				Bundle twhen = transaction.getBundle("when");
				
				JSONObject when = new JSONObject();
				when.put("second", (Integer)twhen.get("second"));
				when.put("hour", (Integer)twhen.get("hour"));
				when.put("monthDay", (Integer)twhen.get("monthDay"));
				when.put("month", (Integer)twhen.get("month"));
				when.put("year", (Integer)twhen.get("year"));
				
				JSONObject req = new JSONObject();
				req.put("amount", transaction.get("amount"));
				req.put("memo", transaction.get("memo"));
				req.put("partner", transaction.get("partner"));
				req.put("wallet", transaction.get("wallet"));
				req.put("when", when);

				post.setEntity(new StringEntity(req.toString()));
				post.setHeader("content-type", "application/json");

				Log.e("json",req.toString());
				
				Log.e("sent", "Man this is cool");
				
				return http_client.execute(post);
			} catch(JSONException e) {
				// TODO Auto-generated catch block
				Log.e("error", "Mean Exception");
				e.printStackTrace();				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Log.e("error", "Mean Exception");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("error", "Mean Exception");
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(HttpResponse result) {
			try {
				//BufferedReader reader = new BufferedReader(new InputStreamReader(result.getEntity().getContent()));
				//String first_line = reader.readLine();

				Log.e("success", "everything is good");
				
				finish();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}/* catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
}
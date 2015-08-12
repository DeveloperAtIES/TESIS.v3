package com.example.tesis.v3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class GCMSetSend extends AsyncTask<Void, Void, String> {
	private static final String TAG = "GCMRelated";
	Context ctx;
	GoogleCloudMessaging gcm;
	String SENDER_ID = "447429034516"; // Google Console TESIS project ID
	String regid = null;
	private int send;

	public GCMSetSend(Context ctx, GoogleCloudMessaging gcm, int send) {
		this.ctx = ctx;
		this.gcm = gcm;
		this.send = send;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(Void... arg0) {
		String msg = "";
		try {
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(ctx);
			}
			regid = gcm.register(SENDER_ID);
			msg = "Device registered, registration ID=" + regid;

			// You should send the registration ID to your server over HTTP,
			// so it can use GCM/HTTP or CCS to send messages to your app.
			// The request to your server should be authenticated if your app
			// is using accounts.
			if(sendRegistrationIdToBackend()){
				Log.d(TAG,"set Send to "+ send+" success.");
			} else{
				Log.d(TAG,"Failed to set Send.");
			}

			// For this demo: we don't need to send it because the device
			// will send upstream messages to a server that echo back the
			// message using the 'from' address in the message.

			// Persist the regID - no need to register again.
//			storeRegistrationId(ctx, regid);
		} catch (IOException ex) {
			msg = "Error :" + ex.getMessage();
			// If there is an error, don't just keep trying to register.
			// Require the user to click a button again, or perform
			// exponential back-off.
		}
		return msg;
	}

	private boolean sendRegistrationIdToBackend() {
		URI url = null;
		try {
			url = new URI("http://tesis.earth.sinica.edu.tw/mobileapp/GCMSetSend.php?regId=" + regid+"&send=" + send);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet();
		request.setURI(url);
		try {
			HttpResponse httpResponse = httpclient.execute(request);
			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream inputStream = httpEntity.getContent();
			String result = "";
			new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();

			InputStreamReader r = new InputStreamReader(inputStream, "UTF-8");
			int intch;
			while ((intch = r.read()) != -1) {
				char ch = (char) intch;
				// Log.i("app", Character.toString(ch));
				String s = new String(Character.toString(ch).getBytes(),
						"UTF-8");
				sb.append(s);
			}
			inputStream.close();
			result = sb.toString();
			Log.d(TAG,"result: "+result);
			return true;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
//		Toast.makeText(ctx,
//				"Registration Completed. Now you can see the notifications",
//				Toast.LENGTH_SHORT).show();
		Log.v(TAG, result);
	}
}

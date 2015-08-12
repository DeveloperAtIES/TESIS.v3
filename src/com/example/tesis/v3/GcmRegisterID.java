package com.example.tesis.v3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

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
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class GcmRegisterID extends AsyncTask<Void, Void, String> {
	private static final String TAG = "GCMRelated";
	Context ctx;
	GoogleCloudMessaging gcm;
	String SENDER_ID = "447429034516"; // Google Console TESIS project ID
	String regid = null;
	private int appVersion;

	public GcmRegisterID(Context ctx, GoogleCloudMessaging gcm, int appVersion) {
		this.ctx = ctx;
		this.gcm = gcm;
		this.appVersion = appVersion;
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
			if (sendRegistrationIdToBackend()) {
				storeRegistrationId(ctx, regid);
			}

			// For this demo: we don't need to send it because the device
			// will send upstream messages to a server that echo back the
			// message using the 'from' address in the message.

			// Persist the regID - no need to register again.
			// storeRegistrationId(ctx, regid);
		} catch (IOException ex) {
			msg = "Error :" + ex.getMessage();
			// If there is an error, don't just keep trying to register.
			// Require the user to click a button again, or perform
			// exponential back-off.
		}
		return msg;
	}

	private void storeRegistrationId(Context ctx, String regid) {
		final SharedPreferences prefs = ctx.getSharedPreferences(
				HomeActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(HomeActivity.PROPERTY_REG_ID, regid);
		editor.putInt(HomeActivity.PROPERTY_APP_VERSION, appVersion);
		editor.commit();

	}

	private boolean sendRegistrationIdToBackend() {
		URI url = null;
		try {
			url = new URI(
					"http://tesis.earth.sinica.edu.tw/mobileapp/GCMSaveID2Database.php?regId="
							+ regid);
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
		
		// Toast.makeText(ctx,
		// "Registration Completed. Now you can see the notifications",
		// Toast.LENGTH_SHORT).show();
		settingNotificationAtFirst();
		Log.v(TAG, result);
		super.onPostExecute(result);
	}
	
	private void settingNotificationAtFirst(){
		HashMap<String, Integer> settingHashMap = null;
		File EQFile = ctx.getFileStreamPath(ConstantVariables.settingPreferenceFilename);
		if (EQFile.exists()) {
			try {
				FileInputStream fis;
				fis = ctx.openFileInput(ConstantVariables.settingPreferenceFilename);
				ObjectInputStream ois = new ObjectInputStream(fis);
				settingHashMap = (HashMap<String, Integer>) ois.readObject();
				ois.close();
				fis.close();
				int ntfState = settingHashMap.get("ntfState");
				Log.d(TAG,"Set Send at first :"+ntfState);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					new GCMSetSend(ctx,
							gcm,
							ntfState)
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				else
					new GCMSetSend(ctx,
							gcm,
							ntfState).execute();
			} catch (Exception e){
				Log.e(TAG,"cannot set notification at first.");
			}
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				new GCMSetSend(ctx,
						gcm,
						1)
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			else
				new GCMSetSend(ctx,
						gcm,
						1).execute();
		}
	}

}

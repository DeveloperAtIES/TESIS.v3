package com.example.tesis.v3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.tesis.v3.HomeActivity.updateGeneralEQ;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private static final String TAG = "GcmIntentService";
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	protected EarthquakeData mEarthquakeData;
	HashMap<String, String> mNewEarthquakeMap = new HashMap<String, String>();

	GcmIntentService gcmIntentService;

	public GcmIntentService() {
		super("GcmIntentService");
		gcmIntentService = this;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);
        int lastSendNFID;

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				Log.i(TAG, "Send error: " + extras.toString());
				// sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				// sendNotification("Deleted messages on server: "
				// + extras.toString());
				Log.i(TAG, "Deleted messages on server: " + extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				// This loop represents the service doing some work.
				// for (int i = 0; i < 5; i++) {
				// Log.i(TAG,
				// "Working... " + (i + 1) + "/5 @ "
				// + SystemClock.elapsedRealtime());
				// try {
				// Thread.sleep(5000);
				// } catch (InterruptedException e) {
				// }
				// }

				Log.d(TAG, "download new earthquake");
				if (HomeActivity.isConnected(this)) {
					mNewEarthquakeMap = EarthquakeData.getDataFromURL(extras
							.getString("message"));
					Log.i(TAG, "Received: " + extras.toString());
					Log.i(TAG,
							"Download Earthquake: "
									+ mNewEarthquakeMap.toString());

					// myDate From = new myDate();
					// From.resetFrom();
					// myDate To = new myDate();
					// mUpdateEarthquakeList = mEarthquakeData.getData(
					// From.DatetoString(), To.DatetoString(), false);
				}

				// try {
				// Thread.sleep(5000);
				// } catch (InterruptedException e) {
				// }
				// Log.i(TAG, "Completed work @ " +
				// SystemClock.elapsedRealtime());
				// Post notification of received message.

                // 0415 check newest send Notification id
                File newestIDFile = this
                        .getFileStreamPath(ConstantVariables.newestSendNFFilename);
                if (newestIDFile.exists()) {
                    try {
                        FileInputStream fis;
                        fis = this.openFileInput(ConstantVariables.newestSendNFFilename);
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        String newestID = (String) ois.readObject();
                        ois.close();
                        fis.close();
                        Log.d("myTag", "load newest ID from file:" + newestID);
                        lastSendNFID = Integer.parseInt(newestID);
                        if (Integer.parseInt(mNewEarthquakeMap.get("No")) > lastSendNFID) {
                            sendNotification(mNewEarthquakeMap);
                            //TODO save new ID
                            newestID = mNewEarthquakeMap.get("No");
                            Integer tmpInteger = Integer.parseInt(newestID);
                            newestID = tmpInteger.toString();
                            try {
                                if (newestIDFile.exists() || newestIDFile.createNewFile()) {
                                    FileOutputStream fos = this.openFileOutput(ConstantVariables.newestSendNFFilename, this.MODE_PRIVATE);
                                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                                    oos.writeObject(newestID);
                                    oos.flush();
                                    oos.close();
                                    fos.close();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                this.deleteFile(ConstantVariables.newestSendNFFilename);
                                Log.e("myTag", "Fail to Write newest NF ID to file.");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    sendNotification(mNewEarthquakeMap);
                    //TODO save new ID
                    String newestID = mNewEarthquakeMap.get("No");
                    Integer tmpInteger = Integer.parseInt(newestID);
                    newestID = tmpInteger.toString();
                    try {
                        if (newestIDFile.createNewFile()) {
                            FileOutputStream fos = this.openFileOutput(ConstantVariables.newestSendNFFilename, this.MODE_PRIVATE);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(newestID);
                            oos.flush();
                            oos.close();
                            fos.close();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        this.deleteFile(ConstantVariables.newestSendNFFilename);
                        Log.e("myTag", "Fail to Write newest NF ID to file.");
                    }
                }

//				sendNotification(mNewEarthquakeMap);
				// Log.i(TAG, "Received: " + extras.toString());
				if (HomeActivity.isConnected(getApplicationContext())) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
						new updateGeneralEQ()
								.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					else
						new updateGeneralEQ().execute();
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(HashMap<String, String> hMap) {

		NotificationManager manager = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, summaryActivity.class);
		intent.putExtra("earthquakeInfo", hMap);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(summaryActivity.class);
		stackBuilder.addNextIntent(intent);

		PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder nfBuilder = new NotificationCompat.Builder(
				this)
				.setContentTitle("規模" + hMap.get("ml") + "新地震")
				.setContentText(hMap.get("DateAndTime"))
				.setSmallIcon(R.drawable.small_alert)
				.setLargeIcon(
						BitmapFactory.decodeResource(getResources(),
								R.drawable.ic_launcher)).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_ALL)
				.setContentIntent(pendingIntent);

		manager.notify(0, nfBuilder.build());

		// mNotificationManager = (NotificationManager) this
		// .getSystemService(Context.NOTIFICATION_SERVICE);
		//
		// PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
		// new Intent(this, HomeActivity.class), 0);
		//
		// NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
		// this)
		// // .setSmallIcon(R.drawable.ic_stat_gcm)
		// .setContentTitle("GCMDemo")
		// .setSmallIcon(R.drawable.ic_launcher)
		// .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
		// .setContentText(msg);
		//
		// mBuilder.setContentIntent(contentIntent);
		// mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	@SuppressWarnings("rawtypes")
	class updateGeneralEQ extends
			AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				Void... params) {
			ArrayList<HashMap<String, String>> mUpdateEarthquakeList = EarthquakeData
					.getData(gcmIntentService, myDate.getFrom().DatetoString(),
							myDate.getTo().DatetoString(), true);
			return mUpdateEarthquakeList;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			if (result != null && result.size() != 0) {
				ConstantVariables.writeEQToInternalFile(gcmIntentService,
						result, ConstantVariables.generalFilename);
			}
			super.onPostExecute(result);
		}

	}
}

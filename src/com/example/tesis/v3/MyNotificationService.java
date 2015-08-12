//package com.example.tesis.v3;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.concurrent.ThreadPoolExecutor;
//
//import com.example.tesis.v3.HomeActivity.updateGeneralEQ;
//import com.google.android.gms.internal.fi;
//import com.google.android.gms.maps.model.BitmapDescriptor;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Intent;
//import android.graphics.BitmapFactory;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Looper;
//import android.os.Message;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.TaskStackBuilder;
//import android.util.Log;
//import android.widget.Toast;
//
//public class MyNotificationService extends Service {
//
//	String tag = "NotificationTag";
//	MyNotificationService myNotificationService;
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		return null;
//	}
//
//	private Handler handler;
//	protected EarthquakeData mEarthquakeData;
//	ArrayList<HashMap<String, String>> mNewEarthquakeList = null;
//
//	@Override
//	public void onCreate() {
//		Log.d(tag, "onCreate: start service");
//
//		super.onCreate();
//	}
//
//	final int CHECK_UPDATE = ConstantVariables.CHECK_UPDATE;
//
//	private void setHandler() {
//		handler = new Handler(Looper.getMainLooper()) {
//
//			public void handleMessage(Message msg) {
//				switch (msg.what) {
//				// 當收到的Message的代號為我們剛剛訂的代號就做下面的動作。
//				case CHECK_UPDATE:
//					Log.d(tag, "My Notification Service: send notification");
//					// Toast.makeText(myNotificationService,
//					// "send notification if new eq occurs",
//					// Toast.LENGTH_LONG).show();
//					NotificationManager manager = (NotificationManager) myNotificationService
//							.getSystemService(NOTIFICATION_SERVICE);
//					HashMap<String, String> hMap = mNewEarthquakeList.get(0);
//
//					Intent intent = new Intent(myNotificationService,
//							summaryActivity.class);
//					intent.putExtra("earthquakeInfo", hMap);
//
//					TaskStackBuilder stackBuilder = TaskStackBuilder
//							.create(myNotificationService);
//					stackBuilder.addParentStack(summaryActivity.class);
//					stackBuilder.addNextIntent(intent);
//
//					PendingIntent pendingIntent = stackBuilder
//							.getPendingIntent(0,
//									PendingIntent.FLAG_UPDATE_CURRENT);
//
//					NotificationCompat.Builder nfBuilder = new NotificationCompat.Builder(
//							myNotificationService)
//							.setContentTitle("規模" + hMap.get("ml") + "新地震")
//							.setContentText(hMap.get("DateAndTime"))
//							.setSmallIcon(R.drawable.small_alert)
//							.setLargeIcon(
//									BitmapFactory.decodeResource(
//											getResources(),
//											R.drawable.ic_launcher))
//							.setContentInfo(mNewEarthquakeList.size() + "")
//							.setAutoCancel(true)
//							.setDefaults(Notification.DEFAULT_ALL)
//							.setContentIntent(pendingIntent);
//					
//					manager.notify(0, nfBuilder.build());
//
//					break;
//
//				}
//				super.handleMessage(msg);
//			}
//
//		};
//	}
//
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.d(tag,
//				"onStartCommand: start service again while service has already been started.");
//		myNotificationService = this;
//		setHandler();
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				while (true) {
//					try {
//						Thread.sleep(10000);
//						Log.d(tag, "My Notification Service: check update");
//						if (HomeActivity.isConnected(myNotificationService)) {
//							myDate From = new myDate();
//							From.resetFrom();
//							myDate To = new myDate();
//							mEarthquakeData = new EarthquakeData(
//									myNotificationService);
//							mNewEarthquakeList = mEarthquakeData.getData(
//									From.DatetoString(), To.DatetoString(),
//									true);
//
//							if (handler != null && mNewEarthquakeList != null
//									&& mNewEarthquakeList.size() > 0) {
//								Message m = new Message();
//								m.what = CHECK_UPDATE;
//								handler.sendMessage(m);
//							}
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();
//		return super.onStartCommand(intent, flags, startId);
//	}
//
//	@Override
//	public void onDestroy() {
//		Log.d(tag, "onDestroy: service stopped.");
//		handler = null;
//		super.onDestroy();
//	}
//
//}

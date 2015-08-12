package com.example.tesis.v3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.photoview.PhotoViewAttacher;

import com.example.tesis.v3.HomeActivity.updateGeneralEQ;
import com.example.tesis.v3.MapOverlay.downloadBall;
import com.example.tesis.v3.MapOverlay.downloadMarkerInfo;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;

import android.R.integer;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class summaryActivity extends ActionBarActivity {

	setupGoogleMap mSetupGoogleMap;
	protected HashMap<String, String> eqHashMap;
	protected PopupWindow mPopupWindow;
	boolean isPopup = false;
	public summaryActivity mSummaryActivity;
	MapOverlay mapOverlay;
	final String tag = "myTag";
	final int HALF = 0, FULL_MAP = 1, FULL_CONTENT = 2;
	int state;
	WebView webView;
	final int CURRENT_LOCATION_UPDATE = ConstantVariables.CURRENT_LOCATION_UPDATE;
	GPSTracker mGpsTracker;

	// myMapView mapView;
	final String timeTag = "timeTag";
	long startTime, endTime;
	ProgressDialog progressDialog;
	Handler handler;

	private void calculateRuntime(String info) {
		endTime = System.currentTimeMillis();
		Log.d(timeTag, "In summary Activity: " + info + ": "
				+ (endTime - startTime));
		startTime = System.currentTimeMillis();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_summary);
		setContentView(R.layout.activity_summary_2);
		mSummaryActivity = this;
		System.gc();

		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		int memoryClass = am.getMemoryClass();
		Log.d("MemoryTag", "In SummaryActivity onCreate: memory less:"
				+ Integer.toString(memoryClass));

		startTime = System.currentTimeMillis();
		// mapView = (myMapView) findViewById(R.id.map);
		// mapView.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Intent intent = getIntent();
		@SuppressWarnings("unchecked")
		HashMap<String, String> earthquakeInfo = (HashMap<String, String>) intent
				.getSerializableExtra("earthquakeInfo");
		eqHashMap = earthquakeInfo;

		// TODO get earthquake list to implement 最近地震

		// TextView titleTextView = (TextView) findViewById(R.id.textViewTitle);
		// TextView locationTextView = (TextView)
		// findViewById(R.id.textViewLocation);
		// TextView dateTextView = (TextView) findViewById(R.id.textViewDate);

		// XXX for activity_summary.xml
		// titleTextView.setText("M" + earthquakeInfo.get("ml") + " - "
		// + earthquakeInfo.get("depth") + "km ");
		// TODO the direction from the earthquake to user
		mSummaryActivity.getSupportActionBar().setTitle(
				"ML" + earthquakeInfo.get("ml") + "，深度"
						+ earthquakeInfo.get("depth") + "km");
		mSummaryActivity.getSupportActionBar().setSubtitle(
				earthquakeInfo.get("DateAndTime") + " (Taipei time)");
		// locationTextView.setText(earthquakeInfo.get("Location"));
		// dateTextView.setText(earthquakeInfo.get("DateAndTime"));

		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View popupWindow = layoutInflater
				.inflate(R.layout.popup_map_menu, null);

		mPopupWindow = new PopupWindow(popupWindow, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setContentView(popupWindow);
		calculateRuntime("read HashMap, set action bar, set popupwindow.");

		mSetupGoogleMap = new setupGoogleMap(this, mPopupWindow);

		calculateRuntime("setup google map");

		ImageButton mapMenuButton = (ImageButton) findViewById(R.id.imageButtonMapMenu);
		mapMenuButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isPopup) {
					mPopupWindow.dismiss();
					isPopup = false;
				} else {
					mPopupWindow.update();
					mPopupWindow
							.showAsDropDown(findViewById(R.id.imageButtonMapMenu));
					isPopup = true;
				}
			}
		});

		state = HALF;
		setLayout(state);
		setImageButton();

		webView = (WebView) findViewById(R.id.webView1);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			TaskdownloadComments = (downloadComments) new downloadComments()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			TaskdownloadComments = (downloadComments) new downloadComments().execute(0);

		mGpsTracker = new GPSTracker(this);
		if (mGpsTracker.canGetLocation) {
			currentLocation = mGpsTracker.getLocation();
			// Log.d(tag,"currentLocation:"+currentLocation.toString());
		}

		handler = new Handler() {

			public void handleMessage(Message msg) {
				switch (msg.what) {
				// 當收到的Message的代號為我們剛剛訂的代號就做下面的動作。
				case CURRENT_LOCATION_UPDATE:
					// call back here, 更改current Location
					if (mGpsTracker != null) {
						currentLocation = mGpsTracker.getLocation();
						locationUpdate();
					}
					break;

				}
				super.handleMessage(msg);
			}

		};
		mGpsTracker.setHandler(handler);
		Log.d("MemoryTag",
				"In SummaryActivity onCreate end: memory less:"
						+ Integer
								.toString(((ActivityManager) getSystemService(ACTIVITY_SERVICE))
										.getMemoryClass()));
	}

	protected void locationUpdate() {
		currentLocation = mGpsTracker.getLocation();
		Location location = new Location("start");
		double startLat = Double.parseDouble(eqHashMap.get("lat"));
		double startLng = Double.parseDouble(eqHashMap.get("lng"));
		location.setLatitude(startLat);
		location.setLongitude(startLng);
		float distance = location.distanceTo(currentLocation);

		// set direction here
		double dx = location.getLongitude() - currentLocation.getLongitude();
		double dy = location.getLatitude() - currentLocation.getLatitude();
		double theta = Math.atan2(dy, dx);
		// Log.d(tag, "theta:" + theta);
		if (theta < 0) {
			theta += 2 * Math.PI;
		}
		if (theta > 2 * Math.PI * 31 / 32 || theta <= 2 * Math.PI * 1 / 32) {
			eqHashMap.put("direction", "東方");
		} else if (theta > 2 * Math.PI * 1 / 32
				&& theta <= 2 * Math.PI * 3 / 32) {
			eqHashMap.put("direction", "東北東方");
		} else if (theta > 2 * Math.PI * 3 / 32
				&& theta <= 2 * Math.PI * 5 / 32) {
			eqHashMap.put("direction", "東北方");
		} else if (theta > 2 * Math.PI * 5 / 32
				&& theta <= 2 * Math.PI * 7 / 32) {
			eqHashMap.put("direction", "北北東方");
		} else if (theta > 2 * Math.PI * 7 / 32
				&& theta <= 2 * Math.PI * 9 / 32) {
			eqHashMap.put("direction", "北方");
		} else if (theta > 2 * Math.PI * 9 / 32
				&& theta <= 2 * Math.PI * 11 / 32) {
			eqHashMap.put("direction", "北北西方");
		} else if (theta > 2 * Math.PI * 11 / 32
				&& theta <= 2 * Math.PI * 13 / 32) {
			eqHashMap.put("direction", "西北方");
		} else if (theta > 2 * Math.PI * 13 / 32
				&& theta <= 2 * Math.PI * 15 / 32) {
			eqHashMap.put("direction", "西北西方");
		} else if (theta > 2 * Math.PI * 15 / 32
				&& theta <= 2 * Math.PI * 17 / 32) {
			eqHashMap.put("direction", "西方");
		} else if (theta > 2 * Math.PI * 17 / 32
				&& theta <= 2 * Math.PI * 19 / 32) {
			eqHashMap.put("direction", "西南西方");
		} else if (theta > 2 * Math.PI * 19 / 32
				&& theta <= 2 * Math.PI * 21 / 32) {
			eqHashMap.put("direction", "西南方");
		} else if (theta > 2 * Math.PI * 21 / 32
				&& theta <= 2 * Math.PI * 23 / 32) {
			eqHashMap.put("direction", "南南西方");
		} else if (theta > 2 * Math.PI * 23 / 32
				&& theta <= 2 * Math.PI * 25 / 32) {
			eqHashMap.put("direction", "南方");
		} else if (theta > 2 * Math.PI * 25 / 32
				&& theta <= 2 * Math.PI * 27 / 32) {
			eqHashMap.put("direction", "南南東方");
		} else if (theta > 2 * Math.PI * 27 / 32
				&& theta <= 2 * Math.PI * 29 / 32) {
			eqHashMap.put("direction", "東南方");
		} else if (theta > 2 * Math.PI * 29 / 32
				&& theta <= 2 * Math.PI * 31 / 32) {
			eqHashMap.put("direction", "東南東方");
		}

		eqHashMap.put("distance", "震央距離現在位置 " + eqHashMap.get("direction")
				+ (int) (distance / 1000) + "公里");
	}

	LinearLayout linearLayoutSummary;
	int contentHeight = 0;

	protected void setLayout(int state) {
		linearLayoutSummary = (LinearLayout) findViewById(R.id.linearLayoutSummary);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;

		if (state == FULL_MAP) {
			Log.d(tag, "full map");
			Log.d(tag,
					"content width, height:" + linearLayoutSummary.getWidth()
							+ "," + linearLayoutSummary.getHeight());
			contentHeight = linearLayoutSummary.getHeight();

			SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			View view = fragment.getView();
			RelativeLayout.LayoutParams mapLayoutParams = new RelativeLayout.LayoutParams(
					width, contentHeight - 100);
			view.setLayoutParams(mapLayoutParams);
			view.requestLayout();
			linearLayoutSummary.scrollTo(0, 0);

		} else if (state == FULL_CONTENT) {
			Log.d(tag, "full content");
			Log.d(tag,
					"summary width, height:" + linearLayoutSummary.getWidth()
							+ "," + linearLayoutSummary.getHeight());
			contentHeight = linearLayoutSummary.getHeight();

			SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			View view = fragment.getView();
			RelativeLayout.LayoutParams mapLayoutParams = new RelativeLayout.LayoutParams(
					width, height * 3 / 5);
			view.setLayoutParams(mapLayoutParams);
			view.requestLayout();
			linearLayoutSummary.scrollTo(0, height * 3 / 5);

			LinearLayout linearLayout = (LinearLayout) findViewById(R.id.contentLayout);
			Log.d(tag, "content width, height:" + linearLayout.getWidth() + ","
					+ linearLayout.getHeight());

			ScrollView scrollView = (ScrollView) findViewById(R.id.scrollViewWeb);
			Log.d(tag, "scrollView width, height:" + scrollView.getWidth()
					+ "," + scrollView.getHeight());

			WebView webView = (WebView) findViewById(R.id.webView1);
			LinearLayout.LayoutParams contentLayoutParams = new LinearLayout.LayoutParams(
					width, contentHeight);
			LinearLayout.LayoutParams webLayoutParams = new LinearLayout.LayoutParams(
					width, contentHeight - 100);
			linearLayout.setLayoutParams(contentLayoutParams);
			linearLayout.requestLayout();
			// scrollView.setLayoutParams(webLayoutParams);
			// webView.setLayoutParams(webLayoutParams);
			// webView.requestLayout();

		} else {
			Log.d(tag, "half");

			SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			View view = fragment.getView();
			RelativeLayout.LayoutParams mapLayoutParams = new RelativeLayout.LayoutParams(
					width, height * 3 / 5);
			view.setLayoutParams(mapLayoutParams);
			view.requestLayout();
			linearLayoutSummary.scrollTo(0, 0);
		}

	}

	ImageButton imageButtonUp, imageButtonDown;
	Bitmap upBitmap, upBitmap_press, downBitmap, downBitmap_press;

	protected void setImageButton() {
		try {
			InputStream ims = getAssets().open("up.png");
			Drawable d = Drawable.createFromStream(ims, null);
			Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
			upBitmap = Bitmap.createScaledBitmap(bitmap,
			// width * 1 / 8,
			// width * bitmap.getHeight() * 1 / bitmap.getWidth() / 8,
					100, 100, true);

		} catch (IOException ex) {
			Log.d("Here!!", "load up bitmap error.");
			ex.printStackTrace();
		}

		try {
			InputStream ims = getAssets().open("up2.png");
			Drawable d = Drawable.createFromStream(ims, null);
			Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
			upBitmap_press = Bitmap.createScaledBitmap(bitmap,
			// width * 1 / 8,
			// width * bitmap.getHeight() * 1 / bitmap.getWidth() / 8,
					100, 100, true);

		} catch (IOException ex) {
			Log.d("Here!!", "load up bitmap press error.");
			ex.printStackTrace();
		}

		try {
			InputStream ims = getAssets().open("down.png");
			Drawable d = Drawable.createFromStream(ims, null);
			Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
			downBitmap = Bitmap.createScaledBitmap(bitmap,
			// width * 1 / 8,
			// width * bitmap.getHeight() * 1 / bitmap.getWidth() / 8,
					100, 100, true);
		} catch (IOException ex) {
			Log.d("Here!!", "load down bitmap error.");
			ex.printStackTrace();
		}
		try {
			InputStream ims = getAssets().open("down2.png");
			Drawable d = Drawable.createFromStream(ims, null);
			Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
			downBitmap_press = Bitmap.createScaledBitmap(bitmap,
			// width * 1 / 8,
			// width * bitmap.getHeight() * 1 / bitmap.getWidth() / 8,
					100, 100, true);
		} catch (IOException ex) {
			Log.d("Here!!", "load down bitmap press error.");
			ex.printStackTrace();
		}

		imageButtonUp = (ImageButton) findViewById(R.id.imageButtonUp);
		imageButtonDown = (ImageButton) findViewById(R.id.imageButtonDown);
		imageButtonUp.setImageBitmap(upBitmap);
		imageButtonDown.setImageBitmap(downBitmap);

		imageButtonUp.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton) v).setImageBitmap(upBitmap);
				} else {
					((ImageButton) v).setImageBitmap(upBitmap_press);
				}
				return false;
			}
		});

		imageButtonDown.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton) v).setImageBitmap(downBitmap);
				} else {
					((ImageButton) v).setImageBitmap(downBitmap_press);
				}
				return false;
			}
		});

		imageButtonUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (state == HALF) {
					if (mPopupWindow.isShowing()) {
						mPopupWindow.dismiss();
					}
					state = FULL_CONTENT;
					setLayout(state);
				} else if (state == FULL_MAP) {
					state = HALF;
					setLayout(state);
				}
			}
		});
		imageButtonDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (state == HALF) {
					state = FULL_MAP;
					setLayout(state);
				} else if (state == FULL_CONTENT) {
					state = HALF;
					setLayout(state);
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.summary, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.reload_setting:
			mapOverlay.setEarthquakeMarker();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	downloadComments TaskdownloadComments;
	final String AsyncTaskTag = "AsyncTaskTag";

	@Override
	protected void onDestroy() {
		// mapView.onDestroy();
		// Log.i(tag, "In summaryActivity: onDestroy");
		// handler.removeMessages(CURRENT_LOCATION_UPDATE);
		// mGpsTracker.stopUsingGPS();
		// mGpsTracker = null;
		//
		// if (mapOverlay.TaskDownloadMarkerInfo != null) {
		// Log.i(AsyncTaskTag, "Download marker Info canceled.");
		// mapOverlay.TaskDownloadMarkerInfo.cancel(true);
		// }
		// if (mapOverlay.TaskDownloadVolleyball != null) {
		// Log.i(AsyncTaskTag, "Download Volleyball canceled.");
		// mapOverlay.TaskDownloadVolleyball.cancel(true);
		// }
		//
		// if (mapOverlay.TaskLoadResource != null) {
		// Log.i(AsyncTaskTag, "Load Resource canceled.");
		// mapOverlay.TaskLoadResource.cancel(true);
		// }
		// if (TaskdownloadComments != null) {
		// Log.i(AsyncTaskTag, "Download comments canceled.");
		// TaskdownloadComments.cancel(true);
		// }
		// mSetupGoogleMap.mMap.clear();
		//
		// mSetupGoogleMap = null;
		//
		// // eqHashMap = null;
		// mPopupWindow = null;
		// progressDialog = null;
		// handler = null;
		// linearLayoutSummary = null;
		// mSummaryActivity = null;
		// unbindDrawables(findViewById(R.id.linearLayoutSummary));
		// System.gc();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		mGpsTracker = new GPSTracker(this);
		if (mGpsTracker.canGetLocation) {
			currentLocation = mGpsTracker.getLocation();
			// Log.d(tag,"currentLocation:"+currentLocation.toString());
		}
		mSetupGoogleMap.setupMapIfNeed();
		if(!TaskdownloadComments.isDownloadFinish){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				TaskdownloadComments = (downloadComments) new downloadComments()
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			else
				TaskdownloadComments = (downloadComments) new downloadComments().execute(0);
		}
		if(mapOverlay.TaskDownloadBall.isTaskCancel || mapOverlay.TaskDownloadMarkerInfo.isTaskCancel || mapOverlay.TaskLoadResource.isTaskCancel){
			mSetupGoogleMap.mapOverlay = new MapOverlay(this, mSetupGoogleMap.mMap);
			mapOverlay = mSetupGoogleMap.mapOverlay;
			mapOverlay.setEarthquakeMarker();
			
			mSetupGoogleMap.setHandler();
		}
		
		super.onResume();
	}

	// TODO on Resume or on Destroy set map

	@Override
	protected void onPause() {
		Log.d(tag, "In summaryActivity: onPause");
		// unbindDrawables(findViewById(R.id.linearLayoutSummary));
		// System.gc();
		// TODO save the checkbox data
		if (mSetupGoogleMap.mCheckBoxData != null) {
			File chbxFile = getFileStreamPath(ConstantVariables.checkboxDataFilename);
			FileOutputStream fos;
			ObjectOutputStream oos;
			try {
				if (chbxFile.exists() || chbxFile.createNewFile()) {
					fos = openFileOutput(
							ConstantVariables.checkboxDataFilename,
							MODE_PRIVATE);
					oos = new ObjectOutputStream(fos);
					oos.writeObject(mSetupGoogleMap.mCheckBoxData);
					oos.flush();
					oos.close();
					fos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("myTag", "Fail to Write checkbox data to file.");
			}
		}
		super.onPause();
	}

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}

	@Override
	protected void onStop() {
		Log.d(tag, "In summaryActivity: onStop");
		// mGpsTracker.stopUsingGPS();
		// mGpsTracker = null;
//		if (handler != null) {
//			handler.removeMessages(CURRENT_LOCATION_UPDATE);
//		}
		if (mGpsTracker != null) {
			mGpsTracker.stopUsingGPS();
		}
		mGpsTracker = null;

		if (mapOverlay.TaskDownloadMarkerInfo != null) {
			Log.i(AsyncTaskTag, "Download marker Info canceled.");
			mapOverlay.TaskDownloadMarkerInfo.cancel(true);
		}
		if (mapOverlay.TaskDownloadBall != null) {
			Log.i(AsyncTaskTag, "Download Volleyball canceled.");
			mapOverlay.TaskDownloadBall.cancel(true);
		}

		if (mapOverlay.TaskLoadResource != null) {
			Log.i(AsyncTaskTag, "Load Resource canceled.");
			mapOverlay.TaskLoadResource.cancel(true);
		}
		if (TaskdownloadComments != null) {
			Log.i(AsyncTaskTag, "Download comments canceled.");
			TaskdownloadComments.cancel(true);
		}
//		if(mSetupGoogleMap.mMap!=null){
//			mSetupGoogleMap.mMap.clear();
//		}
//
//		mSetupGoogleMap = null;
//
//		// eqHashMap = null;
//		mPopupWindow = null;
//		progressDialog = null;
//		handler = null;
//		linearLayoutSummary = null;
//		mSummaryActivity = null;
//		unbindDrawables(findViewById(R.id.linearLayoutSummary));
		System.gc();

		super.onStop();
	}

	class downloadComments extends AsyncTask {
		String data = "";
		boolean isDownloadFinish = false;

		@Override
		protected void onPreExecute() {
			Log.d(tag, "preExcute");
			ScrollView scrollView = (ScrollView) mSummaryActivity
					.findViewById(R.id.scrollViewWeb);
			scrollView.removeAllViews();
			View child = getLayoutInflater().inflate(R.layout.progress_bar,
					null);
			TextView textView = (TextView) child
					.findViewById(R.id.textViewOnLoadingPage);
			textView.setVisibility(View.VISIBLE);

			Animation animFadein = AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.fade_in_out_anim);
			child.findViewById(R.id.textViewOnLoadingPage).setAnimation(
					animFadein);

			scrollView.addView(child);
		}

		@Override
		protected Object doInBackground(Object... params) {
			// InputStream is = null;
			// String result = "";
			// String url =
			// "http://tesis.earth.sinica.edu.tw/common/php/getcomments.php?id=EC0521082159070";
			//
			// try {
			// HttpClient httpclient = new DefaultHttpClient();
			// HttpPost httppost = new HttpPost(url);
			// HttpResponse response = httpclient.execute(httppost);
			// HttpEntity entity = response.getEntity();
			// is = entity.getContent();
			//
			// } catch (Exception e) {
			// Log.e(tag, "Error in http connection " + e.toString());
			// }
			// // convert response to string
			// try {
			// BufferedReader reader = new BufferedReader(new InputStreamReader(
			// is, "iso-8859-1"), 8);
			// StringBuilder sb = new StringBuilder();
			// String line = null;
			// while ((line = reader.readLine()) != null) {
			// sb.append(line + "\n");
			// }
			// is.close();
			// result = sb.toString();
			// Log.d(tag,"In getJsonArray: result = "+result);
			// } catch (Exception e) {
			// Log.e(tag, "Error converting result " + e.toString());
			// }

            // FIXME: getcomments.php is deprecate
//			JSONArray jsonArray = JSONfunctions
//					.getJSONArrayfromURL("http://tesis.earth.sinica.edu.tw/common/php/getcomments.php?id=EC0521082159070");
//			if (jsonArray != null) {
//				try {
//					data = jsonArray.get(0).toString();
//					// Log.d(tag, "get comment data : " + data);
//				} catch (JSONException e) {
//					Log.e(tag, "Error parsing data " + e.toString());
//				}
//			}

            // 0713 use processdatamobile.php
            data = eqHashMap.get("Tectonic");
            Log.d(tag,"tectonic data:"+data);

			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);

			ScrollView scrollView = (ScrollView) mSummaryActivity
					.findViewById(R.id.scrollViewWeb);
			scrollView.removeAllViews();
			View child = getLayoutInflater().inflate(R.layout.webview, null);
			scrollView.addView(child);
			webView = (WebView) mSummaryActivity
					.findViewById(R.id.webViewInflated);
			webView.loadData(data, "text/html; charset=UTF-8", null);
			isDownloadFinish = true;
		}

	}

	protected static boolean isConnected(Context ctx) {
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}
		if (info.isRoaming()) {
			// here is the roaming option you can change it if you want to
			// disable Internet while roaming, just return false
			return false;
		}
		return true;
	}

	Location currentLocation;

}

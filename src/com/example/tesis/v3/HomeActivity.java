package com.example.tesis.v3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.example.tesis.v3.summaryActivity.downloadComments;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.internal.ig;
import com.google.android.gms.internal.of;

import android.R.anim;
import android.R.integer;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextSwitcher;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class HomeActivity extends ActionBarActivity implements
		ActionBar.OnNavigationListener {

	protected static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	final String tag = "myTag";
	public HomeActivity homeActivity;
	protected Location currentLocation;
	setupGoogleMap mSetupGoogleMap;
	GPSTracker mGpsTracker;
	boolean isCheckingUpdate = true;

	// final int CHECK_UPDATE_EVERY_N_MIN =
	// ConstantVariables.CHECK_UPDATE_EVERY_N_MIN;

	String generalFilename = ConstantVariables.generalFilename;
	String newestIDFilename = ConstantVariables.newestIDFilename;

	ArrayList<HashMap<String, String>> mGeneralEarthquakeList = null;
	ArrayList<HashMap<String, String>> mDisplayedEarthquakeList = null;
	ArrayList<HashMap<String, String>> mManualEarthquakeList = null;
	// ArrayList<HashMap<String, String>> mUpdateEarthquakeList = null;
	protected ProgressBar mProgress;
	boolean isGeneralDoneWrite = false;
	String errorMsg;
	boolean isSavedFileBroken = false;

	protected EarthquakeData mEarthquakeData;
	protected int mProgressStatus = 0;
	FrameLayout frameLayout;
	ListView mListView;
	final int CURRENT_LOCATION_UPDATE = ConstantVariables.CURRENT_LOCATION_UPDATE;
	final int CHECK_UPDATE = ConstantVariables.CHECK_UPDATE;
	Handler handler;
	final String AsyncTaskTag = "AsyncTaskTag";

	// 1017 add GCM
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "TESIS.v3.2014.10.17";
	private static final String TAG = "GCMRelated";
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	String regid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		homeActivity = this;

		// File dirFile = getFilesDir();
		// File[] files = dirFile.listFiles();
		// for(int i=0;i<files.length;++i){
		// files[i].delete();
		// }

		final ActionBar actionBar = getSupportActionBar();

		// XXX This is fun to have a image background on actionbar
		// InputStream ims;
		// try {
		// ims = getAssets().open("TESIS_logo_2.5.png");
		// Drawable d = Drawable.createFromStream(ims, null);
		// actionBar.setBackgroundDrawable(d);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		// actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle("TESIS");
		actionBar.setSubtitle("-");
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// // Set up the dropdown list navigation in the action bar.
		// actionBar.setListNavigationCallbacks(
		// // Specify a SpinnerAdapter to populate the dropdown list.
		// new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1,
		// android.R.id.text1, new String[] { "地震列表", "地震專欄",
		// "地震防護" }), this);
		// actionBar.setListNavigationCallbacks(new myArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, android.R.id.text1,
		// new String[] { "地震列表", "地震專欄", "地震防護" }), this);

		// actionBar.setListNavigationCallbacks(new myArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, new String[] { "地震列表",
		// "地震專欄" }), this);

		mGpsTracker = new GPSTracker(this);
		if (mGpsTracker.canGetLocation) {
			currentLocation = mGpsTracker.getLocation();
			// Log.d(tag,"currentLocation:"+currentLocation.toString());
		}

		loadEQList();

		// setHandler();
		startGCM();
	}

	/**
	 * 1017 add GCM GCM main function
	 */
	private void startGCM() {
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
			regid = getRegistrationId(getApplicationContext());
			if (!regid.isEmpty()) {
				Log.d(TAG, "get GCM ID:" + regid);
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					new GcmRegisterID(getApplicationContext(), gcm,
							getAppVersion(getApplicationContext()))
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				else
					new GcmRegisterID(getApplicationContext(), gcm,
							getAppVersion(getApplicationContext())).execute();
			}
		}
	}

	/**
	 * 1017 add GCM Check the device to make sure it has the Google Play
	 * Services APK. If it doesn't, display a dialog that allows users to
	 * download the APK from the Google Play Store or enable it in the device's
	 * system settings.
	 */

	protected boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * 1017 add GCM Gets the current registration ID for application on GCM
	 * service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	protected String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");

		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		Log.i(TAG, "registeredVersion version :" + registeredVersion);
		int currentVersion = getAppVersion(getApplicationContext());
		Log.i(TAG, "currentVersion version :" + currentVersion);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * 1017 add GCM
	 * 
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(HomeActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * 1017 add GCM
	 * 
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	// private void setUpadteThread() {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// while (isCheckingUpdate) {
	// try {
	// Log.d(tag, "check update EQList.");
	// Thread.sleep(CHECK_UPDATE_EVERY_N_MIN * 1000 * 60);
	// if (isConnected(getApplicationContext())
	// && isDownloadFisnished && isUpdateFinished) {
	// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
	// TaskUpdate = new updateGeneralEQ()
	// .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	// else
	// TaskUpdate = new updateGeneralEQ().execute();
	// }
	// Log.d(tag, "after update EQList.");
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }).start();
	// }

	protected Handler mHandler = new Handler();

	protected void deleteAllInternalFile() {
		File dirFile = getFilesDir();
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; ++i) {
			// TODO do not delete the info file
			files[i].delete();
		}
	}

	protected void loadEQList() {

		// check if list exist in internal storage
		File generalEQFile = getFileStreamPath(generalFilename);
		if (generalEQFile.exists()) {
			// load EQ file
			try {
				FileInputStream fis;
				fis = openFileInput(generalFilename);
				ObjectInputStream ois = new ObjectInputStream(fis);
				mGeneralEarthquakeList = (ArrayList<HashMap<String, String>>) ois
						.readObject();
				ois.close();
				fis.close();
				if (mGeneralEarthquakeList == null
						|| mGeneralEarthquakeList.size() <= 0) {
					isSavedFileBroken = true;
					deleteFile(generalFilename);
				} else {
					isSavedFileBroken = false;
				}
				// Log.d(tag,
				// "Load internal EQList Sucess, first EQ in list is :"
				// + mGeneralEarthquakeList.get(0).get("No") + " .");
			} catch (Exception e) {
				e.printStackTrace();
				deleteFile(generalFilename);
				isSavedFileBroken = true;
			}
			if (isSavedFileBroken) {
				deleteAllInternalFile();
				if (isConnected(getApplicationContext())) {
					// Toast "地震資料損毀，將重新下載地震資料，請耐心等候"
					// File newestIDFile = getFileStreamPath(newestIDFilename);
					// newestIDFile.delete();
					Toast.makeText(homeActivity, "地震資料損毀，將重新下載地震資料，請耐心等候",
							Toast.LENGTH_SHORT).show();
					Log.d(tag, "eqlist file is break, delete newest file");
					isGeneralDoneWrite = true;
					errorMsg = new String("");
					// mProgress = (ProgressBar)
					// findViewById(R.id.progressBar1);
					// new Thread(new Runnable() {
					//
					// @Override
					// public void run() {
					// while (mProgressStatus < 100) {
					// mHandler.post(new Runnable() {
					//
					// @Override
					// public void run() {
					// mProgress.setProgress(mProgressStatus);
					// }
					// });
					// }
					// }
					// });
					setDownloadLayout();
					TaskDownload = downloadTask();
					// if (Build.VERSION.SDK_INT >=
					// Build.VERSION_CODES.HONEYCOMB)
					// TaskDownload = new DownloadEQListForegroundAsyncTask()
					// .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,myDate.getFrom(),myDate.getTo());
					// else
					// TaskDownload = new
					// DownloadEQListForegroundAsyncTask().execute(myDate.getFrom(),myDate.getTo());
				} else {
					showAlertDialog("地震資料損毀，請連上網路", "無網路", true);
				}

			} else {
				if (!isConnected(getApplicationContext())) {
					// errorMsg = new
					// String("尚未連上網路，將導入舊地震列表，部分功能可能無法使用，建議連上網路");
					Toast.makeText(homeActivity,
							"尚未連上網路，將導入舊地震列表，部分功能可能無法使用，建議連上網路",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(homeActivity, "載入已存地震，將執行更新檢查是否有新地震",
							Toast.LENGTH_SHORT).show();
					TaskDownload = downloadTask();
				}
				mGeneralEarthquakeList = setEQlistLocation(mGeneralEarthquakeList);
				mDisplayedEarthquakeList = setDisplayedEarthquakeList(
						mGeneralEarthquakeList, false);
				adaptEQList(mDisplayedEarthquakeList);
				setHandler();

			}
		} else {
			deleteAllInternalFile();
			if (isConnected(getApplicationContext())) {
				// set View to Progress bar
				// mProgress = (ProgressBar) findViewById(R.id.progressBar1);
				// new Thread(new Runnable() {
				//
				// @Override
				// public void run() {
				// while (mProgressStatus < 100) {
				// mHandler.post(new Runnable() {
				//
				// @Override
				// public void run() {
				// mProgress.setProgress(mProgressStatus);
				// }
				// });
				// }
				// }
				// });
				setDownloadLayout();
				TaskDownload = downloadTask();
			} else {
				showAlertDialog("尚無地震資料，請連上網路", "無網路", true);
			}
		}

	}

	AsyncTask TaskDownload;

	private AsyncTask downloadTask() {
		if (TaskDownload != null) {
			TaskDownload.cancel(true);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			TaskDownload = new updateGeneralEQ()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			TaskDownload = new updateGeneralEQ().execute();
		return TaskDownload;
	}

	private void showAlertDialog(String errorMessage, String errorTitle,
			boolean isFinishActivity) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(homeActivity);
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.setTitle(errorTitle);
		alertDialog.setMessage(errorMessage);
		if (isFinishActivity) {
			alertDialog.setPositiveButton("確定", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					homeActivity.finish();
				}
			});
		} else {
			alertDialog.setPositiveButton("確定", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
		}
		alertDialog.show();
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

	int loadingTextIndex = 0;
	TextSwitcher textSwitcher;

	private void setDownloadLayout() {
		frameLayout = (FrameLayout) homeActivity.findViewById(R.id.container);
		frameLayout.removeAllViews();
		View child = getLayoutInflater().inflate(R.layout.progress_bar, null);

		// Animation animFadein = AnimationUtils.loadAnimation(
		// getApplicationContext(), R.anim.fade_in_out_anim);
		// child.findViewById(R.id.textViewOnLoadingPage).setAnimation(animFadein);

		textSwitcher = (TextSwitcher) child
				.findViewById(R.id.textSwitcherOnLoadingPage);

		textSwitcher
				.setInAnimation(getApplicationContext(), R.anim.abc_fade_in);
		textSwitcher.setOutAnimation(getApplicationContext(),
				R.anim.abc_fade_out);

		TextView textView1 = new TextView(getApplicationContext());
		textView1.setTextAppearance(getApplicationContext(),
				R.style.GenericProgresstextColor1);
		textView1.setGravity(Gravity.CENTER_VERTICAL
				| Gravity.CENTER_HORIZONTAL);
		TextView textView2 = new TextView(getApplicationContext());
		textView2.setTextAppearance(getApplicationContext(),
				R.style.GenericProgresstextColor2);
		textView2.setGravity(Gravity.CENTER_VERTICAL
				| Gravity.CENTER_HORIZONTAL);

		textSwitcher.addView(textView1);
		textSwitcher.addView(textView2);

		textSwitcher.setText(ConstantVariables.LoadingText[loadingTextIndex
				% ConstantVariables.LoadingText.length]);

		frameLayout.addView(child);

		Timer timer = new Timer("myTimer");
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				homeActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						textSwitcher
								.setText(ConstantVariables.LoadingText[(loadingTextIndex++)
										% ConstantVariables.LoadingText.length]);

					}
				});

			}
		}, 0, 4000);
	}

	AsyncTask taskDownloadManualAsyncTask;

	void downloadManualEQList(myDate from, myDate to) {
		homeActivity.getActionBar().setSubtitle(
				from.DatetoString() + "~" + to.DatetoString());
		setDownloadLayout();
		if (taskDownloadManualAsyncTask != null) {
			taskDownloadManualAsyncTask.cancel(true);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			taskDownloadManualAsyncTask = new DownloadEQListManualTask()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, from, to);
		else
			taskDownloadManualAsyncTask = new DownloadEQListManualTask()
					.execute(from, to);

	}

	private boolean isDownloadEQListManualTaskFisnished = true;

	class DownloadEQListManualTask extends
			AsyncTask<myDate, Void, ArrayList<HashMap<String, String>>> {

		@Override
		protected void onPreExecute() {
			// Log.d(AsyncTaskTag,
			// "In download EQ List Foreground Task: preExcute");
			// frameLayout = (FrameLayout) homeActivity
			// .findViewById(R.id.container);
			// frameLayout.removeAllViews();
			// View child = getLayoutInflater().inflate(R.layout.progress_bar,
			// null);
			//
			// Animation animFadein = AnimationUtils.loadAnimation(
			// getApplicationContext(), R.anim.fade_in_out_anim);
			// child.findViewById(R.id.textViewOnLoadingPage).setAnimation(
			// animFadein);
			//
			// frameLayout.addView(child);
			// isDownloadEQListManualTaskFisnished = false;
			super.onPreExecute();
		}

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				myDate... params) {
			ArrayList<HashMap<String, String>> resultArrayList = EarthquakeData
					.getData(homeActivity, params[0].DatetoString(),
							params[1].DatetoString(), false);
			isDownloadEQListManualTaskFisnished = true;
			return resultArrayList;
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			// TODO check is general or manual
			if (result != null) {
				// Log.d(tag, mGeneralEarthquakeList.get(0).toString());
				// ConstantVariables.writeEQToInternalFile(homeActivity,
				// result, generalFilename);
				// mGeneralEarthquakeList = result;
				mManualEarthquakeList = setEQlistLocation(result);

				mDisplayedEarthquakeList = setDisplayedEarthquakeList(
						mManualEarthquakeList, true);
				adaptEQList(mDisplayedEarthquakeList);
				setHandler();
				// setUpadteThread();
				// homeActivity.registerReceiver(new UpdateReceiver(),
				// new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			} else {
				showAlertDialog("日期區間無地震發生或資料下載失敗", "無地震資料", false);
				if (mGeneralEarthquakeList != null) {
					mDisplayedEarthquakeList = setDisplayedEarthquakeList(
							mGeneralEarthquakeList, false);
					adaptEQList(mDisplayedEarthquakeList);
				}
			}
			super.onPostExecute(result);
		}
	}

	// private boolean isDownloadFisnished = true;

	// protected class DownloadEQListTask extends AsyncTask {
	// @Override
	// protected void onPreExecute() {
	// Log.d(AsyncTaskTag, "In download EQ List Task: preExcute");
	// frameLayout = (FrameLayout) homeActivity
	// .findViewById(R.id.container);
	// frameLayout.removeAllViews();
	// View child = getLayoutInflater().inflate(R.layout.progress_bar,
	// null);
	//
	// Animation animFadein = AnimationUtils.loadAnimation(
	// getApplicationContext(), R.anim.fade_in_out_anim);
	// child.findViewById(R.id.textViewOnLoadingPage).setAnimation(
	// animFadein);
	//
	// frameLayout.addView(child);
	// isDownloadFisnished = false;
	// }
	//
	// @Override
	// protected Object doInBackground(Object... params) {
	// myDate From = new myDate();
	// From.resetFrom();
	// myDate To = new myDate();
	// mGeneralEarthquakeList = EarthquakeData.getData(homeActivity,
	// From.DatetoString(), To.DatetoString(), false);
	// Log.d(AsyncTaskTag, "In download EQ List Task: download EQ finish.");
	// isDownloadFisnished = true;
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(Object result) {
	// Log.d(AsyncTaskTag, "In download EQ List Task: postExcute");
	// // write list to file
	// if (mGeneralEarthquakeList != null) {
	// // Log.d(tag, mGeneralEarthquakeList.get(0).toString());
	// ConstantVariables.writeEQToInternalFile(homeActivity,
	// mGeneralEarthquakeList, generalFilename);
	// adaptEQList();
	// setHandler();
	// // setUpadteThread();
	// // homeActivity.registerReceiver(new UpdateReceiver(),
	// // new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	// } else {
	//
	// showAlertDialog("無法連接伺服器，請稍後嘗試", "伺服器無回應", true);
	// }
	//
	// }
	//
	// }

	private boolean isUpdateFinished = true;

	@SuppressWarnings("rawtypes")
	class updateGeneralEQ extends
			AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {

		@Override
		protected void onPreExecute() {
			isUpdateFinished = false;
			Log.d(AsyncTaskTag, "In upadteGeneralEQ Task: preExcute");
			super.onPreExecute();
		}

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				Void... params) {
			ArrayList<HashMap<String, String>> mUpdateEarthquakeList = EarthquakeData
					.getData(homeActivity, myDate.getFrom().DatetoString(),
							myDate.getTo().DatetoString(), true);
			Log.d(AsyncTaskTag, "update EQlist" + mUpdateEarthquakeList);
			isUpdateFinished = true;
			return mUpdateEarthquakeList;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			Log.d(AsyncTaskTag, "In upadteGeneralEQ Task: postExcute");
			if (result != null && result.size() != 0) {
				// Log.d(tag, "In upadteGeneralEQ Task: postExcute :"
				// + mManualEarthquakeList);
				if (mManualEarthquakeList == null) {
					mGeneralEarthquakeList = setEQlistLocation(result);
					mDisplayedEarthquakeList = setDisplayedEarthquakeList(
							mGeneralEarthquakeList, false);
					// Log.d(tag, "In upadteGeneralEQ Task: postExcute :"
					// + mDisplayedEarthquakeList.toString());
					if (handler == null) {
						setHandler();
					}
					Message m = new Message();
					// 定義 Message的代號，handler才知道這個號碼是不是自己該處理的。
					m.what = CHECK_UPDATE;
					handler.sendMessage(m);

					// adaptEQList(mDisplayedEarthquakeList);
				}

				Toast.makeText(homeActivity, "下載完畢", Toast.LENGTH_SHORT).show();
			} else {
				// if first time download, close the app.
				Toast.makeText(homeActivity, "暫無新資料", Toast.LENGTH_SHORT)
						.show();
			}
			if (isManualUpdate) {
				Toast.makeText(homeActivity, "更新完畢", Toast.LENGTH_SHORT).show();
				isManualUpdate = false;
			}
			super.onPostExecute(result);
		}

	}

	protected void adaptEQList(ArrayList<HashMap<String, String>> mList) {

		frameLayout = (FrameLayout) homeActivity.findViewById(R.id.container);
		frameLayout.removeAllViews();
		View child = getLayoutInflater()
				.inflate(R.layout.earthquake_list, null);
		frameLayout.addView(child);

		mListView = (ListView) homeActivity.findViewById(R.id.listView1);

		// setEQlistLocation();
		// setDisplayedEarthquakeList(mList, false);

		// Log.d(tag, "In adapt EQ list , display first EQ :"
		// + mDisplayedEarthquakeList.get(0).get("No"));

		myEarthquakeListAdapter mEarthquakeListAdapter = new myEarthquakeListAdapter(
				homeActivity,
				// mGeneralEarthquakeList,
				mList, R.layout.drawer_list_item, new String[] { "ml",
						"DateAndTime", "distance", "Location", "isNew" },
				new int[] { R.id.eqlist_ml, R.id.eqlist_date,
						R.id.eqlist_distance, R.id.eqlist_location,
						R.id.eqlist_new });
		mListView.setAdapter(mEarthquakeListAdapter);
		mListView.setOnItemClickListener(new myOnItemClickListener());

		// TODO implement manual update
		// TODO or implement auto update after a while
	}

	double minML, maxML;
	int inDate, minDeep, maxDeep, inDistance;
	ArrayList<HashMap<String, String>> mSettingList;

	private ArrayList<HashMap<String, String>> setDisplayedEarthquakeList(
			ArrayList<HashMap<String, String>> mList, boolean isManual) {

		ArrayList<HashMap<String, String>> mFilteredList = new ArrayList<HashMap<String, String>>();
		HashMap<String, Integer> settingHashMap = null;
		File EQFile = getFileStreamPath(settingPreferenceFilename);
		if (EQFile.exists()) {
			try {
				FileInputStream fis;
				fis = openFileInput(settingPreferenceFilename);
				ObjectInputStream ois = new ObjectInputStream(fis);
				settingHashMap = (HashMap<String, Integer>) ois.readObject();
				ois.close();
				fis.close();
				double minML = Double.parseDouble(nums_ML[settingHashMap
						.get("minML")]);
				double maxML = Double.parseDouble(nums_ML[settingHashMap
						.get("maxML")]);
				double minDeep = Double.parseDouble(nums_Depth[settingHashMap
						.get("minDeep")]);
				double maxDeep = Double.parseDouble(nums_Depth[settingHashMap
						.get("maxDeep")]);
				int inDistance = Integer.parseInt(nums_Distance[settingHashMap
						.get("inDistance")]);
				int inDate = settingHashMap.get("inDate");

				Log.d(tag, "load setting Value." + "\nminML:" + minML
						+ "\nmaxML:" + maxML + "\nminDeep:" + minDeep
						+ "\nmaxDeep:" + maxDeep + "\ninDate:" + inDate
						+ "\ninDistance:" + inDistance);

				for (int i = 0; i < mList.size(); ++i) {
					// Log.d(tag,"set Dispalyed EQ : distance "+mGeneralEarthquakeList.get(i).get("distance_value")
					// );
					if (Double.parseDouble(mList.get(i).get("ml")) >= minML
							&& Double.parseDouble(mList.get(i).get("ml")) <= maxML
							&& Double.parseDouble(mList.get(i).get("depth")) >= minDeep
							&& Double.parseDouble(mList.get(i).get("depth")) <= maxDeep
							&& Double.parseDouble(mList.get(i).get(
									"distance_value")) <= inDistance) {

						// Log.d(tag,"set Dispalyed EQ : No "+mGeneralEarthquakeList.get(i).get("No")
						// );
						Calendar today = Calendar.getInstance();
						String date_of_eqString = mList.get(i).get("date");
						Log.d(tag, "dateString:" + date_of_eqString);
						DateTimeFormatter dateStringFormatter = DateTimeFormat
								.forPattern("yyyy-MM-dd");
						DateTime todayDateTime = new DateTime(
								today.getTimeInMillis());
						DateTime eqDateTime = dateStringFormatter
								.parseDateTime(date_of_eqString);
						int days = Days.daysBetween(new LocalDate(eqDateTime),
								new LocalDate(todayDateTime)).getDays();
						int months = Months.monthsBetween(
								new LocalDate(eqDateTime),
								new LocalDate(todayDateTime)).getMonths();

						Log.d(tag, "days:" + days + " months:" + months);

						if (!isManual) {
							switch (inDate) {
							case SETTING_DATE_ALL:
								mFilteredList.add(mList.get(i));
								((ActionBar) getSupportActionBar())
										.setSubtitle("過去三個月顯著地震列表");
								break;
							case SETTING_DATE_1_day:
								if (days <= 1)
									mFilteredList.add(mList.get(i));
								((ActionBar) getSupportActionBar())
										.setSubtitle("過去一日內顯著地震列表");
								break;
							case SETTING_DATE_1_week:
								if (days <= 7)
									mFilteredList.add(mList.get(i));
								((ActionBar) getSupportActionBar())
										.setSubtitle("過去一週內顯著地震列表");
								break;
							case SETTING_DATE_1_month:
								if (months <= 1)
									mFilteredList.add(mList.get(i));
								((ActionBar) getSupportActionBar())
										.setSubtitle("過去一個月內顯著地震列表");
								break;

							case SETTING_DATE_2_month:
								if (months <= 2)
									mFilteredList.add(mList.get(i));
								((ActionBar) getSupportActionBar())
										.setSubtitle("過去兩個月內顯著地震列表");
								break;

							case SETTING_DATE_3_month:
								if (months <= 3)
									mFilteredList.add(mList.get(i));
								((ActionBar) getSupportActionBar())
										.setSubtitle("過去三個月內顯著地震列表");
								break;

							default:
								mFilteredList.add(mList.get(i));
								break;
							}
						} else {
							mFilteredList.add(mList.get(i));
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				Log.e(tag, "cannot load settingHashMap from file.");
				// Toast.makeText(homeActivity, "篩選失敗",
				// Toast.LENGTH_SHORT).show();
				ConstantVariables.saveSetting(homeActivity, 0,
						nums_ML.length - 1, 0, nums_Depth.length - 1,
						nums_Distance.length - 1, SETTING_DATE_ALL,
						ConstantVariables.SETTING_NOTIFICATION_STATE_ON);
				mFilteredList = mList;
			}
			if (mFilteredList.size() == 0) {
				if (isManual) {
					Toast.makeText(homeActivity, "沒有符合條件的地震，將重設地震篩選條件",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(homeActivity, "沒有符合條件的地震，將重設地震篩選條件",
							Toast.LENGTH_SHORT).show();
				}
				ConstantVariables.saveSetting(homeActivity, 0,
						nums_ML.length - 1, 0, nums_Depth.length - 1,
						nums_Distance.length - 1, SETTING_DATE_ALL,
						ConstantVariables.SETTING_NOTIFICATION_STATE_ON);
				mFilteredList = mList;

			}
		} else {
			ConstantVariables.saveSetting(homeActivity, 0, nums_ML.length - 1,
					0, nums_Depth.length - 1, nums_Distance.length - 1,
					SETTING_DATE_ALL,
					ConstantVariables.SETTING_NOTIFICATION_STATE_ON);
			mFilteredList = mList;
		}

		return mFilteredList;
	}

	String settingPreferenceFilename = ConstantVariables.settingPreferenceFilename;

	// ML 0-100 => 0.0-10.0
	// Deep 0-350
	// Distance 0 => all
	// Date 0 => all, others see flags "SETTING_DATE_*" below
	String nums_ML[] = ConstantVariables.nums_ML;
	String nums_Depth[] = ConstantVariables.nums_Depth;
	String nums_Distance[] = ConstantVariables.nums_Distance;
	String nums_Date[] = ConstantVariables.nums_Date;
	final int SETTING_DATE_ALL = ConstantVariables.SETTING_DATE_ALL;
	final int SETTING_DATE_1_day = ConstantVariables.SETTING_DATE_1_day;
	final int SETTING_DATE_1_week = ConstantVariables.SETTING_DATE_1_week;
	final int SETTING_DATE_1_month = ConstantVariables.SETTING_DATE_1_month;
	final int SETTING_DATE_2_month = ConstantVariables.SETTING_DATE_2_month;
	final int SETTING_DATE_3_month = ConstantVariables.SETTING_DATE_3_month;

	protected ArrayList<HashMap<String, String>> setEQlistLocation(
			ArrayList<HashMap<String, String>> mList) {

		if (currentLocation == null) {
			for (int i = 0; i < mList.size(); ++i) {
				mList.get(i).put("distance", "未連上網路，無法估計震央距離現在位置");
				mList.get(i).put("distance_value", "" + 0);
			}
		} else {
			for (int i = 0; i < mList.size(); ++i) {
				Location location = new Location("start");
				double startLat = Double.parseDouble(mList.get(i).get("lat"));
				double startLng = Double.parseDouble(mList.get(i).get("lng"));
				location.setLatitude(startLat);
				location.setLongitude(startLng);

				// Log.d(tag,
				// "start:" + location.getLatitude() + ","
				// + location.getLongitude() + " to:"
				// + currentLocation.getLatitude() + ","
				// + currentLocation.getLongitude());
				float distance = location.distanceTo(currentLocation);
				// Log.d(tag, "distance:" + distance);
				// if () {
				//
				// mGeneralEarthquakeList.get(i).put("distance",
				// "未連上網路，無法估計震央距離現在位置");
				// } else {
				// mGeneralEarthquakeList.get(i).put("distance",
				// "震央距離現在位置約 " + (int) (distance / 1000) + " 公里");
				// }

				// set direction here

				HashMap<String, String> eqHashMap = mList.get(i);
				double dx = location.getLongitude()
						- currentLocation.getLongitude();
				double dy = location.getLatitude()
						- currentLocation.getLatitude();
				double theta = Math.atan2(dy, dx);
				// Log.d(tag, "theta:" + theta);
				if (theta < 0) {
					theta += 2 * Math.PI;
				}
				if (theta > 2 * Math.PI * 31 / 32
						|| theta <= 2 * Math.PI * 1 / 32) {
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

				eqHashMap.put("distance_value", "" + distance / 1000);

				eqHashMap.put("distance",
						"震央位於現在位置 " + eqHashMap.get("direction")
								+ (int) (distance / 1000) + "公里處");
			}
		}
		return mList;
	}

	private void setHandler() {
		handler = new Handler(Looper.getMainLooper()) {

			public void handleMessage(Message msg) {
				switch (msg.what) {
				// 當收到的Message的代號為我們剛剛訂的代號就做下面的動作。
				case CURRENT_LOCATION_UPDATE:
					// call back here, 更改current Location
					if (mGpsTracker != null) {
						currentLocation = mGpsTracker.getLocation();
						if (mManualEarthquakeList != null) {
							mDisplayedEarthquakeList = setEQlistLocation(mManualEarthquakeList);
						} else {
							mDisplayedEarthquakeList = setEQlistLocation(mGeneralEarthquakeList);
							ConstantVariables.writeEQToInternalFile(
									homeActivity, mGeneralEarthquakeList,
									generalFilename);
						}

					}
					break;
				case CHECK_UPDATE:
					Log.d(tag, "handler check update at thread:");
					adaptEQList(mDisplayedEarthquakeList);
					// ConstantVariables.writeEQToInternalFile(homeActivity,
					// mGeneralEarthquakeList, generalFilename);

					break;

				}
				super.handleMessage(msg);
			}

		};
		if (mGpsTracker == null) {
			mGpsTracker = new GPSTracker(this);
		}
		mGpsTracker.setHandler(handler);
	}

	protected class myOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
				long arg3) {
			if (isConnected(getApplicationContext())) {
				Log.d(tag,
						"EQList onItemClick, mEQList size:" + arg0.getCount());
				Intent intent = new Intent(homeActivity, summaryActivity.class);
				HashMap<String, String> earthquakeInfo = mDisplayedEarthquakeList
						.get(arg2);
				intent.putExtra("earthquakeInfo", earthquakeInfo);
				startActivity(intent);

			} else {
				showAlertDialog("尚未連上網路，無法查看詳細資料", "無網路", false);
			}
		}
	}

	@Override
	protected void onResume() {
		Log.d(tag, "In HomeActivity: onResume");
		Log.d("MemoryTag",
				"In SummaryActivity onResume : memory less:"
						+ Integer
								.toString(((ActivityManager) getSystemService(ACTIVITY_SERVICE))
										.getMemoryClass()));
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d(tag, "In HomeActivity: onPause:");
		// mGpsTracker.stopUsingGPS();
		// mGpsTracker = null;
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d(tag, "In HomeActivity: onStop: write file.");
		if (mGeneralEarthquakeList != null) {
			for (int i = 0; i < mGeneralEarthquakeList.size(); ++i) {
				mGeneralEarthquakeList.get(i).put("isNew", "false");
			}
			ConstantVariables.writeEQToInternalFile(homeActivity,
					mGeneralEarthquakeList, generalFilename);
		}
		if (handler != null) {
			handler.removeMessages(CURRENT_LOCATION_UPDATE);
		}
		isCheckingUpdate = false;
		if (mGpsTracker != null) {
			mGpsTracker.stopUsingGPS();
			mGpsTracker = null;
		}
		if (TaskDownload != null) {
			TaskDownload.cancel(true);
		}
		super.onStop();
	}

	/*** ========================Navigation=========================== ***/

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	final int SETTING = 1001;
	boolean isManualUpdate = false;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Log.d(tag,
		// "onOptionsItemSelected:" + item.getItemId() + ","
		// + item.getTitle() +
		// " action:"+R.id.action_settings+" reload:"+R.id.reload_setting);
		switch (item.getItemId()) {
		case R.id.action_settings:
			// settingDialog.show();
			Intent intent = new Intent(homeActivity, SettingActivity.class);
			// startActivity(intent);
			startActivityForResult(intent, SETTING);
			break;

		case R.id.reload_setting:
			if (isConnected(getApplicationContext())
					&& isDownloadEQListManualTaskFisnished && isUpdateFinished
					&& !isManualUpdate) {
//				TaskDownload = downloadTask();
//				isManualUpdate = true;
//				Toast.makeText(homeActivity, "更新地震列表...", Toast.LENGTH_SHORT)
//						.show();
                //2015/06/10 change reload logic
                deleteAllInternalFile();
                setDownloadLayout();
                TaskDownload = downloadTask();
			} else if (!isDownloadEQListManualTaskFisnished
					|| !isUpdateFinished || isManualUpdate) {
				Toast.makeText(homeActivity, "正在檢查更新，請稍後", Toast.LENGTH_SHORT)
						.show();
			} else if (!isConnected(getApplicationContext())) {
				Toast.makeText(homeActivity, "尚未連上網路，無法執行更新",
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.datePicker_setting:
			if (isConnected(getApplicationContext())) {
				MyDoubleDatePicker myDatePicker = new MyDoubleDatePicker(this);
				myDatePicker.show();
			} else {
				showAlertDialog("尚未連上網路，無法使用此功能", "無網路", false);
			}
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SETTING:
			// 0825 comming back from setting
			// setGPSTrakerIfNeed();
			Log.d(tag, "on Activity Result: mManualEarthquakeList"
					+ mManualEarthquakeList);
			setHandler();
			if (mManualEarthquakeList != null) {
				mDisplayedEarthquakeList = setDisplayedEarthquakeList(
						mManualEarthquakeList, true);
			} else {
				mDisplayedEarthquakeList = setDisplayedEarthquakeList(
						mGeneralEarthquakeList, false);
			}
			adaptEQList(mDisplayedEarthquakeList);
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		if (position == 1) {
			loadEQList();
		} else {
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment).commit();
		}
		return true;
	}

	// public class UpdateReceiver extends BroadcastReceiver {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// Log.d(tag, "In Updatereceiver: internet change");
	// ConnectivityManager connectivityManager = (ConnectivityManager) context
	// .getSystemService(Context.CONNECTIVITY_SERVICE);
	// NetworkInfo mobile = connectivityManager
	// .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	// NetworkInfo wifi = connectivityManager
	// .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	// if (wifi.isAvailable() || mobile.isAvailable()) {
	// if (TaskUpdate != null) {
	// TaskUpdate.cancel(true);
	// }
	// TaskUpdate = new updateGeneralEQ().execute();
	// isManualUpdate = true;
	// Toast.makeText(homeActivity, "更新地震列表...", Toast.LENGTH_SHORT)
	// .show();
	// }
	//
	// }
	// }

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView;
			rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			// TextView dummyTextView = (TextView) rootView
			// .findViewById(R.id.section_label);
			// dummyTextView.setText(Integer.toString(getArguments().getInt(
			// ARG_SECTION_NUMBER)));

			return rootView;
		}
	}
}

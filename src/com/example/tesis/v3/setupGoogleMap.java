package com.example.tesis.v3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.senab.photoview.PhotoViewAttacher;

import android.R.anim;
import android.R.integer;
import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.cardemulation.CardEmulation;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RawRes;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class setupGoogleMap {
	public GoogleMap mMap;
	// myMapView mapView;
	summaryActivity mainActivity;
	// setupGoogleMapTool mSetupGoogleMapTool;
	MapOverlay mapOverlay;
	// ArrayList<HashMap<String, String>> mEarthquakeList;
	HashMap<String, String> eqHashMap;
	final String tag = "myTag";
	final String timeTag = "timeTag";
	long startTime, endTime;
	ImageView mImageView, mImageView2;
	View popupWindow;
	PopupWindow mPopupWindow;
	ScrollView mScrollView;

	private void calculateRuntime(String info) {
		endTime = System.currentTimeMillis();
		Log.d(timeTag, "In setupGoogleMap: " + info + ": "
				+ (endTime - startTime));
		startTime = System.currentTimeMillis();
	}

	public setupGoogleMap(summaryActivity mainActivity, PopupWindow mPopupWindow) {
		this.mainActivity = mainActivity;
		this.mPopupWindow = mPopupWindow;
		this.eqHashMap = mainActivity.eqHashMap;
		// this.mapView = mapView;
		startTime = System.currentTimeMillis();
		setupMapIfNeed();
	}

	protected Bitmap scaleBitmapOnScreenSize(Drawable d) {
		DisplayMetrics metrics = new DisplayMetrics();
		metrics = mainActivity.getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		Log.d("bitmap", "screen width:" + width + " height:" + height);
		Bitmap oldBitmap = ((BitmapDrawable) d).getBitmap();
		// sony Xperia SP 720*1280
		float scalingFactor = (float) width / 720;
		Bitmap newBitmap = ConstantVariables.ScaleBitmap(oldBitmap,
				scalingFactor);
		// oldBitmap.recycle();
		return newBitmap;
	}

	protected void loadMapBar() {
		mImageView = (ImageView) mainActivity.findViewById(R.id.imageView1);
		// load map bar image
		// mImageView.setImageResource(R.drawable.ml_map_bar_trans);
		mImageView.setImageBitmap(scaleBitmapOnScreenSize(mainActivity
				.getResources().getDrawable(R.drawable.ml_map_bar_trans)));
		// try {
		// InputStream ims = mainActivity.getAssets().open(
		// "ml_map_bar_trans.png");
		// BitmapFactory.Options options=new BitmapFactory.Options();
		// options.inJustDecodeBounds = false;
		// options.inSampleSize = 1;
		// Bitmap bitmap = BitmapFactory.decodeStream(ims,null,options);
		// mImageView.setImageBitmap(bitmap);
		// mImageView.setImageResource(R.drawable.ml_map_bar_trans);
		// DisplayMetrics metrics = new DisplayMetrics();
		// mainActivity.getWindowManager().getDefaultDisplay()
		// .getMetrics(metrics);
		// int width = metrics.widthPixels;
		// int height = metrics.heightPixels;
		// Log.d("Here!!", "window size " + width + " " + height);
		// float density =
		// mainActivity.getResources().getDisplayMetrics().density;
		// if ((float) width / density < 600) {
		// Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap,
		// width * 1 / 8,
		// width * bitmap.getHeight() * 1 / bitmap.getWidth() / 8,
		// true);
		// mImageView.setImageBitmap(newBitmap);
		// } else if ((float) width / density > 600) {
		// Bitmap newBitmap = Bitmap
		// .createScaledBitmap(bitmap, width * 1 / 15, width
		// * bitmap.getHeight() * 1 / bitmap.getWidth()
		// / 15, true);
		// mImageView.setImageBitmap(newBitmap);
		// }
		// if(!bitmap.isRecycled()){
		// bitmap.recycle();
		// System.gc();
		// }
		// } catch (IOException ex) {
		// Log.d("Here!!", "load map bar error.");
		// ex.printStackTrace();
		// }
	}

	Bitmap historyBitmap1, historyBitmap2;
	ImageView historyImageView;

	protected void loadHistoryBar() {
		historyImageView = (ImageView) mainActivity
				.findViewById(R.id.imageView2);
		// historyImageView.setImageResource(R.drawable.colorbar);
		historyImageView.setImageBitmap(scaleBitmapOnScreenSize(mainActivity
				.getResources().getDrawable(R.drawable.colorbar)));

//		sizeImageView = (ImageView) mainActivity.findViewById(R.id.imageView3);
//		sizeImageView.setImageResource(R.drawable.size);
		// double depth = Double.parseDouble(eqHashMap.get("depth"));
		// if(depth > 70){
		// // historyImageView.setImageBitmap(historyBitmap1);
		// historyImageView.setImageResource(R.drawable.history_bar_ocean_0);
		// } else{
		// // historyImageView.setImageBitmap(historyBitmap2);
		// historyImageView.setImageResource(R.drawable.history_bar_ocean_1);
		// }
		// historyImageView = (ImageView)
		// mainActivity.findViewById(R.id.imageView2);
		// // load map bar image
		// try {
		// InputStream ims = mainActivity.getAssets().open(
		// "history_bar_ocean_0.png");
		// // InputStream ims = mainActivity.getAssets().open(
		// // "history_bar_copper_0.png");
		// BitmapFactory.Options options=new BitmapFactory.Options();
		// options.inJustDecodeBounds = false;
		// options.inSampleSize = 2;
		// historyBitmap1 = BitmapFactory.decodeStream(ims,null,options);
		// DisplayMetrics metrics = new DisplayMetrics();
		// mainActivity.getWindowManager().getDefaultDisplay()
		// .getMetrics(metrics);
		// int width = metrics.widthPixels;
		// int height = metrics.heightPixels;
		// Log.d("Here!!", "window size " + width + " " + height);
		// float density =
		// mainActivity.getResources().getDisplayMetrics().density;
		// if ((float) width / density < 600) {
		// Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap,
		// width * 1 / 12,
		// width * bitmap.getHeight() * 1 / bitmap.getWidth() / 12,
		// true);
		// // mImageView.setImageBitmap(newBitmap);
		// historyBitmap1 = newBitmap;
		// } else if ((float) width / density > 600) {
		// Bitmap newBitmap = Bitmap
		// .createScaledBitmap(bitmap, width * 1 / 24, width
		// * bitmap.getHeight() * 1 / bitmap.getWidth()
		// / 24, true);
		// // mImageView.setImageBitmap(newBitmap);
		// historyBitmap1 = newBitmap;
		// }
		//

		// ims = mainActivity.getAssets().open("history_bar_ocean_1.png");
		// // ims = mainActivity.getAssets().open("history_bar_copper_1.png");
		// historyBitmap2 = BitmapFactory.decodeStream(ims,null,options);
		// metrics = new DisplayMetrics();
		// mainActivity.getWindowManager().getDefaultDisplay()
		// .getMetrics(metrics);
		// width = metrics.widthPixels;
		// height = metrics.heightPixels;
		// Log.d("Here!!", "window size " + width + " " + height);
		// density = mainActivity.getResources().getDisplayMetrics().density;
		// if ((float) width / density < 600) {
		// Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap,
		// width * 1 / 12,
		// width * bitmap.getHeight() * 1 / bitmap.getWidth() / 12,
		// true);
		// // mImageView.setImageBitmap(newBitmap);
		// historyBitmap2 = newBitmap;
		// } else if ((float) width / density > 600) {
		// Bitmap newBitmap = Bitmap
		// .createScaledBitmap(bitmap, width * 1 / 24, width
		// * bitmap.getHeight() * 1 / bitmap.getWidth()
		// / 24, true);
		// // mImageView.setImageBitmap(newBitmap);
		// historyBitmap2 = newBitmap;
		// }

		// bitmap.recycle();
		// } catch (IOException ex) {
		// Log.d("Here!!", "load HistoryBar error.");
		// ex.printStackTrace();
		// }
	}

	Bitmap geoStateBitmap;
	PopupWindow mGeoStatePopupWindow;

	protected void loadGeoState() {
		// XXX need to check if memory is not enough
		DisplayMetrics metrics = new DisplayMetrics();
		mainActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		// try {
		// InputStream ims = mainActivity.getAssets().open("legend.jpg");
		// BitmapFactory.Options options=new BitmapFactory.Options();
		// options.inJustDecodeBounds = false;
		// options.inSampleSize = 2;
		// geoStateBitmap = BitmapFactory.decodeStream(ims,null,options);
		// // geoStateBitmap = Bitmap.createScaledBitmap(bitmap, width * 1 / 1,
		// // width * bitmap.getHeight() * 1 / bitmap.getWidth() / 1,
		// // true);
		// } catch (IOException ex) {
		// Log.d("Here!!", "load loadGeoState error.");
		// ex.printStackTrace();
		// }

		LayoutInflater layoutInflater = LayoutInflater.from(mainActivity);
		View popupWindow = layoutInflater.inflate(R.layout.popup_geo_state,
				null);

		mGeoStatePopupWindow = new PopupWindow(popupWindow, width, height / 2);
		// mGeoStatePopupWindow = new PopupWindow(popupWindow);
		mGeoStatePopupWindow.setContentView(popupWindow);

		ImageView imageView = (ImageView) mGeoStatePopupWindow.getContentView()
				.findViewById(R.id.imageViewGeoState);
		// imageView.setImageBitmap(geoStateBitmap);
		// Drawable drawable = mainActivity.getResources().getDrawable(
		// R.drawable.legend);
		// Bitmap oldBitmap = ((BitmapDrawable) drawable).getBitmap();
		// Bitmap newBitmap = Bitmap.createScaledBitmap(oldBitmap, width,
		// (int) (oldBitmap.getHeight() * width / oldBitmap.getWidth()), true);
		// // imageView.setImageResource(R.drawable.legend);
		// imageView.setImageBitmap(newBitmap);
		PhotoViewAttacher mAttacher = new PhotoViewAttacher(imageView);
		ImageButton closeButton = (ImageButton) mGeoStatePopupWindow
				.getContentView().findViewById(R.id.imageButtonGeoState);
		// Button closeButton = (Button) mGeoStatePopupWindow.getContentView()
		// .findViewById(R.id.buttonGeoState);
		closeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isGeoStatePopup) {
					isGeoStatePopup = false;
					mGeoStatePopupWindow.dismiss();
				}
			}
		});
	}

	Marker infoOpenedMarker = null;

	protected void setupMapIfNeed() {
		startTime = System.currentTimeMillis();
		loadMapBar();
		calculateRuntime("loadMapBar");

		loadGeoState();
		loadHistoryBar();
		// TODO load geostate and map bar in AsyncTask
		// calculateRuntime( "loadGeoState");

		if (mMap == null) {
			mMap = ((SupportMapFragment) mainActivity
					.getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			calculateRuntime("get map");
			// mMap = ((mySupportMapFragment) mainActivity
			// .getSupportFragmentManager().findFragmentById(R.id.map))
			// .getMap();
			// mScrollView = (ScrollView) mainActivity
			// .findViewById(R.id.mainScrollView);
			// ((mySupportMapFragment) mainActivity.getSupportFragmentManager()
			// .findFragmentById(R.id.map))
			// .setListener(new mySupportMapFragment.OnTouchListener() {
			// @Override
			// public void onTouch() {
			// mScrollView
			// .requestDisallowInterceptTouchEvent(true);
			// }
			// });

			if (mMap != null) {
				setupGoogleMapOnCreate();
			}

		}
		// mMap = mapView.getMap();
		// mMap.getUiSettings().setMyLocationButtonEnabled(false);
		// mMap.setMyLocationEnabled(true);
		//
		// // Needs to call MapsInitializer before doing any CameraUpdateFactory
		// // calls
		// try {
		// MapsInitializer.initialize(mainActivity);
		// // } catch (GooglePlayServicesNotAvailableException e) {
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// setupGoogleMapOnCreate();
	}

	private static final LatLng TAIWAN = new LatLng(23.45, 120.68);
	ScrollView mainScrollView;
	TextView[] textViews;

	private void setupGoogleMapOnCreate() {
		float density = mainActivity.getResources().getDisplayMetrics().density;
		int top = (int) (100 * density);
		mMap.setPadding(0, top, 0, 0);

		UiSettings uiSettings = mMap.getUiSettings();
		uiSettings.setMapToolbarEnabled(true);
		
		mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
			@Override
			public View getInfoWindow(Marker marker) {
				// if you return null, it will just use the default window
				return null;
			}

			@Override
			public View getInfoContents(Marker marker) {
				String title = marker.getTitle();
				String content = marker.getSnippet();
				TextView textView = new TextView(mainActivity
						.getApplicationContext());
				// textView.setGravity(Gravity.CENTER);
				if (title == null && content == null) {
					return null;
				} else if (title != null && content != null) {
					textView.setText(title + "\n" + content);
				} else if (title != null) {
					textView.setText(title);
				} else {
					textView.setText(content);
				}
				textView.setTextColor(Color.BLACK);
				// Log.d("Here!", "set marker Info " + textView.getText());
				return textView;
			}
		});

		// // Updates the location and zoom of the MapView
		// CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new
		// LatLng(43.1, -87.9), 10);
		// mMap.animateCamera(cameraUpdate);
		Double lat = Double.parseDouble(eqHashMap.get("lat"));
		Double lng = Double.parseDouble(eqHashMap.get("lng"));
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng),
				8));
		textViews = new TextView[2];
		for (int i = 0; i < textViews.length; ++i) {
			RelativeLayout relativeLayout = (RelativeLayout) mainActivity
					.findViewById(R.id.mapLayout);
			TextView textView = new TextView(mainActivity);
			textViews[i] = textView;
			relativeLayout.addView(textViews[i]);
		}
		// mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
		//
		// @Override
		// public void onCameraChange(CameraPosition arg0) {
		// // checkTag();
		// }
		// });
		mMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				infoOpenedMarker = null;
				Log.d(tag, "marker info window is closed " + arg0.toString());
				if (mainActivity.mPopupWindow.isShowing()) {
					mainActivity.mPopupWindow.dismiss();
				}
			}
		});
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker arg0) {
				infoOpenedMarker = arg0;
				Log.d(tag, "marker is clicked " + arg0.toString());
				if (mainActivity.mPopupWindow.isShowing()) {
					mainActivity.mPopupWindow.dismiss();
				}
				return false;
			}
		});

		// TODO List Adapter with checkbox data
		File chbxFile = mainActivity
				.getFileStreamPath(ConstantVariables.checkboxDataFilename);
		if (chbxFile.exists()) {
			try {
				FileInputStream fis;
				fis = mainActivity
						.openFileInput(ConstantVariables.checkboxDataFilename);
				ObjectInputStream ois = new ObjectInputStream(fis);
				mCheckBoxData = (HashMap<Integer, Boolean>) ois.readObject();
				ois.close();
				fis.close();
			} catch (Exception e) {
				Log.e(tag, "checkBox data file broken");
				chbxFile.delete();
				e.printStackTrace();
				mCheckBoxData = new HashMap<Integer, Boolean>();
				for (int i = 0; i < 40; ++i) {
					mCheckBoxData.put((Integer) i, false);
				}
				mCheckBoxData.put(100, true); // CWB
				mCheckBoxData.put(101, false); // gCap
				mCheckBoxData.put(102, false); // P-alert

				// 0828 add all volleyballs
				// mCheckBoxData.put(200, true);// CMTs
				mCheckBoxData.put(201, true);// gCAP
				mCheckBoxData.put(202, false);// BATS
				mCheckBoxData.put(203, false);// New_BATS
				mCheckBoxData.put(204, false);// FMNEAR

				mCheckBoxData.put(0, true);
				mCheckBoxData.put(12, true);
				// mCheckBoxData.put(2, true);

				// checkBox5.setChecked(true);
				mCheckBoxData.put(30, false);
			}
		} else {
			mCheckBoxData = new HashMap<Integer, Boolean>();
			for (int i = 0; i < 40; ++i) {
				mCheckBoxData.put((Integer) i, false);
			}
			mCheckBoxData.put(100, true); // CWB
			mCheckBoxData.put(101, false); // gCap
			mCheckBoxData.put(102, false); // P-alert

			// 0828 add all volleyballs
			// mCheckBoxData.put(200, true);// CMTs
			mCheckBoxData.put(201, true);// gCAP
			mCheckBoxData.put(202, false);// BATS
			mCheckBoxData.put(203, false);// New_BATS
			mCheckBoxData.put(204, false);// FMNEAR

			mCheckBoxData.put(0, true);
			mCheckBoxData.put(12, true);
			// mCheckBoxData.put(2, true);

			// checkBox5.setChecked(true);
			mCheckBoxData.put(30, false);
		}

		Log.d(tag, "mCheckBoxData:" + mCheckBoxData.toString());

		setMapTool();
		calculateRuntime("set map tool");
		mapOverlay = new MapOverlay(mainActivity, mMap);

		mapOverlay.setEarthquakeMarker();
		mainActivity.mapOverlay = mapOverlay;
		calculateRuntime("set map overlay");
		setHandler();
		calculateRuntime("set Handler for checkTag after resource load finish");
	}

	Handler handler;
	final int LOAD_RESOURCE_FINISHED = ConstantVariables.LOAD_RESOURCE_FINISHED;

	protected void setHandler() {
		handler = new Handler(Looper.getMainLooper()) {

			public void handleMessage(Message msg) {
				switch (msg.what) {
				// 當收到的Message的代號為我們剛剛訂的代號就做下面的動作。
				case LOAD_RESOURCE_FINISHED:
					calculateRuntime("first check tag");
					checkTag();
					break;

				}
				super.handleMessage(msg);
			}

		};
		mapOverlay.setHandler(handler);
	}

	// protected void drawPolylines() {
	// for (int i = 0; i < textViews.length; ++i) {
	// RelativeLayout relativeLayout = (RelativeLayout) mainActivity
	// .findViewById(R.id.mapLayout);
	// relativeLayout.removeView(textViews[i]);
	// }
	// LatLngBounds curScreen =
	// mMap.getProjection().getVisibleRegion().latLngBounds;
	// LatLng northEast = curScreen.northeast;
	// LatLng southWest = curScreen.southwest;
	// double dNS = northEast.latitude - southWest.latitude;
	// double dEW = northEast.longitude - southWest.longitude;
	// Log.d(tag, "d(NS,EW) = (" + dNS + "," + dEW + ")");
	// double logNS = Math.log10(dNS / 4);
	// double logEW = Math.log10(dEW / 4);
	// Log.d(tag, "log(NS,EW) = (" + logNS + "," + logEW + ")");
	// int scaleNS = (int) Math.floor(logNS);
	// int scaleEW = (int) Math.floor(logEW);
	// Log.d(tag, "scale(NS,EW) = (" + scaleNS + "," + scaleEW + ")");
	// double xNS = logNS - scaleNS;
	// double xEW = logEW - scaleEW;
	// double dLineNS, dLineEW;
	// if (xNS > 0.699) {
	// dLineNS = Math.pow(10, scaleNS);
	// dLineNS = dLineNS * 5;
	// } else if (xNS > 0.301) {
	// dLineNS = Math.pow(10, scaleNS);
	// dLineNS = dLineNS * 2;
	// } else {
	// dLineNS = Math.pow(10, scaleNS);
	// dLineNS = dLineNS * 1;
	// }
	// if (xEW > 0.699) {
	// dLineEW = Math.pow(10, scaleEW);
	// dLineEW = dLineEW * 5;
	// } else if (xEW > 0.301) {
	// dLineEW = Math.pow(10, scaleEW);
	// dLineEW = dLineEW * 2;
	// } else {
	// dLineEW = Math.pow(10, scaleEW);
	// dLineEW = dLineEW * 1;
	// }
	// Log.d(tag, "dLine(NS,EW) = (" + dLineNS + "," + dLineEW + ")");
	// int s = (int) Math.floor(southWest.latitude);
	// int n = (int) Math.ceil(northEast.latitude);
	// int e = (int) Math.ceil(northEast.longitude);
	// int w = (int) Math.floor(southWest.longitude);
	// if (s > n) {
	// int tmp = s;
	// s = n;
	// n = tmp;
	// }
	// if (w > e) {
	// int tmp = w;
	// w = e;
	// e = tmp;
	// }
	// Projection projection = mMap.getProjection();
	// textViews = new TextView[(int) ((n - s) / dLineNS) + 1
	// + (int) ((e - w) / dLineEW) + 1];
	// int textViewsOffset = 0;
	// for (double i = s; i < n; i = i + dLineNS) {
	// PolylineOptions polylineOptions = new PolylineOptions().width(1)
	// .color(Color.DKGRAY);
	// polylineOptions.add(new LatLng(i, e));
	// polylineOptions.add(new LatLng(i, w));
	// mMap.addPolyline(polylineOptions);
	// Point screenPosition = projection.toScreenLocation(new LatLng(i, w
	// + dLineEW));
	//
	// TextView textView = new TextView(mainActivity);
	// textView.setText(Double.toString(i));
	// textView.setX(screenPosition.x);
	// textView.setY(screenPosition.y);
	// RelativeLayout relativeLayout = (RelativeLayout) mainActivity
	// .findViewById(R.id.mapLayout);
	// relativeLayout.addView(textView);
	// textViews[textViewsOffset++] = textView;
	// }
	// for (double j = w; j < e; j = j + dLineEW) {
	// PolylineOptions polylineOptions = new PolylineOptions().width(1)
	// .color(Color.DKGRAY);
	// polylineOptions.add(new LatLng(n, j));
	// polylineOptions.add(new LatLng(s, j));
	// mMap.addPolyline(polylineOptions);
	// Point screenPosition = projection.toScreenLocation(new LatLng(n
	// - dLineNS, j));
	//
	// TextView textView = new TextView(mainActivity);
	// if (j > w + dLineEW)
	// textView.setText(Double.toString(j));
	// textView.setX(screenPosition.x);
	// textView.setY(screenPosition.y);
	// RelativeLayout relativeLayout = (RelativeLayout) mainActivity
	// .findViewById(R.id.mapLayout);
	// relativeLayout.addView(textView);
	// textViews[textViewsOffset++] = textView;
	//
	// }
	//
	// }

	CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6,
			checkBox7;
	Button button4;
	// Spinner spinnerCh1, spinnerCh2;
	boolean isGeoStatePopup = false;
	SeekBar seekBarCh1;
	int seekBarProgress;
	// , seekBarCh2;
	int lastProgress = 0;
	String[] geoMapStrings = { "CWB", "gCAP", "P-alert" };
	// checkBox data 100,101,102
	int geoMapStringsSelcted = 0;
	String[] ballStrings = { "gCAP", "BATS", "Auto_BATS", "FMNEAR" };
	// checkbox data 201,202,203,204
	int ballStringSelcted = 0;
	myArrayAdapter<String> arrayAdapterCh1, arrayAdapterCh2;

	Button spinnerBtn1, spinnerBtn2;

	/*
	 * checkBox1:震度圖 checkBox2:地質圖 checkBox3:活動斷層 checkBox4:震間變形 checkBox5:震源機制
	 * checkBox6:衛星地圖 checkbox7:背景地震 button4:地質圖圖示
	 */
	private void setMapTool() {

		checkBox1 = (CheckBox) mPopupWindow.getContentView().findViewById(
				R.id.checkBox1);
		checkBox2 = (CheckBox) mPopupWindow.getContentView().findViewById(
				R.id.checkBox2);
		checkBox3 = (CheckBox) mPopupWindow.getContentView().findViewById(
				R.id.checkBox3);
		checkBox4 = (CheckBox) mPopupWindow.getContentView().findViewById(
				R.id.checkBox4);
		checkBox5 = (CheckBox) mPopupWindow.getContentView().findViewById(
				R.id.checkBox5);
		checkBox6 = (CheckBox) mPopupWindow.getContentView().findViewById(
				R.id.checkBox6);
		checkBox7 = (CheckBox) mPopupWindow.getContentView().findViewById(
				R.id.checkBox7);
		// button1 = (Button) mainActivity.findViewById(R.id.button1);
		// button2 = (Button) mainActivity.findViewById(R.id.button2);
		// button3 = (Button) mainActivity.findViewById(R.id.button3);
		button4 = (Button) mainActivity.findViewById(R.id.button4);
		seekBarCh1 = (SeekBar) mainActivity.findViewById(R.id.seekBar1);
		// seekBarCh2 = (SeekBar) mainActivity.findViewById(R.id.seekBar2);
		// spinnerCh1 = (Spinner) mainActivity.findViewById(R.id.spinner1);
		// spinnerCh2 = (Spinner) mainActivity.findViewById(R.id.spinner2);

		spinnerBtn1 = (Button) mainActivity.findViewById(R.id.buttonSpinner1);
		spinnerBtn2 = (Button) mainActivity.findViewById(R.id.buttonSpinner2);

		arrayAdapterCh1 = new myArrayAdapter<String>(mainActivity,
				android.R.layout.simple_spinner_dropdown_item, geoMapStrings);
		arrayAdapterCh2 = new myArrayAdapter<String>(mainActivity,
				android.R.layout.simple_spinner_dropdown_item, ballStrings);

		// TODO set button available when geomap is checked
		button4.setVisibility(View.INVISIBLE);
		seekBarCh1.setVisibility(View.INVISIBLE);
		// seekBarCh2.setVisibility(View.INVISIBLE);
		// spinnerCh1.setVisibility(View.INVISIBLE);
		// spinnerCh2.setVisibility(View.INVISIBLE);
		spinnerBtn1.setVisibility(View.INVISIBLE);
		spinnerBtn2.setVisibility(View.INVISIBLE);

		checkBox1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					mCheckBoxData.put(3, true);
					// spinnerCh1.setVisibility(View.VISIBLE);
					seekBarCh1.setVisibility(View.VISIBLE);
					spinnerBtn1.setVisibility(View.VISIBLE);

				} else {
					mCheckBoxData.put(3, false);
					// spinnerCh1.setVisibility(View.INVISIBLE);
					seekBarCh1.setVisibility(View.INVISIBLE);
					spinnerBtn1.setVisibility(View.INVISIBLE);
				}
				checkTag();
			}
		});
		checkBox2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					mCheckBoxData.put(10, true);
					// seekBarCh2.setVisibility(View.VISIBLE);
				} else {
					mCheckBoxData.put(10, false);
					// seekBarCh2.setVisibility(View.INVISIBLE);
				}
				checkTag();
			}
		});

		checkBox3.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					mCheckBoxData.put(12, true);
				} else {
					mCheckBoxData.put(12, false);
				}
				checkTag();
			}
		});
		checkBox4.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					mCheckBoxData.put(11, true);
				} else {
					mCheckBoxData.put(11, false);
				}
				checkTag();
			}
		});
		checkBox5.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					mCheckBoxData.put(2, true);
					// spinnerCh2.setVisibility(View.VISIBLE);
					spinnerBtn2.setVisibility(View.VISIBLE);
				} else {
					mCheckBoxData.put(2, false);
					// spinnerCh2.setVisibility(View.INVISIBLE);
					spinnerBtn2.setVisibility(View.INVISIBLE);
				}
				checkTag();
			}
		});

		checkBox6.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				} else {
					mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				}
				checkTag();
			}
		});

		checkBox7.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					mCheckBoxData.put(5, true);
				} else {
					mCheckBoxData.put(5, false);
				}
				checkTag();
			}
		});

		button4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isGeoStatePopup) {
					mGeoStatePopupWindow.dismiss();
					isGeoStatePopup = false;
				} else {
					mGeoStatePopupWindow.update();
					// mGeoStatePopupWindow.showAsDropDown(mainActivity
					// .findViewById(R.id.button4));
					LinearLayout linearLayout = (LinearLayout) mainActivity
							.findViewById(R.id.linearLayoutSummary);
					mGeoStatePopupWindow.showAtLocation(linearLayout,
							Gravity.BOTTOM, 0, 0);
					isGeoStatePopup = true;
				}
			}
		});

		seekBarCh1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				checkTag();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// checkTag();
				if (progress <= 12) {
					seekBarCh1.setProgress(0);
				} else if (progress > 12 && progress <= 37) {
					seekBarCh1.setProgress(25);
				} else if (progress > 37 && progress <= 62) {
					seekBarCh1.setProgress(50);
				} else if (progress > 62 && progress <= 87) {
					seekBarCh1.setProgress(75);
				} else if (progress > 87) {
					seekBarCh1.setProgress(100);
				}
				seekBarProgress = seekBar.getMax() - seekBar.getProgress();
			}
		});

		// seekBarCh2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
		//
		// @Override
		// public void onStopTrackingTouch(SeekBar seekBar) {
		// }
		//
		// @Override
		// public void onStartTrackingTouch(SeekBar seekBar) {
		// }
		//
		// @Override
		// public void onProgressChanged(SeekBar seekBar, int progress,
		// boolean fromUser) {
		// checkTag();
		// }
		// });

		if (mCheckBoxData.get(100)) {
			spinnerBtn1.setText(geoMapStrings[0]);
		} else if (mCheckBoxData.get(101)) {
			spinnerBtn1.setText(geoMapStrings[1]);
		} else if (mCheckBoxData.get(102)) {
			spinnerBtn1.setText(geoMapStrings[2]);
		}

		spinnerBtn1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(mainActivity)
						.setTitle("震度圖選項")
						.setAdapter(arrayAdapterCh1,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										spinnerBtn1
												.setText(geoMapStrings[which]);
										mCheckBoxData.put(100, false); // CWB
										mCheckBoxData.put(101, false); // gCap
										mCheckBoxData.put(102, false); // P-alert
										switch (which) {
										case 0:
											mCheckBoxData.put(100, true);
											break;
										case 1:
											mCheckBoxData.put(101, true);
											break;
										case 2:
											mCheckBoxData.put(102, true);
											break;
										default:
											break;
										}
										checkTag();
										dialog.dismiss();
									}
								}).create().show();

			}
		});

		if (mCheckBoxData.get(201)) {
			spinnerBtn2.setText(ballStrings[0]);
		} else if (mCheckBoxData.get(202)) {
			spinnerBtn2.setText(ballStrings[1]);
		} else if (mCheckBoxData.get(203)) {
			spinnerBtn2.setText(ballStrings[2]);
		} else if (mCheckBoxData.get(204)) {
			spinnerBtn2.setText(ballStrings[3]);
		}
		spinnerBtn2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(mainActivity)
						.setTitle("震源機制選項")
						.setAdapter(arrayAdapterCh2,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										spinnerBtn2.setText(ballStrings[which]);

										mCheckBoxData.put(201, false);// gCAP
										mCheckBoxData.put(202, false);// BATS
										mCheckBoxData.put(203, false);// New_BATS
										mCheckBoxData.put(204, false);// FMNEAR
										switch (which) {
										case 0:
											mCheckBoxData.put(201, true);
											break;
										case 1:
											mCheckBoxData.put(202, true);
											break;
										case 2:
											mCheckBoxData.put(203, true);
											break;
										case 3:
											mCheckBoxData.put(204, true);
											break;

										default:
											break;
										}
										checkTag();

										dialog.dismiss();
									}
								}).create().show();

			}
		});

		// spinnerCh2.setAdapter(arrayAdapterCh2);
		//
		// if (mCheckBoxData.get(201)) {
		// spinnerCh2.setSelection(0);
		// } else if (mCheckBoxData.get(202)) {
		// spinnerCh2.setSelection(1);
		// } else if (mCheckBoxData.get(203)) {
		// spinnerCh2.setSelection(2);
		// } else if (mCheckBoxData.get(204)) {
		// spinnerCh2.setSelection(3);
		// }

		// spinnerCh2.setPrompt("-震源機制-");
		// spinnerCh2.setOnItemSelectedListener(new OnItemSelectedListener() {
		//
		// @Override
		// public void onItemSelected(AdapterView<?> parent, View view,
		// int position, long id) {
		// mCheckBoxData.put(201, false);// gCAP
		// mCheckBoxData.put(202, false);// BATS
		// mCheckBoxData.put(203, false);// New_BATS
		// mCheckBoxData.put(204, false);// FMNEAR
		// switch (position) {
		// case 0:
		// mCheckBoxData.put(201, true);
		// break;
		// case 1:
		// mCheckBoxData.put(202, true);
		// break;
		// case 2:
		// mCheckBoxData.put(203, true);
		// break;
		// case 3:
		// mCheckBoxData.put(204, true);
		// break;
		//
		// default:
		// break;
		// }
		// checkTag();
		// }
		//
		// @Override
		// public void onNothingSelected(AdapterView<?> parent) {
		//
		// }
		// });

	}

	/*
	 * CheckBox id = group*10+child Group 0 child 0:最新地震資訊 child 1:動畫顯示
	 * child2:震源機制 child 3:震度圖 child 4:即時震度圖 child 5:背景地震 Group 1 child 0:地質圖
	 * child 1:震間變形 child2:活動斷層 child 3:寬頻測站 Group 2 child 0:道路地圖 child 1:衛星地圖
	 * child 2:地形圖 child0:座標線
	 */
	protected HashMap<Integer, Boolean> mCheckBoxData;

	/*
	 * checkBox1:震度圖 checkBox2:地質圖 checkBox3:活動斷層 checkBox4:震間變形 checkBox5:震源機制
	 * checkBox6:衛星地圖 button1:CWB button2:gCAP button3:P-alert button4:地質圖圖示
	 */
	public synchronized void checkTag() {
		mMap.clear();

		// mCheckBoxData = mSetupGoogleMapTool.mListAdapter.mCheckBoxData;
		// Log.d("Here!", "In setGM Checkbox data:" + mCheckBoxData.toString());

		if (mCheckBoxData.get(10)) { // 地質圖
			checkBox2.setChecked(true);
			if (mapOverlay.isloadResourceFinished) {
				if (summaryActivity.isConnected(mainActivity
						.getApplicationContext())) {
					mapOverlay.draw(1, (float) 0);
					// mMap.addTileOverlay(mapOverlay.tileOverlayOptions2);
					// mMap.addTileOverlay(mapOverlay.tileOverlayOptions);
				} else {
					mapOverlay.draw(2, (float) 0);

				}

			}
			button4.setVisibility(View.VISIBLE);
			// mapOverlay.draw(2, (float) seekBarCh2.getProgress() / 100);
		} else {
			button4.setVisibility(View.INVISIBLE);
			mapOverlay.isDraw2 = false;
		}

		if (mCheckBoxData.get(3)) { // 震度圖
			Log.d("Here!", "Draw geo map now.");
			checkBox1.setChecked(true);
			// spinnerCh1.setVisibility(View.VISIBLE);
			spinnerBtn1.setVisibility(View.VISIBLE);
			seekBarCh1.setVisibility(View.VISIBLE);
			seekBarCh1.setProgress(100 - seekBarProgress);
			// seekBar.setVisibility(View.VISIBLE);
			if (mCheckBoxData.get(100)) {
				mapOverlay.draw(10, (float) seekBarProgress / 100);
			} else if (mCheckBoxData.get(101)) {
				mapOverlay.draw(9, (float) seekBarProgress / 100);
			} else if (mCheckBoxData.get(102)) {
				mapOverlay.draw(15, (float) seekBarProgress / 100);
			}
		} else {
			// mCheckBoxData.put(100, false); // CWB
			// mCheckBoxData.put(101, false); // gCap
			// mCheckBoxData.put(102, false); // P-alert
			mapOverlay.isDraw9 = false;
			mapOverlay.isDraw10 = false;
			mapOverlay.isDraw15 = false;
		}

		if (mCheckBoxData.get(2)) { // 震源機制
			checkBox5.setChecked(true);
			// spinnerCh2.setVisibility(View.VISIBLE);
			spinnerBtn2.setVisibility(View.VISIBLE);
			mImageView.setVisibility(ImageView.INVISIBLE);

			if (mCheckBoxData.get(201)) {
				mapOverlay.draw(17, (float) 0);
			} else if (mCheckBoxData.get(202)) {
				mapOverlay.draw(18, (float) 0);
			} else if (mCheckBoxData.get(203)) {
				mapOverlay.draw(19, (float) 0);
			} else if (mCheckBoxData.get(204)) {
				mapOverlay.draw(20, (float) 0);
			}

		} else {
			// mapOverlay.draw(1, (float) 0);
			if (!mCheckBoxData.get(5)) {
				mImageView.setVisibility(ImageView.VISIBLE);
				mapOverlay.draw(7, (float) 0);
			}
			mapOverlay.isDraw17 = false;
			mapOverlay.isDraw18 = false;
			mapOverlay.isDraw19 = false;
			mapOverlay.isDraw20 = false;
			// mCheckBoxData.put(201, false);// gCAP
			// mCheckBoxData.put(202, false);// BATS
			// mCheckBoxData.put(203, false);// New_BATS
			// mCheckBoxData.put(204, false);// FMNEAR

		}

		if (mCheckBoxData.get(11)) { // 震間變形
			checkBox4.setChecked(true);
			mapOverlay.draw(11, (float) 0);
		}
		if (mCheckBoxData.get(12)) { // 活動斷層
			checkBox3.setChecked(true);
			mapOverlay.draw(3, (float) 0);
		}

		// if (mCheckBoxData.get(30)) { // 0811 add polyline
		// drawPolylines();
		// }
		if (infoOpenedMarker != null) {
			Log.d(tag, "checkTag:showInfoWindow " + infoOpenedMarker);
			infoOpenedMarker.showInfoWindow();
		}

		if (mCheckBoxData.get(5)) { // 背景地震

			historyImageView.setVisibility(View.VISIBLE);
//			sizeImageView.setVisibility(View.VISIBLE);
			checkBox7.setChecked(true);
			if (mapOverlay.isloadResourceFinished) {
				if (summaryActivity.isConnected(mainActivity
						.getApplicationContext())) {
					mapOverlay.draw(21, (float) 0);
				} else {
					mapOverlay.draw(22, (float) 0);

				}

			}
			// mapOverlay.draw(22, (float) 0);
			if (!mCheckBoxData.get(2)) {
				mImageView.setVisibility(ImageView.INVISIBLE);
				// set Marker Icon to star
				mapOverlay.draw(23, (float) 0);
			}
		} else {
			historyImageView.setVisibility(View.INVISIBLE);
//			sizeImageView.setVisibility(View.INVISIBLE);
			if (!mCheckBoxData.get(2)) {
				mImageView.setVisibility(ImageView.VISIBLE);
				mapOverlay.draw(7, (float) 0);
			}
		}

	}

}

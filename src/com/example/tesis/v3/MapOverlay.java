package com.example.tesis.v3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.lang.Math;

import android.R.integer;
import android.R.string;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

public class MapOverlay implements Serializable {
	private static final long serialVersionUID = 123456789;
	GoogleMap mMap;
	public Point[] points = new Point[4216];
	// MainActivity fActivity;
	summaryActivity fActivity;
	AssetManager mngr;
	public GroundOverlayOptions mapBar;
	public GroundOverlayOptions geoMap;
	public PolylineOptions[] pOptions = new PolylineOptions[100];
	private int pOptionsNum = 0;
	HashMap<String, String> hMap;
	final String tag = "myTag";
	final String timeTag = "timeTag";
	final String AsyncTaskTag = "AsyncTaskTag";
	long startTime, endTime;
	// public downloadBall TaskDownloadVolleyball;
	public downloadMarkerInfo TaskDownloadMarkerInfo;
	public loadResources TaskLoadResource;
	// public AsyncTask TaskLoadResource, TaskDownloadMarkerInfo,
	// TaskDownloadVolleyball;

	final int zIndex_Polyline = 5;
	final int zIndex_tileOverlay = 0;
	// final int zIndex_marker = 10;
	final int zIndex_groundOverlay = 3;

	// public MapOverlay(MainActivity fActivity,GoogleMap mMap){
	// this.mMap = mMap;
	// mngr = fActivity.getAssets();
	// this.fActivity = fActivity;
	// loadData1(); //活動斷層
	// loadData2(); //map bar
	// loadData3(); //地質圖
	// loadData4(); //BATS,CWB Stations
	// loadData5(); //vector*/
	// }
	private void calculateRuntime(String info) {
		endTime = System.currentTimeMillis();
		Log.d(timeTag, "In MapOverlay: " + info + ": " + (endTime - startTime));
		startTime = System.currentTimeMillis();
	}

	public MapOverlay(summaryActivity mainActivity, GoogleMap mMap) {
		this.mMap = mMap;
		mngr = mainActivity.getAssets();
		this.fActivity = mainActivity;
		hMap = mainActivity.eqHashMap;
		startTime = System.currentTimeMillis();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			TaskLoadResource = (loadResources) new loadResources()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			TaskLoadResource = (loadResources) new loadResources().execute(0);

		calculateRuntime("loadData resource in background");
	}

	// public void setMap(GoogleMap map, MainActivity fActivity){
	// this.fActivity = fActivity;
	// this.mMap = map;
	// }

	public void setMap(GoogleMap map, summaryActivity fActivity) {
		this.fActivity = fActivity;
		this.mMap = map;
	}

	final String[] lineName = ConstantVariables.lineName2;
	/*
	 * int[] ODindex={0,1,2,16,21,22,23,25,26,27,28,29,31,36,37}; int[]
	 * OSindex={12}; int[] BDindex; int[] BSindex; int[]
	 * RDindex={3,5,6,7,8,10,17,24,30,32,33,34,35};
	 */
	// None 0,OD 1,OS 2,BD 3,BS 4,RD 5
	final int[] lineColor = ConstantVariables.lineColor2;
	MarkerOptions[] lineMarkerOptions;
	CircleOptions[] lineCircleOptions;

	public void loadData1() { // 活動斷層
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(mngr.open("fault2014_convert_2.txt")));
			Log.d("Here!!", "Open File success!");
			String s;
			int i = 0;
			int gi = 0;
			while ((s = bufferedReader.readLine()) != null) {
//				Log.d("Here!!",s);
				if (s.equals("X")) {
//					Log.d("Here!!","gi="+gi);
					gi++;
				} else {
					StringTokenizer st = new StringTokenizer(s);
					points[i] = new Point();
					points[i].index = i;
					points[i].groupIndex = gi;
					points[i].latLng = new LatLng(Double.parseDouble(st
							.nextToken()), Double.parseDouble(st.nextToken()));
					i++;
				}

			}
			bufferedReader.close();
		} catch (Exception e) {
			Log.d("Here!!", "IOException!");
			e.printStackTrace();
		}
		int scale = 20;
		lineMarkerOptions = new MarkerOptions[points.length / scale + 1];
		Log.d(tag, "lineMarkerOptions length:" + points.length / 20);
		pOptions[pOptionsNum] = new PolylineOptions().width(5)
				.color(Color.rgb(255, 153, 0)).zIndex(zIndex_Polyline);
        BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.small_red_5);
		for (int i = 0; i < points.length - 1; ++i) {
			if (i % scale == 0) {
				int index = points[i].groupIndex - 1;
				String icon_name = null;
				switch (lineColor[index]) {
				case 0:
					icon_name = "small_red_5.png";
                    bd = BitmapDescriptorFactory.fromResource(R.drawable.small_red_5);
					break;
				case 1:
					icon_name = "small_orange_5.png";
                    bd = BitmapDescriptorFactory.fromResource(R.drawable.small_orange_5);
					break;
				case 2:
					icon_name = "small_orange_5.png";
                    bd = BitmapDescriptorFactory.fromResource(R.drawable.small_orange_5);
					break;
				case 3:
					icon_name = "small_black_5.png";
                    bd = BitmapDescriptorFactory.fromResource(R.drawable.small_black_5);
					break;
				case 4:
					icon_name = "small_black_5.png";
                    bd = BitmapDescriptorFactory.fromResource(R.drawable.small_black_5);
					break;
				case 5:
					icon_name = "small_red_5.png";
                    bd = BitmapDescriptorFactory.fromResource(R.drawable.small_red_5);
					break;
				default:
					break;
				}
				String content = new String(lineName[points[i].groupIndex - 1]);
				MarkerOptions markerOption = new MarkerOptions()
						.position(points[i].latLng).anchor(0.5f, 0.5f)
						.icon(bd)
						// .snippet(
						// // i + "\n" + points[i].groupIndex + "\n" +
						// "(" + points[i].latLng.latitude + ","
						// + points[i].latLng.longitude + ")")
						.title(content);
//				Log.d(tag, "index:" + i / 20);
				lineMarkerOptions[i / scale] = markerOption;
			}

			if (points[i].groupIndex == points[i + 1].groupIndex) {
				// pOptions[pOptionsNum].add(points[i].latLng,
				// points[i + 1].latLng);
				pOptions[pOptionsNum].add(points[i].latLng);
			} else {
				pOptions[pOptionsNum].add(points[i].latLng);
				pOptionsNum++;
				int index = points[i + 1].groupIndex - 1;
				switch (lineColor[index]) {
				case 0:
					pOptions[pOptionsNum] = new PolylineOptions().width(5)
							.color(Color.RED).zIndex(zIndex_Polyline);
					break;
				case 1:
					pOptions[pOptionsNum] = new PolylineOptions().width(5)
							.color(Color.rgb(255, 153, 0))
							.zIndex(zIndex_Polyline);
					break;
				case 2:
					pOptions[pOptionsNum] = new PolylineOptions().width(5)
							.color(Color.rgb(255, 153, 0))
							.zIndex(zIndex_Polyline);
					break;
				case 3:
					pOptions[pOptionsNum] = new PolylineOptions().width(5)
							.color(Color.BLACK).zIndex(zIndex_Polyline);
					break;
				case 4:
					pOptions[pOptionsNum] = new PolylineOptions().width(5)
							.color(Color.BLACK).zIndex(zIndex_Polyline);
					break;
				case 5:
					pOptions[pOptionsNum] = new PolylineOptions().width(5)
							.color(Color.RED).zIndex(zIndex_Polyline);
					break;
				default:
					break;
				}

			}
		}
	}

	// public void loadData2() { // map bar
	// BitmapDescriptor image;
	// LatLngBounds bounds;
	// image = BitmapDescriptorFactory.fromAsset("ml_map_bar_new_trans.png");
	// bounds = new LatLngBounds(new LatLng(21.6, 119.15), new LatLng(24.3,
	// 120.0));
	// mapBar = new GroundOverlayOptions().image(image)
	// .positionFromBounds(bounds).transparency((float) 0)
	// .zIndex(zIndex_groundOverlay);
	// }

	public TileOverlayOptions tileOverlayOptions, tileOverlayOptions2;
	public GroundOverlayOptions islandTurtle, islandGreen, islandLanYu,
			islandLiuChiu, islandPongHu;

	public void loadData3() { // 地質圖
		BitmapDescriptor image;
		LatLngBounds bounds;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		// XXX sampleSize = 1 will crush, out of memory...
		try {
			Bitmap img = BitmapFactory.decodeStream(
					mngr.open("TWgeomap_revise_small.png"), null, opts);
			image = BitmapDescriptorFactory.fromBitmap(img);
			bounds = new LatLngBounds(new LatLng(21.895, 119.314), new LatLng(
					25.298, 121.992));
			geoMap = new GroundOverlayOptions().image(image)
					.positionFromBounds(bounds).transparency((float) 0)
					.zIndex(zIndex_groundOverlay);

			// / 1105 add island independently
			img = BitmapFactory.decodeStream(mngr.open("island_green.png"));
			image = BitmapDescriptorFactory.fromBitmap(img);
			bounds = new LatLngBounds(new LatLng(22.632, 121.463), new LatLng(
					22.682, 121.513));
			islandGreen = new GroundOverlayOptions().image(image)
					.positionFromBounds(bounds).transparency((float) 0)
					.zIndex(zIndex_groundOverlay);

			img = BitmapFactory.decodeStream(mngr.open("island_lanYu.png"));
			image = BitmapDescriptorFactory.fromBitmap(img);
			bounds = new LatLngBounds(new LatLng(21.945, 121.499), new LatLng(
					22.094, 121.631));
			islandLanYu = new GroundOverlayOptions().image(image)
					.positionFromBounds(bounds).transparency((float) 0)
					.zIndex(zIndex_groundOverlay);

			img = BitmapFactory.decodeStream(mngr.open("island_liuchiu.png"));
			image = BitmapDescriptorFactory.fromBitmap(img);
			bounds = new LatLngBounds(new LatLng(22.321, 120.352), new LatLng(
					22.356, 120.390));
			islandLiuChiu = new GroundOverlayOptions().image(image)
					.positionFromBounds(bounds).transparency((float) 0)
					.zIndex(zIndex_groundOverlay);

			img = BitmapFactory.decodeStream(mngr.open("island_pongHu.png"),
					null, opts);
			image = BitmapDescriptorFactory.fromBitmap(img);
			bounds = new LatLngBounds(new LatLng(23.186, 119.311), new LatLng(
					23.799, 119.698));
			islandPongHu = new GroundOverlayOptions().image(image)
					.positionFromBounds(bounds).transparency((float) 0)
					.zIndex(zIndex_groundOverlay);

			img = BitmapFactory.decodeStream(mngr.open("island_turtle.png"));
			image = BitmapDescriptorFactory.fromBitmap(img);
			bounds = new LatLngBounds(new LatLng(24.836, 121.935), new LatLng(
					24.857, 121.965));
			islandTurtle = new GroundOverlayOptions().image(image)
					.positionFromBounds(bounds).transparency((float) 0)
					.zIndex(zIndex_groundOverlay);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("Here!!", "catch draw 2 Exception!");
		}
		// tileOverlayOptions2 = new TileOverlayOptions()
		// .tileProvider(new CustomGeoMapTileProvider(mngr));
		tileOverlayOptions2 = new TileOverlayOptions().tileProvider(
				new myUrlTileProvider(fActivity.getApplicationContext()))
				.zIndex(zIndex_tileOverlay);
	}

	public ArrayList<MarkerOptions> batsList, cwbList;

	// public void loadData4() { // BATS Stations
	// batsList = new ArrayList<MarkerOptions>();
	// cwbList = new ArrayList<MarkerOptions>();
	// try {
	// BufferedReader bufferedReader = new BufferedReader(
	// new InputStreamReader(mngr.open("BATS_Stations_new.txt")));
	// String s = bufferedReader.readLine();
	// // Station Location Latitude Longitude Elevation
	// // Geology Equipment Communication Operating Network Station_Type
	//
	// while ((s = bufferedReader.readLine()) != null) {
	//
	// HashMap<String, String> hMap = new HashMap<String, String>();
	// StringTokenizer st = new StringTokenizer(s);
	// hMap.put("Station", st.nextToken());
	// hMap.put("Location", st.nextToken());
	// hMap.put("Latitude", st.nextToken());
	// hMap.put("Longitude", st.nextToken());
	// hMap.put("Elevation", st.nextToken());
	// hMap.put("Geology", st.nextToken());
	// hMap.put("Equipment", st.nextToken());
	// hMap.put("Communication", st.nextToken());
	// hMap.put("Operating", st.nextToken());
	// hMap.put("Network", st.nextToken());
	// hMap.put("Station_Type", st.nextToken());
	// Double lat = Double.parseDouble(hMap.get("Latitude"));
	// Double lng = Double.parseDouble(hMap.get("Longitude"));
	// String title = "Station:" + hMap.get("Station");
	// String content = "Geology:" + hMap.get("Geology");
	// // Log.d("Here!!",hMap.toString());
	// // Log.d("Here!!",hMap.get("Station_Type"));
	// if (hMap.get("Station_Type").equals("BATS")) {
	// BitmapDescriptor image = BitmapDescriptorFactory
	// .fromAsset("red.png");
	// MarkerOptions markerOption = new MarkerOptions()
	// .position(new LatLng(lat, lng)).anchor(0.5f, 0.5f)
	// .title(title).icon(image).snippet(content);
	// batsList.add(markerOption);
	// } else {
	// BitmapDescriptor image = BitmapDescriptorFactory
	// .fromAsset("blue.png");
	// MarkerOptions markerOption = new MarkerOptions()
	// .position(new LatLng(lat, lng)).anchor(0.5f, 0.5f)
	// .title(title).icon(image).snippet(content);
	// cwbList.add(markerOption);
	// // Log.d("Here!!","cwbList"+ cwbList.size());
	// }
	//
	// }
	// bufferedReader.close();
	// } catch (Exception e) {
	// Log.d("Here!!", e.toString());
	// e.printStackTrace();
	// }
	// }

	public ArrayList<HashMap<String, String>> vectorList;
	int VECTOR_NUM = 826;
	private PolylineOptions[] vectorOptionsList = new PolylineOptions[VECTOR_NUM];
	int vectorOptionsNum;

	public void loadData5() { // vector
		vectorList = new ArrayList<HashMap<String, String>>();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(mngr.open("S01R_2007.S0.5_2")));
			String s;
			vectorOptionsNum = 0;
			while ((s = bufferedReader.readLine()) != null) {
				HashMap<String, String> hMap = new HashMap<String, String>();
				StringTokenizer st = new StringTokenizer(s);
				hMap.put("lng1", st.nextToken());
				hMap.put("lat1", st.nextToken());
				hMap.put("lng2", st.nextToken());
				hMap.put("lat2", st.nextToken());
				hMap.put("lng3", st.nextToken());
				hMap.put("lat3", st.nextToken());
				hMap.put("lng4", st.nextToken());
				hMap.put("lat4", st.nextToken());
				// Vn,dVn,Ve,dVe,Vz,dVz
				hMap.put("Vn", st.nextToken());
				hMap.put("dVn", st.nextToken());
				hMap.put("Ve", st.nextToken());
				hMap.put("dVe", st.nextToken());
				hMap.put("Vz", st.nextToken());
				hMap.put("dVz", st.nextToken());

				vectorList.add(hMap);
				Double lat1 = Double.parseDouble(hMap.get("lat1"));
				Double lat2 = Double.parseDouble(hMap.get("lat2"));
				Double lat3 = Double.parseDouble(hMap.get("lat3"));
				Double lat4 = Double.parseDouble(hMap.get("lat4"));
				Double lng1 = Double.parseDouble(hMap.get("lng1"));
				Double lng2 = Double.parseDouble(hMap.get("lng2"));
				Double lng3 = Double.parseDouble(hMap.get("lng3"));
				Double lng4 = Double.parseDouble(hMap.get("lng4"));

				// TODO draw arrow
				// FIXME Error
				vectorOptionsList[vectorOptionsNum] = new PolylineOptions()
						.width(3).color(Color.RED).zIndex(zIndex_Polyline);
				vectorOptionsList[vectorOptionsNum].add(new LatLng(lat1, lng1),
						new LatLng(lat2, lng2));
				// mMap.addPolyline(vectorOptionsList[vectorOptionsNum]);
				vectorOptionsNum++;
				vectorOptionsList[vectorOptionsNum] = new PolylineOptions()
						.width(3).color(Color.RED).zIndex(zIndex_Polyline);
				vectorOptionsList[vectorOptionsNum].add(new LatLng(lat3, lng3),
						new LatLng(lat2, lng2), new LatLng(lat4, lng4));
				vectorOptionsNum++;

				// Log.d("Here", "vectot:"+vectorOptionsNum);
			}
			bufferedReader.close();
		} catch (Exception e) {
			Log.d("Here!!", e.toString());
			e.printStackTrace();
		}
	}

	TileOverlayOptions seisImgTileOverlayOptions;
	GroundOverlayOptions seisGroundOverlayOptions;

	public void loadData6() { // Seis Image Tiletry {
		BitmapDescriptor image;
		LatLngBounds bounds;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 2;
		try {
			Bitmap img = BitmapFactory.decodeStream(
					mngr.open("seismw3_300_small.png"), null, opts);
			image = BitmapDescriptorFactory.fromBitmap(img);
			bounds = new LatLngBounds(new LatLng(20.4, 119), new LatLng(26,
					123.5));
			seisGroundOverlayOptions = new GroundOverlayOptions().image(image)
					.positionFromBounds(bounds).transparency((float) 0)
					.zIndex(zIndex_groundOverlay);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("Here!!", "catch draw seis Exception!");
		}
		seisImgTileOverlayOptions = new TileOverlayOptions().tileProvider(
				new mySeisImgUrlTileProvider()).zIndex(zIndex_groundOverlay);
//		 seisImgTileOverlayOptions = new TileOverlayOptions()
//		 .tileProvider(new CustomSeisImgTileProvider(mngr));
	}

	// public MarkerOptions[] markerOptionsList, volleyballOptionList,
	// pgaOptionList;
	// public CircleOptions[] circleOptionsList;
	public MarkerOptions markerOption, volleyballOption, pgaOption,
			markerStarOption;
	public CircleOptions circleOption;
	int pgaOptionListNum = 0;
	public Marker[] markers;
	public boolean MarkerIsSet = false;
	public boolean vollyballMarkerIsSet = false;
	public boolean MarkerInfoIsSet = false;
	public GroundOverlayOptions pgvOverlay;
	public GroundOverlayOptions cwbOverlay;
	public GroundOverlayOptions pgaOverlay;
	// public GroundOverlayOptions history_img_ocean_Overlay;
	// public GroundOverlayOptions history_img_copper_Overlay;
	// public GroundOverlayOptions history_img_gray_Overlay;
	public int selectMarker = 0;
	// public String[] pgvLinks;
	// public String[] cwbLinks;
	// public String[] pgaLinks; //p-alert
	public String pgvLink;
	public String cwbLink;
	public String pgaLink;
	// public String history_img_ocean_Link;
	// public String history_img_copper_Link;
	// public String history_img_gray_Link;
	ArrayList<HashMap<String, String>> earthquakeArrayList;
	public HashMap<Marker, HashMap<String, String>> MarkerMap;

	// AsyncTask<String, Void, Void> TaskDownloadBall;

	String[] getVolleyballDescription(String gCAPLink, String NewBatsLink,
			String FMNEARLink) {
		String[] descriptionLinks = new String[4];
		// Log.d("string","gCAPLink "+gCAPLink);
		int lastIndex = gCAPLink.lastIndexOf("/");
		// Log.d("string","gCAPLink index of /:"+lastIndex);
		String header = gCAPLink.substring(0, lastIndex + 1);
		descriptionLinks[0] = header.concat("mt.best");
		descriptionLinks[1] = header.concat("mtt.best");
		descriptionLinks[3] = header.concat("mt.FMNEAR");
		lastIndex = NewBatsLink.lastIndexOf("_");
		header = NewBatsLink.substring(0, lastIndex + 1);
		descriptionLinks[2] = header.concat("cmtsol.txt");
		Log.d("string", "descriptionLinks[0]:" + descriptionLinks[0]);
		Log.d("string", "descriptionLinks[1]:" + descriptionLinks[1]);
		Log.d("string", "descriptionLinks[2]:" + descriptionLinks[2]);
		Log.d("string", "descriptionLinks[3]:" + descriptionLinks[3]);
		return descriptionLinks;
	}

	downloadBall TaskDownloadBall;
	String[] VdescriptionLinks;

	@SuppressWarnings("unchecked")
	public void setEarthquakeMarker() {
		MarkerIsSet = false;
		// earthquakeArrayList = list;
		vollyballMarkerIsSet = false;
		PGAisLoad = false;
		// TaskDownloadVolleyball = new downloadVolleyball().execute();
		// TaskDownloadBall = new downloadBall().execute(hMap.get("gCAP"),
		// hMap.get("BATS"), hMap.get("New_BATS"), hMap.get("FMNEAR"));
		VdescriptionLinks = getVolleyballDescription(hMap.get("gCAP"),
				hMap.get("New_BATS"), hMap.get("FMNEAR"));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			// TaskLoadResource = new
			// loadResources().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			TaskDownloadBall = (downloadBall) new downloadBall()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							hMap.get("gCAP"), hMap.get("BATS"),
							hMap.get("New_BATS"), hMap.get("FMNEAR"));
		else
			TaskDownloadBall = (downloadBall) new downloadBall().execute(
					hMap.get("gCAP"), hMap.get("BATS"), hMap.get("New_BATS"),
					hMap.get("FMNEAR"));
		// markerOptionsList = new MarkerOptions[earthquakeArrayList.size()];
		// circleOptionsList = new CircleOptions[earthquakeArrayList.size()];
		// pgvLinks = new String[earthquakeArrayList.size()];
		// cwbLinks = new String[earthquakeArrayList.size()];
		// pgaLinks = new String[earthquakeArrayList.size()];
		// for (int i = 0; i < earthquakeArrayList.size(); ++i) {
		// Log.d("Here!",""+i);
		// HashMap<String, String> hMap = earthquakeArrayList.get(i);
		// /Log.d("Here!",hMap.toString());
		Double lat = Double.parseDouble(hMap.get("lat"));
		Double lng = Double.parseDouble(hMap.get("lng"));
		// String title = "詳細資料";
		// String content = hMap.get("Date") + "\n" + hMap.get("Time") +
		// " UTC+8"
		// + "\n" + hMap.get("Depth") + "\n" + hMap.get("ML");
		pgvLink = hMap.get("pgvlink");
		cwbLink = hMap.get("intensitymap");
		pgaLink = hMap.get("pgacontour1");
		// history_img_ocean_Link = hMap.get("history_img_ocean");
		// history_img_copper_Link = hMap.get("history_img_copper");
		// history_img_gray_Link = hMap.get("history_img_gray");
		// Log.d(tag, "history_img_copper_Link:" + history_img_copper_Link);
		// Log.d("Here!","before set options");
		double depth = Double.parseDouble(hMap.get("depth"));
		int ml = (int) Double.parseDouble(hMap.get("ml"));
		String markerIconFilename = "";
		if (depth <= 15) {
			markerIconFilename = "icon_event15_" + ml + ".png";
		} else if (depth >= 15 && depth < 30) {
			markerIconFilename = "icon_event30_" + ml + ".png";
		} else if (depth >= 30 && depth < 70) {
			markerIconFilename = "icon_event70_" + ml + ".png";
		} else if (depth >= 70 && depth < 150) {
			markerIconFilename = "icon_event150_" + ml + ".png";
		} else if (depth >= 150 && depth < 300) {
			markerIconFilename = "icon_event300_" + ml + ".png";
		}
		// Log.d("Here!", "icon_name:" + markerIconFilename);
		try {
			markerOption = new MarkerOptions().position(new LatLng(lat, lng))
					.anchor(0.5f, 0.5f)
					// .title(title)
					.icon(BitmapDescriptorFactory
							.fromBitmap(scaleBitmapOnScreenSize(Drawable
									.createFromStream(
											fActivity.getAssets().open(
													"archeive/"
															+ markerIconFilename),
											null)

							)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			markerOption = new MarkerOptions().position(new LatLng(lat, lng))
					.anchor(0.5f, 0.5f)
					// .title(title)
					.icon(BitmapDescriptorFactory
							.fromAsset("archeive/"
									+ markerIconFilename));
		}

		markerStarOption = new MarkerOptions()
				.position(new LatLng(lat, lng))
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory
						.fromBitmap(scaleBitmapOnScreenSize(fActivity
								.getResources().getDrawable(R.drawable.star))));
		// .snippet(content);

		// int radius = calculatePGARadius(Double.parseDouble(hMap.get("ml")));
		// circleOption = new CircleOptions().center(new LatLng(lat, lng))
		// .radius(radius * 1000).strokeWidth(5)
		// .strokeColor(Color.parseColor("#ff00a0ff"))
		// .fillColor(Color.parseColor("#6000a0ff"));
		// }
		MarkerIsSet = true;
		selectMarker = 0;
		// TODO call select Marker
		selectMarkerInfo();

	}

	@SuppressWarnings("unchecked")
	protected void selectMarkerInfo() {
		MarkerInfoIsSet = false;
		// TaskDownloadMarkerInfo = new downloadMarkerInfo().execute();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			TaskDownloadMarkerInfo = (downloadMarkerInfo) new downloadMarkerInfo()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			TaskDownloadMarkerInfo = (downloadMarkerInfo) new downloadMarkerInfo()
					.execute();
	}

	// private int calculatePGARadius(double ML) {
	// double PGA = 8.0 / 980;
	// double c1 = 0.0159;
	// double c2 = 0.868;
	// double c3 = 1.09;
	// double c4 = 0.0606;
	// double c5 = 0.700;
	// double result = Math.pow(PGA / (c1 * Math.exp(c2 * ML)), -1 / c3) - c4
	// * Math.exp(c5 * ML);
	// Log.d("Here!", "ML " + ML + " has radius " + result);
	// return (int) result;
	// }

	boolean isDraw2 = false, isDraw9 = false, isDraw10 = false,
			isDraw15 = false, isDraw17 = false, isDraw18 = false,
			isDraw19 = false, isDraw20 = false, isDraw21 = false,
			isDraw22 = false, isDraw23 = false;

	public void draw(int actionNumber, float trans) {
		Log.d("Here!!", "draw:" + actionNumber);
		switch (actionNumber) {
		case 1:// 地質圖-connect
			if (isloadResourceFinished) {
				mMap.addTileOverlay(tileOverlayOptions2);
				mMap.addGroundOverlay(islandGreen);
				mMap.addGroundOverlay(islandLanYu);
				mMap.addGroundOverlay(islandLiuChiu);
				mMap.addGroundOverlay(islandPongHu);
				mMap.addGroundOverlay(islandTurtle);
			}
			break;
		case 2: // 地質圖-disconnect
			if (isloadResourceFinished) {
				geoMap = new GroundOverlayOptions().image(geoMap.getImage())
						.positionFromBounds(geoMap.getBounds())
						.transparency(trans).zIndex(zIndex_groundOverlay);
				mMap.addGroundOverlay(geoMap);
				if (!isDraw2) {
					Toast.makeText(fActivity, "未連上網路，無法提供高解析度地質圖",
							Toast.LENGTH_SHORT).show();
				}
				isDraw2 = true;
			}
			break;

		case 3:// 活動斷層
				// for(int i=0;i<pOptions.length;++i){
				// if(pOptions[i]!=null)
			if (isloadResourceFinished) {
				for (int i = 0; i <= pOptionsNum; ++i) {
					mMap.addPolyline(pOptions[i]);

				}
                Log.d(tag,"linkMarkerOptions:"+lineMarkerOptions.length+","+lineMarkerOptions[0]);
				for (int i = 0; i < lineMarkerOptions.length; ++i) {
					mMap.addMarker(lineMarkerOptions[i]);
				}
			}
			break;
		// case 4:// BATS Stations
		// for (int i = 0; i < batsList.size(); ++i) {
		// mMap.addMarker(batsList.get(i));
		// }
		// break;
		// case 5:// CWB Stations
		// for (int i = 0; i < cwbList.size(); ++i) {
		// mMap.addMarker(cwbList.get(i));
		// }
		// break;
		// case 6:// P-alert
		// if(MarkerInfoIsSet){
		// for(int i=0;i< pgaOptionListNum;++i){
		// mMap.addMarker(pgaOptionList[i]);
		// }
		// }
		// else {
		// Toast.makeText(fActivity,
		// "資料尚在下載，請稍後使用本功能",Toast.LENGTH_SHORT).show();
		// }
		// break;
		case 7: // draw marker
			if (MarkerIsSet) {
				// markers = new Marker[markerOptionsList.length];
				// MarkerMap = new HashMap<Marker, HashMap<String,String>>();
				// for (int i = 0; i < markerOptionsList.length; ++i) {
				// markers[i] = mMap.addMarker(markerOptionsList[i]);
				// MarkerMap.put(markers[i], earthquakeArrayList.get(i));
				// }

				mMap.addMarker(markerOption);
			} else {
				// Toast.makeText(fActivity,
				// "資料尚在下載，請稍後使用本功能",Toast.LENGTH_SHORT).show();
			}
			break;
		// case 8: // draw volley ball
		// if (VolleyballisLoad) {
		// // markers = new Marker[volleyballOptionList.length];
		// // MarkerMap = new HashMap<Marker, HashMap<String,String>>();
		// // for (int i = 0; i < volleyballOptionList.length; ++i) {
		// // markers[i] = mMap.addMarker(volleyballOptionList[i]);
		// // MarkerMap.put(markers[i], earthquakeArrayList.get(i));
		// // }
		//
		// mMap.addMarker(volleyballOption);
		// } else {
		// Toast.makeText(fActivity.getApplicationContext(),
		// "資料尚在下載，請稍後使用本功能", Toast.LENGTH_SHORT).show();
		// // new downloadVolleyball().execute();
		// try {
		// Thread.sleep(2000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// if (VolleyballisLoad) {
		// draw(8, 0);
		// }
		// }
		// break;
		case 9: // PGV
			if (PGVisLoad && pgvOverlay != null) {
				mMap.addGroundOverlay(pgvOverlay.transparency(trans));
			} else if (PGVisLoad) {
				fActivity.mSetupGoogleMap.seekBarCh1
						.setVisibility(View.INVISIBLE);
				// Toast.makeText(fActivity,
				// "資料尚在下載，請稍後使用本功能",Toast.LENGTH_SHORT).show();
				if (!isDraw9) {
					Toast.makeText(fActivity, "本地震尚無此震度圖資料", Toast.LENGTH_SHORT)
							.show();
				}
				// new downloadMarkerInfo().execute();
			} else {
				// fActivity.mSetupGoogleMap.seekBar.setVisibility(View.VISIBLE);
				if (!isDraw9) {
					Toast.makeText(fActivity, "請稍後，正在下載震度圖", Toast.LENGTH_SHORT)
							.show();
				}
			}
			isDraw9 = true;
			break;
		case 10: // CWB
			if (CWBisLoad && cwbOverlay != null) {
				mMap.addGroundOverlay(cwbOverlay.transparency(trans));
			} else if (CWBisLoad) {
				fActivity.mSetupGoogleMap.seekBarCh1
						.setVisibility(View.INVISIBLE);
				// Toast.makeText(fActivity,
				// "資料尚在下載，請稍後使用本功能",Toast.LENGTH_SHORT).show();
				if (!isDraw10) {
					Toast.makeText(fActivity, "本地震尚無此震度圖資料", Toast.LENGTH_SHORT)
							.show();
				}
				// new downloadMarkerInfo().execute();
			} else {
				// fActivity.mSetupGoogleMap.seekBar.setVisibility(View.VISIBLE);
				if (!isDraw10) {
					Toast.makeText(fActivity, "請稍後，正在下載震度圖", Toast.LENGTH_SHORT)
							.show();
				}
			}
			isDraw10 = true;
			break;
		case 11: // vector
			if (isloadResourceFinished) {
				for (int i = 0; i < VECTOR_NUM; ++i) {
					mMap.addPolyline(vectorOptionsList[i]);
				}
				break;
			}
			// case 12: // draw marker
			// if (MarkerIsSet) {
			// // for (int i = 0; i < circleOptionsList.length; ++i) {
			// mMap.addCircle(circleOption);
			// // }
			// } else {
			// Toast.makeText(fActivity, "資料尚在下載，請稍後使用本功能", Toast.LENGTH_SHORT)
			// .show();
			// }
			// break;
			// case 13: // marker animation
			// isMarkerAnim = true;
			// startAnimation();
			// break;
			// case 14: //P-alert
			// // PGA is load then PGAisLoad is true
			// // PGA load error then pgaOptionList will be null
			// if(PGAisLoad && pgaOptionList!=null){
			// for(int i=0;i<pgaOptionListNum;++i){
			// mMap.addMarker(pgaOptionList[i]);
			// }
			// //mMap.addGroundOverlay(cwbOverlay.transparency(trans));
			// }
			// else {
			// //Toast.makeText(fActivity,
			// "資料尚在下載，請稍後使用本功能",Toast.LENGTH_SHORT).show();
			// if(PGAisLoad && pgaOptionList!=null){
			// draw(14, 0);
			// }
			// else if(PGAisLoad && pgaOptionList == null){
			// Toast.makeText(fActivity,
			// "本地震無即時地震資料",Toast.LENGTH_SHORT).show();
			// // new downloadMarkerInfo().execute();
			// }
			// else{
			// Toast.makeText(fActivity, "地震資料下載中",Toast.LENGTH_SHORT).show();
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// draw(14, 0);
			// }
			// }
			// break;
		case 15: // P-alert
			// fActivity.mSetupGoogleMap.seekBar.setVisibility(View.INVISIBLE);
			if (PGAisLoad && pgaOverlay != null) {
				mMap.addGroundOverlay(pgaOverlay.transparency(trans));
			} else if (PGAisLoad) {
				fActivity.mSetupGoogleMap.seekBarCh1
						.setVisibility(View.INVISIBLE);
				// Toast.makeText(fActivity,
				// "資料尚在下載，請稍後使用本功能",Toast.LENGTH_SHORT).show();
				if (!isDraw15) {
					Toast.makeText(fActivity, "本地震尚無此震度圖資料", Toast.LENGTH_SHORT)
							.show();
				}
				// new downloadMarkerInfo().execute();
			} else {
				if (!isDraw15) {
					Toast.makeText(fActivity, "請稍後，正在下載震度圖", Toast.LENGTH_SHORT)
							.show();
				}
			}
			isDraw15 = true;
			break;

		// case 16:// CMTs volleyball
		// if (ballisLoad[0]) {
		// mMap.addMarker(ballOptions[0]);
		// } else {
		// Toast.makeText(fActivity.getApplicationContext(), "本地震尚無此分析資料",
		// Toast.LENGTH_SHORT).show();
		// }
		// break;
		case 17:// gCAP volleyball
			if (ballisLoad[0]) {
				mMap.addMarker(ballOptions[0]);
			} else {
				if (!isDraw17) {
					Toast.makeText(fActivity.getApplicationContext(),
							"本地震尚無此震源機制資料", Toast.LENGTH_SHORT).show();
				}
			}
			isDraw17 = true;
			break;
		case 18:// BATS volleyball
			if (ballisLoad[1]) {
				mMap.addMarker(ballOptions[1]);
			} else {
				if (!isDraw18) {
					Toast.makeText(fActivity.getApplicationContext(),
							"本地震尚無此震源機制資料", Toast.LENGTH_SHORT).show();
				}
			}
			isDraw18 = true;
			break;
		case 19:// New_BATS volleyball
			if (ballisLoad[2]) {
				mMap.addMarker(ballOptions[2]);
			} else {
				if (!isDraw19) {
					Toast.makeText(fActivity.getApplicationContext(),
							"本地震尚無此震源機制資料", Toast.LENGTH_SHORT).show();
				}
			}
			isDraw19 = true;
			break;
		case 20:// FMNEAR volleyball
			if (ballisLoad[3]) {
				mMap.addMarker(ballOptions[3]);
			} else {
				if (!isDraw20) {
					Toast.makeText(fActivity.getApplicationContext(),
							"本地震尚無此震源機制資料", Toast.LENGTH_SHORT).show();
				}
			}
			isDraw20 = true;
			break;
		case 21:// history_img_ocean
			if (isloadResourceFinished) {
				mMap.addTileOverlay(seisImgTileOverlayOptions);
			}
			break;
		// if (HistoryisLoad && history_img_ocean_Overlay != null) {
		// mMap.addGroundOverlay(history_img_ocean_Overlay
		// .transparency(trans));
		// } else if (HistoryisLoad) {
		// if (!isDraw21) {
		// Toast.makeText(fActivity, "本地震尚無背景地震活動資料",
		// Toast.LENGTH_SHORT).show();
		// }
		// } else {
		// if (!isDraw21) {
		// Toast.makeText(fActivity, "請稍後，正在下載背景地震活動",
		// Toast.LENGTH_SHORT).show();
		// }
		// }
		// isDraw21 = true;
		// break;
		// case 22:// history_img_copper
		// if (HistoryisLoad && history_img_copper_Overlay != null) {
		// mMap.addGroundOverlay(history_img_copper_Overlay
		// .transparency(trans));
		// } else if (HistoryisLoad) {
		// if (!isDraw22) {
		// Toast.makeText(fActivity, "本地震尚無背景地震活動資料",
		// Toast.LENGTH_SHORT).show();
		// }
		// } else {
		// if (!isDraw22) {
		// Toast.makeText(fActivity, "請稍後，正在下載背景地震活動",
		// Toast.LENGTH_SHORT).show();
		// }
		// }
		// isDraw22 = true;
		// break;
		// case 23:// history_img_gray
		// if (HistoryisLoad && history_img_gray_Overlay != null) {
		// mMap.addGroundOverlay(history_img_gray_Overlay
		// .transparency(trans));
		// } else if (HistoryisLoad) {
		// if (!isDraw23) {
		// Toast.makeText(fActivity, "本地震尚無背景地震活動資料",
		// Toast.LENGTH_SHORT).show();
		// }
		// } else {
		// if (!isDraw23) {
		// Toast.makeText(fActivity, "請稍後，正在下載背景地震活動",
		// Toast.LENGTH_SHORT).show();
		// }
		// }
		// isDraw23 = true;
		// break;
		//
		case 22: // Seis-disconnect
			if (isloadResourceFinished) {
				// geoMap = new GroundOverlayOptions().image(geoMap.getImage())
				// .positionFromBounds(geoMap.getBounds())
				// .transparency(trans).zIndex(zIndex_groundOverlay);
				mMap.addGroundOverlay(seisGroundOverlayOptions);
				if (!isDraw22) {
					Toast.makeText(fActivity, "未連上網路，無法提供高解析度地質圖",
							Toast.LENGTH_SHORT).show();
				}
				isDraw22 = true;
			}
			break;

		case 23:
			if (MarkerIsSet) {
				mMap.addMarker(markerStarOption);
			}
			break;
		}
	}

	// /////////////////////////End of Draw////////////////////////////////////
	// private Handler mHandler = new Handler();
	// private Runnable mHandlerTask;
	// private int radiusSize;
	// private double maxRadius;
	// LatLng animCirclelatLng;
	// Circle currentCircle, lastCircle;
	//
	// protected void startAnimation() {
	// Marker currentMarker = markers[selectMarker];
	// animCirclelatLng = currentMarker.getPosition();
	// maxRadius = circleOption.getRadius();
	// lastCircle = mMap.addCircle(new CircleOptions()
	// .center(animCirclelatLng).radius(maxRadius / 10).strokeWidth(5)
	// .strokeColor(Color.RED));
	// radiusSize = 0;
	// if (mHandlerTask == null) {
	// mHandlerTask = new Runnable() {
	// @Override
	// public void run() {
	// // Log.d("Here!", "in Anim, radius "+ radiusSize);
	// if (radiusSize < 10) {
	// lastCircle.remove();
	// currentCircle = mMap.addCircle(new CircleOptions()
	// .center(animCirclelatLng)
	// .radius(maxRadius / 10 * ++radiusSize)
	// .strokeWidth(5).strokeColor(Color.RED));
	// lastCircle = currentCircle;
	//
	// } else {
	// radiusSize = 0;
	// }
	// mHandler.postDelayed(mHandlerTask, 100);
	// }
	// };
	// }
	// mHandlerTask.run();
	//
	// }
	//
	// protected boolean isMarkerAnim = true;
	//
	// protected void stopAnimation() {
	// isMarkerAnim = false;
	// if (mHandlerTask != null) {
	// mHandler.removeCallbacks(mHandlerTask);
	// // mHandler.removeCallbacksAndMessages(null);
	// }
	// }

	public class Point {
		int index;
		int groupIndex;
		LatLng latLng;
	}

	protected boolean PGAisLoad = false;
	protected boolean PGVisLoad = false;
	protected boolean CWBisLoad = false;

	// protected boolean HistoryisLoad = false;

	// TODO set p-alert icon color
	// public double[][] colorTable={{0.001,255,255,255},{0.5,227,255,227},
	// {1,136,255,136},{1.5,28,255,28},{2,108,255,0},{2.5,255,255,0},
	// {3,255,170,0},{3.5,255,90,0},{4,255,36,0},{4.5,245,0,0},
	// {5,186,0,0},{5.5,153,12,51},{6,153,44,178},{6.5,153,41,165},
	// {7,153,51,204}};
	// static int alpha = 192;
	// int[] colorTable = { Color.argb(alpha, 255, 255, 255),// 0
	// Color.argb(alpha, 227, 255, 227),// 0.5
	// Color.argb(alpha, 136, 255, 136),// 1
	// Color.argb(alpha, 28, 255, 28),// 1.5
	// Color.argb(alpha, 108, 255, 0),// 2
	// Color.argb(alpha, 255, 255, 0),// 2.5
	// Color.argb(alpha, 255, 170, 0),// 3
	// Color.argb(alpha, 255, 90, 0),// 3.5
	// Color.argb(alpha, 255, 36, 0),// 4
	// Color.argb(alpha, 245, 0, 0),// 4.5
	// Color.argb(alpha, 186, 0, 0),// 5
	// Color.argb(alpha, 153, 12, 51),// 5.5
	// Color.argb(alpha, 153, 44, 178),// 6
	// Color.argb(alpha, 153, 41, 165),// 6.5
	// Color.argb(alpha, 153, 51, 204) // 7
	// };

	@SuppressWarnings("rawtypes")
	class downloadMarkerInfo extends AsyncTask {
		public boolean isTaskCancel = false;

		@Override
		protected Object doInBackground(Object... params) {
			Log.d(AsyncTaskTag,
					"In MapOverlay: markerInfo: doInbackground start");
			System.gc();
			// download PGV
			Bitmap bmImg = null;
			try {
				PGAisLoad = false;
				// URL url = new URL(pgvLinks[selectMarker]);
				URL url = new URL(pgaLink);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 1;
				bmImg = BitmapFactory.decodeStream(is, null, opts);
				if (bmImg != null) {
					BitmapDescriptor image = BitmapDescriptorFactory
							.fromBitmap(bmImg);
					LatLngBounds bounds = new LatLngBounds(new LatLng(21.885,
							120.03), new LatLng(25.315, 122.01));
					pgaOverlay = new GroundOverlayOptions().image(image)
							.positionFromBounds(bounds)
							.zIndex(zIndex_groundOverlay);
					// mMap.addGroundOverlay(pgvOverlay);
					PGAisLoad = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Here!!", "Load pga Error!" + e.toString());
				// pgvOverlay = new GroundOverlayOptions();
				pgaOverlay = null;
				PGAisLoad = false;
			}
			if (bmImg != null && !bmImg.isRecycled()) {
				bmImg.recycle();
			}
			if (isCancelled()) {
				isTaskCancel = true;
				return null;
			}
			try {
				PGVisLoad = false;
				// URL url = new URL(pgvLinks[selectMarker]);
				URL url = new URL(pgvLink);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 1;
				bmImg = BitmapFactory.decodeStream(is, null, opts);
				if (bmImg != null) {
					BitmapDescriptor image = BitmapDescriptorFactory
							.fromBitmap(bmImg);
					LatLngBounds bounds = new LatLngBounds(new LatLng(21.67,
							119.71), new LatLng(25.45, 122.17));
					pgvOverlay = new GroundOverlayOptions().image(image)
							.positionFromBounds(bounds)
							.zIndex(zIndex_groundOverlay);
					// mMap.addGroundOverlay(pgvOverlay);
					PGVisLoad = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Here!!", "Load pgv Error!" + e.toString());
				// pgvOverlay = new GroundOverlayOptions();
				pgvOverlay = null;
				PGVisLoad = false;
			}
			if (bmImg != null && !bmImg.isRecycled()) {
				bmImg.recycle();
			}
			if (isCancelled()) {
				isTaskCancel = true;
				return null;
			}
			// download CWB
			try {
				CWBisLoad = false;
				// URL url = new URL(cwbLinks[selectMarker]);
				URL url = new URL(cwbLink);
				// Log.d("Here!!", "cwbURL:"+cwbLinks[selectMarker]);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 1;
				bmImg = BitmapFactory.decodeStream(is, null, opts);
				if (bmImg != null) {
					BitmapDescriptor image = BitmapDescriptorFactory
							.fromBitmap(bmImg);
					LatLngBounds bounds = new LatLngBounds(new LatLng(19.7,
							118.28), new LatLng(26.45, 123.20));
					cwbOverlay = new GroundOverlayOptions().image(image)
							.positionFromBounds(bounds)
							.zIndex(zIndex_groundOverlay);
					CWBisLoad = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Here!!", "Load cwb Error! " + e.toString());
				// cwbOverlay = new GroundOverlayOptions();
				CWBisLoad = false;
			}
			if (bmImg != null && !bmImg.isRecycled()) {
				bmImg.recycle();
			}
			if (isCancelled()) {
				isTaskCancel = true;
				return null;
			}
			// try {
			// HistoryisLoad = false;
			// // URL url = new URL(cwbLinks[selectMarker]);
			// URL url = new URL(history_img_ocean_Link);
			// // URL url = new URL(history_img_copper_Link);
			// // URL url = new URL(history_img_gray_Link);
			// // Log.d("Here!!", "cwbURL:"+cwbLinks[selectMarker]);
			// HttpURLConnection conn = (HttpURLConnection) url
			// .openConnection();
			// conn.setDoInput(true);
			// conn.connect();
			// InputStream is = conn.getInputStream();
			// BitmapFactory.Options opts = new BitmapFactory.Options();
			// opts.inJustDecodeBounds = false;
			// opts.inSampleSize = 2;
			// bmImg = BitmapFactory.decodeStream(is, null, opts);
			// if (bmImg != null) {
			// BitmapDescriptor image = BitmapDescriptorFactory
			// .fromBitmap(bmImg);
			// Double lat = Double.parseDouble(hMap.get("lat"));
			// Double lng = Double.parseDouble(hMap.get("lng"));
			// LatLngBounds bounds = new LatLngBounds(new LatLng(
			// lat - 0.6, lng - 0.6), new LatLng(lat + 0.6,
			// lng + 0.6));
			// history_img_ocean_Overlay = new GroundOverlayOptions()
			// .image(image).positionFromBounds(bounds)
			// .zIndex(zIndex_groundOverlay);
			// // history_img_copper_Overlay = new GroundOverlayOptions()
			// // .image(image).positionFromBounds(bounds)
			// // .zIndex(zIndex_groundOverlay);
			// HistoryisLoad = true;
			// }
			// } catch (Exception e) {
			// e.printStackTrace();
			// Log.d("Here!!", "Load history Error! " + e.toString());
			// if (isDraw21) { // ocean
			// Toast.makeText(fActivity, "本地震尚無背景地震活動資料",
			// Toast.LENGTH_SHORT).show();
			// }
			// // if(isDraw22){ //copper
			// // Toast.makeText(fActivity, "本地震尚無背景地震活動資料",
			// // Toast.LENGTH_SHORT).show();
			// // }
			// HistoryisLoad = false;
			// }
			Log.d(AsyncTaskTag,
					"In MapOverlay: markerInfo: doInbackground finish");
			if (bmImg != null && !bmImg.isRecycled()) {
				bmImg.recycle();
			}
			if (isCancelled()) {
				isTaskCancel = true;
				return null;
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (!isTaskCancel) {
				MarkerInfoIsSet = true;
				// fActivity.mSetupGoogleMap.checkTag();
				Log.d("Here!", "onPostExcute MarkerInfoIsSet true");
				Log.d(AsyncTaskTag, "In MapOverlay: markerInfo: onPostExecute");
				if (handler != null) {
					Message m = new Message();
					// 定義 Message的代號，handler才知道這個號碼是不是自己該處理的。
					m.what = LOAD_RESOURCE_FINISHED;
					Log.d(tag,
							"In MapOverlay: LOAD_RESOURCE_FINISHED marker Info");
					handler.sendMessage(m);
				}
			}
		}

		@Override
		protected void onCancelled() {
			Log.d(AsyncTaskTag, "In MapOverlay: markerInfo: onCancelled");
			super.onCancelled();
		}

	}

	public MarkerOptions[] ballOptions;
	protected Boolean[] ballisLoad = { false, false, false, false };

	class downloadBall extends AsyncTask<String, Void, Void> {
		public boolean isTaskCancel = false;

		@Override
		protected Void doInBackground(String... link) {
			Log.d(AsyncTaskTag, "In MapOverlay: ball: doInBackground start");
			boolean catchExp = false;

			Double lat = Double.parseDouble(hMap.get("lat"));
			Double lng = Double.parseDouble(hMap.get("lng"));
			// String title = "詳細資料";
			// String content;
			String content = hMap.get("Date") + "\n" + hMap.get("Time")
					+ "UTC+8" + "\n" + hMap.get("Depth") + "\n"
					+ hMap.get("ML");

			ballOptions = new MarkerOptions[link.length];
			// download volley ball
			for (int i = 0; i < link.length; ++i) {
				Bitmap bmImg = null;
				catchExp = false;
				try {
					URL url = new URL(link[i]);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream is = conn.getInputStream();
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = false;
					opts.inSampleSize = 1;
					bmImg = BitmapFactory.decodeStream(is, null, opts);

					Bitmap newBitmap = Bitmap.createScaledBitmap(bmImg, 80, 80,
							true);
					switch (i) {
					case 0:
						content = getGCAPContent();
						break;
					case 2:
						content = getNEWBATSContent();
						break;
					case 3:
						content = getFMNEARContent();
						break;
					}
					ballOptions[i] = new MarkerOptions()
							.position(new LatLng(lat, lng))
							.anchor(0.5f, 0.5f)
							// .title(title)
							.icon(BitmapDescriptorFactory.fromBitmap(newBitmap))
							.snippet(content);
					if (newBitmap != null && !newBitmap.isRecycled()) {
						newBitmap.recycle();
					}

				} catch (Exception e) {
					e.printStackTrace();
					Log.d("Here!!", "Load Ball " + i + " Exception!");
					catchExp = true;
				}

				if (catchExp) {
					ballisLoad[i] = false;
				} else {
					ballisLoad[i] = true;
				}
				if (bmImg != null && !bmImg.isRecycled()) {
					bmImg.recycle();
				}
				if (isCancelled()) {
					isTaskCancel = true;
					break;
				}
			}
			// while(fActivity.mSetupGoogleMap==null){
			// // download finished before map is load = ="
			// }
			Log.d(AsyncTaskTag, "In MapOverlay: ball: doInBackground finish");

			if (isCancelled()) {
				isTaskCancel = true;
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// vollyballMarkerIsSet = true;
			// Log.d("Here!", "onPostExcute vollyballMarkerIsSet true");
			Log.d(AsyncTaskTag, "In MapOverlay: ball: onPostExecute");
			if (!isTaskCancel) {
				// fActivity.mSetupGoogleMap.checkTag();
				if (handler != null) {
					Message m = new Message();
					// 定義 Message的代號，handler才知道這個號碼是不是自己該處理的。
					m.what = LOAD_RESOURCE_FINISHED;
					Log.d(tag,
							"In MapOverlay: LOAD_RESOURCE_FINISHED volley ball");
					handler.sendMessage(m);
				}
			}
		}

		@Override
		protected void onCancelled() {
			Log.d(AsyncTaskTag, "In MapOverlay: ball: onCancelled");
			super.onCancelled();
		}

	}

	String getGCAPContent() {
		String content = null;
		Double tmpDouble;
		int tmpInt;
		try {
			URL url = new URL(VdescriptionLinks[0]);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(is));
			String s = bufferedReader.readLine();
			Log.d("string", "GCAP mt.best:" + s);
			StringTokenizer st = new StringTokenizer(s);
			for (int i = 0; i < 5; ++i)
				st.nextToken();
			String strike1 = st.nextToken();
			Log.d("string", "GCAP strike1:" + strike1);
			String dip1 = st.nextToken();
			String slip1 = st.nextToken();
			st.nextToken();
			String mw = st.nextToken();
			for (int i = 0; i < 11; ++i)
				st.nextToken();
			String clvd1 = st.nextToken();
			Log.d("string", "GCAP clvd1:" + clvd1);
			tmpDouble = Double.parseDouble(clvd1);
			tmpDouble *= 100;
			tmpInt = tmpDouble.intValue();
			clvd1 = tmpInt + "%";
			String clvd2 = st.nextToken();
			st.nextToken();
			String depth = st.nextToken();
			tmpDouble = Double.parseDouble(depth);
			tmpInt = (int) Math.round(tmpDouble);
			depth = tmpInt + "";
			s = bufferedReader.readLine();
			st = new StringTokenizer(s);
			String strike2 = st.nextToken();
			Log.d("string", "GCAP strike2:" + strike2);
			String dip2 = st.nextToken();
			String slip2 = st.nextToken();
			Log.d("string", "GCAP slip2:" + slip2);

			url = new URL(VdescriptionLinks[1]);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			is = conn.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(is));
			s = bufferedReader.readLine();
			Log.d("string", "GCAP mtt.best:" + s);
			st = new StringTokenizer(s);
			for (int i = 0; i < 13; ++i)
				st.nextToken();
			String vr = st.nextToken();
			tmpDouble = Double.parseDouble(vr);
			tmpInt = (int) Math.round(tmpDouble);
			vr = tmpInt + "%";
			tmpDouble = Double.parseDouble(strike1);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				strike1 = "    " + tmpInt;
			} else if (tmpInt / 100 <= 0) {
				strike1 = "  " + tmpInt;
			} else {
				strike1 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(strike2);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				strike2 = "    " + tmpInt;
			} else if (tmpInt / 100 <= 0) {
				strike2 = "  " + tmpInt;
			} else {
				strike2 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(dip1);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				dip1 = "    " + tmpInt;
			} else {
				dip1 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(dip2);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				dip2 = "  " + tmpInt;
			} else {
				dip2 = tmpInt + "";
			}
			content = "NP1:\t" + strike1 + "\u00B0\t/\t" + dip1 + "\u00B0\t/\t"
					+ slip1 + "\u00B0\n" + "NP2:\t" + strike2 + "\u00B0\t/\t"
					+ dip2 + "\u00B0\t/\t" + slip2 + "\u00B0\n" + "Mw: " + mw
					+ "\t" + "Depth: " + depth + "km\n" + "VR: " + vr + "\t"
					+ "Clvd: " + clvd1 + "\t";

			Log.d("string", "content:" + content);
		} catch (Exception e) {
			e.printStackTrace();
			content = null;
		}
		return content;
	}

	String getNEWBATSContent() {
		String content = null;
		Double tmpDouble;
		int tmpInt;
		try {
			URL url = new URL(VdescriptionLinks[2]);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(is));
			String s = bufferedReader.readLine();
			Log.d("string", "NEWBATS :" + s);
			StringTokenizer st = new StringTokenizer(s, ";");
			for (int i = 0; i < 6; ++i)
				st.nextToken();
			String strike1 = st.nextToken();
			Log.d("string", "NEWBATS strike1:" + strike1);
			String dip1 = st.nextToken();
			String slip1 = st.nextToken();

			String depth = st.nextToken();
			String strike2 = st.nextToken();
			Log.d("string", "NEWBATS strike2:" + strike2);
			String dip2 = st.nextToken();
			String slip2 = st.nextToken();
			String mw = st.nextToken();
			String misfit = st.nextToken();
			st.nextToken();
			String clvd = st.nextToken();
			tmpDouble = Double.parseDouble(clvd);
			tmpInt = (int) Math.round(tmpDouble);
			clvd = tmpInt + "%";
			Log.d("string", "NEWBATS clvd:" + clvd);

			tmpDouble = Double.parseDouble(strike1);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				strike1 = "    " + tmpInt;
			} else if (tmpInt / 100 <= 0) {
				strike1 = "  " + tmpInt;
			} else {
				strike1 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(strike2);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				strike2 = "    " + tmpInt;
			} else if (tmpInt / 100 <= 0) {
				strike2 = "  " + tmpInt;
			} else {
				strike2 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(dip1);
			tmpInt = (int) Math.round(tmpDouble);
			dip1 = tmpInt + "";
			tmpDouble = Double.parseDouble(dip2);
			tmpInt = (int) Math.round(tmpDouble);
			dip2 = tmpInt + "";
			tmpDouble = Double.parseDouble(dip1);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				dip1 = "    " + tmpInt;
			} else {
				dip1 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(dip2);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				dip2 = "  " + tmpInt;
			} else {
				dip2 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(slip1);
			tmpInt = (int) Math.round(tmpDouble);
			slip1 = tmpInt + "";
			tmpDouble = Double.parseDouble(slip2);
			tmpInt = (int) Math.round(tmpDouble);
			slip2 = tmpInt + "";
			content = "NP1:\t" + strike1 + "\u00B0\t/\t" + dip1 + "\u00B0\t/\t"
					+ slip1 + "\u00B0\n" + "NP2:\t" + strike2 + "\u00B0\t/\t"
					+ dip2 + "\u00B0\t/\t" + slip2 + "\u00B0\n" + "Mw: " + mw
					+ "\t" + "Depth: " + depth + "km\n" + "Clvd: " + clvd
					+ "\t" + "Misfit: " + misfit;

		} catch (Exception e) {
			e.printStackTrace();
			content = null;
		}
		return content;
	}

	String getFMNEARContent() {
		String content = null;
		Double tmpDouble;
		int tmpInt;
		try {
			URL url = new URL(VdescriptionLinks[3]);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(is));
			String s = bufferedReader.readLine();
			Log.d("string", "FMNEAR:" + s);
			StringTokenizer st = new StringTokenizer(s);
			String strike1 = st.nextToken();
			Log.d("string", "FMNEAR strike1:" + strike1);
			String dip1 = st.nextToken();
			String slip1 = st.nextToken();
			String strike2 = st.nextToken();
			Log.d("string", "FMNEAR strike2:" + strike2);
			String dip2 = st.nextToken();
			String slip2 = st.nextToken();
			String ci = st.nextToken();
			String depth = st.nextToken();
			tmpDouble = Double.parseDouble(depth);
			tmpInt = (int) Math.round(tmpDouble);
			depth = tmpInt + "";
			String mw = st.nextToken();
			Log.d("string", "FMNEAR mw:" + mw);
			tmpDouble = Double.parseDouble(strike1);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				strike1 = "    " + tmpInt;
			} else if (tmpInt / 100 <= 0) {
				strike1 = "  " + tmpInt;
			} else {
				strike1 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(strike2);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				strike2 = "    " + tmpInt;
			} else if (tmpInt / 100 <= 0) {
				strike2 = "  " + tmpInt;
			} else {
				strike2 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(dip1);
			tmpInt = (int) Math.round(tmpDouble);
			dip1 = tmpInt + "";
			tmpDouble = Double.parseDouble(dip2);
			tmpInt = (int) Math.round(tmpDouble);
			dip2 = tmpInt + "";
			tmpDouble = Double.parseDouble(dip1);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				dip1 = "    " + tmpInt;
			} else {
				dip1 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(dip2);
			tmpInt = (int) Math.round(tmpDouble);
			if (tmpInt / 10 <= 0) {
				dip2 = "  " + tmpInt;
			} else {
				dip2 = tmpInt + "";
			}
			tmpDouble = Double.parseDouble(slip1);
			tmpInt = (int) Math.round(tmpDouble);
			slip1 = tmpInt + "";
			tmpDouble = Double.parseDouble(slip2);
			tmpInt = (int) Math.round(tmpDouble);
			slip2 = tmpInt + "";
			content = "NP1:\t" + strike1 + "\u00B0\t/\t" + dip1 + "\u00B0\t/\t"
					+ slip1 + "\u00B0\n" + "NP2:\t" + strike2 + "\u00B0\t/\t"
					+ dip2 + "\u00B0\t/\t" + slip2 + "\u00B0\n" + "Mw: " + mw
					+ "\t" + "Depth: " + depth + "km\n" + "C.I.: " + ci;
		} catch (Exception e) {
			e.printStackTrace();
			content = null;
		}
		return content;
	}

	boolean isloadResourceFinished = false;

	@SuppressWarnings("rawtypes")
	class loadResources extends AsyncTask {
		public boolean isTaskCancel = false;

		@Override
		protected void onPreExecute() {
			isloadResourceFinished = false;
			Log.d(AsyncTaskTag, "In MapOverlay: loadResources: onPreExecute");
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(Object... params) {
			Log.d(AsyncTaskTag,
					"In MapOverlay: loadResources: doInBackground start");
			loadData1(); // 活動斷層
			// loadData2(); // map bar
			if (isCancelled()) {
				isTaskCancel = true;
				return null;
			}
			loadData3(); // 地質圖
			// loadData4(); //BATS,CWB Stations
			if (isCancelled()) {
				isTaskCancel = true;
				return null;
			}
			loadData5(); // vector*/
			if (isCancelled()) {
				isTaskCancel = true;
				return null;
			}
			loadData6(); // Seis Tile*/
			if (isCancelled()) {
				isTaskCancel = true;
				return null;
			}
			Log.d(AsyncTaskTag,

			"In MapOverlay: loadResources: doInBackground finish");
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			Log.d(tag, "In MapOverlay: onPostExecute: LOAD_RESOURCE_FINISHED");
			if (!isTaskCancel) {
				isloadResourceFinished = true;
				if (handler != null) {
					Message m = new Message();
					// 定義 Message的代號，handler才知道這個號碼是不是自己該處理的。
					m.what = LOAD_RESOURCE_FINISHED;
					Log.d(tag, "In MapOverlay: LOAD_RESOURCE_FINISHED resource");
					handler.sendMessage(m);
				}
				Log.d(AsyncTaskTag,
						"In MapOverlay: loadResources: onPostExecute");
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			Log.d(AsyncTaskTag, "In MapOverlay: loadResources: onCancelled");
			super.onCancelled();
		}

	}

	final int LOAD_RESOURCE_FINISHED = ConstantVariables.LOAD_RESOURCE_FINISHED;
	Handler handler = null;

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	protected Bitmap scaleBitmapOnScreenSize(Drawable d) {
		DisplayMetrics metrics = new DisplayMetrics();
		metrics = fActivity.getResources().getDisplayMetrics();
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

}

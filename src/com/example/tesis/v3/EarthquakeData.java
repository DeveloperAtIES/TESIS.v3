/*
 * EarthquakeData.java 
 * Use JSONfunctions.java methods to download earthquake data from http://140.109.80.214/
 */

package com.example.tesis.v3;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.is;

import android.R.integer;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

public class EarthquakeData {
	// protected ArrayList<HashMap<String, String>> EarthquakeList;
	// MainActivity mainActivity;
	// LoadingPage loadingPage;
	// HomeActivity mainActivity;
	// Context mainActivity;
	// protected boolean EQDataisLoad = false;

	final String timeTag = "timeTag";
	long startTime, endTime;

	// String generalFilename = "TESIS.earthquakeList.general.data";
	// String newestIDFilename = "TESIS.newest.earthquake.id";
	String generalFilename = ConstantVariables.generalFilename;
	String newestIDFilename = ConstantVariables.newestIDFilename;

	// ArrayList<HashMap<String, String>> mGeneralEarthquakeList = null;

	// final int MAX_STORED_EQ_LENGTH = ConstantVariables.MAX_STORED_EQ_LENGTH;

	// public EarthquakeData(HomeActivity homeActivity) {
	// this.mainActivity = homeActivity.getApplicationContext();
	// }

	// public EarthquakeData(MyNotificationService notificationService) {
	// this.mainActivity = notificationService.getApplicationContext();
	// }

	// public EarthquakeData(GcmIntentService gcmIntentService) {
	// this.mainActivity = gcmIntentService.getApplicationContext();
	// }

	// public EarthquakeData(MainActivity mainActivity) {
	// this.mainActivity = mainActivity;
	// }

	//
	// public EarthquakeData(LoadingPage loadingPage) {
	// this.loadingPage = loadingPage;
	// }
	// private boolean IdisLoad = false;
	// private static int newestId;

	public static HashMap<String, String> getDataFromURL(String URL) {
		HashMap<String, String> map;
		JSONObject json = JSONfunctions.getJSONfromURL(URL);
		if (json == null) {
			return null;
		}
		try {
			JSONArray earthquakes = json.getJSONArray("earthquakes");
			JSONObject e = earthquakes.getJSONObject(0);
			map = getHashMapFromJsonObject(e);

		} catch (JSONException e) {
			Log.e("GCMIntentService", "Error getJSONArray earthquakes");
			return null;
		}
		return map;
	}

	public static ArrayList<HashMap<String, String>> getData(Context ctx,
			String from, String to, boolean isSave) {
		/*
		 * return value:
		 * 1.null (download error or no need to update)
		 * 2.new EQList (is auto saved)
		 */

		ArrayList<HashMap<String, String>> EarthquakeList = new ArrayList<HashMap<String, String>>();
		Log.d("Here!", "from:" + from + "to:" + to);
		JSONArray jsonID = JSONfunctions
				.getJSONArrayfromURL("http://tesis.earth.sinica.edu.tw/common/"
						+ "php/getid.php?" + "start=" + from + "&end=" + to);
		if (jsonID == null) {
			Log.d("Here!", "Cannot download earthquake data ID.");
			// Cause by wifi opened but not connect to network.
			return null;
		}

		String from_id = "", to_id = "";
		try {
			from_id = jsonID.get(0).toString();
			to_id = jsonID.get(jsonID.length() - 1).toString();
			Log.d("myTag", "fromID:" + from_id + " toID:" + to_id);
		} catch (JSONException e) {
			Log.e("myTag", "Error parsing ID data " + e.toString());
			return null;
		}

		int newestId = 0;

		// 0807 check newest id
		File newestIDFile = ctx
				.getFileStreamPath(ConstantVariables.newestIDFilename);
		if (isSave && newestIDFile.exists()) {
			try {
				FileInputStream fis;
				fis = ctx.openFileInput(ConstantVariables.newestIDFilename);
				ObjectInputStream ois = new ObjectInputStream(fis);
				String newestID = (String) ois.readObject();
				ois.close();
				fis.close();
				Log.d("myTag", "load newest ID from file:" + newestID);
				newestId = Integer.parseInt(newestID);
				Integer tmpInteger = newestId + 1;
				from_id = tmpInteger.toString();
			} catch (Exception e) {
				e.printStackTrace();
				// newest id break
			}
			Log.d("Here!", "modified, fromID:" + from_id + " toID:" + to_id);
			if (newestId >= Integer.parseInt(to_id)) {
				// TODO make Toast
				Log.d("myTag", "The EQ List is already the newsest.");
				return null;
			}
		}
		// else {
		// Integer tmpInteger = Integer.parseInt(to_id)
		// - ConstantVariables.MAX_STORED_EQ_LENGTH;
		// from_id = tmpInteger.toString();
		// }

		if (Integer.parseInt(from_id) <= Integer.parseInt(to_id)) {
			JSONObject json = JSONfunctions
					.getJSONfromURL("http://tesis.earth.sinica.edu.tw/common/"
							+ "php/processdatamobile.php?" + "firstid='"
							+ from_id + "'&secondid='" + to_id + "'");
			// Log.d("Here!", json.toString());
			if (json == null) {
				// TODO make Toast
				Log.d("myTag", "Download EQ content error.");
				return null;
			}
			try {
				JSONArray earthquakes = json.getJSONArray("earthquakes");
				for (int i = earthquakes.length() - 1; i >= 0; i--) {
					HashMap<String, String> map;
					map = getHashMapFromJsonObject(earthquakes.getJSONObject(i));
					if (map != null) {
						EarthquakeList.add(map);
					}
				}
			} catch (JSONException e) {
				Log.e("myLog", "Error getJSONArray earthquakes");
			}

			if (isSave) {

				File generalEQFile = ctx
						.getFileStreamPath(ConstantVariables.generalFilename);
				if (generalEQFile.exists()) {

					try {
						FileInputStream fis;
						fis = ctx
								.openFileInput(ConstantVariables.generalFilename);
						ObjectInputStream ois = new ObjectInputStream(fis);
						ArrayList<HashMap<String, String>> mGeneralEarthquakeList = (ArrayList<HashMap<String, String>>) ois
								.readObject();
						ois.close();
						fis.close();

						ArrayList<HashMap<String, String>> newList = new ArrayList<HashMap<String, String>>();
						Log.d("myTag",
								"merge new map, add " + EarthquakeList.size()
										+ " items.");
						if (mGeneralEarthquakeList != null
								&& mGeneralEarthquakeList.size()
										+ EarthquakeList.size() < ConstantVariables.MAX_STORED_EQ_LENGTH) {
							for (int i = 0; i < EarthquakeList.size(); ++i) {
								newList.add(i, EarthquakeList.get(i));
							}
							for (int i = 0; i < mGeneralEarthquakeList.size(); ++i) {
								newList.add(EarthquakeList.size() + i,
										mGeneralEarthquakeList.get(i));
							}
						} else {

							for (int i = 0; i < EarthquakeList.size(); ++i) {
								newList.add(i, EarthquakeList.get(i));
							}
							for (int i = EarthquakeList.size(); i < ConstantVariables.MAX_STORED_EQ_LENGTH; ++i) {
								newList.add(
										i,
										mGeneralEarthquakeList.get(i
												- EarthquakeList.size()));
							}
						}

						// //is Update so auto save
						ConstantVariables.writeEQToInternalFile(ctx,newList,ConstantVariables.generalFilename);
						return newList;

					} catch (Exception e) {
						e.printStackTrace();
						// TODO general file break.
						ConstantVariables.writeEQToInternalFile(ctx,EarthquakeList,ConstantVariables.generalFilename);
						return EarthquakeList;
					}
				} else {
					// TODO no general file break.
					ConstantVariables.writeEQToInternalFile(ctx,EarthquakeList,ConstantVariables.generalFilename);
					return EarthquakeList;
				}

			} else { // Download Foreground
				for(int i=0;i<EarthquakeList.size();++i){
					EarthquakeList.get(i).put("isNew", "false");
				}
				return EarthquakeList;
			}

		}
		// the date time is not correct set.
		return null;

	}

	private static HashMap<String, String> getHashMapFromJsonObject(JSONObject e) {
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			// Log.d("Here!!", "get new earthquake id:" + e.getString("No"));
			map.put("No", e.getString("No"));
//			map.put("CWB_ID", "CWB ID: " + e.getString("CWB_ID"));
//			map.put("ML", "ML: " + e.getString("ML"));
//			map.put("Date", "Date: " + e.getString("Date"));
//			map.put("Time", "Taipei Time: " + e.getString("Time"));
//			map.put("Depth", "Depth: " + e.getString("Depth") + " KM");
//			map.put("Longitude", "Longitude: " + e.getString("Longitude"));
//			map.put("Latitude", "Latitude: " + e.getString("Latitude"));
//			map.put("Coordinates",
//					"Coordinates: " + "(" + e.getString("Latitude") + ","
//							+ e.getString("Longitude") + ")");
//			map.put("Timestring", "Date and Time: " + e.getString("Date") + " "
//					+ e.getString("Time") + "UTC");
			map.put("DateAndTime",
					e.getString("Date") + "，" + e.getString("Time"));
			// data for drawing
			map.put("lat", e.getString("Latitude"));
			map.put("lng", e.getString("Longitude"));
			map.put("depth", e.getString("Depth"));
			map.put("ml", e.getString("ML"));
			map.put("volleyBall", e.getString("ballimg"));
			map.put("pgvlink", e.getString("pgvlink"));
			map.put("pgafilename", e.getString("pgafilename"));
			map.put("intensitymap", e.getString("intensitymap"));

			// 2014/05/12 add cuntour
			map.put("pgacontour", e.getString("pgacontour"));
			map.put("pgacontour1", e.getString("pgacontour1"));

			// data for earthquake information
			 map.put("date", e.getString("Date"));
			 map.put("time", e.getString("Time"));
			// map.put("shakemovielink", e.getString("shakemovielink"));
			// map.put("strike1", e.getString("strike1"));
			// map.put("dip1", e.getString("dip1"));
			// map.put("slip1", e.getString("slip1"));
			// map.put("model", e.getString("model"));
			// map.put("depth_", e.getString("depth"));
			// map.put("mw", e.getString("mw"));
			// map.put("rms", e.getString("rms"));
			// map.put("strike2", e.getString("strike2"));
			// map.put("dip2", e.getString("dip2"));
			// map.put("slip2", e.getString("slip2"));
			// map.put("err1", e.getString("err1"));
			// map.put("err2", e.getString("err2"));
			// map.put("err3", e.getString("err3"));
			// map.put("iso1", e.getString("iso1"));
			// map.put("iso2", e.getString("iso2"));
			// map.put("clvd1", e.getString("clvd1"));
			// map.put("clvd2", e.getString("clvd2"));
			// map.put("vr", e.getString("vr"));
			// map.put("mrr", e.getString("mrr"));
			// map.put("mtt", e.getString("mtt"));
			// map.put("mff", e.getString("mff"));
			// map.put("mrt", e.getString("mrt"));
			// map.put("mrf", e.getString("mrf"));
			// map.put("mtf", e.getString("mtf"));
			// map.put("m0", e.getString("m0"));
			// map.put("markerurl", e.getString("markerurl"));
			// // map.put("mtlink", e.getString("mtlink"));
			// map.put("deplink", e.getString("deplink"));
			// map.put("wavlink", e.getString("wavlink"));
			// map.put("nearlink", e.getString("nearlink"));
			map.put("year", e.getString("year"));
			map.put("day", e.getString("day"));
			map.put("hour", e.getString("hour"));
			map.put("timestring", e.getString("timestring"));

			String location = e.getString("Location").replaceAll("\\r\\n", "");
			// Log.d("myTag",
			// "location:"
			// + location.replaceAll("\\(", "\n\\("));
			map.put("Location", location.replace("\\(", "\t\\("));

			// 0821 add other ball
			// CMTs,gCAP,BATS,New_BATS,FMNEAR
//			map.put("CMTs", e.getString("CMTs"));
			map.put("gCAP", e.getString("gCAP"));
			map.put("BATS", e.getString("BATS"));
			map.put("New_BATS", e.getString("New_BATS"));
			map.put("FMNEAR", e.getString("FMNEAR"));

			// 1106 add history ball
//			map.put("history_img_ocean", e.getString("history_img_ocean"));
//			map.put("history_img_copper", e.getString("history_img_copper"));
//			map.put("history_img_gray", e.getString("history_img_gray"));

			// 1121 add new state
			map.put("isNew", "true");

            // 0713 add tectonic
            map.put("Tectonic", e.getString("Tectonic"));

		} catch (JSONException error) {
			Log.e("myLog", "Error parsing data " + error.toString());
			return null;
		}
		return map;
	}
}

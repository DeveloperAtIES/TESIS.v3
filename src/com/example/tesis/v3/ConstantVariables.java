package com.example.tesis.v3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class ConstantVariables {

	// Progress bar text
	public static final String[] LoadingText = { "資料下載中，請保持網路暢通。", "資料量大或網路過慢時，\n將費時較久，請耐心等候。", "第一次下載後，\n將自動儲存地震列表基本資訊，\n供離線使用。" };
	// notice: 如果有改過MAX_STORED_EQ_LENGTH，那麼generalFilename跟newestIDFilename都要改。
	public static final String generalFilename = "TESIS.earthquakeList.general.data.20150712";
	public static final String newestIDFilename = "TESIS.newest.earthquake.id.20150712";
    public static final String newestSendNFFilename = "TESIS.newist.notification.eq.id.20150712";
	// public static final String lastOpenedIDFilename =
	// "TESIS.newest.earthquake.id.last";
	public static final String settingPreferenceFilename = "TESIS.earthquakeList.setting";
	public static final String checkboxDataFilename = "TESIS.earthquakeList.checkbox.data";

	public static final int MAX_STORED_EQ_LENGTH = 50;
	public static final int CHECK_UPDATE_EVERY_N_MIN = 1;

	// setting preferneces
	// ML 0-100 => 0.0-10.0
	// Deep 0-350
	// Distance 0 => all
	// Date 0 => all, others see flags "SETTING_DATE_*" below
	public static final String nums_ML[] = { "0.0", "0.5", "1.0", "1.5", "2.0",
			"2.5", "3.0", "3.5", "4.0", "4.5", "5.0", "5.5", "6.0", "6.5",
			"7.0", "7.5", "8.0", "8.5", "9.0", "9.5", "10.0" };

	public static final String nums_Depth[] = { "0", "50", "100", "150", "200",
			"250", "300", "350" };
	// public static final String nums_Depth[] = { "0", "10", "20", "30", "40",
	// "50", "60", "70", "80", "90", "100", "110", "120", "130", "140",
	// "150", "160", "170", "180", "190", "200", "210", "220", "230",
	// "240", "250", "260", "270", "280", "290", "300", "310", "320",
	// "330", "340", "350" };

	public static final String nums_Distance[] = { "0", "50", "100", "150",
			"200", "250", "300", "350", "400", "450", "500", "1000" };
	// public static final String nums_Distance[] = { "0", "10", "20", "30",
	// "40",
	// "50", "60", "70", "80", "90", "100", "110", "120", "130", "140",
	// "150", "160", "170", "180", "190", "200", "210", "220", "230",
	// "240", "250", "260", "270", "280", "290", "300", "310", "320",
	// "330", "340", "350", "360", "370", "380", "390", "400", "410",
	// "420", "430", "440", "450", "460", "470", "480", "490", "500" , "1000" };

	public static final String nums_Date[] = { "全部", "一天", "一週", "一個月", "兩個月",
			"三個月" };
	public static final int SETTING_DATE_ALL = 0;
	public static final int SETTING_DATE_1_day = 1;
	public static final int SETTING_DATE_1_week = 2;
	public static final int SETTING_DATE_1_month = 3;
	public static final int SETTING_DATE_2_month = 4;
	public static final int SETTING_DATE_3_month = 5;

	public static final int SETTING_NOTIFICATION_STATE_ON = 1;
	public static final int SETTING_NOTIFICATION_STATE_OFF = 0;

	public static void saveSetting(Context ctx, int minML, int maxML,
			int minDeep, int maxDeep, int inDistance, int inDate, int ntfState) {
		HashMap<String, Integer> settingHashMap = new HashMap<String, Integer>();
		settingHashMap.put("minML", minML);
		settingHashMap.put("maxML", maxML);
		settingHashMap.put("minDeep", minDeep);
		settingHashMap.put("maxDeep", maxDeep);
		settingHashMap.put("inDistance", inDistance);
		settingHashMap.put("inDate", inDate);
		settingHashMap.put("ntfState", ntfState);
		// settingHashMap.put("", );

		File file = ctx.getFileStreamPath(settingPreferenceFilename);
		FileOutputStream fos;
		ObjectOutputStream oos;
		try {
			if (file.exists() || file.createNewFile()) {
				fos = ctx.openFileOutput(settingPreferenceFilename,
						ctx.MODE_PRIVATE);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(settingHashMap);
				oos.flush();
				oos.close();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("myTag", "Fail to Write setting prefernece to file.");
		}
		Log.d("myTag", "Write setting prefernece to file. ");
	}

	public static int[] loadSetting(Context ctx) {
		HashMap<String, Integer> settingHashMap;
		File file = ctx.getFileStreamPath(settingPreferenceFilename);
		if (file.exists()) {
			try {
				FileInputStream fis;
				fis = ctx.openFileInput(settingPreferenceFilename);
				ObjectInputStream ois = new ObjectInputStream(fis);
				settingHashMap = (HashMap<String, Integer>) ois.readObject();
				ois.close();
				fis.close();
				Log.d("myTag",
						"In Setting View, load settingHashMap from file."
								+ "\nminML:" + settingHashMap.get("minML")
								+ "\nmaxML:" + settingHashMap.get("maxML")
								+ "\nminDeep:" + settingHashMap.get("minDeep")
								+ "\nmaxDeep:" + settingHashMap.get("maxDeep")
								+ "\ninDate:" + settingHashMap.get("inDate")
								+ "\ninDistance:"
								+ settingHashMap.get("inDistance"));
				int[] settingValue = { settingHashMap.get("minML"),
						settingHashMap.get("maxML"),
						settingHashMap.get("minDeep"),
						settingHashMap.get("maxDeep"),
						settingHashMap.get("inDistance"),
						settingHashMap.get("inDate"),
						settingHashMap.get("ntfState") };
				return settingValue;

			} catch (Exception e) {
				e.printStackTrace();
				Log.e("myTag",
						"In Setting View, cannot load settingHashMap from file.");
				ConstantVariables.saveSetting(ctx, 0, nums_ML.length - 1, 0,
						nums_Depth.length - 1, nums_Distance.length - 1,
						SETTING_DATE_ALL, SETTING_NOTIFICATION_STATE_ON);
				int[] settingValue = { 0, nums_ML.length - 1, 0,
						nums_Depth.length - 1, nums_Distance.length - 1,
						SETTING_DATE_ALL, SETTING_NOTIFICATION_STATE_ON };
				return settingValue;
			}
		} else {
			ConstantVariables.saveSetting(ctx, 0, nums_ML.length - 1, 0,
					nums_Depth.length - 1, nums_Distance.length - 1,
					SETTING_DATE_ALL, SETTING_NOTIFICATION_STATE_ON);
			int[] settingValue = { 0, nums_ML.length - 1, 0,
					nums_Depth.length - 1, nums_Distance.length - 1,
					SETTING_DATE_ALL, SETTING_NOTIFICATION_STATE_ON };
			return settingValue;
		}
	}

	protected static boolean writeEQToInternalFile(Context ctx,
			ArrayList<HashMap<String, String>> mList, String filename) {

		if (mList == null || mList.size() <= 0) {
			Log.e("myTag", "writeEQToFile failed: EQList not exist.");
			return false;
		}

		File EQFile = ctx.getFileStreamPath(filename);
		FileOutputStream fos;
		ObjectOutputStream oos;
		try {
			if (EQFile.exists() || EQFile.createNewFile()) {
				fos = ctx.openFileOutput(filename, ctx.MODE_PRIVATE);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(mList);
				oos.flush();
				oos.close();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("myTag", "Fail to Write EQlist to file.");
			ctx.deleteFile(filename);
			return false;
		}

		File newestIDFile = ctx.getFileStreamPath(newestIDFilename);
		String newestID = mList.get(0).get("No");
		Integer tmpInteger = Integer.parseInt(newestID);
		newestID = tmpInteger.toString();
		try {
			if (newestIDFile.exists() || newestIDFile.createNewFile()) {
				fos = ctx.openFileOutput(newestIDFilename, ctx.MODE_PRIVATE);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(newestID);
				oos.flush();
				oos.close();
				fos.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ctx.deleteFile(newestIDFilename);
			Log.e("myTag", "Fail to Write newest ID to file.");
			return false;
		}
		Log.e("myTag", "writeEQToFile.");
		return true;
	}

	// protected static boolean recordLastOpenedEQtoInternalFile(Context ctx,
	// ArrayList<HashMap<String, String>> mList) {
	//
	// File lastOpenedFile = ctx.getFileStreamPath(lastOpenedIDFilename);
	// FileOutputStream fos;
	// ObjectOutputStream oos;
	// String newestID = mList.get(0).get("No");
	// Integer tmpInteger = Integer.parseInt(newestID);
	// newestID = tmpInteger.toString();
	// try {
	// if (lastOpenedFile.exists() || lastOpenedFile.createNewFile()) {
	// fos = ctx.openFileOutput(newestIDFilename, ctx.MODE_PRIVATE);
	// oos = new ObjectOutputStream(fos);
	// oos.writeObject(newestID);
	// oos.flush();
	// oos.close();
	// fos.close();
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// Log.e("myTag", "Fail to Write last opened ID to file.");
	// return false;
	// }
	// return true;
	// }

	// Handler Message
	public static final int CURRENT_LOCATION_UPDATE = 8848;
	public static final int CHECK_UPDATE = 8849;
	public static final int LOAD_RESOURCE_FINISHED = 7777;

	// MapOverlay
	public static final String[] lineName = { "山腳斷層", "湖口斷層", "新竹斷層", "新城斷層",
			"獅潭斷層", "三義斷層", "大甲斷層", "大甲斷層", "鐵砧山斷層", "屯子腳斷層", "彰化斷層", "大茅埔斷層",
			"九芎坑斷層", "梅山斷層", "大尖山斷層", "大尖山斷層", "木屐寮斷層", "六甲斷層", "觸口斷層", "觸口斷層",
			"新化斷層", "後甲里斷層", "左鎮斷層", "小崗山斷層", "旗山斷層", "潮州斷層", "潮州斷層", "恆春斷層",
			"米崙斷層", "嶺頂斷層", "瑞穗斷層", "奇美斷層", "玉里斷層", "玉里斷層", "池上斷層", "鹿野斷層",
			"利吉斷層", "利吉斷層", "", "", "", "", "", "", "", "", "", "", "",
			"車籠埔斷層", "車籠埔斷層", "車籠埔斷層" };

	public static final String[] lineName2 = { "山腳斷層", "湖口斷層", "新竹斷層", "新城斷層",
			"新城斷層", "獅潭斷層", "三義斷層", "大甲斷層", "大甲斷層", "鐵砧山斷層", "鐵砧山斷層", "屯子腳斷層",
			"彰化斷層", "大茅埔斷層", "九芎坑斷層", "梅山斷層", "大尖山斷層", "大尖山斷層", "木屐寮斷層",
			"六甲斷層", "觸口斷層", "觸口斷層", "新化斷層", "後甲里斷層", "左鎮斷層", "左鎮斷層", "左鎮斷層",
			"小崗山斷層", "旗山斷層", "潮州斷層", "潮州斷層", "恆春斷層", "米崙斷層", "嶺頂斷層", "瑞穗斷層",
			"奇美斷層", "玉里斷層", "玉里斷層", "池上斷層", "鹿野斷層", "利吉斷層", "利吉斷層",
			"車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層",
			"車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層",
			"車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層",
			"車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層", "車籠埔斷層及其支斷層",
			"車籠埔支斷層(隘寮斷層)"

	};
	/*
	 * int[] ODindex={0,1,2,16,21,22,23,25,26,27,28,29,31,36,37}; int[]
	 * OSindex={12}; int[] BDindex; int[] BSindex; int[]
	 * RDindex={3,5,6,7,8,10,17,24,30,32,33,34,35};
	 */
	// None 0,OD 1,OS 2,BD 3,BS 4,RD 5
	public static final int[] lineColor = { 1, 1, 1, 5, 0, 5, 5, 5, 5, 0, 5, 0,
			2, 0, 0, 0, 1, 5, 0, 0, 0, 1, 1, 1, 5, 1, 1, 1, 1, 1, 5, 1, 5, 5,
			5, 5, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public static final int[] lineColor2 = { 1, 1, 1, 5, 5, 0, 5, 5, 5, 5, 5,
			0, 5, 0, 2, 0, 0, 0, 1, 5, 0, 0, 0, 1, 1, 1, 1, 1, 5, 1, 1, 1, 1,
			1, 5, 1, 5, 5, 5, 5, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0 };

	// ///////////////////////////////////////////////////////////////////////
	/*
	 * wifi connections
	 */
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

	protected static int getWifiConnectionSpeed(Context ctx) {
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		if (info.isConnected()) {
			WifiManager wifiManager = (WifiManager) ctx
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo connectionInfo = wifiManager.getConnectionInfo();
			if (connectionInfo != null
					&& !TextUtils.isEmpty(connectionInfo.getSSID())) {
				return connectionInfo.getLinkSpeed();
			}
		}
		return 0;
	}
	
	public static Bitmap ScaleBitmap(Bitmap bm, float scalingFactor) {
        int scaleHeight = (int) (bm.getHeight() * scalingFactor);
        int scaleWidth = (int) (bm.getWidth() * scalingFactor);

        return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
    }


}

package com.example.tesis.v3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.Executor;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.tesis.v3.summaryActivity.downloadComments;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.R.integer;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends ActionBarActivity {

	private MyCustomAdapter mAdapter;
	ListView listView;
	SettingActivity settingActivity;

	final String SETTING_ML = "規模範圍";
	final String SETTING_DEEP = "深度範圍";
	final String SETTING_DATE = "距離現在時間";
	final String SETTING_LOCATE = "距離現在位置";
	final String SETTING_NOTIFICATION = "新地震通知";

	NumberPicker np1, np2;
	Button confirmButton;

	View currentSettingView = null;
	int currentPosition = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settingActivity = this;
		setContentView(R.layout.activity_setting);
		getSupportActionBar().setTitle("設定地震篩選條件");
		getSupportActionBar().setSubtitle("TESIS");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		listView = (ListView) findViewById(R.id.listViewInSetting);

		mAdapter = new MyCustomAdapter(settingActivity);
		mAdapter.addSeparatorItem("地震數值");
		mAdapter.addItem(SETTING_ML);
		mAdapter.addItem(SETTING_DEEP);
		mAdapter.addSeparatorItem("時間");
		mAdapter.addItem(SETTING_DATE);
		mAdapter.addSeparatorItem("地點");
		mAdapter.addItem(SETTING_LOCATE);
		// 0922 add notification
		mAdapter.addSeparatorItem("通知");
		mAdapter.addItem(SETTING_NOTIFICATION);

		listView.setAdapter(mAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				currentPosition = position;
				int[] settingValue = ConstantVariables
						.loadSetting(settingActivity);

				settingDialog = new Dialog(SettingActivity.this,
						R.style.Theme_Base_AppCompat_Dialog_Light_FixedSize);
				if (mAdapter.getItem(position).equals(SETTING_ML)) {
					settingDialog
							.setContentView(R.layout.setting_number_picker);
					settingDialog.setTitle("規模範圍 (0.0-10.0)");
					np1 = (NumberPicker) settingDialog
							.findViewById(R.id.numberPicker1);
					np1.setMaxValue(nums_ML.length - 1);
					np1.setMinValue(0);
					np1.setWrapSelectorWheel(false);
					np1.setDisplayedValues(nums_ML);
					np1.setValue(settingValue[0]);
					np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

					np2 = (NumberPicker) settingDialog
							.findViewById(R.id.numberPicker2);
					np2.setMaxValue(nums_ML.length - 1);
					np2.setMinValue(0);
					np2.setWrapSelectorWheel(false);
					np2.setDisplayedValues(nums_ML);
					np2.setValue(settingValue[1]);
					np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

					np1.setOnValueChangedListener(new OnValueChangeListener() {

						@Override
						public void onValueChange(NumberPicker picker,
								int oldVal, int newVal) {
							if (newVal > np2.getValue()) {
								np2.setValue(newVal);
							}
						}
					});

					np2.setOnValueChangedListener(new OnValueChangeListener() {

						@Override
						public void onValueChange(NumberPicker picker,
								int oldVal, int newVal) {
							if (newVal < np1.getValue()) {
								np1.setValue(newVal);
							}
						}
					});

					confirmButton = (Button) settingDialog
							.findViewById(R.id.buttonConfirm);
					confirmButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							int[] settingValue = ConstantVariables
									.loadSetting(settingActivity);
							// saveSetting(np1.getValue(), np2.getValue(),
							// settingValue[2], settingValue[3],
							// settingValue[4], settingValue[5]);
							ConstantVariables.saveSetting(settingActivity,
									np1.getValue(), np2.getValue(),
									settingValue[2], settingValue[3],
									settingValue[4], settingValue[5],
									settingValue[6]);
							TextView tx = (TextView) listView.getChildAt(
									currentPosition).findViewById(
									R.id.textViewSettingListItem2);
							tx.setText(nums_ML[np1.getValue()] + " - "
									+ nums_ML[np2.getValue()] + " ML");
							np1 = null;
							np2 = null;
							settingDialog.dismiss();
						}
					});

					settingDialog.show();

				} else if (mAdapter.getItem(position).equals(SETTING_DEEP)) {
					settingDialog
							.setContentView(R.layout.setting_number_picker);
					settingDialog.setTitle("深度範圍 (0-500)");
					np1 = (NumberPicker) settingDialog
							.findViewById(R.id.numberPicker1);
					np1.setMaxValue(nums_Depth.length - 1);
					np1.setMinValue(0);
					np1.setWrapSelectorWheel(false);
					np1.setDisplayedValues(nums_Depth);
					np1.setValue(settingValue[2]);
					np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

					np2 = (NumberPicker) settingDialog
							.findViewById(R.id.numberPicker2);
					np2.setMaxValue(nums_Depth.length - 1);
					np2.setMinValue(0);
					np2.setWrapSelectorWheel(false);
					np2.setDisplayedValues(nums_Depth);
					np2.setValue(settingValue[3]);
					np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

					np1.setOnValueChangedListener(new OnValueChangeListener() {

						@Override
						public void onValueChange(NumberPicker picker,
								int oldVal, int newVal) {
							if (newVal > np2.getValue()) {
								np2.setValue(newVal);
							}
						}
					});

					np2.setOnValueChangedListener(new OnValueChangeListener() {

						@Override
						public void onValueChange(NumberPicker picker,
								int oldVal, int newVal) {
							if (newVal < np1.getValue()) {
								np1.setValue(newVal);
							}
						}
					});

					confirmButton = (Button) settingDialog
							.findViewById(R.id.buttonConfirm);
					confirmButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							int[] settingValue = ConstantVariables
									.loadSetting(settingActivity);
							ConstantVariables.saveSetting(settingActivity,
									settingValue[0], settingValue[1],
									np1.getValue(), np2.getValue(),
									settingValue[4], settingValue[5],
									settingValue[6]);

							TextView tx = (TextView) listView.getChildAt(
									currentPosition).findViewById(
									R.id.textViewSettingListItem2);
							tx.setText(nums_Depth[np1.getValue()] + " - "
									+ nums_Depth[np2.getValue()] + " km 之內");
							np1 = null;
							np2 = null;
							settingDialog.dismiss();
						}
					});

					settingDialog.show();

				} else if (mAdapter.getItem(position).equals(SETTING_DATE)) {
					settingDialog
							.setContentView(R.layout.setting_number_picker_2);
					settingDialog.setTitle("距離現在時間");
					np1 = (NumberPicker) settingDialog
							.findViewById(R.id.numberPicker1);
					np1.setMaxValue(nums_Date.length - 1);
					np1.setMinValue(0);
					np1.setWrapSelectorWheel(false);
					np1.setDisplayedValues(nums_Date);
					np1.setValue(settingValue[5]);
					np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

					confirmButton = (Button) settingDialog
							.findViewById(R.id.buttonConfirm);
					confirmButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							int[] settingValue = ConstantVariables
									.loadSetting(settingActivity);
							ConstantVariables.saveSetting(settingActivity,
									settingValue[0], settingValue[1],
									settingValue[2], settingValue[3],
									settingValue[4], np1.getValue(),
									settingValue[6]);

							TextView tx = (TextView) listView.getChildAt(
									currentPosition).findViewById(
									R.id.textViewSettingListItem2);
							tx.setText(nums_Date[np1.getValue()] + " 之內");
							np1 = null;
							settingDialog.dismiss();
						}
					});

					settingDialog.show();
				} else if (mAdapter.getItem(position).equals(SETTING_LOCATE)) {
					settingDialog
							.setContentView(R.layout.setting_number_picker_2);
					settingDialog.setTitle("距離現在位置 (km)");
					np1 = (NumberPicker) settingDialog
							.findViewById(R.id.numberPicker1);
					np1.setMaxValue(nums_Distance.length - 1);
					np1.setMinValue(0);
					np1.setWrapSelectorWheel(false);
					np1.setDisplayedValues(nums_Distance);
					np1.setValue(settingValue[4]);
					np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

					confirmButton = (Button) settingDialog
							.findViewById(R.id.buttonConfirm);
					confirmButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							int[] settingValue = ConstantVariables
									.loadSetting(settingActivity);
							ConstantVariables.saveSetting(settingActivity,
									settingValue[0], settingValue[1],
									settingValue[2], settingValue[3],
									np1.getValue(), settingValue[5],
									settingValue[6]);

							TextView tx = (TextView) listView.getChildAt(
									currentPosition).findViewById(
									R.id.textViewSettingListItem2);
							tx.setText(nums_Distance[np1.getValue()] + "km 之內");
							np1 = null;
							settingDialog.dismiss();
						}
					});

					settingDialog.show();
				}
			}
		});

	}

	@Override
	protected void onDestroy() {
		Log.d("myTag", "In Setting View: onDestroy.");
		setResult(RESULT_OK);
		super.onDestroy();
	}

	Dialog settingDialog;

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

	// private void saveSetting(int minML, int maxML, int minDeep, int maxDeep,
	// int inDistance, int inDate) {
	// HashMap<String, Integer> settingHashMap = new HashMap<String, Integer>();
	// settingHashMap.put("minML", minML);
	// settingHashMap.put("maxML", maxML);
	// settingHashMap.put("minDeep", minDeep);
	// settingHashMap.put("maxDeep", maxDeep);
	// settingHashMap.put("inDistance", inDistance);
	// settingHashMap.put("inDate", inDate);
	// // settingHashMap.put("", );
	//
	// File file = getFileStreamPath(settingPreferenceFilename);
	// FileOutputStream fos;
	// ObjectOutputStream oos;
	// try {
	// if (file.exists() || file.createNewFile()) {
	// fos = openFileOutput(settingPreferenceFilename, MODE_PRIVATE);
	// oos = new ObjectOutputStream(fos);
	// oos.writeObject(settingHashMap);
	// oos.flush();
	// oos.close();
	// fos.close();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// Log.e("myTag", "Fail to Write setting prefernece to file.");
	// }
	// Log.d("myTag", "Write setting prefernece to file. ");
	// }

	// private int[] loadSetting() {
	// HashMap<String, Integer> settingHashMap;
	// File file = getFileStreamPath(settingPreferenceFilename);
	// if (file.exists()) {
	// try {
	// FileInputStream fis;
	// fis = openFileInput(settingPreferenceFilename);
	// ObjectInputStream ois = new ObjectInputStream(fis);
	// settingHashMap = (HashMap<String, Integer>) ois.readObject();
	// ois.close();
	// fis.close();
	// Log.d("myTag",
	// "In Setting View, load settingHashMap from file."
	// + "\nminML:" + settingHashMap.get("minML")
	// + "\nmaxML:" + settingHashMap.get("maxML")
	// + "\nminDeep:" + settingHashMap.get("minDeep")
	// + "\nmaxDeep:" + settingHashMap.get("maxDeep")
	// + "\ninDate:" + settingHashMap.get("inDate")
	// + "\ninDistance:"
	// + settingHashMap.get("inDistance"));
	// int[] settingValue = { settingHashMap.get("minML"),
	// settingHashMap.get("maxML"),
	// settingHashMap.get("minDeep"),
	// settingHashMap.get("maxDeep"),
	// settingHashMap.get("inDistance"),
	// settingHashMap.get("inDate") };
	// return settingValue;
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// Log.e("myTag",
	// "In Setting View, cannot load settingHashMap from file.");
	// ConstantVariables.saveSetting(settingActivity,0, nums_ML.length - 1, 0,
	// nums_Depth.length - 1,
	// nums_Distance.length - 1, SETTING_DATE_ALL);
	// int[] settingValue = { 0, nums_ML.length - 1, 0,
	// nums_Depth.length - 1, nums_Distance.length - 1,
	// SETTING_DATE_ALL };
	// return settingValue;
	// }
	// } else {
	// ConstantVariables.saveSetting(settingActivity,0, nums_ML.length - 1, 0,
	// nums_Depth.length - 1,
	// nums_Distance.length - 1, SETTING_DATE_ALL);
	// int[] settingValue = { 0, nums_ML.length - 1, 0,
	// nums_Depth.length - 1, nums_Distance.length - 1,
	// SETTING_DATE_ALL };
	// return settingValue;
	// }
	// }

	private class MyCustomAdapter extends BaseAdapter {
		private static final int TYPE_ITEM = 0;
		private static final int TYPE_SEPARATOR = 1;
		private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
		private ArrayList<String> mData = new ArrayList<String>();
		private LayoutInflater mInflater;

		private TreeSet mSeparatorsSet = new TreeSet();
		int[] settingValue = null;
		SettingActivity settingActivity;

		public MyCustomAdapter(SettingActivity settingActivity) {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.settingActivity = settingActivity;
			settingValue = ConstantVariables.loadSetting(settingActivity);
		}

		public void addItem(final String item) {
			mData.add(item);
			notifyDataSetChanged();
		}

		public void addSeparatorItem(final String item) {
			mData.add(item);
			// save separator position
			mSeparatorsSet.add(mData.size() - 1);
			notifyDataSetChanged();
		}

		@Override
		public int getItemViewType(int position) {
			return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR
					: TYPE_ITEM;
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_MAX_COUNT;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public String getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			int type = getItemViewType(position);

			if (convertView == null) {
				holder = new ViewHolder();
				switch (type) {
				case TYPE_ITEM:
					if (getItem(position).equals(SETTING_NOTIFICATION)) { // CheckBox
						convertView = mInflater.inflate(
								R.layout.setting_list_item2, null);
						holder.checkBox = (CheckBox) convertView
								.findViewById(R.id.checkBoxSettingListItem);
						holder.checkBox.setText(SETTING_NOTIFICATION);
						if (settingValue[6] == ConstantVariables.SETTING_NOTIFICATION_STATE_ON) {
							holder.checkBox.setChecked(true);
						} else {
							holder.checkBox.setChecked(false);
						}
						holder.checkBox
								.setOnCheckedChangeListener(new OnCheckedChangeListener() {

									@Override
									public void onCheckedChanged(
											CompoundButton buttonView,
											boolean isChecked) {
										if (isChecked) {
											// startService(new Intent(
											// settingActivity,
											// MyNotificationService.class));
											// TODO set send to 1, check if ID
											// registered
											if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
												new GCMSetSend(
														settingActivity
																.getApplicationContext(),
														GoogleCloudMessaging
																.getInstance(settingActivity
																		.getApplicationContext()),
														1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
											else
												new GCMSetSend(
														settingActivity
																.getApplicationContext(),
														GoogleCloudMessaging
																.getInstance(settingActivity
																		.getApplicationContext()),
														1).execute();

											ConstantVariables
													.saveSetting(
															settingActivity,
															settingValue[0],
															settingValue[1],
															settingValue[2],
															settingValue[3],
															settingValue[4],
															settingValue[5],
															ConstantVariables.SETTING_NOTIFICATION_STATE_ON);
										} else {
											// stopService(new Intent(
											// settingActivity,
											// MyNotificationService.class));
											if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
												new GCMSetSend(
														settingActivity
																.getApplicationContext(),
														GoogleCloudMessaging
																.getInstance(settingActivity
																		.getApplicationContext()),
														0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
											else
												new GCMSetSend(
														settingActivity
																.getApplicationContext(),
														GoogleCloudMessaging
																.getInstance(settingActivity
																		.getApplicationContext()),
														0).execute();
											ConstantVariables
													.saveSetting(
															settingActivity,
															settingValue[0],
															settingValue[1],
															settingValue[2],
															settingValue[3],
															settingValue[4],
															settingValue[5],
															ConstantVariables.SETTING_NOTIFICATION_STATE_OFF);
										}
									}
								});

					} else { // TextView
						convertView = mInflater.inflate(
								R.layout.setting_list_item, null);
						holder.textView = (TextView) convertView
								.findViewById(R.id.textViewSettingListItem1);
						TextView contentTextView = (TextView) convertView
								.findViewById(R.id.textViewSettingListItem2);
						String contentString = null;
						if (getItem(position).equals(SETTING_ML)) {
							contentString = nums_ML[settingValue[0]] + " - "
									+ nums_ML[settingValue[1]] + " ML";
						} else if (getItem(position).equals(SETTING_DEEP)) {
							contentString = nums_Depth[settingValue[2]] + " - "
									+ nums_Depth[settingValue[3]] + " km";
						} else if (getItem(position).equals(SETTING_DATE)) {
							switch (settingValue[5]) {
							case SETTING_DATE_ALL:
								contentString = "-";
								break;
							default:
								contentString = nums_Date[settingValue[5]]
										+ " 之內";
								break;
							}
						} else if (getItem(position).equals(SETTING_LOCATE)) {
							switch (settingValue[4]) {
							case 0:
								contentString = "-";
								break;

							default:
								contentString = nums_Distance[settingValue[4]]
										+ "km 之內";
								break;
							}
						}
						contentTextView.setText(contentString);
						holder.textView.setText(mData.get(position));
					}
					break;
				case TYPE_SEPARATOR:
					convertView = mInflater.inflate(
							R.layout.section_header_divider, null);
					holder.textView = (TextView) convertView
							.findViewById(R.id.textViewSectionHeader);
					holder.textView.setText(mData.get(position));
					break;
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// holder.textView.setText(mData.get(position));
			return convertView;
		}

		@Override
		public boolean areAllItemsEnabled() {
			// return super.areAllItemsEnabled();
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			if (getItemViewType(position) == TYPE_ITEM) {
				return true;
			} else {
				return false;
			}
		}

	}

	public static class ViewHolder {
		public TextView textView;
		public CheckBox checkBox;
	}
}

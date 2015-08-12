package com.example.tesis.v3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	final String tag = "myTag";
	int width, height;
	MainActivity mainActivity;
	ImageView mImage;
	Boolean isClick = false;
	Dialog infoDialog;
	String infoFilename = "TESIS.info";
	TextView textViewAgree, textViewDisagree;
	CheckBox checkBoxDoNotShowAgain;
	boolean showInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mainActivity = this;

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("TESIS");
		actionBar.setSubtitle("台灣地震科學資訊系統");

		File infoFile = getFileStreamPath(infoFilename);
		if (infoFile.exists()) {
			showInfo = false;
		} else {
			showInfo = true;
		}

		if (showInfo) {

			infoDialog = new Dialog(MainActivity.this,
					R.style.Theme_Base_AppCompat_Dialog_FixedSize);
			infoDialog.setContentView(R.layout.dialog_info);
			infoDialog.setTitle("通知");
			
			
			checkBoxDoNotShowAgain = (CheckBox) infoDialog
					.findViewById(R.id.checkBoxDoNotShowAgain);
			textViewAgree = (TextView) infoDialog
					.findViewById(R.id.textViewAgree);
			textViewAgree.setClickable(true);
			textViewDisagree = (TextView) infoDialog
					.findViewById(R.id.textViewDisagree);
			textViewDisagree.setClickable(true);

			textViewAgree.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					infoDialog.dismiss();
					if (checkBoxDoNotShowAgain.isChecked()) {
						File infoFile = getFileStreamPath(infoFilename);
						try {
							infoFile.createNewFile();
						} catch (IOException e) {
							Log.d(tag,
									"IN MainActivity: Unable to create new file");
							e.printStackTrace();
						}
						start();
					} else {
						start();
					}

				}
			});

			textViewDisagree.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					infoDialog.dismiss();
					mainActivity.finish();
				}
			});

			infoDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					mainActivity.finish();
				}
			});
			
			
			infoDialog.show();
		} else {
			start();
		}
	}

	private void start() {
		RelativeLayout mainRelativeLayout = (RelativeLayout) findViewById(R.id.main_relativeLayout);
		mainRelativeLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				isClick = true;
				Intent intent = new Intent(mainActivity, HomeActivity.class);
				startActivity(intent);
				mainActivity.finish();
			}
		});

		float density = getResources().getDisplayMetrics().density;
		Log.d(tag, "this device density is " + density);

		mImage = (ImageView) findViewById(R.id.imageView1);

		try {
			// get input stream
			InputStream ims = getAssets().open("TESIS_logo_2.5.png");
			// load image as Drawable
			BitmapFactory.Options options=new BitmapFactory.Options();
		     options.inJustDecodeBounds = false;
		     options.inSampleSize = 1;   
			Bitmap bitmap = BitmapFactory.decodeStream(ims,null,options);
//			DisplayMetrics metrics = new DisplayMetrics();
//			getWindowManager().getDefaultDisplay().getMetrics(metrics);
//			width = metrics.widthPixels;
//			height = metrics.heightPixels;
//			float dens = metrics.density;
//			float fx = metrics.xdpi;
//			float fy = metrics.ydpi;
//			Log.d(tag, width + " " + height + " " + dens + " " + fx + " " + fy);
//			// TODO case density
//			Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, width
//					* bitmap.getHeight() / bitmap.getWidth(), true);
			mImage.setImageBitmap(bitmap);
		} catch (IOException ex) {
			Log.d(tag, "load asset error.");
			ex.printStackTrace();
		}

		AnimationSet animationSet = new AnimationSet(false);
		// pass false to use different interpolator
		Animation transAnimation1 = AnimationUtils.loadAnimation(this,
				R.anim.translate_1);
		transAnimation1.setInterpolator(new AccelerateDecelerateInterpolator());
		Animation transAnimation2 = AnimationUtils.loadAnimation(this,
				R.anim.translate_2);
		transAnimation1.setInterpolator(new LinearInterpolator());
		animationSet.addAnimation(transAnimation1);
		animationSet.addAnimation(transAnimation2);
		mImage.setAnimation(animationSet);

		if (!isClick) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					mImage.setVisibility(View.INVISIBLE);
					if (!isClick) {
						Intent intent = new Intent(mainActivity,
								HomeActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.abc_fade_in,
								R.anim.abc_slide_out_top);
						mainActivity.finish();
					}
				}
			}, 4000);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

package com.example.tesis.v3;

import java.sql.Date;
import java.util.Calendar;

import android.app.Dialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class MyDoubleDatePicker implements OnDateChangedListener {
	HomeActivity homeActivity;
	private myDate From, To;
	Dialog dialogDatePicker;
	DatePicker fromDatePicker, toDatePicker;

	public MyDoubleDatePicker(HomeActivity homeActivity) {
		this.homeActivity = homeActivity;
		dialogDatePicker = new Dialog(homeActivity);
		dialogDatePicker.setContentView(R.layout.double_date_picker);
		fromDatePicker = (DatePicker) dialogDatePicker
				.findViewById(R.id.datePicker1);
		toDatePicker = (DatePicker) dialogDatePicker
				.findViewById(R.id.datePicker2);
		// fromDatePicker.updateDate(myDate.getFrom().getYear(),
		// myDate.getFrom().getMonth(), myDate.getFrom().getDay());
		fromDatePicker.init(myDate.getFrom().getYear(), myDate.getFrom()
				.getMonth()-1, myDate.getFrom().getDay(), this);
		toDatePicker.init(myDate.getTo().getYear(), myDate.getTo().getMonth()-1,
				myDate.getTo().getDay(), this);

		Button btn = (Button) dialogDatePicker
				.findViewById(R.id.buttonConfirmDate);

		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO select date
				setDate();
				// Log.d("Here!",
				// "at picker From:"+From.DatetoString()+" To:"+To.DatetoString());
				dialogDatePicker.dismiss();
			}
		});
		dialogDatePicker.setTitle("請選擇日期區間");
	}

	private void setDate() {
		DatePicker datePicker1 = (DatePicker) dialogDatePicker
				.findViewById(R.id.datePicker1);
		DatePicker datePicker2 = (DatePicker) dialogDatePicker
				.findViewById(R.id.datePicker2);
		From = new myDate(datePicker1.getYear(), datePicker1.getMonth(),
				datePicker1.getDayOfMonth());
		To = new myDate(datePicker2.getYear(), datePicker2.getMonth(),
				datePicker2.getDayOfMonth());
		// homeActivity.downloadEarthquakeData(From, To);
		homeActivity.downloadManualEQList(From, To);
	}

	public void show() {
		dialogDatePicker.show();
	}

	public myDate getFrom() {
		return From;
	}

	public myDate getTo() {
		return To;
	}

	@Override
	public void onDateChanged(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar calendarFrom = Calendar.getInstance();
		Calendar calendarTo = Calendar.getInstance();
		calendarFrom.set(fromDatePicker.getYear(), fromDatePicker.getMonth(),
				fromDatePicker.getDayOfMonth());
		calendarTo.set(toDatePicker.getYear(), toDatePicker.getMonth(),
				toDatePicker.getDayOfMonth());
		if (calendarFrom.getTimeInMillis() > calendarTo.getTimeInMillis()) {
			if (view == fromDatePicker) {
				toDatePicker.updateDate(year, monthOfYear, dayOfMonth+1);
			} else if (view == toDatePicker) {
				fromDatePicker.updateDate(year, monthOfYear, dayOfMonth-1);
			}
		}
	}

}

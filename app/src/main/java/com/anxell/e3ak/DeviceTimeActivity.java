package com.anxell.e3ak;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.anxell.e3ak.custom.My2TextView;
import com.anxell.e3ak.custom.MySwitch;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DeviceTimeActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = DeviceTimeActivity.class.getSimpleName();

    private LinearLayout mDeviceTimeContainerV;
    private My2TextView mDeviceTimeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_time);

        findViews();
        setListeners();
        getDeviceTime();
        mDeviceTimeContainerV.setVisibility(View.VISIBLE);
    }

    private void findViews() {
        mDeviceTimeContainerV = (LinearLayout) findViewById(R.id.deviceTimeContaienr);
        mDeviceTimeTV = (My2TextView) findViewById(R.id.deviceTime);
    }

    private void setListeners() {
        //MySwitch mySwitch = (MySwitch) findViewById(R.id.automaticSetting);
       // mySwitch.setOnCheckedChangeListener(this);

        mDeviceTimeTV.setOnClickListener(this);
    }

    private void getDeviceTime() {

        Calendar c = Calendar.getInstance();
        int year_dl = ((SettingActivity.tmpTime[0] <<8)& 0x0000ff00) | (SettingActivity.tmpTime[1]& 0x000000ff);
        int month_dl = (SettingActivity.tmpTime[2]);
        int day_dl = (SettingActivity.tmpTime[3]);
        int hour_dl = (SettingActivity.tmpTime[4]);
        int minute_dl = (SettingActivity.tmpTime[5]);
        c.set(Calendar.MINUTE, minute_dl);
        c.set(Calendar.DAY_OF_MONTH,day_dl);
        c.set(Calendar.YEAR,year_dl);
        c.set(Calendar.MONTH,month_dl-1);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, hour_dl);


        Date date = c.getTime();
        mDeviceTimeTV.setValue(dateTimeFormat(date));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.deviceTime:
                showDateTimePicker(mDeviceTimeTV);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionLeftToRight();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (checked) {
            mDeviceTimeContainerV.setVisibility(View.VISIBLE);
        } else {
            mDeviceTimeContainerV.setVisibility(View.GONE);
        }
    }

    private void showDateTimePicker(final My2TextView view) {
        SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                getResources().getString(R.string.device_time),
                getResources().getString(R.string.ok),
                getResources().getString(R.string.cancel)
        );
        dateTimeDialogFragment.startAtCalendarView();
        dateTimeDialogFragment.set24HoursMode(true);


        int year_dl = ((SettingActivity.tmpTime[0] <<8)& 0x0000ff00) | (SettingActivity.tmpTime[1]& 0x000000ff);
        int month_dl = (SettingActivity.tmpTime[2]-1);
        int day_dl = (SettingActivity.tmpTime[3]);
        int hour_dl = (SettingActivity.tmpTime[4]);
        int minute_dl = (SettingActivity.tmpTime[5]);
        dateTimeDialogFragment.setDefaultYear(year_dl);
        dateTimeDialogFragment.setDefaultMonth(month_dl);
        dateTimeDialogFragment.setDefaultDay(day_dl);
        dateTimeDialogFragment.setDefaultHourOfDay(hour_dl);
        dateTimeDialogFragment.setDefaultMinute(minute_dl);
        try {
            dateTimeDialogFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                view.setValue(dateTimeFormat(date));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                SettingActivity.tmpTime[0] = (byte)(calendar.get(Calendar.YEAR) >> 8);
                SettingActivity.tmpTime[1]  = (byte)(calendar.get(Calendar.YEAR) &0xFF);
                SettingActivity.tmpTime[2]  = (byte)((calendar.get(Calendar.MONTH) &0xFF)+1);
                SettingActivity.tmpTime[3]  = (byte)(calendar.get(Calendar.DAY_OF_MONTH) &0xFF);
                SettingActivity.tmpTime[4]  = (byte)(calendar.get(Calendar.HOUR_OF_DAY) &0xFF);
                SettingActivity.tmpTime[5]  = (byte)(calendar.get(Calendar.MINUTE) &0xFF);
                SettingActivity.tmpTime[6]  = (byte)(0x00);
                SettingActivity.updateStatus = SettingActivity.up_deviceTime;


            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Date is get on negative button click
            }
        });

        dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

package com.anxell.e3ak;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;

import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.transport.APPConfig;
import com.anxell.e3ak.transport.bpActivity;
import com.anxell.e3ak.util.Util;

public class ProximityReadRangeActivity2 extends bpActivity implements SeekBar.OnSeekBarChangeListener  {

    private FontTextView mDeviceDistanceTV;
    private FontTextView mDeviceCurrDistanceTV;
    private SeekBar expectLEVELBar;
    private String deviceBDAddr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximity_read_range_2);

        findViews();
        setListeners();
        Intent intent = getIntent();
        int currLevel = intent.getIntExtra(APPConfig.RSSI_LEVEL_Tag, 0);
        Util.debugMessage("Proximity","curr rssi="+currLevel,true);
        deviceBDAddr = intent.getStringExtra(APPConfig.deviceBddrTag);
        mDeviceDistanceTV.setText(""+currLevel);
        int expectLevel = loadDeviceRSSILevel(deviceBDAddr);
        mDeviceDistanceTV.setText(""+expectLevel);
        expectLEVELBar.setProgress(expectLevel);
        expectLEVELBar.setMax(20);


    }

    private void findViews() {
        mDeviceDistanceTV = (FontTextView) findViewById(R.id.deviceDistanceValue);
        mDeviceCurrDistanceTV = (FontTextView) findViewById(R.id.deviceDistance);
        expectLEVELBar = (SeekBar)findViewById(R.id.proximity_expect_seekBar);
    }

    private void setListeners() {
        ((SeekBar)findViewById(R.id.proximity_expect_seekBar)).setOnSeekBarChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionLeftToRight();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if(progress < 1){
            progress = 1;
            expectLEVELBar.setProgress( progress);
        }
        mDeviceDistanceTV.setText(progress + "");

        saveDeviceRSSILevel(deviceBDAddr,progress);

        int rssi = loadDeviceRSSILevel(deviceBDAddr);
        SettingActivity.updateStatus = SettingActivity.up_rssi_level;
        UserSettingActivity.updateStatus = UserSettingActivity.up_rssi_level;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

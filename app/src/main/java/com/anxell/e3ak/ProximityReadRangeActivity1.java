package com.anxell.e3ak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;

import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.transport.APPConfig;
import com.anxell.e3ak.transport.bpActivity;
import static com.anxell.e3ak.transport.RBLService.mBluetoothGatt;

import java.util.Timer;
import java.util.TimerTask;

public class ProximityReadRangeActivity1 extends bpActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private BroadcastReceiver receiver;
    private static FontTextView mDeviceDistanceTV;
    private FontTextView mDeviceCurrDistanceTV;
    private SeekBar expectLEVELBar;
    private String deviceBDAddr;
    private Timer timer;
    private TimerTask task;
    private Handler handle;
    private BroadcastReceiver bcReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximity_read_range_1);
        handle = new Handler();

        findViews();
        setListeners();
        Intent intent = getIntent();
//        int currLevel = intent.getIntExtra(APPConfig.RSSI_LEVEL_Tag, 0);
//        Util.debugMessage("Proximity","curr rssi="+currLevel,true);
        deviceBDAddr = intent.getStringExtra(APPConfig.deviceBddrTag);
//        mDeviceCurrDistanceTV.setText(""+currLevel);
        int expectLevel = loadDeviceRSSILevel(deviceBDAddr);
        mDeviceDistanceTV.setText(""+expectLevel);
        expectLEVELBar.setProgress(expectLevel);
        expectLEVELBar.setMax(20);



        bcReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {

                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        int device_rssi = Integer.valueOf(intent.getExtras().getString("EXTRA_DATA"));
//                        mDeviceCurrDistanceTV.setText("" + APPConfig.Convert_RSSI_to_LEVEL(device_rssi) + "("+device_rssi+")");
                        mDeviceCurrDistanceTV.setText("" + APPConfig.Convert_RSSI_to_LEVEL(device_rssi));
                    }
                });

            }
        };


        IntentFilter ifilter = new IntentFilter("TWKAZUYA");
        registerReceiver(bcReceiver,ifilter);






        task = new TimerTask() {
            @Override
            public void run() {
//                readRSSI();
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothGatt.readRemoteRssi();
                    }
                });
            }
        };

        timer = new Timer();
        timer.schedule(task, 500, 500);


//        mBluetoothGatt.readRemoteRssi();
    }

    private void findViews() {
        mDeviceDistanceTV = (FontTextView) findViewById(R.id.deviceDistanceValue);
        mDeviceCurrDistanceTV = (FontTextView) findViewById(R.id.deviceDistance);
        expectLEVELBar = (SeekBar)findViewById(R.id.seekBar);
    }

    private void setListeners() {
        findViewById(R.id.skip).setOnClickListener(this);
        ((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        findViewById(R.id.done).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.skip:
                openHomePage();
                break;

            case R.id.done:
                openHomePage();
                break;
        }
    }

    private void openHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionLeftToRight();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

        //        if(progress < 1){
//            progress = 1;
//            expectLEVELBar.setProgress( progress);
//        }
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



    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter itf = new IntentFilter("TWKAZUYA");
        this.registerReceiver(this.bcReceiver, itf);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.bcReceiver);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        task.cancel();

    }
}

package com.anxell.e3ak;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.transport.APPConfig;
import com.anxell.e3ak.transport.BPprotocol;
import com.anxell.e3ak.transport.RBLService;
import com.anxell.e3ak.transport.bpActivity;
import com.anxell.e3ak.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ProximityReadRangeActivity2 extends bpActivity implements SeekBar.OnSeekBarChangeListener  {

    private FontTextView mDeviceDistanceTV;
    private FontTextView mDeviceCurrDistanceTV;
    private SeekBar expectLEVELBar;
    private String deviceBDAddr;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner BS;
    private Timer timer;
    private TimerTask timerTask;
    public static String rssi= "";
    private int avg_count = 0;
    private int tmp_rssi = 0;

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
        mDeviceCurrDistanceTV.setText(""+currLevel);
        int expectLevel = loadDeviceRSSILevel(deviceBDAddr);
        mDeviceDistanceTV.setText(""+expectLevel);
        expectLEVELBar.setProgress(expectLevel);
        expectLEVELBar.setMax(20);


        mBluetoothManager=(BluetoothManager)this.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter=mBluetoothManager.getAdapter();
        BS =  mBluetoothAdapter.getBluetoothLeScanner();


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
//        startTimer();
        List<ScanFilter> f = new ArrayList<>() ;
//        ScanFilter filter1 = new ScanFilter.Builder().setDeviceAddress(deviceBDAddr).build();
        ScanFilter filter1 = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(RBLService.UUID_BLE_E3K_SERVICE.toString())).build();
        f.add(filter1);
        ScanSettings bleScanSetting= new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        BS.startScan(null,bleScanSetting,scanCallback);
    }






    @Override
    protected void onPause() {
        super.onPause();
//        stoptimertask();
        BS.stopScan(scanCallback);
    }

    public void startTimer() {

        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask,100,1000);

    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

//                Log.d("test", String.valueOf(System.currentTimeMillis()));

//                List<ScanFilter> f = new ArrayList<>() ;
//                ScanFilter filter1 = new ScanFilter.Builder().setDeviceAddress(deviceBDAddr).build();
//                f.add(filter1);
//
//                ScanSettings bleScanSetting= new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
//
//                final BluetoothLeScanner BS =  mBluetoothAdapter.getBluetoothLeScanner();
//
//
//
//                new Handler(getMainLooper()).postDelayed(new Runnable() { //啟動一個Handler，並使用postDelayed在10秒後自動執行此Runnable()
//                    @Override
//                    public void run() {
//                        BS.stopScan(scanCallback);
//                    }
//                },700); //SCAN_TIME為幾秒後要執行此Runnable，此範例中為10秒
//
//                BS.startScan(null,bleScanSetting,scanCallback);


//                Log.d("",deviceBDAddr);
//                new Handler(getMainLooper()).post(new Runnable(){
//                    public void run(){
//                        //處理少量資訊或UI
//                        readRSSI();
//                        mDeviceCurrDistanceTV.setText(""+deviceBDAddr);
//                    }
//                });



            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }










    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if(result.getDevice().getAddress().toString().equals(deviceBDAddr)) {
//                if (avg_count < 1)
//                {
//                    tmp_rssi += result.getRssi();
//                    avg_count += 1;
//                }
//                else
//                {
//                    tmp_rssi = tmp_rssi / 1;
//                    mDeviceCurrDistanceTV.setText("" + APPConfig.Convert_RSSI_to_LEVEL(tmp_rssi) + "(" + tmp_rssi + ")");
//                    tmp_rssi = 0;
//                    avg_count = 0;
//                }

//                mDeviceCurrDistanceTV.setText("" + APPConfig.Convert_RSSI_to_LEVEL(result.getRssi()) + "(" + result.getRssi() + ")");
                mDeviceCurrDistanceTV.setText("" + APPConfig.Convert_RSSI_to_LEVEL(result.getRssi()));
//                String msg = "" + (20 - APPConfig.Convert_RSSI_to_LEVEL(result.getRssi()));
//                Toast.makeText(ProximityReadRangeActivity2.this,msg,Toast.LENGTH_SHORT).show();
            }


        }



    };








}

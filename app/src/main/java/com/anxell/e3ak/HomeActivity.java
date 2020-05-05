package com.anxell.e3ak;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.Process;
import android.provider.Settings;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AlertDialog;
import android.text.AndroidCharacter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anxell.e3ak.custom.FontButton;
import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.custom.MyButton;
import com.anxell.e3ak.custom.MyDeviceView;
import com.anxell.e3ak.transport.APPConfig;
import com.anxell.e3ak.transport.AdminMenu;
import com.anxell.e3ak.transport.BPprotocol;
import com.anxell.e3ak.transport.RBLService;
import com.anxell.e3ak.transport.ScanItem;
import com.anxell.e3ak.transport.ScanItemData;
import com.anxell.e3ak.transport.bpActivity;
import com.anxell.e3ak.util.Util;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HomeActivity extends bpActivity implements View.OnClickListener {
    private String TAG = HomeActivity.class.getSimpleName().toString();
    private Boolean debugFlag = false;
    private PercentRelativeLayout mFoundV;
    private MyDeviceView mDeviceV;
    private FontTextView mDoorStatusTV;
    private RelativeLayout mNotFoundV;
    private RelativeLayout mSearchingV;
    private ImageButton mDoorIB;
    private MyButton mOpenBtn;
    private ImageButton mOpenedIB;
    private ImageButton mRefreshIB;
    private PercentRelativeLayout mProgressBarV;
    private FontTextView mAutoOpenTV;
    private View mCurrentView;
    private boolean mIsAutoOpen;
    private static BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings mLEScanSettings;
    private List<ScanFilter> mLEScanFilters;
    private ScanItemData deviceInfoList = new ScanItemData();
    private static final int REQUEST_ENABLE_BT = 1;
    private int reconnectTime = 6; // 6 sec
    private BluetoothDevice forceDevice;
    private boolean isForce = false;
    private boolean isOpenDoor = false;
    private boolean isAutoMode = false;
    public static boolean isEnroll = false;
    private boolean isKeepOpen = false;
    private boolean ScanningTimerFlag = false;
    private boolean bgAutoTimerflag = false;
    private Thread scaningTimer = null;
    private Thread bgAutoTimer  = null;
    private Handler disConTimer = new Handler();
    private Handler  ConTimer = new Handler();
    public  static String befSettingBDAddr = "";
    public  static boolean isEditDeviceName = false;
    private static boolean  isConService = false;
    private boolean isReady = false;
    private boolean isScanning = false;
    private boolean isCheckConnection = false;
    private Thread CheckConnection = null;
    private Handler handler;
    private FontTextView mModelTV;
    private ImageView infoOperatingIV;
    private TextView mAutoRangeSettingValueV;

    private static final int REQUEST_ENABLE_LOCATION = 2;
    private boolean isShowing_BT_dialog = false;
    private boolean isShowing_GPS_dialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Initial(getLocalClassName());
        setContentView(R.layout.activity_home);

        handler = new Handler();
        findViews();
        setListeners();

        mCurrentView = mFoundV;

        showModel("ABC123");
        // start demo
        // mDeviceV.setDeviceName("E3AK001");
        Intent intent = getIntent();
        intent.setClass(this,RBLService.class);
        bindService(intent, ServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver,  getIntentFilter());
        initBLE();
        currentClassName = getLocalClassName();
        mDeviceV.setDeviceName("    ");
        mDeviceV.setStatusDotEnable(false);
        isConService = false;
        mCurrentView = mSearchingV;
        String PhoneModel = android.os.Build.MODEL;
        Util.debugMessage(TAG,"PhoneModel ="+PhoneModel,debugFlag);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Util.debugMessage(TAG,"onRestart",debugFlag);
        if(!isConService){
            currentClassName = getLocalClassName();
            Intent intent = getIntent();
            intent.setClass(this,RBLService.class);
            bindService(intent, ServiceConnection, BIND_AUTO_CREATE);
            IntentFilter filter = getIntentFilter();
            filter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
            registerReceiver(mGattUpdateReceiver, filter);
            //deviceInfoList.scanItems.clear();
            isCheckConnection = true;
            if(isEditDeviceName) {
                BLE_Scanner_Start(false);


                Handler scanTimer = new Handler();
                scanTimer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BLE_Scanner_Start(true);
                        isEditDeviceName = false;

                    }
                },2000);

            }


        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Util.debugMessage(TAG,"onStop",debugFlag);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        int id= android.os.Process.myPid();
        android.os.Process.killProcess(id);
    }

    private void initBLE(){
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            //show_toast_msg("BLE Service not supported");
            finish();
            return;
        } else {
            if (!mBluetoothAdapter.isOffloadedScanBatchingSupported()) {
                Util.debugMessage(TAG,"Not support ScanBatching!!",debugFlag);
            }
            //if(!mBluetoothAdapter.isEnabled())
            // mBluetoothAdapter.enable();
            //Get System BLE MAC Address
            update_system_ble_mac_addrss();

        }


    }


    public void checkEventClose(){

        CheckConnection = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    while(isCheckConnection) {

                        if(getConnectedDevices().size()>0){

                            if(!isOpenDoor && !isEnroll &&!isKeepOpen){
                                Util.debugMessage(TAG,"disconnect not success",debugFlag);

                                disconnect();
                                runOnUiThread(checkEventTask);
                            }
                        }
                        try {
                            Thread.sleep(1000);

                        } catch (java.lang.InterruptedException e) {

                        }
                    }
                    try {
                        Thread.sleep(1000);

                    } catch (java.lang.InterruptedException e) {

                    }
                }
            }
        });
        CheckConnection.start();


    }
    private Runnable checkEventTask = new Runnable() {
        @Override
        public void run() {
            disconnectUpdate();
        }
    };
    @Override
    public void getERROREvent(String bdAddress){
        connect(bdAddress);
        StartConnectTimer();
    }

    @Override
    public void noSuchElement(){
        if(isAutoMode){
            bpProtocol.reInitQueue();
            StopBgAutoTimer();

            try{
                Thread.sleep(1000);

            }catch (java.lang.InterruptedException e){
            }

            StartBgAutoTimer();
        }



    }
    @Override
    public void update_service_connect() {
        super.update_service_connect();
        Util.debugMessage(TAG,"service connection",debugFlag);
        isConService = true;
        isAutoMode = sharedPreferences.getBoolean(APPConfig.isAutoTag,false);

        if(isAutoMode){
            Util.debugMessage(TAG,"auto on",debugFlag);
//            StopScanningTimer();
            CreateBgAutoTimer();
            StartBgAutoTimer();
//            mAutoOpenTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_tick, 0, 0, 0);
            mAutoOpenTV.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.checkbox_tick,0,0,0);
        }

        String PhoneModel2 = Build.MANUFACTURER;

        //if(PhoneModel2.equals("OPPO")){


        isCheckConnection = true;
        checkEventClose();
        //}
        Util.debugMessage(TAG,"MANUFACTURER="+PhoneModel2,debugFlag);


    }

    @Override
    protected void onResume() {
        super.onResume();

        //isActive = true;

        check_BT_HomeActivity(true);

        // ClearNotification();

        // mIsBackGround_Mode = false;
        // registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private void BLE_Scanner_Start(boolean option) {



        if (option) {

            Util.debugMessage(TAG,"le scan start",debugFlag);
            mLEScanner.startScan(null, mLEScanSettings, mScanCallback);
            isScanning = true;
            //mLEScanner.startScan(mScanCallback);


        } else {

            mLEScanner.stopScan(mScanCallback);
            isScanning = false;
            Util.debugMessage(TAG,"stop BLE Scan",debugFlag);

        }
    }
    /*public boolean removeBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public boolean cancelBondProcess(
                                     BluetoothDevice device)

            throws Exception
    {   Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }*/


    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if (result.getScanRecord().getManufacturerSpecificData(93) == null)
            {
                return;
            }

            //super.onScanResult(callbackType, result);
            int duplicate_idx = -1;

            //Util.debugMessage(TAG,"onScanResult: NEW Address = [" + result.getDevice().getAddress() + "], RSSI = " + result.getRssi() + ".",debugFlag);

            // Util.debugMessage(TAG, "onScanResult: Check RSSI. Taget = " + AutoMode_DetectRSSI + ", Device = " + result.getRssi() + ".",debugFlag);
            //Util.debugMessage(TAG,"deviceName="+result.getDevice().getName(),debugFlag);
            //Reload Period Scan Refresh Count
            //  mPeriodScanRefreshCnt = mPeriodScanRefresh_Max;
            BluetoothDevice test = mBluetoothAdapter.getRemoteDevice(result.getDevice().getAddress());
            // Util.debugMessage(TAG,"test deviceName="+test.getName()+"bd Addr="+test.getAddress(),debugFlag);

            /////123123123///////

            byte rawData[] = result.getScanRecord().getBytes();
//            byte mandufacturerData[] = result.getScanRecord().getManufacturerSpecificData(0x0000);
            SparseArray<byte[]> mandufacturerData = result.getScanRecord().getManufacturerSpecificData();

            if (debugFlag) {
                int count = 0;
                for (byte tmp : rawData){
                    Util.debugMessage(TAG, String.format("Data[%d]=%02x", count, tmp), true);
                    count++;
                }
            }


//            String customID = Integer.toHexString(mandufacturerData.keyAt(0) & 0x00FF | 0xFF00);
            String companyID = Integer.toHexString(mandufacturerData.keyAt(0) & 0xFFFF | 0x0000);
            companyID = Util.padRight(companyID,4,'0');




//            String customID = Util.bytesToHex(Arrays.copyOfRange(customID_byte ,0,2));
            Util.debugMessage(TAG,"companyID="+companyID,debugFlag);


            String customID = Util.bytesToHex(Arrays.copyOfRange(mandufacturerData.valueAt(0),3,5));
            Util.debugMessage(TAG,"customID="+customID,debugFlag);

            String customIDStr = (String)APPConfig.advertisingData.CUSTOM_IDs.get(customID.toUpperCase());
            Util.debugMessage(TAG,"customIDStr="+customIDStr,debugFlag);
            if(customIDStr==null)
                return;


//            if  (customIDStr != APPConfig.CustomID.toUpperCase())
//            {
//                return;
//            }


//            String deviceModel = Util.bytesToHex(Arrays.copyOfRange(mandufacturerData.valueAt(0),0,2));
//            Util.debugMessage(TAG,"deviceModel="+deviceModel,debugFlag);
            String deviceCategory = Util.bytesToHex(Arrays.copyOfRange(mandufacturerData.valueAt(0),2,3));
            Util.debugMessage(TAG,"deviceCategory="+deviceCategory,debugFlag);

            String deviceReserved = Util.bytesToHex(Arrays.copyOfRange(mandufacturerData.valueAt(0),5,6));
            Util.debugMessage(TAG,"deviceReserved="+deviceReserved,debugFlag);


//            String deviceModelStr = (String)APPConfig.advertisingData.dev_Model.get(deviceModel.toUpperCase());
//            if(deviceModelStr==null) {
//                return;
//            }
//            Util.debugMessage(TAG,"deviceModelStr="+deviceModelStr,debugFlag);
            String deviceCategoryStr = (String)APPConfig.advertisingData.dev_Category.get(deviceCategory.toUpperCase());
            if(deviceCategory==null)
            {
                return;
            }

            Util.debugMessage(TAG,"deviceCategoryStr="+deviceCategoryStr,debugFlag);
//            String deviceColorStr = (String)APPConfig.advertisingData.dev_Color.get(deviceColor.toUpperCase());
//            if (deviceColor==null)
//                return;
//            Util.debugMessage(TAG,"deviceColorStr="+deviceColorStr,debugFlag);
            String deviceReservedStr = (String)APPConfig.advertisingData.dev_Reserved.get(deviceReserved.toUpperCase());
            if(deviceReservedStr==null) {
                return;
            }
            Util.debugMessage(TAG,"deviceReservedStr="+deviceReservedStr,debugFlag);

            ///0505
            if(APPConfig.CustomID.equals(customIDStr)) {
                return;
            }
//            if(!deviceModelStr.contains(APPConfig.deviceSeries))
//            {
//                return;
//            }
//            if(!APPConfig.deviceSeries.contains(deviceModelStr))   //easiprox特殊比對
//            {
//                return;
//            }

            /////123123123///////

            if(result.getDevice().getAddress().toString().substring(0,8).equals(BPprotocol.bp_address)) {
                ScanItem tmpScanItem_FullRange = new ScanItem(result.getDevice().getName(), result.getDevice().getAddress(), result.getRssi(), 18);
                //Util.debugMessage(TAG, "deviceName=" + result.getDevice().getName(), debugFlag);



                int i = 0;


                //Check Duplicate
                duplicate_idx = deviceInfoList.check_device_exist_by_addr(result.getDevice().getAddress());

                if (duplicate_idx >= 0) {
                    //Duplicate
                    //Check RSSI

                    if (tmpScanItem_FullRange.rssi >= APPConfig.E3K_DEVICES_BLE_RSSI_MIN) {
                        //AVG RSSI
                        tmpScanItem_FullRange.rssi = (tmpScanItem_FullRange.rssi + deviceInfoList.scanItems.get(duplicate_idx).rssi) / 2;
                        //Replace Item
                        deviceInfoList.scanItems.set(duplicate_idx, tmpScanItem_FullRange);

                        //Log.v(TAG, "Replace: = [" + tmpScanItem.dev_addr + "], RSSI = " + tmpScanItem.rssi + ".");
                    } /*else {

                            deviceInfoList.scanItems.remove(duplicate_idx);
                        }*/
                } else {
                    //New
                    //Check RSSI
                    if (tmpScanItem_FullRange.rssi >= APPConfig.E3K_DEVICES_BLE_RSSI_MIN) {
                        deviceInfoList.scanItems.add(tmpScanItem_FullRange);
                        //mDeviceV.setDeviceName(tmpScanItem_FullRange.dev_name);
                        if(!checkDeviceLevelExist(tmpScanItem_FullRange.dev_addr))
                            saveDeviceRSSILevel(tmpScanItem_FullRange.dev_addr,APPConfig.E3K_DEVICES_BLE_RSSI_LEVEL_DEFAULT);
                    }
                }


                Collections.sort(deviceInfoList.scanItems, new Comparator<ScanItem>() {
                    @Override
                    public int compare(ScanItem lhs, ScanItem rhs) {
                        return rhs.rssi - lhs.rssi;
                    }
                });

            }

            if(deviceInfoList.size() > 0 && !isOpenDoor &&!isEnroll) {

                // Util.debugMessage(TAG,"update home UI",debugFlag);
                if(!isForce)
                {
                    mDeviceV.setDeviceName(deviceInfoList.scanItems.get(0).dev_name);
                    mModelTV.setText(getResources().getText(R.string.device_distance) + " : " + APPConfig.Convert_RSSI_to_LEVEL(deviceInfoList.scanItems.get(0).rssi));
                }
                else{

                    if(forceDevice.getAddress().equals(result.getDevice().getAddress().toString())){
                        if(!mDeviceV.getDeviceName().equals(result.getDevice().getName())){
                            mDeviceV.setDeviceName(result.getDevice().getName());
                            forceDevice = result.getDevice();
                        }

                    }
                    mModelTV.setText(getResources().getText(R.string.device_distance) + " : " + APPConfig.Convert_RSSI_to_LEVEL(deviceInfoList.scanItems.get(0).rssi));
                }
                updateView(mFoundV);
                mDeviceV.setStatusDotEnable(true);
                int expectLEVEL = loadDeviceRSSILevel(getTargetDevice().getAddress());
                int currrentLEVEL = APPConfig.Convert_RSSI_to_LEVEL(getCurrLevel(getTargetDevice().getAddress()));
                mModelTV.setText(getResources().getText(R.string.device_distance) + " : " + currrentLEVEL);
                mAutoRangeSettingValueV.setText(getResources().getText(R.string.proximity_read_range_setting)+" : "+expectLEVEL);

            }


        }



        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            //super.onBatchScanResults(results);
            Util.debugMessage(TAG,"onBatchScanResults: " + results.size() + " results",debugFlag);

            for (ScanResult result : results) {
                Util.debugMessage(TAG,"onBatchScanResults: NEW Address = [" + result.getDevice().getAddress() + "], RSSI = " + result.getRssi() + ".",debugFlag);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {

            Util.debugMessage(TAG, "onScanFailed: Error Code = " + errorCode,debugFlag);

            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    //nki_BLE_Scanner_Start(false);
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                case SCAN_FAILED_INTERNAL_ERROR:
                    //nki_BLE_Scanner_Start(false);
                    break;
            }

            super.onScanFailed(errorCode);
        }
    };

    @Override
    public void BLEReady() {
        super.BLEReady();
        isReady = true;

        ConTimer.removeCallbacksAndMessages(null);


        if(!isEnroll)
            bpProtocol.queueClear();
        Util.debugMessage(TAG,"the device is connected ",debugFlag);


        boolean isAdminConfig_Latest = sharedPreferences.getBoolean(getBluetoothDeviceAddress(), false);


        if(isOpenDoor) {
            if(isAdminConfig_Latest){
                bpProtocol.setAdminIdentify(mSYS_BLE_MAC_Address_RAW);
                Util.debugMessage(TAG,"Admin put cmd",debugFlag);

            }
            else {
                int userIndex = sharedPreferences.getInt(BPprotocol.indexTag+getBluetoothDeviceAddress(),0);
                bpProtocol.setUserIdentifyDirect(mSYS_BLE_MAC_Address_RAW, false, userIndex);
                Util.debugMessage(TAG,"user put cmd",debugFlag);
            }

        }else if(isKeepOpen){
            bpProtocol.getConfig();
        }

    }



    @Override
    public void cmdDataCacheEvent(byte cmd, byte cmdType,byte data[], int datalen){
        switch ((char) cmd) {
            case BPprotocol.cmd_admin_enroll:
            case BPprotocol.cmd_user_enroll:
            case BPprotocol.cmd_admin_indentify:
            case BPprotocol.cmd_user_indentify:


                Util.debugMessage(TAG,"create disconnect timer",debugFlag);
                disConTimer.postDelayed(DisConTask,APPConfig.disTimeOut);
                 /*   disConTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Util.debugMessage(TAG,"disconnect start",debugFlag);
                                    if(!isKeepOpen)
                                    ForceDisconnect();


                                }
                            });


                        }
                    },APPConfig.disTimeOut);
*/

                break;

            /*
            case BPprotocol.cmd_device_config:
                if (cmd[1] == (byte) BPprotocol.type_write) {
                    tmpConfig = Arrays.copyOf(data,data.length);
                    for(byte tmp:data)
                        Util.debugMessage(TAG,String.format("%02x",tmp),debugFlag);
                    for(byte tmp:tmpConfig)
                        Util.debugMessage(TAG,"data="+String.format("%02x",tmp),debugFlag);
                }

                break;*/


            default:


        }

    }
    private Runnable DisConTask = new Runnable() {
        @Override
        public void run() {
            Util.debugMessage(TAG,"disconnect start",debugFlag);
            if(!isKeepOpen)
                ForceDisconnect();
        }
    };
    private Runnable ConTask = new Runnable() {
        @Override
        public void run() {
            if(!isReady && isOpenDoor){
                Util.debugMessage(TAG,"connect time out1",debugFlag);
                isOpenDoor = false;
                isKeepOpen = false;

                ForceDisconnect();

            }
        }
    };
    @Override
    public void cmdAnalysis(byte cmd, byte cmdType, byte data[], int datalen){

        switch ((char) cmd) {

            case BPprotocol.cmd_user_enroll:

                if (datalen > 1) {
                    int UserIndex = 0;
                    UserIndex = encode.getUnsignedTwoByte(data);

                    Util.debugMessage(TAG,"User enroll index=" + UserIndex,debugFlag);

                    sharedPreferences.edit().putInt(BPprotocol.indexTag+getBluetoothDeviceAddress(), UserIndex).commit();
                    Util.debugMessage(TAG,"Enroll OK",debugFlag);

                    sharedPreferences.edit().putBoolean(getBluetoothDeviceAddress(), false).commit();

                    show_toast_msg(getString(R.string.eroll_success));

                } else {
                    show_toast_msg(getString(R.string.eroll_fail));
                    Util.debugMessage(TAG, "Enroll fail",debugFlag);
                }


                break;

            case BPprotocol.cmd_device_config:
                if (cmdType == (byte) BPprotocol.type_read) {

                    boolean door_sensor_opt = (data[0] != 0);
                    byte lock_type = data[1];
                    int delay_secs = ((data[2] << 8) & 0x0000ff00) | (data[3] & 0x000000ff);
                    boolean ir_sensor_opt = (data[4] != 0);

                    if (lock_type == BPprotocol.door_statis_KeepOpen) {
                        Util.debugMessage(TAG, "delay Time", debugFlag);
                        lock_type = BPprotocol.door_statis_delayTime;

                    } else {
                        Util.debugMessage(TAG, "KeepOpen", debugFlag);
                        lock_type = BPprotocol.door_statis_KeepOpen;

                    }
                    Util.debugMessage(TAG, "send new config", debugFlag);

                    bpProtocol.setConfig(door_sensor_opt, lock_type, delay_secs, ir_sensor_opt);

                }else if (cmdType == (byte) BPprotocol.type_write)
                    ForceDisconnect();




                break;



            case BPprotocol.cmd_fw_version:
//                int major, minor;
//
//                //Get Data
//                major = data[0];
//                minor = data[1];
//                if (major == 1 && minor >= 6){
//                    Util.debugMessage(TAG,"exeForce disconnect",debugFlag);
//                    bpProtocol.executeForceDisconnect();
//                }else
//                    disconnect();

                bpProtocol.executeForceDisconnect();
                break;
            case BPprotocol.cmd_admin_enroll:


                if (data[0] == BPprotocol.result_success) {
                    show_toast_msg(getString(R.string.eroll_success));
                    sharedPreferences.edit().putBoolean(getBluetoothDeviceAddress(), true).apply();
                }
                else {
                    show_toast_msg(getString(R.string.eroll_fail));
                    Util.debugMessage(TAG, "ADMIN ENROLL FAIL", debugFlag);
                }
                break;
            case BPprotocol.cmd_admin_indentify:
            case BPprotocol.cmd_user_indentify:
                if(!isAutoMode){
                    switch(data[0]){

                        case BPprotocol.open_fail_no_eroll:
                            show_toast_msg(getString(R.string.open_fail_no_eroll));
                            break;

                        case BPprotocol.open_fail_PD:
                            show_toast_msg(getString(R.string.open_fail_permission_denied));
                            break;

                    }
                }

                break;

            default:
                Util.debugMessage(TAG,"cmd="+String.format("%02x",cmd),debugFlag);

        }

        stop_anime();

//        switch ((char) cmd) {
//
//            case BPprotocol.cmd_user_enroll:
//
//                if (datalen > 1) {
//                    int UserIndex = 0;
//                    UserIndex = encode.getUnsignedTwoByte(data);
//
//                    Util.debugMessage(TAG,"User enroll index=" + UserIndex,debugFlag);
//
//                    sharedPreferences.edit().putInt(BPprotocol.indexTag+getBluetoothDeviceAddress(), UserIndex).commit();
//                    Util.debugMessage(TAG,"Enroll OK",debugFlag);
//
//                    sharedPreferences.edit().putBoolean(getBluetoothDeviceAddress(), false).commit();
//
//                    show_toast_msg(getString(R.string.eroll_success));
//
//                } else {
//                    show_toast_msg(getString(R.string.eroll_fail));
//                    Util.debugMessage(TAG, "Enroll fail",debugFlag);
//                }
//
//
//                break;
//
//            case BPprotocol.cmd_device_config:
//                if (cmdType == (byte) BPprotocol.type_read) {
//
//                    boolean door_sensor_opt = (data[0] != 0);
//                    byte lock_type = data[1];
//                    int delay_secs = ((data[2] << 8) & 0x0000ff00) | (data[3] & 0x000000ff);
//                    boolean ir_sensor_opt = (data[4] != 0);
//
//                    if (lock_type == BPprotocol.door_statis_KeepOpen) {
//                        Util.debugMessage(TAG, "delay Time", debugFlag);
//                        lock_type = BPprotocol.door_statis_delayTime;
//
//                    } else {
//                        Util.debugMessage(TAG, "KeepOpen", debugFlag);
//                        lock_type = BPprotocol.door_statis_KeepOpen;
//
//                    }
//                    Util.debugMessage(TAG, "send new config", debugFlag);
//
//                    bpProtocol.setConfig(door_sensor_opt, lock_type, delay_secs, ir_sensor_opt);
//
//                }else if (cmdType == (byte) BPprotocol.type_write)
//                    ForceDisconnect();
//
//
//
//
//                break;
//
//
//
//            case BPprotocol.cmd_fw_version:
//
//
//                Util.debugMessage(TAG,"exeForce disconnect",debugFlag);
//                bpProtocol.executeForceDisconnect();
//
//                break;
//            case BPprotocol.cmd_admin_enroll:
//
//                if (data[0] == BPprotocol.result_success) {
//                    show_toast_msg(getString(R.string.eroll_success));
//                    sharedPreferences.edit().putBoolean(getBluetoothDeviceAddress(), true).apply();
//                }
//                else {
//                    show_toast_msg(getString(R.string.eroll_fail));
//                    Util.debugMessage(TAG, "ADMIN ENROLL FAIL", debugFlag);
//                }
//                break;
//
//            case BPprotocol.cmd_admin_indentify:
//            case BPprotocol.cmd_user_indentify:
//                if(!isAutoMode){
//                    switch(data[0]){
//
//                        case BPprotocol.open_fail_no_eroll:
//                            show_toast_msg(getString(R.string.open_fail_no_eroll));
//                            break;
//
//                        case BPprotocol.open_fail_PD:
//                            show_toast_msg(getString(R.string.open_fail_permission_denied));
//                            break;
//
//                    }
//                }
//
//
//
//                break;
//
//
//            default:
//                Util.debugMessage(TAG,"cmd="+String.format("%02x",cmd),debugFlag);
//
//        }
//
//        stop_anime();
    }


    @Override
    public void disconnectUpdate() {


        Calendar time = Calendar.getInstance();

        int min = time.get(Calendar.MINUTE);
        int sec =  time.get(Calendar.SECOND);
        int disconTime = min *60 + sec;
        Util.debugMessage(TAG,"disconnected diconTime="+disconTime+"bdtag="+getBluetoothDeviceAddress()+"Time",debugFlag);
        sharedPreferences.edit().putInt(getBluetoothDeviceAddress()+"Time", disconTime).apply();
        isOpenDoor = false;
        isEnroll = false;
        isKeepOpen = false;
        BLESocketClose();
        bpProtocol.queueClear();

        if(isAutoMode&&!bgAutoTimerflag)
            StartBgAutoTimer();
        else if(!ScanningTimerFlag)
            StartScanningTimer();
        mDoorIB.setImageResource(R.drawable.door_close);
        mDoorStatusTV.setText(R.string.door_closed);
        mDoorStatusTV.setTextColor(getResources().getColor(android.R.color.darker_gray));
        mOpenBtn.setVisibility(View.VISIBLE);
        mOpenedIB.setVisibility(View.GONE);

        stop_anime();
    }

    private void findViews() {
        mFoundV = (PercentRelativeLayout) findViewById(R.id.foundView);
        mDeviceV = (MyDeviceView) findViewById(R.id.deviceView);
        mDoorStatusTV = (FontTextView) findViewById(R.id.doorStatus);
        mNotFoundV = (RelativeLayout) findViewById(R.id.notFoundView);
        mSearchingV = (RelativeLayout) findViewById(R.id.searchingView);
        mDoorIB = (ImageButton) findViewById(R.id.door);
        mOpenBtn = (MyButton) findViewById(R.id.open);
        mOpenedIB = (ImageButton) findViewById(R.id.opened);
        mRefreshIB = (ImageButton) findViewById(R.id.refresh);
        mProgressBarV = (PercentRelativeLayout) findViewById(R.id.progressBar);
        mAutoOpenTV = (FontTextView) findViewById(R.id.autoOpen);
        mModelTV = (FontTextView) findViewById(R.id.model);
        mAutoRangeSettingValueV = (TextView) findViewById(R.id.auto_range_setting);
    }

    private void setListeners() {
        findViewById(R.id.relogin).setOnClickListener(this);
        findViewById(R.id.setup).setOnClickListener(this);
        mDeviceV.setOnClickListener(this);
        mDoorIB.setOnClickListener(this);
        mOpenBtn.setOnClickListener(this);
//        mOpenBtn.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                String deviceAddr;
//
//                if(deviceInfoList.size() > 0){
//                    deviceAddr= getTargetDevice().getAddress();
//
//                    if(!deviceAddr.equals("")&&!isAutoMode){
//                        boolean isAdmin = sharedPreferences.getBoolean(deviceAddr,false);
//
//                        if(isAdmin) {
//
//                            isKeepOpen = true;
//                            BLE_Scanner_Start(true);
//                            if(connect(deviceAddr))
//                                Util.debugMessage(TAG,"open btn connect ok",debugFlag);
//                            else
//                                Util.debugMessage(TAG,"connect fail",debugFlag);
//                            StartConnectTimer();
//                        }
//                    }
//                }
//
//                return true;
//            }
//
//        });
        mRefreshIB.setOnClickListener(this);
        mAutoOpenTV.setOnClickListener(this);

        // start demo
        findViewById(R.id.search).setOnClickListener(this);
        findViewById(R.id.notFound).setOnClickListener(this);
        findViewById(R.id.found).setOnClickListener(this);
        // end demo
    }

    private void showModel(String model) {
        Resources res = getResources();
        String text = String.format(res.getString(R.string.model), model);

        FontTextView modelTV = (FontTextView) findViewById(R.id.model);
        modelTV.setText(text);
        modelTV.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        findViewById(R.id.relogin).setEnabled(false);
        findViewById(R.id.setup).setEnabled(false);

        switch (view.getId()) {
            case R.id.deviceView:
                if(deviceInfoList.size()>0)
                    showDevices();
                break;

            case R.id.relogin:
                //openPasswordPage();
                if (!isAutoMode){
                    BLE_Scanner_Start(false);
//                    StopScanningTimer();
                    if (deviceInfoList.size() >0 && !isEnroll && !isOpenDoor) {
                        AdminMenu adminMenu = new AdminMenu(this, mFoundV, bpProtocol);
                        String bdAddr = "";
                        bdAddr = getTargetDevice().getAddress();
                        adminMenu.UsersEnrollDialog(mSYS_BLE_MAC_Address_RAW,APPConfig.ADMIN_ENROLL, bdAddr);
                    }else{
                        StartScanningTimer();
                    }

                }else{

                    show_toast_msg(getString(R.string.AUTO_ENABLE_CONFLICT));
                }
                break;

            case R.id.setup:
                if (!isAutoMode){
//                    StopScanningTimer();
                    if (deviceInfoList.size() >0 && !isEnroll && !isOpenDoor) {
                        BLE_Scanner_Start(false);
                        isCheckConnection = false;
                        openSettingPage();
                    }else{
                        StartScanningTimer();
                    }

                }else{

                    show_toast_msg(getString(R.string.AUTO_ENABLE_CONFLICT));
                }


                break;

            case R.id.door:
            case R.id.open:

                openDoor();
                break;

            case R.id.autoOpen:
                //if(deviceInfoList.size()>0)
                updateStatus();
                break;

            // start demo
            /*case R.id.search:
                showSearchDialog();
                updateView(mSearchingV);
                break;*/

           /* case R.id.notFound:
                updateView(mNotFoundV);
                break;
*/
            case R.id.found:
                updateView(mFoundV);
                break;
            // end demo
        }



        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.relogin).setEnabled(true);
                findViewById(R.id.setup).setEnabled(true);
            }
        },500);

    }

    private void openDoor() {
        if(!isAutoMode) {
            if(!isOpenDoor && !isEnroll){



                if(deviceInfoList.size() > 0) {
                    BLE_Scanner_Start(false);
                    String targetAddr = getTargetDevice().getAddress();


                    if (!targetAddr.equals("")) {
                        mDoorStatusTV.setText(R.string.door_opened);
                        mDoorStatusTV.setTextColor(this.getResources().getColor(R.color.green));
                        mDoorIB.setImageResource(R.drawable.door_open);
                        mDoorIB.setEnabled(false);
                        mOpenBtn.setVisibility(View.GONE);
                        mOpenedIB.setVisibility(View.VISIBLE);
//                        StopScanningTimer();
                        BLE_Scanner_Start(true);
                        start_anime();
                        if(connect(targetAddr))
                            Util.debugMessage(TAG,"connect ok",debugFlag);
                        else
                            Util.debugMessage(TAG,"connect fail",debugFlag);
                        StartConnectTimer();

                        isOpenDoor = true;
                    } else
                        isOpenDoor = false;



                }
            }else{
                StartScanningTimer();
            }
        }else{
            show_toast_msg(getString(R.string.AUTO_ENABLE_CONFLICT));
        }
    }

    private void StartScanningTimer(){
        StopScanningTimer();
        /*BLE_Scanner_Start(false);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BLE_Scanner_Start(true);
            }
        },500);*/
        if(!isEditDeviceName){
            BLE_Scanner_Start(true);
            // Util.debugMessage(TAG,"timer rescan",true);
        }
        ScanningTimerFlag = true;
        scaningTimer = new Thread(new Runnable() {
            @Override
            public void run() {
                while(ScanningTimerFlag){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            check_BT_HomeActivity(false);
                            check_GPS_HomeActivity();
                        }
                    });
                    //Util.debugMessage(TAG,"ScanTimer",debugFlag);

//                    BLE_Scanner_Start(false);
//                    try{
//                        Thread.sleep(200);
//
//                    }catch(java.lang.InterruptedException e){
//
//                    }

//                    Calendar dt = Calendar.getInstance();
//
//                    final int thisYear = dt.get(Calendar.YEAR);
//
//                    final int thisMonth = dt.get(Calendar.MONTH)+1;
//
//                    final int thisDate = dt.get(Calendar.DAY_OF_MONTH);
//
//                    final int thisHour = dt.get(Calendar.HOUR_OF_DAY);
//
//                    final int thisMin = dt.get(Calendar.MINUTE);
//
//                    final int thisSec = dt.get(Calendar.SECOND);
//
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mAutoOpenTV.setText("" + Integer.toString(thisYear)+"/" + Integer.toString(thisMonth)+"/" + Integer.toString(thisDate)+" " + Integer.toString(thisHour)+":" + Integer.toString(thisMin)+":" + Integer.toString(thisSec));
//                        }
//                    });

                    if(!isScanning&&!isEditDeviceName) {
                        BLE_Scanner_Start(true);
                        //Util.debugMessage(TAG,"rescan",true);
                    }

                    checkDeviceAlive();

                    try{
                        Thread.sleep(5000);


                    }catch(java.lang.InterruptedException e){

                    }




                }
            }
        });
        scaningTimer.start();
    }
    public  void StartConnectTimer(){
        Util.debugMessage(TAG,"connect time start",debugFlag);
        /*if(ConTimer!=null){
            ConTimer.cancel();
            ConTimer.purge();

        }*/
        isReady = false;


        ConTimer.postDelayed(ConTask,APPConfig.conTimeOut);


    }
    private void StopScanningTimer(){

        ScanningTimerFlag = false;
        BLE_Scanner_Start(false);
        if(scaningTimer!=null)
            scaningTimer.interrupt();
        scaningTimer = null;
    }
    private void StartBgAutoTimer(){
        bgAutoTimerflag = true;

    }
    private void CreateBgAutoTimer(){
        bgAutoTimerflag = true;
        Util.debugMessage(TAG,"start bgAuto",true);
        BLE_Scanner_Start(true);
        if(bgAutoTimer == null ){
            bgAutoTimer = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        while(bgAutoTimerflag) {
                            Util.debugMessage(TAG, "bg thread work", debugFlag);
                            boolean isTriggerOpenDoor = false;
                            BluetoothDevice selectTarget = null;
                            String targetBdAddr = "";
                            if ( !isOpenDoor)
                                Util.debugMessage(TAG, "!isOpenDoor", debugFlag);
                            if (deviceInfoList.size() > 0)
                                Util.debugMessage(TAG, "deviceInfoList.size() > 0", debugFlag);
                            if ((deviceInfoList.size() > 0) && !isOpenDoor) {
                                Util.debugMessage(TAG, "deviceInfoList.size() > 0", debugFlag);
                                selectTarget = getTargetDevice();
                                targetBdAddr = selectTarget.getAddress();
                                isTriggerOpenDoor = checkConnectLimitTime(targetBdAddr);
                                if(selectTarget != null)
                                    Util.debugMessage(TAG, "selectTarget != null", debugFlag);
                                if(isTriggerOpenDoor)
                                    Util.debugMessage(TAG, "isTriggerOpenDoor", debugFlag);
                            }
                            if (isTriggerOpenDoor && selectTarget != null) {
                                Util.debugMessage(TAG, "start auto connection", debugFlag);
                                int expectLEVEL = loadDeviceRSSILevel(targetBdAddr);
                                int currrentLEVEL = APPConfig.Convert_RSSI_to_LEVEL(getCurrLevel(targetBdAddr));
                                Util.debugMessage(TAG, "currrentLEVEL=" + currrentLEVEL + "expectLEVEL" + expectLEVEL, debugFlag);
                                if (expectLEVEL >= currrentLEVEL && !isBackground(HomeActivity.this)) {
                                    //BLE_Scanner_Start(true);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            start_anime();
                                        }
                                    });
                                    if (connect(targetBdAddr))
                                        Util.debugMessage(TAG, "auto connect ok", debugFlag);
                                    else
                                        Util.debugMessage(TAG, "connect fai;", debugFlag);
                                    StartConnectTimer();
                                    isOpenDoor = true;
                                    bgAutoTimerflag = false;
                                }
                            } else if (!isOpenDoor) {

                                checkDeviceAlive();
                            }

                            try {
                                Thread.sleep(1000);

                            } catch (java.lang.InterruptedException e) {

                            }
                        }

                    }
                }
            });
            bgAutoTimer.start();
        }
    }
    private void StopBgAutoTimer(){
        bgAutoTimerflag = false;
          /*  bgAutoTimerflag = false;
            if(bgAutoTimer!=null)
            bgAutoTimer.interrupt();
            bgAutoTimer = null;
            Util.debugMessage(TAG,"stop bgAuto",true);*/
    }

    private void openPasswordPage() {
        Intent intent = new Intent(this, PasswordActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void openSettingPage() {

        Intent intent = new Intent(this, SettingActivity.class);

        BluetoothDevice selectDevice = getTargetDevice();

        Util.debugMessage(TAG,"deviceName = "+selectDevice.getName(),debugFlag);
        Util.debugMessage(TAG,"BDAddr ="+selectDevice.getAddress(),debugFlag);
        boolean isAdmin = sharedPreferences.getBoolean(selectDevice.getAddress(), false);

        Bundle postman = new Bundle();
        postman.putString(APPConfig.deviceNameTag, selectDevice.getName());
        postman.putString(APPConfig.deviceBddrTag,selectDevice.getAddress());
        if(!isAdmin){
            int RSSI = getTargetCurrRSSI(selectDevice.getAddress());

            postman.putInt(APPConfig.RSSI_LEVEL_Tag,RSSI);
            intent.setClass(this,UserSettingActivity.class);
            Util.debugMessage(TAG,"I'm User",debugFlag);
        }
        intent.putExtras(postman);

        startActivity(intent);

        overridePendingTransitionRightToLeft();

        if(isConService){
            unbindService(ServiceConnection);
            unregisterReceiver(mGattUpdateReceiver);
            isConService = false;
        }
    }

    private void updateView(View show) {
        stop_anime();
        mCurrentView = show;
        //Util.debugMessage(TAG,"updateView",debugFlag);
        mCurrentView.setVisibility(View.GONE);
        if (mCurrentView == mSearchingV) {
            //mProgressBarV.setVisibility(View.GONE);
            mModelTV.setVisibility(View.INVISIBLE);
            mAutoRangeSettingValueV.setVisibility(View.INVISIBLE);
        } else if (mCurrentView == mNotFoundV) {
            mRefreshIB.setVisibility(View.GONE);
            mModelTV.setVisibility(View.INVISIBLE);
            mAutoRangeSettingValueV.setVisibility(View.INVISIBLE);
        } else if (mCurrentView == mFoundV) {
            // Util.debugMessage(TAG,"foundV",debugFlag);
            mOpenBtn.setVisibility(View.GONE);
            mOpenedIB.setVisibility(View.GONE);
            mDoorIB.setImageResource(R.drawable.door_close);
            mDoorIB.setEnabled(true);
            mDoorStatusTV.setText(R.string.door_closed);
            mDoorStatusTV.setTextColor(getResources().getColor(android.R.color.black));

            mModelTV.setVisibility(View.VISIBLE);
            mAutoRangeSettingValueV.setVisibility(View.VISIBLE);
        }

        show.setVisibility(View.VISIBLE);
        mCurrentView = show;

        if (show == mSearchingV) {
            mProgressBarV.setVisibility(View.VISIBLE);
            mDeviceV.setVisibility(View.GONE);
        } else if (show == mNotFoundV) {
            mRefreshIB.setVisibility(View.VISIBLE);
        } else if (show == mFoundV) {
            mOpenBtn.setVisibility(View.VISIBLE);
            mProgressBarV.setVisibility(View.GONE);
            mDeviceV.setVisibility(View.VISIBLE);
            mSearchingV.setVisibility(View.GONE);
        }
    }

    private void showDevices() {
        StopScanningTimer();
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(R.string.choose_device);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.cancel, null);

        ListView listView = new ListView(this);

        final String[] deviceList = new String[deviceInfoList.size()];
        final ScanItemData deviceListObj = new ScanItemData();
        for(int i = 0;i<deviceInfoList.size();i++){

            deviceList[i] =  deviceInfoList.scanItems.get(i).dev_name;
            deviceListObj.scanItems.add(deviceInfoList.scanItems.get(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_device, R.id.text, deviceList);

        listView.setAdapter(adapter);

        builder.setView(listView);
        final AlertDialog dialog = builder.create();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isForce){
                    if(forceDevice.getName().equals(deviceList[position])){
                        mDeviceV.setDeviceName(forceDevice.getName());
                        mDeviceV.setSelection(false);
                        isForce = false;
                    }else{
                        Util.debugMessage(TAG,"device Addr="+deviceListObj.scanItems.get(position).dev_addr,debugFlag);
                        Util.debugMessage(TAG,"F name="+forceDevice.getName()+"d name="+deviceListObj.scanItems.get(position).dev_name,debugFlag);

                        isForce  = true;
                        forceDevice  = mBluetoothAdapter.getRemoteDevice(deviceListObj.scanItems.get(position).dev_addr);
                        mDeviceV.setSelection(true);
                        mDeviceV.setDeviceName(forceDevice.getName());
                    }
                }else{

                    isForce  = true;
                    forceDevice  = mBluetoothAdapter.getRemoteDevice(deviceListObj.scanItems.get(position).dev_addr);
                    mDeviceV.setDeviceName(forceDevice.getName());
                    mDeviceV.setSelection(true);
                }
                dialog.dismiss();
            }
        });

        dialog.show();

        StartScanningTimer();
    }

    private void updateStatus() {
        if (mIsAutoOpen) {
            Util.debugMessage(TAG,"auto off",debugFlag);
            StartScanningTimer();
            StopBgAutoTimer();
            isAutoMode = false;
            // bgAutoTimer = null;
            sharedPreferences.edit().putBoolean(APPConfig.isAutoTag,false).commit();
//            mAutoOpenTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_none, 0, 0, 0);
            mAutoOpenTV.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.checkbox_none,0,0,0);
        } else {
            Util.debugMessage(TAG,"auto on",debugFlag);
//            StopScanningTimer();
            CreateBgAutoTimer();
            StartBgAutoTimer();
            isAutoMode = true;
            sharedPreferences.edit().putBoolean(APPConfig.isAutoTag,true).commit();
//            mAutoOpenTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_tick, 0, 0, 0);
            mAutoOpenTV.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.checkbox_tick,0,0,0);
        }

        mIsAutoOpen = !mIsAutoOpen;
    }

    private void showSearchDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.msg_searching);

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        dialogBuilder.show();
    }

    private int getCurrLevel(String bdAddr){

        if(deviceInfoList.size() > 0){

            for(int i=0;i<deviceInfoList.size();i++){
                if(deviceInfoList.scanItems.get(i).dev_addr.equals(bdAddr)){
                    return  deviceInfoList.scanItems.get(i).rssi;
                }
            }

        }
        return 0;
    }
    private void checkDeviceAlive(){

        boolean  need_Check_Alive = true;
        if(deviceInfoList.size() > 0)
            need_Check_Alive = true;
        else {
            need_Check_Alive = false;
            // BLE_Scanner_Start(true);
        }
        if(need_Check_Alive){
            for(int i=0;i<deviceInfoList.size();i++){

                deviceInfoList.scanItems.get(i).alive_cnt -= 1;
                if(deviceInfoList.scanItems.get(i).alive_cnt <=0){
                    deviceInfoList.scanItems.remove(i);
                }
            }

        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(deviceInfoList.size()<=0){
                    updateView(mSearchingV);
                    //mDeviceV.setDeviceName("    ");
                    mDeviceV.setStatusDotEnable(false);
                }

            }
        });

    }
    private BluetoothDevice getTargetDevice(){
        BluetoothDevice target = null;

        if(isForce){
            Util.debugMessage(TAG,"isForce",debugFlag);
            target = forceDevice;
        }else{

//            int current_level = APPConfig.Convert_RSSI_to_LEVEL(deviceInfoList.scanItems.get(0).rssi);
//            Util.debugMessage(TAG,"current Level ="+current_level, debugFlag);
//            target  = mBluetoothAdapter.getRemoteDevice(deviceInfoList.scanItems.get(0).dev_addr);
//            for(int i =0; i<deviceInfoList.size();i++){
//                Util.debugMessage(TAG,"next Level ="+APPConfig.Convert_RSSI_to_LEVEL(deviceInfoList.scanItems.get(i).rssi), debugFlag);
//                if(APPConfig.Convert_RSSI_to_LEVEL(deviceInfoList.scanItems.get(i).rssi) > current_level){
//                    target  = mBluetoothAdapter.getRemoteDevice(deviceInfoList.scanItems.get(i).dev_addr);
//                    current_level =  deviceInfoList.scanItems.get(i).rssi;
//                }
//            }
//            Util.debugMessage(TAG,"target addr="+target.getAddress(),debugFlag);
            target  = mBluetoothAdapter.getRemoteDevice(deviceInfoList.scanItems.get(0).dev_addr);

        }

        return target;
    }
    private int getTargetCurrRSSI(String bdAddr){

        for(int i=0;i<deviceInfoList.size();i++){
            if(deviceInfoList.scanItems.get(i).dev_addr.equals(bdAddr)){
                return deviceInfoList.scanItems.get(i).rssi;
            }
        }
        return -999;
    }
    /* private boolean checkRssiLevelLimit(String bdAddr){
         /*int RSSI;
         Util.debugMessage(TAG,"BD address ="+bdAddr,debugFlag);
         int index = 999999;
         for(int i=0;i<deviceNearestScanInfo.scanItems.size();i++){
             if(mDevicesItemsArrayList.size()>0 && bdAddr.equals(mDevicesItemsArrayList.get(i).dev_addr)){
                 index = i;
                 break;
             }
         }
         if(index !=999999)
             RSSI = Integer.parseInt(mDevicesItemsArrayList.get(index).getRssi_current_level_text(true));
         else
             RSSI = 999999;
         int limt_rssi =  mNkiDevices_data.get_device_expect_rssi_level_by_addr(bdAddr);
         Util.debugMessage(TAG,"limt_rssi ="+limt_rssi +",rssi="+ RSSI,debugFlag);
         if(limt_rssi >=  RSSI)
             return true;
         else
             return false;


     }
                                                                 */
    private boolean checkConnectLimitTime(String bdAddr){
        Calendar time = Calendar.getInstance();
        int min = time.get(Calendar.MINUTE);
        int sec =  time.get(Calendar.SECOND);
        int currTime = min *60 + sec;

        int disconTime = sharedPreferences.getInt(bdAddr+"Time",0);
        Util.debugMessage(TAG,"diconTime="+disconTime+"reconnectTime ="+Math.abs(currTime-disconTime)+"currTime="+currTime ,debugFlag);
        if(Math.abs(currTime-disconTime) >= reconnectTime)
            return true;
        else
            return  false;
    }


    private void ForceDisconnect(){
        //disconnect();
        bpProtocol.getFWVersion();

    }





    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
                BACKGROUND=400 EMPTY=500 FOREGROUND=100
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                Log.i(context.getPackageName(), "此appimportace ="
                        + appProcess.importance
                        + ",context.getClass().getName()="
                        + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "处于后台"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "处于前台"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }




//    private void StartAutoOpenningTimer(){
//        AutoOpenflag = true;
//
//        autoOpenningTimer = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                while(true) {
//                    while (AutoOpenflag) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                mDoorStatusTV.setTextColor(getApplicationContext().getResources().getColor(R.color.green));
//
//                                if (!mDoorStatusTV.getText().equals(getResources().getString(R.string.automatic_sensor_to_open))) {
//                                    mDoorStatusTV.setText(getResources().getString(R.string.automatic_sensor_to_open));
//                                } else {
//                                    mDoorStatusTV.setText("");
//                                }
//
//                            }
//                        });
//
//                        try {
//                            Thread.sleep(200);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//
//                }
//
//            }
//
//        });
//        autoOpenningTimer.start();
//    }
//
//
//
//
//
//
//    private  void StopAutoOpenningTimer() {
//        AutoOpenflag = false;
//        if (autoOpenningTimer != null)
//        {
//            autoOpenningTimer.interrupt();
//        }
//        autoOpenningTimer = null;
//
//        mDoorStatusTV.setTextColor(getApplicationContext().getResources().getColor(R.color.gray));
//        mDoorStatusTV.setText("");
//
//    }





    public void start_anime()
    {
        infoOperatingIV = (ImageView)findViewById(R.id.infoOperating);
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.tip);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (operatingAnim != null) {
            infoOperatingIV.startAnimation(operatingAnim);
        }
    }


    public void stop_anime()
    {
        infoOperatingIV = (ImageView)findViewById(R.id.infoOperating);
        infoOperatingIV.clearAnimation();
    }



    public void check_BT_HomeActivity(boolean StartScanningTimerFlag)
    {
        if (!isShowing_BT_dialog) {
            if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled())) {
                isShowing_BT_dialog = true;
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                if (StartScanningTimerFlag) {
                    mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

                    //mLEScanner = mBluetoothAdapter;
                    mLEScanSettings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();

                    mLEScanFilters = new ArrayList<>();
                    ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(RBLService.UUID_BLE_E3K_SERVICE.toString())).build();
                    mLEScanFilters.add(filter);
                    // BLE_Scanner_Start(true);
//            if(!isAutoMode)
                    StartScanningTimer();
                }
            }
        }
    }



    public void check_GPS_HomeActivity()
    {

        if (!isShowing_GPS_dialog) {
            final LocationManager locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                isShowing_GPS_dialog = true;
                // show open gps message
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
//            builder.setTitle(R.string.gps_warning);
//            builder.setMessage(R.string.no_gps);
                builder.setMessage(R.string.need_gps);
                builder.setNegativeButton(getString(R.string.cancel), new
                        android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int id = android.os.Process.myPid();
                                android.os.Process.killProcess(id);
                            }
                        });
                builder.setPositiveButton(getString(R.string.ok), new
                        android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // jump to setting
                                if (!locManager.isProviderEnabled((LocationManager.GPS_PROVIDER)) && !locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                    Intent enableGPSIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                                startActivity(enableGPSIntent);
                                    startActivityForResult(enableGPSIntent, REQUEST_ENABLE_LOCATION);
                                }
                            }
                        });
                builder.show();
            }
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT)
        {
            isShowing_BT_dialog = false;
            if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled())) {
                int id = android.os.Process.myPid();
                android.os.Process.killProcess(id);

            }
        }

        if (requestCode == REQUEST_ENABLE_LOCATION)
        {
            isShowing_GPS_dialog = false;
            check_GPS_HomeActivity();
        }
    }





}

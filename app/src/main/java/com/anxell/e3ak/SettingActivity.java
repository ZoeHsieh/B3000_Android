package com.anxell.e3ak;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.anxell.e3ak.custom.FontEditText;
import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.custom.My2TextView;
import com.anxell.e3ak.custom.My4TextView;
import com.anxell.e3ak.custom.MySwitch;
import com.anxell.e3ak.transport.APPConfig;
import com.anxell.e3ak.transport.AdminMenu;
import com.anxell.e3ak.transport.BPprotocol;
import com.anxell.e3ak.transport.RBLService;
import com.anxell.e3ak.transport.SettingData;
import com.anxell.e3ak.transport.bpActivity;
import com.anxell.e3ak.util.Util;

import java.lang.annotation.Target;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SettingActivity extends bpActivity implements View.OnClickListener {
    private String TAG = SettingActivity.class.getSimpleName().toString();
    private Boolean debugFlag = true;
    private My4TextView mDeviceNameTV;
    private My2TextView mDeviceTimeTV;
    private My4TextView mDoorReLockTimeTV;
    private My2TextView mDoorActionTV;
    private My4TextView mAdminPWDTV;
    private My2TextView mExpectLEVELTV;
    private MySwitch mTampSwitch;
    private MySwitch mDoorSwitch;
    private ScrollView  settingUI;
    private FontTextView versionTV;
    private RelativeLayout settingMainlayOut;
    private ProgressBar loadDeviceDataBar;
    //private int mProgressStatus = 0;
    //private Handler handler = new Handler();
    private String deviceBDDR = "";
    private int userMax = 0;
    private String mDevice_FW_Version = "";
    private double vr = 0.0;
    private double vrLimit = 1.02;
    private byte currConfig[]=new byte[BPprotocol.len_Device_Config];
    private byte currTime[] = new byte[BPprotocol.len_Device_Time];
    public  static byte tmpConfig[]=new byte[BPprotocol.len_Device_Config];
    public  static byte tmpTime[] = new byte[BPprotocol.len_Device_Time];
    private byte tmpDeviceName[]= new byte[BPprotocol.len_Device_Name];
    private byte tmpPWD[] = new byte[BPprotocol.UserPD_maxLen];
    /*backup and restore*/
    private AdminMenu adminMenu;
    private SettingData settingFile = null;
    boolean isBackStart = false;
    boolean isRestoreStart = false;
    /*------------------------*/

    public static int updateStatus = 0;
    public static final int up_deviceTime = 1;
    public static final int up_deviceConfig = 2;
    public static final int up_none  = 0;
    public static final int up_rssi_level = 3;
    private Timer ConTimer= null;
    private boolean isReady = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Initial(getLocalClassName());
        setContentView(R.layout.activity_setting);
        findViews();
        Intent intent = getIntent();
        intent.setClass(this,RBLService.class);
        bindService(intent, ServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver,  getIntentFilter());
        savedInstanceState = this.getIntent().getExtras();
        String deviceName = savedInstanceState.getString(APPConfig.deviceNameTag);
        deviceBDDR = savedInstanceState.getString(APPConfig.deviceBddrTag);
        mDeviceNameTV.setValue(deviceName);
        int expectLevel = loadDeviceRSSILevel(deviceBDDR);
        mExpectLEVELTV.setValue(""+expectLevel);

        settingUI.setVisibility(View.GONE);

        setListeners();
        // getDeviceTime();
        currentClassName = getLocalClassName();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //RegisterReceiver(this);
        currentClassName = getLocalClassName();

           // registerReceiver(mGattUpdateReceiver, getIntentFilter());

            switch(updateStatus){

            case up_deviceConfig:
                bpProtocol.setConfig(tmpConfig);
                break;

            case up_deviceTime:
                bpProtocol.setDateTime(tmpTime);
                break;

            case up_rssi_level:
                int expectLEVEL = loadDeviceRSSILevel(deviceBDDR);
                Util.debugMessage(TAG,"expectLevel="+ expectLEVEL,debugFlag);
                mExpectLEVELTV.setValue(""+expectLEVEL);
                break;

        }
        updateStatus = up_none;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unRegisterReceiver(this);
    }

    private void findViews() {
        mDeviceNameTV = (My4TextView) findViewById(R.id.deviceName);

        mDeviceTimeTV = (My2TextView) findViewById(R.id.deviceTime);
        mDoorReLockTimeTV = (My4TextView) findViewById(R.id.doorReLockTime);

        mDoorActionTV = (My2TextView) findViewById(R.id.doorLockAction);
        settingUI = (ScrollView) findViewById(R.id.SettingUI);
        mTampSwitch = (MySwitch) findViewById(R.id.tamperSensor);
        mDoorSwitch = (MySwitch) findViewById(R.id.doorSensor);
        mTampSwitch.setSwitchClickable(false);
        mDoorSwitch.setSwitchClickable(false);
        versionTV = (FontTextView) findViewById(R.id.version);
        mAdminPWDTV = (My4TextView) findViewById(R.id.AdminPWD);

        mExpectLEVELTV = (My2TextView) findViewById(R.id.proximityReadRange);
        settingMainlayOut = (RelativeLayout) findViewById(R.id.SettingRelativeLayout);
        loadDeviceDataBar = (ProgressBar)findViewById(R.id.setting_loadingBar);
        adminMenu = new AdminMenu(this, settingMainlayOut,bpProtocol);

    }

    @Override
    public void getERROREvent(String bdAddress){
        connect(bdAddress);
        StartConnectTimer();
    }

    @Override
    public void update_service_connect() {
        super.update_service_connect();
        connect(deviceBDDR);
        bpProtocol.queueClear();
        StartConnectTimer();
        isReady = false;
    }

    private void setListeners() {
        findViewById(R.id.user).setOnClickListener(this);
        findViewById(R.id.history).setOnClickListener(this);
        findViewById(R.id.backup).setOnClickListener(this);
        findViewById(R.id.restore).setOnClickListener(this);
        findViewById(R.id.deviceName).setOnClickListener(this);
        findViewById(R.id.doorLockAction).setOnClickListener(this);
        findViewById(R.id.proximityReadRange).setOnClickListener(this);
        mDoorReLockTimeTV.setOnClickListener(this);
        mDeviceTimeTV.setOnClickListener(this);
        mAdminPWDTV.setOnClickListener(this);
        mTampSwitch.setOnClickListener(this);
        mDoorSwitch.setOnClickListener(this);
        findViewById(R.id.aboutUs).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        forceDisconnect();
        unbindService(ServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
        overridePendingTransitionLeftToRight();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user:


                openUsersPage();
                break;

            case R.id.history:

                openHistoryPage();
                break;

            case R.id.backup:
                if(vr >= vrLimit){


                    settingFile = new SettingData(APPConfig.SETTINGS_FILE_NAME+".bp");
                    settingFile.checkOldBackupFileExist();

                    bpProtocol.getUsersCount();
                    isBackStart = true;
                }
                //showBackupDialog();
                break;

            case R.id.restore:
                if(vr >= vrLimit) {
                    bpProtocol.getUsersCount();
                    isRestoreStart = true;
                }
                break;

            case R.id.deviceName:
                String currentDeviceName = mDeviceNameTV.getValue();
                showDeviceNameDialog(currentDeviceName);
                break;
            case R.id.AdminPWD:
                String currentPWD = mAdminPWDTV.getValue();
                showAdminPWDDialog(currentPWD);
                break;

            case R.id.doorSensor:
                tmpConfig = currConfig;
                if(!mDoorSwitch.isSwitchCheck()){
                    tmpConfig[0] = 0x01;
                    bpProtocol.setConfig(tmpConfig);
                    Util.debugMessage(TAG,"door on",true);
                }else{

                    tmpConfig[0] = 0x00;
                    bpProtocol.setConfig(tmpConfig);
                    Util.debugMessage(TAG,"door off",true);
                }


                break;
            case R.id.tamperSensor:
                tmpConfig = currConfig;
                if(!mTampSwitch.isSwitchCheck()){
                    tmpConfig[4] = 0x01;
                    bpProtocol.setConfig(tmpConfig);
                    Util.debugMessage(TAG,"tamp on",true);
                }else{

                    tmpConfig[4] = 0x00;
                    bpProtocol.setConfig(tmpConfig);
                    Util.debugMessage(TAG,"tamp off",false);
                }
                break;

            case R.id.doorLockAction:
                tmpConfig = currConfig;
                openDoorLockActionPage();
                break;

            case R.id.proximityReadRange:
                openProximityReadPage();
                break;

            case R.id.doorReLockTime:
                tmpConfig = currConfig;
                String ReLockTime= mDoorReLockTimeTV.getValue();
                showReLockTimeDialog(ReLockTime);
                //openDoorReLockTimePage();
                break;

            case R.id.deviceTime:
                openDeviceTimePage();
                break;

            case R.id.aboutUs:
                openAboutUsPage();
                break;
        }
    }

    private void openUsersPage() {
        Intent intent = new Intent(this, UsersActivity2.class);
        intent.putExtra(APPConfig.deviceBddrTag,deviceBDDR);

        startActivity(intent);

        overridePendingTransitionRightToLeft();

       // unregisterReceiver(mGattUpdateReceiver);

    }

    private void openHistoryPage() {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(APPConfig.deviceNameTag,getDeviceName());
        startActivity(intent);

        overridePendingTransitionRightToLeft();

        //unregisterReceiver(mGattUpdateReceiver);

    }

    private void openDoorLockActionPage() {
        Intent intent = new Intent(this, DoorLockActionActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void openProximityReadPage() {
        readRSSI();

    }

    private void openDoorReLockTimePage() {
        Intent intent = new Intent(this, DoorReLockTimeActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void openDeviceTimePage() {
        tmpTime = currTime;
        Intent intent = new Intent(this, DeviceTimeActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void openAboutUsPage() {
        Intent intent = new Intent(this, AboutUsActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void showDeviceNameDialog(String currentDeviceName) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.edit_device_name);
        //dialogBuilder.setMessage(getString(R.string.up_to_16_characters));


        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.my_alert_editor, null);

        dialogBuilder.setView(dialogView);

        final FontEditText editText = (FontEditText) dialogView.findViewById(R.id.editText);
        editText.setText(currentDeviceName);
        editText.setTextColor(Color.BLACK);
        editText.setHint(R.string.up_to_16_characters);
        if (editText.getText().length()>0 ) {
            editText.setSelection(editText.getText().length());
        }
        dialogBuilder.setPositiveButton(R.string.Confirm, null);
        dialogBuilder.setNeutralButton(R.string.cancel, null);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String newDeviceName = editText.getText().toString();
                        if (TextUtils.isEmpty(newDeviceName)) {
                            SettingActivity.this.showMsg(R.string.msg_please_enter_device_name);
                        } else {
                            byte device_name_buf[] = new byte[BPprotocol.len_Device_Name-1];
                            byte[] device_name_tmp = newDeviceName.getBytes(Charset.forName("UTF-8"));
                            int device_name_len = 0;
                            int target_len = (device_name_tmp.length > device_name_buf.length) ? device_name_buf.length : device_name_tmp.length;
                            Util.debugMessage(TAG,"device_name_tmp length : " + device_name_tmp.length,debugFlag);

                            Arrays.fill(device_name_buf, (byte) 0xFF);

                            for (int i = 0; i < target_len; i++)
                                device_name_buf[i] = device_name_tmp[i];

                            device_name_len = target_len;

                            bpProtocol.setDeviceName(device_name_buf, device_name_len);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                int bytes_len = s.toString().getBytes().length;
                int pos = s.length();

                Util.debugMessage(TAG, "String Len= " + s.length(),debugFlag);
                Util.debugMessage(TAG, "Bytes Len= " + bytes_len,debugFlag);

                if (bytes_len > BPprotocol.UserID_maxLen) {
                    s.delete(pos - 1, pos);
                }

            }
        });
        alertDialog.show();
    }

    private void showAdminPWDDialog(String currentPWD) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.settings_Admin_pwd_Edit);
        //dialogBuilder.setMessage(getString(R.string._4_8_digits));

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.my_alert_editor, null);
        dialogBuilder.setView(dialogView);

        final FontEditText editText = (FontEditText) dialogView.findViewById(R.id.editText);

        editText.setText(currentPWD);
        editText.setTextColor(Color.BLACK);
        editText.setHint(R.string._4_8_digits);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setRawInputType(this.getResources().getConfiguration().KEYBOARD_12KEY);
        if (editText.getText().length()>0 ) {
            editText.setSelection(editText.getText().length());
        }
        dialogBuilder.setPositiveButton(R.string.Confirm, null);
        dialogBuilder.setNeutralButton(R.string.cancel, null);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String newPassword = editText.getText().toString();
                        if (TextUtils.isEmpty(newPassword)) {
                            SettingActivity.this.showMsg(R.string.msg_please_enter_device_name);
                        } else {
                            byte[] password_buf = Util.convertStringToByteBuffer(newPassword, BPprotocol.UserPD_maxLen);


                            boolean isDuplicated_Password = Util.checkUserDuplicateByPassword(newPassword, mUserDataList);

                            if (isDuplicated_Password) {
                                show_toast_msg(getResources().getString(R.string.users_manage_edit_status_duplication_password));

                            } else {


                                bpProtocol.setAdminPWD(password_buf);

                            }
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 4)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                else
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);


                try {

                    int isNumeric = Integer.parseInt(editText.getText().toString());
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }catch(NumberFormatException e){
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

                int bytes_len = s.toString().getBytes().length;
                int pos = s.length();


                if (bytes_len > BPprotocol.UserPD_maxLen) {
                    s.delete(pos - 1, pos);
                }
            }
        });

        alertDialog.show();
    }

    private void showReLockTimeDialog(String currentTime) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.edit_door_re_lock_time);
        //dialogBuilder.setMessage(getString(R.string._4_8_digits));

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.my_alert_editor, null);
        dialogBuilder.setView(dialogView);

        final FontEditText editText = (FontEditText) dialogView.findViewById(R.id.editText);

        editText.setText(currentTime);
        editText.setTextColor(Color.BLACK);
        editText.setHint("1~1800");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setRawInputType(this.getResources().getConfiguration().KEYBOARD_12KEY);
        if (editText.getText().length()>0 ) {
            editText.setSelection(editText.getText().length());
        }
        dialogBuilder.setPositiveButton(R.string.Confirm, null);
        dialogBuilder.setNeutralButton(R.string.cancel, null);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String time = editText.getText().toString();
                        try{
                        final int delayTime =  Integer.parseInt(time);
                        if (delayTime >0 && delayTime <=1800) {
                            tmpConfig[2] = (byte) (delayTime >> 8);
                            tmpConfig[3] = (byte) (delayTime & 0xFF);
                            bpProtocol.setConfig(tmpConfig);
                        }
                        }catch(NumberFormatException e){

                        }
                            dialog.dismiss();
                        }
                    }
                );
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 4 && s.toString().length() > 0)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                else
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                if (!editText.getText().toString().isEmpty())
                {     try {

                    int time = Integer.parseInt(editText.getText().toString());

                    if (time > 0 && time <= 1800)
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    else
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }catch(NumberFormatException e){
                      alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }else
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                int bytes_len = s.toString().getBytes().length;
                int pos = s.length();


                if (bytes_len > 4) {
                    s.delete(pos - 1, pos);
                }
            }
        });

        alertDialog.show();
    }
    private void showDoneDialog(final int messageResId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingActivity.this);
                dialogBuilder.setTitle(messageResId);

                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                dialogBuilder.show();
            }
        });
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void connectUpdate(){

    }

    @Override
    public void BLEReady() {
        super.BLEReady();
        update_system_ble_mac_addrss();
        isReady = true;
        bpProtocol.adminLogin(mSYS_BLE_MAC_Address_RAW);

    }

    @Override
    public void update_RSSI(String rssi) {
        Intent intent = new Intent(this, ProximityReadRangeActivity2.class);
        int currLevel = APPConfig.Convert_RSSI_to_LEVEL(Integer.parseInt(rssi));
        intent.putExtra(APPConfig.RSSI_LEVEL_Tag,currLevel);
        intent.putExtra(APPConfig.deviceBddrTag,deviceBDDR);
        Util.debugMessage(TAG,"RSSI="+rssi+"deviceBDDR="+deviceBDDR,debugFlag);

        startActivity(intent);

        overridePendingTransitionRightToLeft();
    }

    @Override
    public void cmdDataCacheEvent(byte cmd, byte cmdType,byte data[], int datalen){
        Util.debugMessage(TAG,"setting cache",debugFlag);
        switch ((char) cmd) {

          /*  case BPprotocol.cmd_device_config:
                if (cmdType == (byte) BPprotocol.type_write) {
                    tmpConfig = Arrays.copyOf(data,data.length);
                    for(byte tmp:data)
                        Util.debugMessage(TAG,String.format("%02x",tmp),debugFlag);
                    for(byte tmp:tmpConfig)
                        Util.debugMessage(TAG,"data="+String.format("%02x",tmp),debugFlag);
                }

                break;*/
            case BPprotocol.cmd_device_time:
                if (cmdType == (byte) BPprotocol.type_write) {
                    tmpTime = data;
                    for(byte tmp:tmpTime)
                        Util.debugMessage(TAG,String.format("%02x",tmp),debugFlag);
                }
                break;
            case BPprotocol.cmd_device_name:
                Util.debugMessage(TAG,"data="+encode.BytetoHexString(data),debugFlag);
                if (cmdType == (byte) BPprotocol.type_write)
                    tmpDeviceName = data;

                break;


            case BPprotocol.cmd_set_admin_pwd:
                if (cmdType == (byte) BPprotocol.type_write)
                    tmpPWD = data;
                break;


            default:


        }

    }
    @Override
    public void cmdAnalysis(byte cmd, byte cmdType, byte data[], int datalen) {
        Util.debugMessage(TAG,"cmdAnalysis",debugFlag);
        String  message = "";
        switch ((char) cmd) {

            case BPprotocol.cmd_device_time:

                if(cmdType == (byte) BPprotocol.type_read){

                    updata_device_time(data);
                }else{

                      //  Util.debugMessage(TAG,"restore ok",debugFlag);
                        if(data[0]  == BPprotocol.result_success){
                           // message = getResources().getString(R.string.program_success);
                            updata_device_time(tmpTime);
                        }
                       /* else
                            message = getResources().getString(R.string.program_fail);
                        show_toast_msg(message);*/

                }

                break;

            case BPprotocol.cmd_device_config:
                if (cmdType == (byte) BPprotocol.type_read) {

                    if(adminMenu.isBackup()) {

                        settingFile.writeData(data);
                        adminMenu.backupRecevice();
                    }else {
                        update_Device_Config(data);
                        bpProtocol.getDateTime();
                        //sendProcessMessage(MSG_PROGRESS_BAR_SETUP_GONE);

                    }

                }else {
                    if (adminMenu.isRestore()) {
                        if (data[0] == BPprotocol.result_success)
                            adminMenu.restoreSend();
                    }
                    else {
                        if (data[0] == BPprotocol.result_success) {

                            for (byte tmp : tmpConfig)
                                update_Device_Config(tmpConfig);
                            //message = getResources().getString(R.string.program_success);

                        } //else
                           // message = getResources().getString(R.string.program_fail);
                       // show_toast_msg(message);
                    }
                }
                break;

            case BPprotocol.cmd_device_name:
                //Util.debugMessage(TAG,"data="+encode.BytetoHexString(data),debugFlag);

                if (cmdType == (byte) BPprotocol.type_read){

                    update_DeviceName(data);
                }

                else {
                    if(adminMenu.isRestore()){
                        if (data[0] == BPprotocol.result_success)
                            adminMenu.restoreSend();

                    }else {
                        HomeActivity.befSettingBDAddr = deviceBDDR;
                        HomeActivity.isEditDeviceName = true;
                        update_DeviceName(tmpDeviceName);
                    }
                }
                break;

            case BPprotocol.cmd_fw_version:
                update_FW_Version(data);
                bpProtocol.getConfig();
                vr = Double.parseDouble(mDevice_FW_Version.substring(1));
                break;

            case BPprotocol.cmd_admin_login:
                if (data[0] == BPprotocol.result_success) {

                        /*userCount = 1;

                        historyCount =0;
                        historyReadIndex = 0;
                        UserLoadingTHD = null;
                        HistoryLoadingTHD = null;
                        isLiveHTHD = false;
                        isLiveUSTHD = false;
                        isBackMain = false;*/
                        //bpProtocol.getUsersCount();
                        HistoryActivity.isHistoryDownloadOK = false;
                        UsersActivity2.isLoadUserListCompleted = false;
                        bpProtocol.getAdminPWD();

                }
                else {
                    //isBackMain = false;
                    onBackPressed();
                    sharedPreferences.edit().putBoolean(getBluetoothDeviceAddress(), false).apply();
                    Util.debugMessage(TAG,"admin login fail",debugFlag);
                    //disconnect();

                }

                break;

            case BPprotocol.cmd_set_admin_pwd:
                if (cmdType == (byte) BPprotocol.type_read) {

                    if(adminMenu.isBackup()) {
                        settingFile.writeData(data);
                        adminMenu.backupRecevice();
                    }else{

                        bpProtocol.getUsersCount();
                        update_Admin_password(data);

                    }
                }else  if (data[0] == BPprotocol.result_success) {
                    if(adminMenu.isRestore())
                        adminMenu.restoreSend();
                    else
                        update_Admin_password(tmpPWD);
                }else
                    Util.debugMessage(TAG,"ADMIN_PWD_WRITE_FAIL",debugFlag);


                break;

            case BPprotocol.cmd_user_data:


                if (cmdType == (byte) BPprotocol.type_read){

                    if(adminMenu.isBackup()) {
                        int count = adminMenu.getBackupCount()+1;

                        //  byte Users_Data []= Arrays.copyOfRange(data,2,data.length);
                        //int curr_count = (data[0] << 8)& 0x0000ff00 | (data[1]& 0x000000ff) ;

                        Util.debugMessage(TAG,"backup count ="+count,debugFlag);
                        adminMenu.backupRecevice();
                        settingFile.writeData(data);
                        // backupUsersData(data,count-2);
                        Util.debugMessage(TAG,"count backupUsers end ",debugFlag);

                        //if( count-2 <=userMax){
                        //bpProtocol.getUserData(count-2);
                        if( count-1 <=userMax){
                            bpProtocol.getUserData(count-1);
                            Util.debugMessage(TAG,"count send data start ",debugFlag);

                        }

                        // else
                        //  bpProtocol.setUserDatalost(count-1);


                    }
                }else{
                    if(adminMenu.isRestore()){
                        if(data[0] == BPprotocol.result_success)
                            adminMenu.restoreSend();




                    }

                }


                break;

            case BPprotocol.cmd_user_counter:
                Util.debugMessage(TAG,"old userMax="+userMax,debugFlag);
                Util.debugMessage(TAG,"data="+encode.BytetoHexString(data),debugFlag);
                userMax = encode.getUnsignedTwoByte(data);
                Util.debugMessage(TAG,"New userMax="+userMax,debugFlag);

                if(isBackStart){
                    isBackStart = false;
                    settingFile.writeUserSize(userMax);
                    adminMenu.Setup_Dialog_Backup(userMax);
                }else if(isRestoreStart){
                    isRestoreStart =false;
                    adminMenu.Setup_Dialog_Restore(userMax,getBluetoothDeviceAddress().toString());

                }else
                    bpProtocol.getFWVersion();

                break;


            case BPprotocol.cmd_erase_user_list:
                if(data[0] == BPprotocol.result_success){
                    if(adminMenu.isRestore())
                        adminMenu.restoreSend();
                }

                break;

                }

    }

    private void updata_device_time(byte data[]){
        String deviceTimeStr = "";
        Calendar deviceDataTime  = Calendar.getInstance();;
        deviceDataTime.set(Calendar.YEAR,((data[0] << 8) & 0x0000ff00) | (data[1] & 0x000000ff));
        deviceDataTime.set(Calendar.MONTH,data[2]-1);
        deviceDataTime.set(Calendar.DAY_OF_MONTH,data[3]);
        deviceDataTime.set(Calendar.HOUR_OF_DAY,data[4]);
        deviceDataTime.set(Calendar.MINUTE,data[5]);
        deviceDataTime.set(Calendar.SECOND,data[6]);
        //deviceTimeStr = Util.Convert_Date_Time_Str(deviceDataTime);
        mDeviceTimeTV.setValue(dateTimeFormat(deviceDataTime.getTime()));
        currTime = data;
    }

    private void update_DeviceName(byte[] data) {
        int len;
        String str_DeviceName;

        //Get Data
        len = data[0];


        Util.debugMessage(TAG,"deviceName="+encode.BytetoHexString(data),debugFlag);
        Util.debugMessage(TAG,"len="+len,debugFlag);
        byte[] name_bytes = Arrays.copyOfRange(data, 1, len + 1);
        Util.debugMessage(TAG,"deviceName2="+encode.BytetoHexString(name_bytes),debugFlag);
        str_DeviceName = new String(name_bytes, Charset.forName("UTF-8"));

        mDeviceNameTV.setValue(str_DeviceName);


    }
    private void update_Device_Config(byte[] data) {
        int delay_secs;
        byte lock_type; //0: By Delay, 1: Always Open, 2: Always Closed
        boolean door_sensor_opt;
        boolean tamper_opt;
        //sendProcessMessage(MSG_PROGRESS_BAR_SETUP_INVISIBLE);

        //mLinearLayout_Setup_Items.setVisibility(View.VISIBLE);
        settingUI.setVisibility(View.VISIBLE);
        loadDeviceDataBar.setVisibility(View.GONE);
        //Get Data
        door_sensor_opt = (data[0] != 0);
        lock_type = data[1];
        delay_secs = ((data[2] << 8) & 0x0000ff00) | (data[3] & 0x000000ff);
        tamper_opt = (data[4] != 0);
        String tmp_str = "";
        if (lock_type == BPprotocol.door_statis_delayTime)
            tmp_str = getResources().getString(R.string.use_re_lock_time);
        else if (lock_type == BPprotocol.door_statis_KeepOpen)
            tmp_str = getResources().getString(R.string.always_unlocked);
        else if (lock_type == BPprotocol.door_statis_KeepLock)
            tmp_str = getResources().getString(R.string.always_locked);
        mDoorActionTV.setValue(tmp_str);
        mDoorReLockTimeTV.setValue(String.format(Locale.US, "%d", delay_secs));
        mDoorSwitch.setSwitchCheck(door_sensor_opt);
        mTampSwitch.setSwitchCheck(tamper_opt);
        currConfig = data;


    }
    private void update_FW_Version(byte[] data) {
        int major, minor;

        //Get Data
        major = data[0];
        minor = data[1];

        mDevice_FW_Version = String.format(Locale.US, "V%d.%02d", major, minor);
        versionTV.setText(mDevice_FW_Version);

    }
    private void update_Admin_password(byte[] data) {

        String str_password="";

        //Get Data

//        for(int i=0; i<len; i++)
//            str_DeviceName = String.format("%s%c", str_DeviceName.toString(), data[i+1]);
        //Log.v(TAG, "len = " + len);
        Util.debugMessage(TAG,"password="+encode.BytetoHexString(data),debugFlag);

        for(int i=0; i <data.length;i++) {
            if(data[i]!=(byte)0xFF) {
                Util.debugMessage(TAG,"data["+i+"]="+String.format(Locale.US, "%x", data[i]),debugFlag);
                str_password += encode.convert_ASCII(String.format(Locale.US, "%02x", data[i]));
            }
        }
        mAdminPWDTV.setValue(str_password);

        sharedPreferences.edit().putString(APPConfig.ADMINPWD_Tag+deviceBDDR,str_password).commit();
    }

    public String getDeviceName(){

        return mDeviceNameTV.getValue();
    }

    public void forceDisconnect(){

        bpProtocol.executeForceDisconnect();
    }
    private void StartConnectTimer(){
        if(ConTimer != null){
            ConTimer.cancel();
            ConTimer.purge();
        }
        if(ConTimer  == null){
            ConTimer = new Timer();
            ConTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!isReady)
                            onBackPressed();
                            Util.debugMessage(TAG,"Connect Time out",debugFlag);
                            ConTimer = null;
                        }
                    });


                }
            },APPConfig.conTimeOut);

        }
    }
}

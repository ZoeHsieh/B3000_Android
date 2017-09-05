package com.anxell.e3ak.transport;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.ShowableListMenu;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;


import com.anxell.e3ak.Config;
import com.anxell.e3ak.HomeActivity;
import com.anxell.e3ak.R;
import com.anxell.e3ak.SettingActivity;
import com.anxell.e3ak.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


/**
 * Created by Sean on 3/23/2017.
 */

public class AdminMenu {
    private final static String TAG = AdminMenu.class.getSimpleName();

    private Activity currActivity;
    private BPprotocol bpProtocol;
    private RelativeLayout mRelativeLayout_Setup;
    private boolean isEnroll = false;
    private int backupCount = 0;
    private boolean isBackup = false;
    private boolean isBackupCancel = false;
    private boolean isBackupDone = false;
    private int restoreCount = 0;
    private int restoreCountbefore = 0;
    private boolean isRestore = false;
    private boolean isRestoreCancel = false;
    private boolean isRestoreDone = false;
    private boolean isRestoreCmdPut = false;
    private AlertDialog mUsersEnrollDialog;
    private boolean debugFlag = false;
    private int dateTimeArray[] = {0,0,0,0,0};
    private SettingData settingFile = null;


    public AdminMenu(Activity activity, PercentRelativeLayout layout, BPprotocol protocol){

            currActivity = activity;
            bpProtocol = protocol;
            mRelativeLayout_Setup = (RelativeLayout) layout;


    }

    public AdminMenu(Activity activity, RelativeLayout layout, BPprotocol protocol){

        currActivity = activity;
        bpProtocol = protocol;
        mRelativeLayout_Setup = layout;


    }
    public void UsersEnrollDialog(final byte mSYS_BLE_MAC_Address_RAW[], final String ADMIN_USER_NAME, final String bdAddr) {
        final View item = LayoutInflater.from(currActivity).inflate(R.layout.users_name_password_dialog, mRelativeLayout_Setup, false);




        AlertDialog.Builder builder = new AlertDialog.Builder(currActivity);

        builder.setTitle(currActivity.getResources().getString(R.string.enroll_dialog_title));
        builder.setCancelable(false);
        //dialog.setMessage("請輸入密碼：");

        builder.setView(item);

        final EditText addUserName = (EditText) item.findViewById(R.id.editText_Users_Add_Dialog_Name);
        final EditText addUserPassword = (EditText) item.findViewById(R.id.editText_Users_Add_Dialog_Password);
            addUserPassword.setRawInputType(currActivity.getResources().getConfiguration().KEYBOARD_12KEY);
        builder.setPositiveButton(currActivity.getString(R.string.Confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bpProtocol.queueClear();
                String ID =   addUserName.getText().toString();
                boolean isAdmin = false;
                if(ID.equals(ADMIN_USER_NAME)) {
                    ID += ".";
                    isAdmin = true;
                }
                byte[] password_buf = Util.convertStringToByteBuffer(addUserPassword.getText().toString(), BPprotocol.UserPD_maxLen);
                byte[] name_buf   = Util.convertStringToByteBuffer(ID, BPprotocol.UserID_maxLen);

                if(isAdmin)
                    bpProtocol.setEnrollAdmin(mSYS_BLE_MAC_Address_RAW, password_buf, name_buf);
                else
                    bpProtocol.setEnrollUser(mSYS_BLE_MAC_Address_RAW, password_buf, name_buf);



                HomeActivity activity =(HomeActivity)currActivity;
                activity.isEnroll = true;
                activity.connect(bdAddr);
                activity.StartConnectTimer();
            }
        });

        builder.setNeutralButton(currActivity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        mUsersEnrollDialog= builder.create();


        addUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.toString().isEmpty()) || (addUserPassword.length() < 4)||addUserName.length()< 0)
                    mUsersEnrollDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                else
                    mUsersEnrollDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        addUserPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.toString().length() < 4) || (addUserName.toString().isEmpty()))
                    mUsersEnrollDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                else
                    mUsersEnrollDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        mUsersEnrollDialog.show();
        mUsersEnrollDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public boolean isEnroll(){
        return isEnroll;
    }

    public void doneEnroll(){

        isEnroll = false;
    }

    public boolean isEnrollDialogIsShowing(){
        if(mUsersEnrollDialog != null)
        return mUsersEnrollDialog.isShowing();
        else
            return false;
    }



    private boolean check_Settings_File_Exist(String bdAddr) throws IOException {
        String file_name;

        //file_name = NKI_E1K_DB_DATA.E1K_SETTINGS_FILE_NAME+"_"+bdAddr+".bp";
        file_name = APPConfig.SETTINGS_FILE_NAME+".bp";
        File file;
        File root = Environment.getExternalStorageDirectory();
        //File root   = Environment.getDownloadCacheDirectory();
        //File root   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (root.canRead()) {
            File dir = new File(root.getAbsolutePath() + "/" + currActivity.getResources().getString(R.string.app_name));
            file = new File(dir, file_name);
            FileInputStream fis = null;
            ObjectInputStream ois = null;

            Util.debugMessage(TAG, "nki_Load_Settings_From_File() => " + file.toString() + " Exist? " + file.exists() + " isFile? " + file.isFile(),debugFlag);

            return file.exists();
        }

        return false;
    }

    public void Setup_Dialog_Backup( final int userMax) {

        isBackup = true;
        isBackupCancel = false;
        backupCount = 0;

        final ProgressDialog progressDialog;

        progressDialog = new ProgressDialog(currActivity);
        progressDialog.setMax(userMax+2);
        progressDialog.setMessage(currActivity.getString(R.string.backup_dialog_message));
        progressDialog.setTitle(currActivity.getString(R.string.backup_dialog_title));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressNumberFormat("%1d / %2d");

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, currActivity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//
                isBackup = false;
                isBackupCancel =true;

                bpProtocol.queueClear();

                progressDialog.dismiss();
            }
        });

        progressDialog.show();

        progressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isBackupDone = false;

                    //Request Data from Device
                    bpProtocol.getConfig();
                    //bpProtocol.getAdminDeviceName();
                    //bpProtocol.getDateTime();
                    bpProtocol.getAdminPWD();
                    if(userMax >0)
                        bpProtocol.getUserData(1);
                    Util.debugMessage(TAG,"userMax="+userMax,debugFlag);
                    do {
                        Thread.sleep(50);

                        int current_progress = backupCount; //* 100 / (userMax+4));
                        Util.debugMessage(TAG,"Current Progress="+current_progress,debugFlag);
                        progressDialog.setProgress(current_progress);

                        //Log.v("sean", "mE1K_DB_Data.data_Need_Received, remain = " + mE1K_DB_Data.data_Need_Received.size() + ", current = " + current_progress + ", Max = " + progressDialog.getMax());

                        if (progressDialog.getProgress() >= progressDialog.getMax()) {

                            isBackup = false;
                            isBackupDone = true;
                            final SettingActivity activity =(SettingActivity)currActivity;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    GeneralDialog.MessagePromptDialog(activity,activity .getString(R.string.backup_status), activity.getString(R.string.backup_completed));

                                }
                            });
                             progressDialog.dismiss();



                        } else {
                            Util.debugMessage(TAG, "progressDialog.getProgress() = " + progressDialog.getProgress(),debugFlag);
                        }

                    } while ((!isBackupDone) && (!isBackupCancel));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public boolean isBackup(){
        return isBackup;
    }
    public void backupRecevice(){
        backupCount++;
    }
    public int getBackupCount(){
        return  backupCount;
    }
    public void Setup_Dialog_Restore(final int userMax, final String bdAddr) {
        //final View item = LayoutInflater.from(currActivity).inflate(R.layout.setup_door_sensor_dialog, mRelativeLayout_Setup, false);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(currActivity);

        dialog.setTitle(currActivity.getString(R.string.restore_check_dialog_title));
        //dialog.setMessage(currActivity.getString(R.string.restore_check_dialog_message));

        //dialog.setView(item);

        dialog.setPositiveButton(currActivity.getResources().getString(R.string.Confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    //  Load_Settings_From_File(userMax,bdAddr);
                    restore_Process_Dialog(userMax);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        dialog.setNeutralButton(currActivity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        try {
            if (check_Settings_File_Exist(bdAddr)) {

                dialog.show();
            } else {

                GeneralDialog.MessagePromptDialog(currActivity,currActivity.getResources().getString(R.string.restore_status),currActivity.getResources().getString(R.string.restore_status_file_not_found));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void restore_Process_Dialog(/*final NKI_E1K_DB_DATA db_data,*/final int userMax) {

        final ProgressDialog progressDialog;

        progressDialog = new ProgressDialog(currActivity);
        progressDialog.setMax(100);
        progressDialog.setMessage(currActivity.getString(R.string.restore_dialog_message));
        progressDialog.setTitle(currActivity.getString(R.string.restore_dialog_title));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressNumberFormat("%1d / %2d");
        isRestore = true;
        restoreCount = 0;
        restoreCountbefore = restoreCount;
        isRestoreCancel =false;
        settingFile = new SettingData(APPConfig.SETTINGS_FILE_NAME+".bp");
        //Put Data to Queue

        //Util.debugMessage(TAG,"password="+db_data.device_Settings.AdminPWD.toString(),debugFlag);
        //Util.debugMessage(TAG,"password len="+db_data.device_Settings.AdminPWD.getBytes().length,debugFlag);
        byte config[]  = settingFile.readData();
        bpProtocol.setConfig(config);

        // bpProtocol.setConfig(db_data.device_Settings.door_sensor_opt, db_data.device_Settings.lock_type, db_data.device_Settings.delay_secs, db_data.device_Settings.ir_sensor_opt);



        // final int restore_max = 4 + db_data.users_items.size();
        final int userSize = settingFile.GetUserSize();
        final int restore_max = 4 + userSize;//db_data.users_items.size();
        Util.debugMessage(TAG,"restore max="+restore_max,debugFlag);
        progressDialog.setMax(restore_max);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, currActivity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SettingActivity activity =(SettingActivity)currActivity;
                isRestoreCancel = true;
                currActivity.onBackPressed();
                bpProtocol.queueClear();


                progressDialog.dismiss();
            }
        });


        progressDialog.show();

        progressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isRestoreDone  = false;

                    do {
                        Thread.sleep(200);


                        if(restoreCountbefore <restoreCount){
                            Util.debugMessage(TAG,"restoreCount="+(restoreCount)+"userSize="+userSize,debugFlag);
                            if(restoreCount ==1&&!isRestoreCmdPut){
                                byte adminPWD [] = settingFile.readData();
                                bpProtocol.setAdminPWD(adminPWD);
                                // bpProtocol.setAdminPWD(Util.convertStringToByteBuffer(db_data.device_Settings.AdminPWD,BPprotocol.UserPD_maxLen));
                                isRestoreCmdPut = true;
                                    /*else  if(restoreCount ==2){

                                    final int year =  db_data.device_Settings.deviceTime.get(Calendar.YEAR);
                                    final int month = db_data.device_Settings.deviceTime.get(Calendar.MONTH);
                                    final int dayOfMont = db_data.device_Settings.deviceTime.get(Calendar.DAY_OF_MONTH);
                                    final int hour = db_data.device_Settings.deviceTime.get(Calendar.HOUR_OF_DAY);
                                    final int min =  db_data.device_Settings.deviceTime.get(Calendar.MINUTE);
                                    final int sec = db_data.device_Settings.deviceTime.get(Calendar.SECOND);
                                    bpProtocol.setDateTime(year,month,dayOfMont,hour,min,sec);
                                }*/}else  if(restoreCount ==2&&!isRestoreCmdPut){
                                SettingActivity main = (SettingActivity)currActivity;
                                String dev_name = main.getDeviceName();
                                byte device_name_buf[] = new byte[BPprotocol.len_Device_Name-1];
                                byte[] device_name_tmp = dev_name.getBytes(Charset.forName("UTF-8"));
                                int device_name_len = 0;
                                int target_len = (device_name_tmp.length > device_name_buf.length) ? device_name_buf.length : device_name_tmp.length;
                                Util.debugMessage(TAG,"device_name_tmp length : " + device_name_tmp.length,debugFlag);

                                Arrays.fill(device_name_buf, (byte) 0xFF);

                                for (int i = 0; i < target_len; i++)
                                    device_name_buf[i] = device_name_tmp[i];
                                device_name_len = target_len;
                                bpProtocol.setDeviceName(device_name_buf, device_name_len);

                                isRestoreCmdPut = true;

                            }

                            else  if(restoreCount ==3&&!isRestoreCmdPut){

                                bpProtocol.setEraseUserList();
                                isRestoreCmdPut = true;
                            }
                            else if(restoreCount-4 < userSize/*db_data.users_items.size()*/&&!isRestoreCmdPut) {

                                byte cmd[]  = settingFile.readData();
                                bpProtocol.restoreUsersData(cmd);

                                isRestoreCmdPut = true;
                            }
                            restoreCountbefore = restoreCount;

                        }

                        int current_progress = restoreCount;

                        progressDialog.setProgress(current_progress);

                        Util.debugMessage(TAG, "Restore Process, remain = " + bpProtocol.getQueueSize() + ", current = " + current_progress + ", Max = " + progressDialog.getMax(),debugFlag);

                        if (progressDialog.getProgress() >= progressDialog.getMax()) {
                            Util.debug_vMessage(TAG,"progressDialog.getProgress Done !!!",debugFlag);

                            progressDialog.dismiss();

                            //Send Message


                            final SettingActivity activity = (SettingActivity)currActivity;
                            restoreCompleted();

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    GeneralDialog.restoreCompletedDialog(activity,activity.getString(R.string.restore_status), activity.getString(R.string.restore_completed));

                                }
                            });

                            //activity.show_toast_msg("restore ok\r\n");
                            //activity.sendProcessMessage(activity.MSG_RESTORE_OK);

                            isRestoreDone = true;
                        } else {
                            Util.debug_vMessage(TAG,"progressDialog.getProgress() = " + progressDialog.getProgress(),debugFlag);
                        }

                    } while ((!isRestoreDone ) && (!isRestoreCancel));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public boolean isRestore(){
        return isRestore;
    }
    public void restoreCompleted(){
        isRestore = false;
    }
    public void restoreSend(){
        restoreCount++;
        isRestoreCmdPut = false;
    }


}

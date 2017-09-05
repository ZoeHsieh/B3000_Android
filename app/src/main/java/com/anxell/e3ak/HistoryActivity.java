package com.anxell.e3ak;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.anxell.e3ak.custom.MyToolbar;
import com.anxell.e3ak.data.HistoryData;
import com.anxell.e3ak.transport.APPConfig;
import com.anxell.e3ak.transport.BPprotocol;
import com.anxell.e3ak.transport.RBLService;
import com.anxell.e3ak.transport.bpActivity;
import com.anxell.e3ak.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends bpActivity implements View.OnClickListener, OnRecyclerViewItemClickListener {
    private final String TAG = HistoryActivity.class.getSimpleName().toString();
    private final boolean debugFlag = true;
    private RecyclerView mRecyclerView;
    private HistoryAdapter mAdapter;
    private int historyReadIndex = 0;
    private int historyReadStart = 0;
    private int historyCount = 0;
    private int historyMax =0;
    public static boolean isHistoryDownloadOK = false;
    private Thread HistoryLoadingTHD = null;
    private ProgressDialog progressDialog = null;
    private boolean isLiveHTHD = false;
    private boolean isCancel = false;
    private static List<HistoryData> mHistoryDatas = new ArrayList<>();;
    private final byte OPEN_TYPE_NA = 0x00;  //Not Available
    private final byte OPEN_TYPE_ANDROID = 0x01;  //Android
    private final byte OPEN_TYPE_IOS = 0x02;  //IOS
    private final byte OPEN_TYPE_KEYPAD = 0x03;  //Keypad
    private final byte OPEN_TYPE_KEEP_ANDROID = 0x11;  //Android - Toggle Keep
    private final byte OPEN_TYPE_KEEP_IOS = 0x12;  //IOS - Toggle Keep
    private final byte OPEN_TYPE_KEEP_KEYPAD = 0x13;  //Keypad - Toggle Keep
    private final byte OPEN_TYPE_TAMPER_ALARM = 0x30;  //Tamper - Alarm
    private final byte OPEN_TYPE_EXIT_BUTTON = 0x31; // EXIT BUTTON
    private String deviceName = "";
    private boolean isDumpHistory = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Initial(getLocalClassName());
        setContentView(R.layout.activity_history);
        Intent intent = getIntent();
        deviceName = intent.getStringExtra(APPConfig.deviceNameTag);

        registerReceiver(mGattUpdateReceiver,  getIntentFilter());

        findViews();
        setListeners();
        setSearchViewTextSize();



        mAdapter = new HistoryAdapter(this,this);

        mAdapter.updateData(mHistoryDatas);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);


        bpProtocol.getHistoryCounter();
        currentClassName =getLocalClassName();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        currentClassName =getLocalClassName();
    }

    @Override
    public void cmdAnalysis(byte cmd, byte cmdType, byte data[], int datalen) {


        switch ((char) cmd) {

            case BPprotocol.cmd_history_data:

                //historyCount= (data[0] << 8)& 0x0000ff00 | (data[1]& 0x000000ff);

                if(historyReadIndex > 0)
                    historyReadIndex --;
                else
                    historyReadIndex = historyMax;

                if(data[2]!=(byte)0xFF & data[3]!=(byte)0xFF & data[data.length-1] != (byte)0xFF)
                    update_History_Data(data);

                historyCount++;

                if(historyCount < historyMax) {
                    bpProtocol.getHistoryByRange(historyReadIndex);

                }else{
                    isHistoryDownloadOK = true;

                }

                break;

            case BPprotocol.cmd_history_counter:
                historyMax = encode.getUnsignedTwoByte(data);
                bpProtocol.getHistoryNextIndex();

                break;

            case BPprotocol.cmd_history_next_index:
                historyReadStart = encode.getUnsignedTwoByte(data);
                historyReadIndex = historyReadStart;
                if(!isHistoryDownloadOK){
                    mHistoryDatas.clear();

                    historyCount = 0;
                    //historyReadIndex = 0;
                    if(historyMax >0){
                        historyReadIndex = historyReadStart;
                        History_Dialog_Loading();
                        bpProtocol.getHistoryByRange(historyReadIndex);


                    }else{
                        historyMax = 0;
                        isHistoryDownloadOK = true;
                    }


                }
                break;




        }
    }
    private void History_Dialog_Loading() {


        progressDialog = new ProgressDialog(this);

        progressDialog.setMax(historyMax);
        progressDialog.setMessage(getString(R.string.download_dialog_message));

        progressDialog.setTitle(getString(R.string.download_dialog_title) + getResources().getString(R.string.settings_history_list));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressNumberFormat("%1d / %2d");

        isCancel = false;

        progressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getResources().getString(R.string.progress_dialog_hide_btn_title),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();

            }
        });
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //show_toast_msg("Cancel !!");
                mHistoryDatas.clear();

                progressDialog.dismiss();
                HistoryLoadingTHD.interrupt();
                isLiveHTHD = false;
                HistoryLoadingTHD = null;
                onBackPressed();
            }
        });

        progressDialog.show();

        //progressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        if(HistoryLoadingTHD == null) {
            isLiveHTHD = true;
            HistoryLoadingTHD = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean isDone = false;

                        do {
                            Thread.sleep(150);

                            progressDialog.setProgress(historyCount);

                            Util.debugMessage(TAG, "historyCount = " + historyCount + ", Max = " + progressDialog.getMax(), debugFlag);

                            if (progressDialog.getProgress() >= progressDialog.getMax() || historyCount >= historyMax) {
                                Util.debugMessage(TAG, "progressDialog.getProgress Done !!!", debugFlag);

                                progressDialog.dismiss();
                                isDone = true;
                                isLiveHTHD = false;
                                HistoryLoadingTHD.interrupt();
                                HistoryLoadingTHD = null;
                                isHistoryDownloadOK = true;
                            } else {
                                Util.debugMessage(TAG, "progressDialog.getProgress() = " + progressDialog.getProgress(), debugFlag);
                                Util.debugMessage(TAG, "progressDialog.getMax() = " + progressDialog.getMax(), debugFlag);
                            }

                        } while ((!isDone) && (!isCancel)&&isLiveHTHD);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            HistoryLoadingTHD.start();
        }
    }

    private void update_History_Data(byte[] data) {


        //Get History Data

        int year;
        byte month, day, hours, minutes, seconds;
        String strDate;
        String strTime;
        byte open_type;
        String strOS_Type;
        String strAll;

        //Get DateTime
        year = ((data[2] << 8) & 0x0000ff00) | (data[3] & 0x000000ff);
        month = data[4];
        day = data[5];
        hours = data[6];
        minutes = data[7];
        seconds = data[8];

        strDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
        strTime = String.format(Locale.US,"%02d:%02d:%02d",hours, minutes, seconds);
        int nameLen = BPprotocol.UserID_maxLen;
        byte tmpArray[] = Arrays.copyOfRange(data, 9, data.length - 1);
        for (int i = 0; i < tmpArray.length; i++) {
            Util.debugMessage(TAG, "tmpArray" + String.format("[%d]=%02x", i, tmpArray[i]), debugFlag);
            if (tmpArray[i] == (byte) BPprotocol.nullData) {
                nameLen = i;
                i = tmpArray.length + i;
            }
        }
        Util.debugMessage(TAG, "nameLen=" + nameLen, debugFlag);
        byte nameArray[] = Arrays.copyOf(tmpArray, nameLen);
        String userID = new String(nameArray, Charset.forName("UTF-8"));
        if(userID.equals(APPConfig.ADMIN_ID))
            userID = APPConfig.ADMIN_ENROLL;
        Util.debugMessage(TAG, "user id=" + userID, debugFlag);


        //Get Open-Type
        open_type = data[25];
        if (open_type == OPEN_TYPE_TAMPER_ALARM)
            userID = getString(R.string.tamper_sensor);
        else if (open_type == OPEN_TYPE_EXIT_BUTTON)
            userID = getString(R.string.openType_Button);
        strOS_Type = Convert_OS_Type_To_String(open_type);

        strAll = String.format(Locale.US, "%2d. %s  %s Phone-ID: %s %s", historyCount, strDate,strTime, userID,strOS_Type);
        Util.debugMessage(TAG, "history data=" + strAll, debugFlag);
        mHistoryDatas.add(new HistoryData(userID,strDate,strTime, strOS_Type));


        mAdapter.updateData(mHistoryDatas);
    }
    private void findViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void History_Dialog_Save() {
       if(!isDumpHistory){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isDumpHistory = true;
                        Thread.sleep(200);

                          save_History_To_File();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

      }
    }

    private void save_History_To_File() {
        String file_name;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf_curr = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US);

        file_name = String.format(Locale.US, "%s_%s.csv", deviceName, sdf_curr.format(c.getTime()));

        File file;
        File root = Environment.getExternalStorageDirectory();
        //File root   = Environment.getDownloadCacheDirectory();
        //File root   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (root.canWrite()) {
            File dir = new File(root.getAbsolutePath() + "/" + getResources().getString(R.string.app_name));
            dir.mkdirs();
            file = new File(dir, file_name);
            FileOutputStream out = null;

            Util.debugMessage(TAG, "nki_Save_History_To_File() => " + file.toString(),debugFlag);

            try {

                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {


                e.printStackTrace();
            }
            try {
                String data_String;
                int idx = 0;

                //Title
                data_String = "\"No\",\"Name\",\"DateTime\",\"Type\" \n";
                out.write(data_String.getBytes());
                Util.debugMessage(TAG, "test6",debugFlag);
                //Write Data
                for (HistoryData item : mHistoryDatas) {


                    data_String = String.format(Locale.US, "\"%d\",\"%s\",\"%s\",\"%s\"\n", idx, item.getId(),item.getDateTime(), item.getDevice());


                    out.write(data_String.getBytes());

                    idx++;
                }

            } catch (IOException e) {

                e.printStackTrace();
            }
            try {
                out.close();

                Uri uriToImage;
                uriToImage = Uri.fromFile(file);

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
                shareIntent.setType("text/csv");

                startActivity(Intent.createChooser(shareIntent, "SAVE TO:"));

            } catch (IOException e) {
                  e.printStackTrace();
            }
        }
        isDumpHistory = false;
        Util.debugMessage(TAG, "Save [" + file_name + "]",debugFlag);
    }
    private String Convert_OS_Type_To_String(byte type) {
        String strOS_Type;
        switch (type) {
            case OPEN_TYPE_NA:
                strOS_Type = "Not Available";
                break;
            case OPEN_TYPE_ANDROID:
                strOS_Type = "Android";
                break;
            case OPEN_TYPE_IOS:
                strOS_Type = "IOS";
                break;
            case OPEN_TYPE_KEYPAD:
                strOS_Type = "Keypad";
                break;
            case OPEN_TYPE_KEEP_KEYPAD:
                strOS_Type = getString(R.string.openType_Keypad);
                break;
            case OPEN_TYPE_TAMPER_ALARM:
                strOS_Type = getString(R.string.openType_Alarm);
                break;
            case OPEN_TYPE_EXIT_BUTTON:
                strOS_Type = getString(R.string.openType_Button);
                break;
            default:
                strOS_Type = "unKnown";
                break;
        }

        return strOS_Type;
    }
    private void setListeners() {
        MyToolbar toolbar = (MyToolbar) findViewById(R.id.toolbarView);
        toolbar.setRight1IconClickListener(this);
        toolbar.setRight2IconClickListener(this);

    }

    private void setSearchViewTextSize() {
        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(13);
    }
    @Override
    public void cmdDataCacheEvent(byte cmd, byte cmdType,byte data[], int datalen){
        Util.debugMessage(TAG,"history cache",debugFlag);


    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rightIcon1:
                if (isHistoryDownloadOK){
                    historyReadStart = 0;
                    historyCount = 0;
                    historyMax = 0;
                    mHistoryDatas.clear();
                    isHistoryDownloadOK = false;
                    bpProtocol.getHistoryCounter();
                }else{
                    if(progressDialog != null)
                        progressDialog.show();

                }
                break;

            case R.id.rightIcon2:
                if(isHistoryDownloadOK){
                    Util.debugMessage(TAG,"test1",debugFlag);
                    History_Dialog_Save();
                    Util.debugMessage(TAG,"test2",debugFlag);
                }else{
                    if(progressDialog != null)
                        progressDialog.show();

                }
               // showShareView();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if(!isHistoryDownloadOK){
            if(progressDialog!=null){
                progressDialog.show();
            }
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    private void showShareView() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "E3AK");
        startActivity(Intent.createChooser(sharingIntent, "Share"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isCancel = true;
        unregisterReceiver(mGattUpdateReceiver);
        overridePendingTransitionLeftToRight();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
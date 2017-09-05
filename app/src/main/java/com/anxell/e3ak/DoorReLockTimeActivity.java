package com.anxell.e3ak;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;

import com.anxell.e3ak.custom.MyEditText;
import com.anxell.e3ak.transport.BPprotocol;
import com.anxell.e3ak.transport.bpActivity;

public class DoorReLockTimeActivity extends bpActivity {

    private MyEditText mSecET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door_re_lock_time);

        findViews();
        int delayTime = ((SettingActivity.tmpConfig[2] << 8) & 0x0000ff00) | (SettingActivity.tmpConfig[3] & 0x000000ff);
        mSecET.setText(""+delayTime);
        if(mSecET.getText().length()>0){
            mSecET.setETSelection(mSecET.getText().length());
        }

        mSecET.addTextChangedListener(new TextWatcher() {
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
                 if(mSecET.getText().length() >0) {
                     final int delayTimes =  Integer.parseInt(mSecET.getText());

                     if (delayTimes <=0 || delayTimes >1800)
                         show_toast_msg(getString(R.string.over_range_alarm));
                 }
            }
        });
    }

    private void findViews() {
        mSecET = (MyEditText) findViewById(R.id.sec);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mSecET.getText().length()>0) {
            int delayTime = Integer.parseInt(mSecET.getText());
            if (delayTime >0 && delayTime <=1800){
            SettingActivity.tmpConfig[2] = (byte) (delayTime >> 8);
            SettingActivity.tmpConfig[3] = (byte) (delayTime & 0xFF);
            SettingActivity.updateStatus = SettingActivity.up_deviceConfig;
            }
        }
        overridePendingTransitionLeftToRight();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

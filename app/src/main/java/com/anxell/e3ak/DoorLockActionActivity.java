package com.anxell.e3ak;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.anxell.e3ak.custom.MyToolbar;
import com.anxell.e3ak.transport.BPprotocol;

public class DoorLockActionActivity extends BaseActivity {
    private RadioGroup doorAction;
    private RadioButton doorDelayTime;
    private RadioButton doorAlwaysOpen;
    private RadioButton doorAlwayslock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door_lock_action);
        doorAction = (RadioGroup) findViewById(R.id.rgroup);
        doorDelayTime = (RadioButton) findViewById(R.id.use_re_lock_time);
        doorAlwaysOpen = (RadioButton) findViewById(R.id.always_unlocked);
        doorAlwayslock = (RadioButton) findViewById(R.id.always_locked);
        doorAction.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {

                    case R.id.use_re_lock_time:
                        doorDelayTime.setChecked(true);
                        doorAlwaysOpen.setChecked(false);
                        doorAlwayslock.setChecked(false);
                        SettingActivity.tmpConfig[1] = BPprotocol.door_statis_delayTime;
                        break;

                    case R.id.always_locked:
                        doorDelayTime.setChecked(false);
                        doorAlwaysOpen.setChecked(false);
                        doorAlwayslock.setChecked(true);
                        SettingActivity.tmpConfig[1] = BPprotocol.door_statis_KeepLock;
                        break;
                    case R.id.always_unlocked:
                        doorDelayTime.setChecked(false);
                        doorAlwaysOpen.setChecked(true);
                        doorAlwayslock.setChecked(false);
                        SettingActivity.tmpConfig[1] = BPprotocol.door_statis_KeepOpen;
                        break;
                }
            }
        });
        if (SettingActivity.tmpConfig[1] == BPprotocol.door_statis_delayTime) {
            doorDelayTime.setChecked(true);
            doorAlwaysOpen.setChecked(false);
            doorAlwayslock.setChecked(false);
        } else if (SettingActivity.tmpConfig[1] == BPprotocol.door_statis_KeepOpen) {
            doorDelayTime.setChecked(false);
            doorAlwaysOpen.setChecked(true);
            doorAlwayslock.setChecked(false);
        } else if (SettingActivity.tmpConfig[1] == BPprotocol.door_statis_KeepLock) {
        doorDelayTime.setChecked(false);
        doorAlwaysOpen.setChecked(false);
        doorAlwayslock.setChecked(true);
    }

}
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SettingActivity.updateStatus = SettingActivity.up_deviceConfig;
        overridePendingTransitionLeftToRight();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

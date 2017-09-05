package com.anxell.e3ak;

import android.graphics.Color;
import android.os.Bundle;

import com.anxell.e3ak.custom.FontButton;
import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.custom.My3TextView;
import com.anxell.e3ak.transport.APPConfig;

public class AboutUsActivity extends BaseActivity {
    private FontButton appversion;
    private My3TextView mModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        appversion = (FontButton)findViewById(R.id.build);
        mModel = (My3TextView) findViewById(R.id.deviceName);
        mModel.setBackground(Color.TRANSPARENT);
        appversion.setText(getString(R.string.APP_version)+APPConfig.AppVersion);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionLeftToRight();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

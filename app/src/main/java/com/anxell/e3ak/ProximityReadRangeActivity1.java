package com.anxell.e3ak;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.anxell.e3ak.custom.FontTextView;

public class ProximityReadRangeActivity1 extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private FontTextView mDeviceDistanceTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximity_read_range_1);

        findViews();
        setListeners();
    }

    private void findViews() {
        mDeviceDistanceTV = (FontTextView) findViewById(R.id.deviceDistanceValue);
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
        mDeviceDistanceTV.setText(progress + "");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}

package com.anxell.e3ak;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.anxell.e3ak.custom.FontButton;
import com.anxell.e3ak.custom.MyButton;
import com.anxell.e3ak.custom.MyDeviceView;


public class SearchActivity extends BaseActivity implements View.OnClickListener {

    private ImageButton mResearchIB;
    private FontButton mSkipBtn;
    private View mSearchingView;
    private View mFoundView;
    private MyDeviceView mDeviceV;
    private MyButton mNextBtn;


    private MyCount myCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        findViews();
        setListeners();

        // start demo
        myCount = new MyCount(1000, 1000);
        mResearchIB.callOnClick();

        mDeviceV.setDeviceName("EA3K001");
        // end demo
    }

    // start demo
    class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            showFoundView();
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }
    // end demo

    private void findViews() {
        mResearchIB = (ImageButton) findViewById(R.id.research);
        mSkipBtn = (FontButton) findViewById(R.id.skip);
        mSearchingView = findViewById(R.id.searchingContent);
        mFoundView = findViewById(R.id.foundContent);
        mDeviceV = (MyDeviceView) mFoundView.findViewById(R.id.deviceView);
        mNextBtn = (MyButton) findViewById(R.id.next);
    }

    private void setListeners() {
        findViewById(R.id.skip).setOnClickListener(this);
        mResearchIB.setOnClickListener(this);
        mDeviceV.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.skip:
                openHomePage();
                break;
            case R.id.research:
                showSearchView();
                myCount.start();
                break;

            case R.id.deviceView:
                showDevices();
                break;

            case R.id.next:
                openPasswordPage();
                break;
        }
    }

    private void openHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void openPasswordPage() {
        Intent intent = new Intent(this, PasswordActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void showSearchView() {
        mSkipBtn.setVisibility(View.VISIBLE);
        mSearchingView.setVisibility(View.VISIBLE);

        mResearchIB.setVisibility(View.GONE);
        mFoundView.setVisibility(View.GONE);
        mNextBtn.setBackground(R.drawable.disabled_btn);
        mNextBtn.setEnabled(false);
    }

    private void showFoundView() {
        mSkipBtn.setVisibility(View.GONE);
        mSearchingView.setVisibility(View.GONE);

        mResearchIB.setVisibility(View.VISIBLE);
        mFoundView.setVisibility(View.VISIBLE);
        mNextBtn.setBackground(R.drawable.green_btn);
        mNextBtn.setEnabled(true);
    }

    private void showDevices() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setTitle(R.string.choose_device);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.cancel, null);

        ListView listView = new ListView(this);
        final String[] items = {"EA3K001", "EA3K-2nd-floor-10", "公司大門"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_device, R.id.text, items);

        listView.setAdapter(adapter);

        builder.setView(listView);
        final AlertDialog dialog = builder.create();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDeviceV.setDeviceName(items[position]);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

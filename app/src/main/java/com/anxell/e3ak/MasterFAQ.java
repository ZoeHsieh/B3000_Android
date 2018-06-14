package com.anxell.e3ak;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import com.anxell.e3ak.transport.bpActivity;

/**
 * Created by twkazuya on 2017/11/8.
 */

public class MasterFAQ extends bpActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Initial(getLocalClassName());
        setContentView(R.layout.master_faq);

        findViewById(R.id.faq_scrollview).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    SettingActivity.setcurrentdate();
                }

                return false;
            }
        });

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
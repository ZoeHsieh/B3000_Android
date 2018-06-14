package com.anxell.e3ak;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import com.anxell.e3ak.transport.bpActivity;

/**
 * Created by twkazuya on 2017/11/8.
 */

public class UserFAQ extends bpActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Initial(getLocalClassName());
        setContentView(R.layout.user_faq);
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
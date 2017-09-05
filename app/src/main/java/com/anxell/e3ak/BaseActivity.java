package com.anxell.e3ak;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseActivity extends AppCompatActivity {

//    @Override
//    public void finish() {
//        super.finish();
//        overridePendingTransitionLeftToRight();
//    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
//        overridePendingTransitionEnter();
    }

    protected void overridePendingTransitionRightToLeft() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    protected void overridePendingTransitionLeftToRight() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    protected void overridePendingTransitionBottomToTop() {
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
    }

    protected void overridePendingTransitionTopToBottom() {
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom);
    }

    protected void showMsg(int msgResId) {
        Toast.makeText(this, msgResId, Toast.LENGTH_SHORT).show();
    }

    protected String dateTimeFormat(Date date) {
        //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd  HH:mm");
        return formatter.format(date);
    }

    protected String timeFormat(Date date) {
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
        return df.format(date);
    }

    protected String timeFormat(String date) {
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
        return df.format(date);
    }
}

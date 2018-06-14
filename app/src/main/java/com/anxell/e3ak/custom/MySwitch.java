package com.anxell.e3ak.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;

import com.anxell.e3ak.R;

/**
 * Created by kay on 2017/6/8.
 */

public class MySwitch extends FrameLayout {
    private FontTextView mTitleTV;
    private SwitchCompat mSwitch;

    public MySwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.my_switch, this);

        mTitleTV = (FontTextView) findViewById(R.id.title);
        mSwitch = (SwitchCompat) findViewById(R.id.switchBtn);
        mSwitch.setFocusable(false);
        showMyAttrs(context, attrs);
    }

    private void showMyAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyFieldAttr);
        String titleText = typedArray.getString(R.styleable.MyFieldAttr_myTitle);
        mTitleTV.setText(titleText);

        typedArray.recycle();
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        mSwitch.setOnCheckedChangeListener(listener);
    }
    public void setSwitchCheck(boolean checked){

        mSwitch.setChecked(checked);
    }
    public void setSwitchClickable(boolean enable){
        mSwitch.setClickable(enable);
    }
    public boolean isSwitchCheck(){

        return  mSwitch.isChecked();
    }
}
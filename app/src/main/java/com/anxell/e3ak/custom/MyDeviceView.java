package com.anxell.e3ak.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.anxell.e3ak.R;

/**
 * Created by kay on 2017/6/8.
 */

public class MyDeviceView extends FrameLayout {
    private FontTextView mDeviceNameTV;

    public MyDeviceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.my_device_view, this);

        mDeviceNameTV = (FontTextView) findViewById(R.id.foundDeviceName);

        showMyAttrs(context, attrs);
    }

    private void showMyAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyDeviceViewAttr);
        boolean showDot = typedArray.getBoolean(R.styleable.MyDeviceViewAttr_showDot, true);
        int fontSize = typedArray.getDimensionPixelSize(R.styleable.MyDeviceViewAttr_android_textSize, 36);

        if (showDot) {
            findViewById(R.id.dot).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.dot).setVisibility(View.GONE);
        }

        mDeviceNameTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);

        typedArray.recycle();
    }
    public void setStatusDotEnable(boolean enable){
        if (enable) {
            findViewById(R.id.dot).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.dot).setVisibility(View.GONE);
        }
    }
    public String getDeviceName() {
        return mDeviceNameTV.getText().toString();
    }

    public void setDeviceName(String name) {
        mDeviceNameTV.setText(name);
    }

    public void setSelection(boolean selection){
            if(selection)
                mDeviceNameTV.setTextColor(getResources().getColor(R.color.dark_yellow));
            else
                mDeviceNameTV.setTextColor(getResources().getColor(R.color.gray4));


    }
}
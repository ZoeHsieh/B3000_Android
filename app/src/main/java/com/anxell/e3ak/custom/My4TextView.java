package com.anxell.e3ak.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.Image;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.anxell.e3ak.R;

/**
 * Created by kay on 2017/6/8.
 */

public class My4TextView extends FrameLayout {
    private FontTextView mTitleTV;
    private FontTextView mValueTV;


    public My4TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.my_4_textview, this);

        mTitleTV = (FontTextView) findViewById(R.id.title);
        mValueTV = (FontTextView) findViewById(R.id.value);

        mValueTV.setTextColor(getResources().getColor(R.color.gray2));
        showMyAttrs(context, attrs);
    }

    private void showMyAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyFieldAttr);
        String title = typedArray.getString(R.styleable.MyFieldAttr_myTitle);
        mTitleTV.setText(title);

        String value = typedArray.getString(R.styleable.MyFieldAttr_value);
        if (!TextUtils.isEmpty(value)) {
            mValueTV.setText(value);
        }



        typedArray.recycle();
    }

    public String getValue() {
        return mValueTV.getText().toString();
    }

    public void setValue(String value) {
        mValueTV.setText(value);
    }
}
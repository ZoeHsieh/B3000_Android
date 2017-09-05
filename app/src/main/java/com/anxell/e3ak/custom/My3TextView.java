package com.anxell.e3ak.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.anxell.e3ak.R;

/**
 * Created by kay on 2017/6/8.
 */

public class My3TextView extends FrameLayout {
    private FontTextView mTitleTV;
    private FontTextView mValueTV;
    private RelativeLayout mLayout;
    public My3TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.my_3_textview, this);

        mTitleTV = (FontTextView) findViewById(R.id.title);
        mValueTV = (FontTextView) findViewById(R.id.value);
        mLayout = (RelativeLayout) findViewById(R.id.my_3_layout);
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

    public void setBackground(int color){
        mLayout.setBackgroundColor(color);
    }
}
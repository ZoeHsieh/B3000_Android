package com.anxell.e3ak.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.anxell.e3ak.R;

/**
 * Created by kay on 2017/6/8.
 */

public class MyButton extends FrameLayout {

    private ImageView mBackgrountIV;
    private FontTextView mText;

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.my_button, this);

        mBackgrountIV = (ImageView) findViewById(R.id.background);
        mText = (FontTextView) findViewById(R.id.text);

        showMyAttrs(context, attrs);
    }

    private void showMyAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyButtonAttr);

        int background = typedArray.getResourceId(R.styleable.MyButtonAttr_android_src, R.drawable.green_btn);
        mBackgrountIV.setImageResource(background);

        String text = typedArray.getString(R.styleable.MyButtonAttr_android_text);
        mText.setText(text);

        typedArray.recycle();
    }

    public void setBackground(int resId) {
        mBackgrountIV.setImageResource(resId);
    }
}
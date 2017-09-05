package com.anxell.e3ak.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.anxell.e3ak.Config;


/**
 * Created by nsdi-monkey on 2017/2/14.
 */

public class FontButton extends Button {

    public FontButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 設定字型
        setTypeface(Typeface.createFromAsset(context.getAssets(), Config.TYPEFACE));
    }

//    @Override
//    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK)
//        {
//            clearFocus();
//        }
//        return super.onKeyPreIme(keyCode, event);
//    }
}

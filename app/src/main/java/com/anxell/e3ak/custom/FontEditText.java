package com.anxell.e3ak.custom;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.anxell.e3ak.Config;
import com.anxell.e3ak.R;


/**
 * Created by nsdi-monkey on 2017/2/14.
 */

public class FontEditText extends EditText {

    public FontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 設定字型
        setTypeface(Typeface.createFromAsset(context.getAssets(), Config.TYPEFACE));
    }
}

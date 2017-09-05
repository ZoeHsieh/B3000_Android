package com.anxell.e3ak;

import android.view.View;

/**
 * Created by nsdi-monkey on 2016/10/12.
 */

public interface OnRecyclerViewItemClickListener {
    void onItemClick(View view, int position);
    void onItemLongClick(View view, int position);
}

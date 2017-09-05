package com.anxell.e3ak;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by nsdi-monkey on 2017/6/13.
 */

public class WeekAdapter extends BaseAdapter {
    private String[] mDatas;

    private LayoutInflater mInflater;
    private boolean weeklyCheckList[];
    public WeekAdapter(Context c, String[] data,boolean ItemCheck[]) {
        mDatas = data;
        weeklyCheckList = ItemCheck;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mDatas.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    public void updateItemCheck(boolean itemCheck[]){
        weeklyCheckList = itemCheck;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_checkbox, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.mCheckBoxIV = (CheckedTextView) convertView.findViewById(R.id.check);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mCheckBoxIV.setText(mDatas[position]);
        viewHolder.mCheckBoxIV.setChecked(weeklyCheckList[position]);
        return convertView;
    }

    private class ViewHolder {
        private CheckedTextView mCheckBoxIV;
    }
}
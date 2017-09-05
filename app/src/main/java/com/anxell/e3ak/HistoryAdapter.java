package com.anxell.e3ak;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.data.HistoryData;

import java.util.List;

import static com.anxell.e3ak.R.color.red;

/**
 * Created by nsdi-monkey on 2017/6/12.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryData> mDatas;
    private OnRecyclerViewItemClickListener mOnItemClickListener;
    private Activity parentActivity;
    public void updateData(List<HistoryData> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    public HistoryAdapter(OnRecyclerViewItemClickListener listener, Activity activity) {
        mOnItemClickListener = listener;
        parentActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HistoryData data = mDatas.get(position);
        String id = data.getId();
        String dateTime = data.getDateTime();
        String device = data.getDevice();

        holder.mIdTV.setText(id);
        holder.mDateTimeTV.setText(dateTime);
        holder.mDeviceTV.setText(device);
        if(device.equals(parentActivity.getString(R.string.openType_Alarm))){
            holder.mIdTV.setTextColor(Color.RED);
            holder.mDateTimeTV.setTextColor(Color.RED);
            holder.mDeviceTV.setTextColor(Color.RED);

        }
        else if(device.equals(parentActivity.getString(R.string.openType_Button))){
            holder.mIdTV.setTextColor(Color.BLUE);
            holder.mDateTimeTV.setTextColor(Color.BLUE);
            holder.mDeviceTV.setTextColor(Color.BLUE);

        }else{
            holder.mIdTV.setTextColor(parentActivity.getResources().getColor(R.color.green));
            holder.mDateTimeTV.setTextColor(parentActivity.getResources().getColor(R.color.gray4));
            holder.mDeviceTV.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public FontTextView mIdTV;
        public FontTextView mDateTimeTV;
        public FontTextView mDeviceTV;

        public ViewHolder(View v) {
            super(v);
            mIdTV = (FontTextView) v.findViewById(R.id.id);
            mDateTimeTV = (FontTextView) v.findViewById(R.id.date);
            mDeviceTV = (FontTextView) v.findViewById(R.id.found);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener == null) return;
            mOnItemClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}

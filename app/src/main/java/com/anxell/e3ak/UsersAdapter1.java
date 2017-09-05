package com.anxell.e3ak;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.data.UserData;

import java.util.List;

/**
 * Created by nsdi-monkey on 2017/6/12.
 */

public class UsersAdapter1 extends RecyclerView.Adapter<UsersAdapter1.ViewHolder> {
    public static final int TYPE_FOOTER = 0;
    public static final int TYPE_NORMAL = 1;

    private List<UserData> mDataList;
    private View mFooterView;

    public void updateData(List<UserData> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }

    public View getFooterView() {
        return mFooterView;
    }
    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (mFooterView == null){
            return TYPE_NORMAL;
        }

        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mFooterView != null && viewType == TYPE_FOOTER){
            return new ViewHolder(mFooterView);
        }

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(getItemViewType(position) != TYPE_NORMAL) return;

        UserData data = mDataList.get(position);
        String id = data.getId();
        String password = data.getPasswrod();


        holder.mIdTV.setText(id);
       // if (TextUtils.isEmpty(type)) {
        //    holder.mTypeTV.setVisibility(View.GONE);
       // } else {
            holder.mTypeTV.setVisibility(View.VISIBLE);
            //holder.mTypeTV.setText(type);
        //}

        holder.mPasswordTV.setText(password);

        holder.mIndicatorIV.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        if (mFooterView != null){
            return mDataList.size() + 1;
        }

        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public FontTextView mIdTV;
        public FontTextView mTypeTV;
        public FontTextView mPasswordTV;
        public FontTextView mNameTV;
        public ImageView mIndicatorIV;

        public ViewHolder(View v) {
            super(v);

            if (v == mFooterView){
                return;
            }

            mIdTV = (FontTextView) v.findViewById(R.id.id);

            mPasswordTV = (FontTextView) v.findViewById(R.id.password);

            mIndicatorIV = (ImageView) v.findViewById(R.id.indicator);
        }
    }
}

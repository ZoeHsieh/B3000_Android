package com.anxell.e3ak;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.data.UserData;

import java.util.List;

/**
 * Created by nsdi-monkey on 2017/6/12.
 */

public class UsersAdapter2 extends RecyclerView.Adapter<UsersAdapter2.ViewHolder> {

    private List<UserData> mDataList;
    private OnRecyclerViewItemClickListener mOnItemClickListener;

    public void updateData(List<UserData> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }

    public UsersAdapter2(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserData data = mDataList.get(position);
        String id = data.getId();
        String password = data.getPasswrod();


        holder.mIdTV.setText(id);

        holder.mPasswordTV.setText(password);

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public FontTextView mIdTV;
        public FontTextView mTypeTV;
        public FontTextView mPasswordTV;
        public FontTextView mNameTV;

        public ViewHolder(View v) {
            super(v);
            mIdTV = (FontTextView) v.findViewById(R.id.id);

            mPasswordTV = (FontTextView) v.findViewById(R.id.password);


            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener == null) return;
            mOnItemClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mOnItemClickListener == null) return true;
            mOnItemClickListener.onItemLongClick(view, getAdapterPosition());
            return true;
        }
    }
}

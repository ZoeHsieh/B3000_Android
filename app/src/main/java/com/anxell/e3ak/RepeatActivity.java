package com.anxell.e3ak;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.anxell.e3ak.transport.BPprotocol;


public class RepeatActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private WeekAdapter mAdapter;
    private boolean weeklyCheckList[] = {false,false,false,false,false,false,false};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat);

        findViews();
        setListeners();

        mAdapter = new WeekAdapter(this, getResources().getStringArray(R.array.week),weeklyCheckList);
        mListView.setAdapter(mAdapter);
        if((AccessTypesScheduleActivity.tmpWeekly & BPprotocol.WEEKLY_TYPE_MON)!=0)
            weeklyCheckList[0] = true;
        if((AccessTypesScheduleActivity.tmpWeekly & BPprotocol.WEEKLY_TYPE_TUE)!=0)
            weeklyCheckList[1] = true;
        if((AccessTypesScheduleActivity.tmpWeekly & BPprotocol.WEEKLY_TYPE_WED)!=0)
            weeklyCheckList[2] = true;
        if((AccessTypesScheduleActivity.tmpWeekly & BPprotocol.WEEKLY_TYPE_THU)!=0)
            weeklyCheckList[3] = true;
        if((AccessTypesScheduleActivity.tmpWeekly & BPprotocol.WEEKLY_TYPE_FRI)!=0)
            weeklyCheckList[4] = true;
        if((AccessTypesScheduleActivity.tmpWeekly & BPprotocol.WEEKLY_TYPE_SAT)!=0)
            weeklyCheckList[5] = true;
        if((AccessTypesScheduleActivity.tmpWeekly & BPprotocol.WEEKLY_TYPE_SUN)!=0)
            weeklyCheckList[6] = true;
        mAdapter.updateItemCheck(weeklyCheckList);
        mAdapter.notifyDataSetChanged();
    }

    private void findViews() {
        mListView = (ListView) findViewById(R.id.listView);
    }

    private void setListeners() {
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        CheckedTextView chkItem = (CheckedTextView) view.findViewById(R.id.check);
        weeklyCheckList[position] = !chkItem.isChecked();
        chkItem.setChecked(!chkItem.isChecked());
        mAdapter.updateItemCheck(weeklyCheckList);
        mAdapter.notifyDataSetChanged();
        byte weekValue[] = { BPprotocol.WEEKLY_TYPE_MON,
                             BPprotocol.WEEKLY_TYPE_TUE,
                             BPprotocol.WEEKLY_TYPE_WED,
                             BPprotocol.WEEKLY_TYPE_THU,
                             BPprotocol.WEEKLY_TYPE_FRI,
                             BPprotocol.WEEKLY_TYPE_SAT,
                             BPprotocol.WEEKLY_TYPE_SUN};
        byte tmpWeekly =0x00;
        for(int i=0;i<weeklyCheckList.length;i++) {
            if (weeklyCheckList[i])
                tmpWeekly |= weekValue[i];
        }
        AccessTypesScheduleActivity.tmpWeekly = tmpWeekly;
       // Toast.makeText(RepeatActivity.this, "您點選了第 "+(position+1)+" 項", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionLeftToRight();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

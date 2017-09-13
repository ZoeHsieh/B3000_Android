package com.anxell.e3ak;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.anxell.e3ak.custom.My2TextView;
import com.anxell.e3ak.custom.MyEditText;
import com.anxell.e3ak.transport.BPprotocol;
import com.anxell.e3ak.transport.bpActivity;
import com.anxell.e3ak.util.Util;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.anxell.e3ak.transport.bpActivity.bpProtocol;
import static com.anxell.e3ak.transport.bpActivity.currentClassName;

public class AccessTypesScheduleActivity extends bpActivity implements View.OnClickListener {
    private final String TAG = AccessTypesScheduleActivity.class.getSimpleName();
    private final boolean debugFlag = true;
    private View mScheduleDetail;
    private My2TextView mScheduleStartTV;
    private My2TextView mScheduleEndTV;

    private MyEditText mAccessTimesDetailET;

    private View mRecurrentDetail;
    private My2TextView mRecurrentStartTV;
    private My2TextView mRecurrentEndTV;
    private My2TextView mRecurrentRepeatTV;
    private RadioButton permanent_RDB;
    private RadioButton schedule_RDB;
    private RadioButton accessTimes_RDB;
    private RadioButton recurrent_RDB;
    private View mSelected = null;
    private byte startTime[] = new byte[BPprotocol.len_Device_Time];
    private byte endTime[] = new byte[BPprotocol.len_Device_Time];
    private byte currProperty[]= new byte[BPprotocol.len_UserProperty_write];
    private final int type_StartTime = 0;
    private final int type_EndTime = 1;
    public static boolean isUpdateWeekly = false;
    public static int tmpWeekly = 0x00;
    private int currWeekly = 0x00;
    private boolean isAccessTimesOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_types_schedule);

        findViews();
        setListeners();
        currProperty = UserInfoActivity.tmpWriteUserProperty;
        for(int i =0;i<currProperty.length;i++)
            Util.debugMessage(TAG,"currProperty["+i+"]="+String.format("%02x",currProperty[i]),debugFlag);

        startTime = Arrays.copyOfRange(currProperty,4,11);
        endTime = Arrays.copyOfRange(currProperty,11,18);
        currWeekly = currProperty[19];
        //Calendar calendar = Calendar.getInstance();

        if(currProperty[3]==BPprotocol.LIMIT_TYPE_NA){
            permanent_RDB.setChecked(true);
            schedule_RDB.setChecked(false);
            accessTimes_RDB.setChecked(false);
            recurrent_RDB.setChecked(false);
        }else if(currProperty[3]==BPprotocol.LIMIT_TYPE_TIMES){
            permanent_RDB.setChecked(false);
            schedule_RDB.setChecked(false);
            accessTimes_RDB.setChecked(true);
            recurrent_RDB.setChecked(false);
            mAccessTimesDetailET.setText(String.valueOf(currProperty[18]&0xFF));
            mAccessTimesDetailET.setETSelection(mAccessTimesDetailET.getText().length());

        }else if(currProperty[3]==BPprotocol.LIMIT_TYPE_PERIOD){
            permanent_RDB.setChecked(false);
            schedule_RDB.setChecked(true);
            accessTimes_RDB.setChecked(false);
            recurrent_RDB.setChecked(false);
            /*int year = ((startTime[0] << 8) & 0x0000ff00) | (startTime[1] & 0x000000ff);
            if(year<=calendar.get(Calendar.YEAR))
                year = calendar.get(Calendar.YEAR);
            calendar.set(year,startTime[2],startTime[3],startTime[4],startTime[5],startTime[6]);
            Date startDate = calendar.getTime();*/
            for(int i =0;i<startTime.length;i++)
                Util.debugMessage(TAG,"start["+i+"]="+String.format("%02x",startTime[i]),debugFlag);

            //mScheduleStartTV.setValue(dateTimeFormat(startDate));
            /*
            year = ((endTime[0] << 8) & 0x0000ff00) | (endTime[1] & 0x000000ff);
            if(year<=calendar.get(Calendar.YEAR))
                year = calendar.get(Calendar.YEAR);

            calendar.set(year,endTime[2],endTime[3],endTime[4],endTime[5],endTime[6]);
            Date endDate = calendar.getTime();
            mScheduleEndTV.setValue(dateTimeFormat(endDate));*/
            initSchedule();
        }else if(currProperty[3]==BPprotocol.LIMIT_TYPE_WEEKLY){
            permanent_RDB.setChecked(false);
            schedule_RDB.setChecked(false);
            accessTimes_RDB.setChecked(false);
            recurrent_RDB.setChecked(true);
            /*int year = ((startTime[0] << 8) & 0x0000ff00) | (startTime[1] & 0x000000ff);
            Date startTimeDate = calendar.getTime();
            mRecurrentStartTV.setValue(timeFormat(startTimeDate));
            calendar.set(year,startTime[2],startTime[3],startTime[4],startTime[5],startTime[6]);
            year = ((endTime[0] << 8) & 0x0000ff00) | (endTime[1] & 0x000000ff);
            calendar.set(year,endTime[2],endTime[3],endTime[4],endTime[5],endTime[6]);
            Date endTimeDate = calendar.getTime();
            mRecurrentEndTV.setValue(timeFormat(endTimeDate));*/
            initRecurrent();
            String limitContent = bpProtocol.Convert_Limit_Weekly(getResources(),currProperty[19]);
            mRecurrentRepeatTV.setValue(limitContent);
        }


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        currWeekly = tmpWeekly;
        String limitContent = bpProtocol.Convert_Limit_Weekly(getResources(),(byte)(currWeekly&0xFF));
        mRecurrentRepeatTV.setValue(limitContent);

    }

    private void findViews() {
        mScheduleDetail = findViewById(R.id.scheduleDetailContent);
        mScheduleStartTV = (My2TextView) mScheduleDetail.findViewById(R.id.scheduleStart);
        mScheduleEndTV = (My2TextView) mScheduleDetail.findViewById(R.id.scheduleEnd);

        mAccessTimesDetailET = (MyEditText) findViewById(R.id.accessTimesDetail);
        mAccessTimesDetailET.setRawInputType(this.getResources().getConfiguration().KEYBOARD_12KEY);
        mRecurrentDetail = findViewById(R.id.recurrentDetailContent);
        mRecurrentStartTV = (My2TextView) mRecurrentDetail.findViewById(R.id.recurrentStart);
        mRecurrentEndTV = (My2TextView) mRecurrentDetail.findViewById(R.id.recurrentEnd);
        mRecurrentRepeatTV = (My2TextView) mRecurrentDetail.findViewById(R.id.recurrentRepeat);
        permanent_RDB = ((RadioButton) findViewById(R.id.permanent));
        schedule_RDB = ((RadioButton) findViewById(R.id.schedule));
        accessTimes_RDB = ((RadioButton) findViewById(R.id.accessTimes));
        recurrent_RDB = ((RadioButton) findViewById(R.id.recurrent));
    }

    private void setListeners() {
        permanent_RDB.setOnCheckedChangeListener(mOnCheckedChangeListener);
        schedule_RDB.setOnCheckedChangeListener(mOnCheckedChangeListener);
        accessTimes_RDB.setOnCheckedChangeListener(mOnCheckedChangeListener);
        recurrent_RDB.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mScheduleStartTV.setOnClickListener(this);
        mScheduleEndTV.setOnClickListener(this);

        mRecurrentStartTV.setOnClickListener(this);
        mRecurrentEndTV.setOnClickListener(this);
        mRecurrentRepeatTV.setOnClickListener(this);

        mAccessTimesDetailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int accessTimes = 0;
                int pos = s.length();
                if(s.length()>0){
                 accessTimes = Integer.parseInt(mAccessTimesDetailET.getText());
                if(s.length()>3){
                    isAccessTimesOK = false;
                    s.delete(pos - 1, pos);
                }else if (accessTimes <= 255){

                    isAccessTimesOK = true;
                }else{
                    show_toast_msg(getString(R.string.over_range_alarm));
                    isAccessTimesOK = false;
                }
                }else
                    isAccessTimesOK = false;
            }
        });
    }

    private void initSchedule() {
        Calendar c = Calendar.getInstance();
        Util.debugMessage(TAG, "year = "+c.get(Calendar.YEAR),debugFlag);
        int year = 0;
        //int curr_year = c.get(Calendar.YEAR);
        if(!(startTime[0]== 0x00 &&startTime[1]==0x00&&startTime[3]==0x00))
        {
             year = ((startTime[0] << 8) & 0x0000ff00) | (startTime[1] & 0x000000ff);
            c.set(Calendar.YEAR,year);
            c.set(Calendar.MONTH,startTime[2]-1);
            c.set(Calendar.DAY_OF_MONTH,startTime[3]);
            c.set(Calendar.HOUR_OF_DAY, startTime[4]);
            c.set(Calendar.MINUTE, startTime[5]);
            c.set(Calendar.SECOND, startTime[6]);

        }else{

            int curr_Year = c.get(Calendar.YEAR);
            startTime[0] = (byte)(curr_Year >> 8);
            startTime[1]= (byte)(curr_Year &0xFF);
            startTime[2]= (byte)c.get(Calendar.MONTH);
            startTime[3]  = (byte)(c.get(Calendar.DAY_OF_MONTH) &0xFF);
            startTime[4]  = (byte)(c.get(Calendar.HOUR_OF_DAY) &0xFF);
            startTime[5]  = (byte)(c.get(Calendar.MINUTE) &0xFF);
            startTime[6]  = (byte)(0x00);
        }

        Date date = c.getTime();
        Util.debugMessage(TAG,"start date="+date.toString()+ "year = "+c.get(Calendar.YEAR),debugFlag);
        mScheduleStartTV.setValue(dateTimeFormat(date));
        if(!(endTime[0]== 0x00 &&endTime[1]==0x00&&endTime[3]==0x00))
        {
            year = ((endTime[0] << 8) & 0x0000ff00) | (endTime[1] & 0x000000ff);
            c.set(Calendar.YEAR,year);
            c.set(Calendar.MONTH,endTime[2]-1);
            c.set(Calendar.DAY_OF_MONTH,endTime[3]);
            c.set(Calendar.HOUR_OF_DAY, endTime[4]);
            c.set(Calendar.MINUTE, endTime[5]);
            c.set(Calendar.SECOND, endTime[6]);

        }else{

            int curr_Year = c.get(Calendar.YEAR);
            endTime[0] = (byte)(curr_Year >> 8);
            endTime[1]= (byte)(curr_Year &0xFF);
            endTime[2]= (byte)c.get(Calendar.MONTH);
            endTime[3]  = (byte)(c.get(Calendar.DAY_OF_MONTH) &0xFF);
            endTime[4]  = (byte)(c.get(Calendar.HOUR_OF_DAY) &0xFF);
            endTime[5]  = (byte)(c.get(Calendar.MINUTE) &0xFF);
            endTime[6]  = (byte)(0x00);
        }


        date = c.getTime();
        Util.debugMessage(TAG,"end date="+date.toString(),debugFlag);
        mScheduleEndTV.setValue(dateTimeFormat(date));
    }

    private void initRecurrent() {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.MINUTE, startTime[5]);
        c.set(Calendar.SECOND, startTime[6]);
        c.set(Calendar.HOUR_OF_DAY, startTime[4]);

        Date date = c.getTime();
        mRecurrentStartTV.setValue(timeFormat(date));

        c.set(Calendar.MINUTE, endTime[5]);
        c.set(Calendar.SECOND, endTime[6]);
        c.set(Calendar.HOUR_OF_DAY, endTime[4]);

        date = c.getTime();
        mRecurrentEndTV.setValue(timeFormat(date));
    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (!isChecked) return;


            switch (buttonView.getId()) {

                case R.id.permanent:
                    currProperty[3]= BPprotocol.LIMIT_TYPE_NA;
                    updateUI(null);
                    break;

                case R.id.schedule:
                    currProperty[3]= BPprotocol.LIMIT_TYPE_PERIOD;
                    updateUI(mScheduleDetail);
                    initSchedule();
                    break;

                case R.id.accessTimes:
                    currProperty[3]= BPprotocol.LIMIT_TYPE_TIMES;
                    updateUI(mAccessTimesDetailET);
                    mAccessTimesDetailET.setText(String.valueOf(currProperty[18]&0xFF));
                    mAccessTimesDetailET.setETSelection(mAccessTimesDetailET.getText().length());
                    break;

                case R.id.recurrent:
                    currProperty[3]= BPprotocol.LIMIT_TYPE_WEEKLY;

                    updateUI(mRecurrentDetail);
                    initRecurrent();
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scheduleStart:
                showDateTimePicker(getResources().getString(R.string.start_time), mScheduleStartTV,startTime,type_StartTime);
                break;

            case R.id.scheduleEnd:
                showDateTimePicker(getResources().getString(R.string.end_time), mScheduleEndTV,endTime,type_EndTime);
                break;

            case R.id.recurrentStart:
                showTimePicker(mRecurrentStartTV,startTime,type_StartTime);
                break;

            case R.id.recurrentEnd:
                showTimePicker(mRecurrentEndTV,endTime,type_EndTime);
                break;

            case R.id.recurrentRepeat:
                openRepeatPage();
                break;
        }
    }

    private void updateUI(View show) {
        UserInfoActivity.isUpdateProperty = true;

        if (mSelected == show) return;

        if (mSelected != null) {
            mSelected.setVisibility(View.GONE);
        }

        if (show != null) {
            show.setVisibility(View.VISIBLE);
        }

        mSelected = show;
    }

    private void showDateTimePicker(String title, final My2TextView view,byte dateTime[],final int type) {
        SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                title,
                getResources().getString(R.string.ok),
                getResources().getString(R.string.cancel)
        );
        dateTimeDialogFragment.startAtCalendarView();
        dateTimeDialogFragment.set24HoursMode(true);
        int year_dl = ((dateTime[0] <<8)& 0x0000ff00) | (dateTime[1]& 0x000000ff);
        //Calendar now = Calendar.getInstance();
        //Util.debugMessage(TAG,"year o= "+year_dl,debugFlag);
        //if(year_dl <= now.get(Calendar.YEAR))
        //    year_dl = now.get(Calendar.YEAR);
        Util.debugMessage(TAG,"year n= "+year_dl,debugFlag);
        int month_dl = (dateTime[2]-1);
        int day_dl = (dateTime[3]);
        int hour_dl = (dateTime[4]);
        int minute_dl = (dateTime[5]);

        dateTimeDialogFragment.setDefaultYear(year_dl);
        dateTimeDialogFragment.setDefaultMonth(month_dl);
        dateTimeDialogFragment.setDefaultDay(day_dl);
        dateTimeDialogFragment.setDefaultHourOfDay(hour_dl);
        dateTimeDialogFragment.setDefaultMinute(minute_dl);
        // dateTimeDialogFragment.setDefaultDateTime(new Date());
        Util.debugMessage(TAG,"get year = "+dateTimeDialogFragment.getYear(),debugFlag);
        try {
            dateTimeDialogFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(TAG, e.getMessage());
        }
        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {

                Calendar calendar = Calendar.getInstance();
                Calendar nowCalendar = Calendar.getInstance();
                calendar.setTime(date);

                Util.debugMessage(TAG,"calendar = "+dateTimeFormat(calendar.getTime()),debugFlag);
                if(type == type_StartTime){

                    if(nowCalendar.get(Calendar.YEAR)> calendar.get(Calendar.YEAR))
                    {
                        calendar.set(Calendar.YEAR,nowCalendar.get(Calendar.YEAR));
                        startTime[0] = (byte) (nowCalendar.get(Calendar.YEAR) >> 8);
                        startTime[1] = (byte) (nowCalendar.get(Calendar.YEAR) & 0xFF);
                    }else{

                        startTime[0] = (byte) (calendar.get(Calendar.YEAR) >> 8);
                        startTime[1] = (byte) (calendar.get(Calendar.YEAR) & 0xFF);
                    }
                    startTime[2]  = (byte)((calendar.get(Calendar.MONTH)+1)&0xFF);
                    startTime[3]  = (byte)(calendar.get(Calendar.DAY_OF_MONTH) &0xFF);
                    startTime[4]  = (byte)(calendar.get(Calendar.HOUR_OF_DAY) &0xFF);
                    startTime[5]  = (byte)(calendar.get(Calendar.MINUTE) &0xFF);
                    startTime[6]  = (byte)(0x00);

                    for(int i =0;i<endTime.length;i++)
                        Util.debugMessage(TAG,"start["+i+"]="+String.format("%02x",startTime[i]),debugFlag);

                }else if(type == type_EndTime){
                    if(nowCalendar.get(Calendar.YEAR) > calendar.get(Calendar.YEAR)){
                        calendar.set(Calendar.YEAR,nowCalendar.get(Calendar.YEAR));
                        endTime[0] = (byte)(nowCalendar.get(Calendar.YEAR) >> 8);
                    endTime[1]  = (byte)(nowCalendar.get(Calendar.YEAR) &0xFF);
                    }else{


                        endTime[0] = (byte)(calendar.get(Calendar.YEAR) >> 8);
                        endTime[1]  = (byte)(calendar.get(Calendar.YEAR) &0xFF);
                    }
                    endTime[2]  = (byte)((calendar.get(Calendar.MONTH)+1) &0xFF);
                    endTime[3]  = (byte)(calendar.get(Calendar.DAY_OF_MONTH) &0xFF);
                    endTime[4]  = (byte)(calendar.get(Calendar.HOUR_OF_DAY) &0xFF);
                    endTime[5]  = (byte)(calendar.get(Calendar.MINUTE) &0xFF);
                    endTime[6]  = (byte)(0x00);
                    for(int i =0;i<endTime.length;i++)
                        Util.debugMessage(TAG,"end["+i+"]="+String.format("%02x",endTime[i]),debugFlag);

                }

                view.setValue(dateTimeFormat(calendar.getTime()));

            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Date is get on negative button click
            }
        });

        dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
    }

    private void showTimePicker(final My2TextView textView,byte dateTime[],final int type) {
        Calendar now = Calendar.getInstance();
         now.set(Calendar.HOUR_OF_DAY,dateTime[4]);
         now.set(Calendar.MINUTE,dateTime[5]);
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.MINUTE, minute);
                        c.set(Calendar.SECOND, second);
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        if(type == type_StartTime){
                         startTime[4]  = (byte)(c.get(Calendar.HOUR_OF_DAY) &0xFF);
                         startTime[5]  = (byte)(c.get(Calendar.MINUTE) &0xFF);
                         startTime[6]  = (byte)(0x00);
                        }else if (type == type_EndTime)
                        {
                            endTime[4]  = (byte)(c.get(Calendar.HOUR_OF_DAY) &0xFF);
                            endTime[5]  = (byte)(c.get(Calendar.MINUTE) &0xFF);
                            endTime[6]  = (byte)(0x00);

                        }
                        Date date = c.getTime();
                        textView.setValue(timeFormat(date));
                    }
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.show(getSupportFragmentManager(), "Timepickerdialog");
    }

    private String pad(int value) {

        if(value < 10) {
            return "0" + value;
        }

        return "" + value;
    }

    private void openRepeatPage() {
        tmpWeekly = currWeekly;
        Intent intent = new Intent(this, RepeatActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        for(int i=0;i<startTime.length;i++)
        currProperty[i+4] = startTime[i];
        for(int i=0;i<endTime.length;i++)
            currProperty[i+11] = endTime[i];
        if(isAccessTimesOK)
            currProperty[18]=(byte)( Integer.parseInt(mAccessTimesDetailET.getText())& 0xFF);
        currProperty[19] = (byte)(currWeekly &0xFF);
        UserInfoActivity.tmpWriteUserProperty = currProperty;
        for(int i=0;i<currProperty.length;i++)
        Util.debugMessage(TAG,"currProperty["+i+"]="+String.format("%02x",currProperty[i]),debugFlag);
        overridePendingTransitionLeftToRight();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

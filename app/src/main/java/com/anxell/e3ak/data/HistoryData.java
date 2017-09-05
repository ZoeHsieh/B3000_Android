package com.anxell.e3ak.data;

import java.io.Serializable;

/**
 * Created by nsdi-monkey on 2017/6/12.
 */

public class HistoryData implements Serializable {
    private String mId;
    private String mDate;
    private String mTime;
    private String mDevice;

    public HistoryData(String id, String date, String time, String device) {
        this.mId = id;
        this.mDate = date;
        this.mTime = time;
        this.mDevice = device;
    }

    public String getId() {
        return mId;
    }

    public String getDateTime() {
        return mDate + "\n" + mTime;
    }

    public String getDevice() {
        return mDevice  ;
    }
}

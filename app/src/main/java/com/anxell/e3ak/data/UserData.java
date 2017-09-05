package com.anxell.e3ak.data;

import java.io.Serializable;

/**
 * Created by nsdi-monkey on 2017/6/12.
 */

public class UserData implements Serializable {
    private String mId;
    private String mPassword;
    private int mUserIndex;

    public UserData(String id,  String password, int userIndex) {
        this.mId = id;
        this.mPassword = password;
        this.mUserIndex = userIndex;

    }

    public String getId() {
        return mId;
    }


    public String getPasswrod() {
        return mPassword;
    }

    public int getUserIndex(){
        return mUserIndex;
    }
}

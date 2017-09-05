package com.anxell.e3ak;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.anxell.e3ak.custom.MyEditText;
import com.anxell.e3ak.custom.MyToolbar;
import com.anxell.e3ak.data.UserData;
import com.anxell.e3ak.transport.APPConfig;
import com.anxell.e3ak.transport.BPprotocol;
import com.anxell.e3ak.transport.GeneralDialog;
import com.anxell.e3ak.transport.bpActivity;
import com.anxell.e3ak.util.Util;

public class AddUserActivity extends bpActivity implements View.OnClickListener {
    private final String TAG = AddUserActivity.class.getSimpleName().toString();
    private final boolean debugFlag = true;
    private MyEditText mIdET;
    private MyEditText mPasswordET;
    private  MyToolbar toolbar;
    private boolean isADDOK = false;
    private int mFrom;
    private String device_BDADDR ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Initial(getLocalClassName());
        setContentView(R.layout.activity_add_user);

        findViews();
        setListeners();

        Bundle bundle = getIntent().getExtras();
        mFrom = bundle.getInt("from");
        device_BDADDR = bundle.getString(APPConfig.deviceBddrTag);
        toolbar.setRightEnableColor(false);
    }

    private void findViews() {
        mIdET = (MyEditText) findViewById(R.id.id);
        mPasswordET = (MyEditText) findViewById(R.id.password);
        mIdET.setTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.toString().isEmpty()) || (mPasswordET.getLength() < 4))
                    isADDOK = false;
                else
                    isADDOK = true;
                toolbar.setRightEnableColor(isADDOK);
                int bytes_len = s.toString().getBytes().length;
                int pos = s.length();

                Util.debugMessage(TAG, "String Len= " + s.length(),debugFlag);
                Util.debugMessage(TAG, "Bytes Len= " + bytes_len,debugFlag);

                if (bytes_len > BPprotocol.UserID_maxLen) {
                    s.delete(pos - 1, pos);
                }

            }
        });
        mPasswordET.setInputType(this.getResources().getConfiguration().KEYBOARD_12KEY);
        mPasswordET.setTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.toString().length() < 4) || (mIdET.toString().isEmpty()))
                    isADDOK = false;
                else
                    isADDOK = true;


                try {

                    int isNumeric = Integer.parseInt(mPasswordET.getText().toString());
                    isADDOK = true;
                }catch(NumberFormatException e){
                    isADDOK = false;
                }
                toolbar.setRightEnableColor(isADDOK);
            }
        });

    }

    private void setListeners() {
        toolbar = (MyToolbar) findViewById(R.id.toolbarView);
        toolbar.hideNavigationIcon();
        toolbar.setRightBtnClickListener(this);
        toolbar.setLeftBtnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rightTV:
                if(isADDOK){
                    if (isOkForData()) {
                        saveData();
                    }
                }
                break;

            case R.id.leftTV:
                onBackPressed();
                break;
        }
    }

    private boolean isOkForData() {
        String id = mIdET.getText();
        String password = mPasswordET.getText();
        String currAdminPWD = sharedPreferences.getString(APPConfig.ADMINPWD_Tag+device_BDADDR,"");
        boolean isDuplicated_Name = Util.checkUserDuplicateByName( id, mUserDataList);
        boolean isDuplicated_Password = Util.checkUserDuplicateByPassword(password,mUserDataList);
        boolean isAdminName1 = Util.checkUserNameAdmin(id.toUpperCase(), APPConfig.ADMIN_ID);
        boolean isAdminName2 = Util.checkUserNameAdmin(id.toUpperCase(), APPConfig.ADMIN_ENROLL);
        boolean isAdminPassword = Util.checkUserPWDAdmin(password.toString(),currAdminPWD);

       if (isAdminName1 || isAdminName2) {

           GeneralDialog.MessagePromptDialog(this, "", getResources().getString(R.string.users_manage_edit_status_Admin_name));
            return false;
       }else if(isAdminPassword) {
           GeneralDialog.MessagePromptDialog(this, "", getResources().getString(R.string.users_manage_edit_status_Admin_pwd));
            return false;
       }else if (isDuplicated_Name) {
           //nki_show_toast_msg("Name Duplication!!");
           GeneralDialog.MessagePromptDialog(this, "", getResources().getString(R.string.users_manage_edit_status_duplication_name));
            return false;
       } else if (isDuplicated_Password) {
           //nki_show_toast_msg("Password Duplication!!");
           GeneralDialog.MessagePromptDialog(this, "", getResources().getString(R.string.users_manage_edit_status_duplication_password));
            return  false;
       }
        return true;
    }

    private void saveData() {
        UsersActivity2.userInfoData = new UserData(mIdET.getText(),mPasswordET.getText(),0);
        UsersActivity2.updateStatus = UsersActivity2.up_user_Add;
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        switch (mFrom) {
            case Config.FROM_USER_1_PAGE:
                overridePendingTransitionTopToBottom();
                break;

            case Config.FROM_USER_2_PAGE:
                overridePendingTransitionLeftToRight();
                break;
        }
    }
}

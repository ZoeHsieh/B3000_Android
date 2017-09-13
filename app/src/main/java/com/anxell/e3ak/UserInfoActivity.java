package com.anxell.e3ak;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.anxell.e3ak.custom.FontEditText;
import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.custom.My2TextView;
import com.anxell.e3ak.custom.My3TextView;
import com.anxell.e3ak.custom.My4TextView;
import com.anxell.e3ak.custom.MyEditText;
import com.anxell.e3ak.custom.MySwitch;
import com.anxell.e3ak.custom.MyToolbar;
import com.anxell.e3ak.data.UserData;
import com.anxell.e3ak.transport.APPConfig;
import com.anxell.e3ak.transport.BPprotocol;
import com.anxell.e3ak.transport.GeneralDialog;
import com.anxell.e3ak.transport.bpActivity;
import com.anxell.e3ak.util.Util;

import java.nio.charset.Charset;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UserInfoActivity extends bpActivity implements View.OnClickListener {
            private final String TAG = UserInfoActivity.class.getSimpleName().toString();
            private boolean debugFlag = true;
            private My2TextView mLimitTypeTV;
            private MySwitch mKeyPadSwitch;
            private MyEditText mIDETV;
            private MyEditText mPWDETV;
            private FontTextView mScheduleTV;
            private String deviceBD_ADDR = "";
            private UserData userData;
            private byte currUserProperty[] = new byte[BPprotocol.len_UserProperty_read];
            private byte tmpID[] = new byte[BPprotocol.UserID_maxLen];
            private byte tmpPWD[] = new byte[BPprotocol.UserPD_maxLen];
            public static byte tmpWriteUserProperty[] = new byte[BPprotocol.len_UserProperty_write];
            public static boolean isUpdateProperty = false;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                Initial(getLocalClassName());
                setContentView(R.layout.activity_user_info);
                registerReceiver(mGattUpdateReceiver,  getIntentFilter());
                Bundle bundle =getIntent().getExtras();
                deviceBD_ADDR =  bundle.getString(APPConfig.deviceBddrTag);
                userData = (UserData) bundle.getSerializable("data");
                findViews();
                setListeners();
                mIDETV.setText(userData.getId());
                mPWDETV.setText(userData.getPasswrod());

                bpProtocol.getUserProperty(userData.getUserIndex());
                currentClassName = getLocalClassName();
            }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(isUpdateProperty){
            tmpWriteUserProperty[0] = (byte)(userData.getUserIndex() >> 8 );
            tmpWriteUserProperty[1] = (byte)(userData.getUserIndex() & 0xFF);

            bpProtocol.setProperty(tmpWriteUserProperty);
            isUpdateProperty = false;
        }
            currentClassName = getLocalClassName();


    }

    private void findViews() {
                mScheduleTV = (FontTextView) findViewById(R.id.schedule);
                mIDETV = (MyEditText)findViewById(R.id.id_info);
                mPWDETV = (MyEditText)findViewById(R.id.password_info);
                //mIDETV.setViewFocusable(false);
                //mPWDETV.setViewFocusable(false);
                //mIDETV.setCursorEnable(false);
                //mPWDETV.setCursorEnable(false);
                mIDETV.setInputType(InputType.TYPE_NULL);
                mIDETV.setClickable(true);
                mIDETV.setOnClickListener(this);
                mPWDETV.setInputType(InputType.TYPE_NULL);
                mKeyPadSwitch = (MySwitch)findViewById(R.id.keypad_switch);
                mKeyPadSwitch.setSwitchClickable(false);
                mLimitTypeTV = (My2TextView) findViewById(R.id.limitType);

            }

            private void setListeners() {
                MyToolbar toolbar = (MyToolbar) findViewById(R.id.toolbarView);
                toolbar.setRightBtnClickListener(this);
                mKeyPadSwitch.setOnClickListener(this);
                mPWDETV.setETVOnClickListener(this);
                mIDETV.setETVOnClickListener(this);
                mIDETV.setTextTag("IDETV");
                mPWDETV.setTextTag("PDETV");
                //LinearLayout userIDlayOut = (LinearLayout)findViewById(R.id.userID_layOut);
                //LinearLayout userPwdlayOut = (LinearLayout)findViewById(R.id.userPwd_layOut);
                //userIDlayOut.setOnClickListener(this);
                //userPwdlayOut.setOnClickListener(this);
                mLimitTypeTV.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                Util.debugMessage(TAG,"getEvent ID="+view.getId(),debugFlag);
                LinearLayout layout = (LinearLayout)findViewById(R.id.userInfoLayout);

                switch (view.getId()) {
                    case R.id.rightTV:
                        showDeleteDialog();
                        break;

                    case R.id.value:
                        if(view.getTag().equals("IDETV"))
                        Users_Edit_Dialog_Name(mUserDataList,userData, APPConfig.ADMIN_ID,APPConfig.ADMIN_ENROLL,layout);
                        else  if(view.getTag().equals("PDETV"))
                        {

                            String AdminPWD = sharedPreferences.getString(APPConfig.ADMINPWD_Tag + deviceBD_ADDR, "");
                            Users_Edit_Dialog_Password(mUserDataList, userData, layout, AdminPWD);
                        }
                        break;
                    case R.id.limitType:
                        openAccessTypesSchedulePage();
                        break;
                    case R.id.keypad_switch:

                        tmpWriteUserProperty[0] = (byte)(userData.getUserIndex() >> 8 );
                        tmpWriteUserProperty[1] = (byte)(userData.getUserIndex() & 0xFF);
                        for(int i=0;i<BPprotocol.len_UserProperty_write-2;i++)
                            tmpWriteUserProperty[i+2]= currUserProperty[i];
                        if(mKeyPadSwitch.isSwitchCheck())
                             tmpWriteUserProperty[2] = 0x00;
                        else
                            tmpWriteUserProperty[2] = 0x01;
                        bpProtocol.setProperty(tmpWriteUserProperty);
                        break;
                }
            }

            private void showDeleteDialog() {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.msg_delete);

                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        UsersActivity2.updateStatus = UsersActivity2.up_user_del;
                        UsersActivity2.userInfoData = userData;
                        onBackPressed();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, null);

                dialogBuilder.show();
            }

            private void openAccessTypesSchedulePage() {
                for(int i =0;i<tmpWriteUserProperty.length;i++)
                    tmpWriteUserProperty[i] = 0x00;
                for(int i=0;i<BPprotocol.len_UserProperty_write-2;i++)
                    tmpWriteUserProperty[i+2]= currUserProperty[i];

                for(int i=0;i<tmpWriteUserProperty.length;i++)
                    Util.debugMessage(TAG,"tmpWriteUserProperty["+i+"]="+String.format("%02x",tmpWriteUserProperty[i]),debugFlag);
                isUpdateProperty = false;
                Intent intent = new Intent(this, AccessTypesScheduleActivity.class);
                startActivity(intent);

                overridePendingTransitionRightToLeft();
            }

            @Override
            public void onBackPressed() {
                super.onBackPressed();
                UsersActivity2.userInfoData = userData;
                overridePendingTransitionLeftToRight();
            }

            @Override
            public boolean onSupportNavigateUp() {
                onBackPressed();
                return true;
            }

            @Override
            public void cmdAnalysis(byte cmd, byte cmdType, byte data[], int datalen) {
                String  message = "";
                Util.debugMessage(TAG,"test2",debugFlag);
                switch ((char) cmd) {
                    case BPprotocol.cmd_set_user_id:
                        if (data[0] == BPprotocol.result_success){
                            message = getResources().getString(R.string.program_success);
                            int nameLen = BPprotocol.UserID_maxLen;
                            for(int i=0;i<tmpID.length;i++){
                                if(tmpID[i]==(byte)BPprotocol.nullData){
                                    nameLen = i;
                                    i = tmpID.length+i;
                                }
                            }
                            byte nameArray[]= Arrays.copyOf(tmpID,nameLen);
                            String userID = new String(nameArray, Charset.forName("UTF-8"));
                            userData = new UserData(userID, userData.getPasswrod(), userData.getUserIndex());
                            int o_Position = getUserPosition(userData.getUserIndex());
                            mUserDataList.remove(o_Position);
                            mUserDataList.add(o_Position,userData);
                            mIDETV.setText(userID);

                        }else
                            message = getResources().getString(R.string.program_fail);
                       // show_toast_msg(message);
                        break;
                    case BPprotocol.cmd_set_user_pwd:
                        if (data[0] == BPprotocol.result_success){
                            message = getResources().getString(R.string.program_success);
                            String strPassword = new String(tmpPWD, Charset.forName("UTF-8"));
                            userData = new UserData(userData.getId(), strPassword, userData.getUserIndex());
                            int o_Position = getUserPosition(userData.getUserIndex());
                            mUserDataList.remove(o_Position);
                            mUserDataList.add(o_Position,userData);
                            mPWDETV.setText(strPassword);
                        }
                        else
                            message = getResources().getString(R.string.program_fail);
                        //show_toast_msg(message);
                        break;
                    case BPprotocol.cmd_user_property:

                        if (cmdType == (byte) BPprotocol.type_read) {
                            Util.debugMessage(TAG,"test1",debugFlag);
                            update_UserProperty(data);
                        }else
                        if (data[0] == BPprotocol.result_success) {
                          /*  int year =0;
                            mUsersListAdapter.notifyDataSetChanged();
                            mUsers_Edit_Curr_Item.limit.code_unlock = tmpProperty[2];
                            mUsers_Edit_Curr_Item.limit.type = tmpProperty[3];
                            year = ((tmpProperty[4] << 8) & 0x0000ff00) | (tmpProperty[5] & 0x000000ff);
                            mUsers_Edit_Curr_Item.limit.start.set(year, tmpProperty[6]-1, tmpProperty[7],tmpProperty[8], tmpProperty[9], tmpProperty[10]);
                            year = ((tmpProperty[11] << 8) & 0x0000ff00) | (tmpProperty[12] & 0x000000ff);
                            mUsers_Edit_Curr_Item.limit.end.set(year, tmpProperty[13]-1, tmpProperty[14], tmpProperty[15], tmpProperty[16], tmpProperty[17]);
                            mUsers_Edit_Curr_Item.limit.times = tmpProperty[18];
                            mUsers_Edit_Curr_Item.limit.weekly = tmpProperty[19];
                            //Update UI
                            update_UI_Page_User_Edit();*/
                            byte updateData[]= Arrays.copyOfRange(tmpWriteUserProperty,2,tmpWriteUserProperty.length);

                            update_UserProperty(updateData);
                            message = getResources().getString(R.string.program_success);
                        }else
                            message = getResources().getString(R.string.program_fail);
                        //show_toast_msg(message);
                        break;
                }

            }

            public void update_UserProperty(byte data[]){
                for(int i = 0;i<data.length;i++)
                Util.debugMessage(TAG,"data["+ i +"]="+data[i],debugFlag);
                boolean isKeypad = (data[0]!= 0);
                byte limitType = data[1];
                byte startTime[] = Arrays.copyOfRange(data,2,9);
                byte endTime[] = Arrays.copyOfRange(data,9,16);
                byte times = data[16];
                byte weekly = data[17];
                Resources res = getResources();
                Calendar calendar = Calendar.getInstance();
                String limitContent = "";
                int isfirstAdd = 0;
                for(int i=0;i<startTime.length;i++) {
                    if (startTime[i]==0x00){
                        ++isfirstAdd;
                    }

                }
                if(isfirstAdd == 7){
                    data[2] = (byte)(calendar.get(Calendar.YEAR) >> 8);
                    data[3] = (byte)(calendar.get(Calendar.YEAR)&0x00FF);
                    data[4] = (byte)(byte)(calendar.get(Calendar.MONTH)+1);
                    data[5] = (byte)(byte)(calendar.get(Calendar.DAY_OF_MONTH));
                    data[6] = (byte)(byte)(calendar.get(Calendar.HOUR_OF_DAY));
                    data[7] = (byte)(byte)(calendar.get(Calendar.MINUTE));
                    data[8] = (byte)(byte)(calendar.get(Calendar.SECOND));
                    data[9] = (byte)(calendar.get(Calendar.YEAR) >> 8);
                    data[10] = (byte)(calendar.get(Calendar.YEAR)&0x00FF);
                    data[11] = (byte)(byte)(calendar.get(Calendar.MONTH)+1);
                    data[12] = (byte)(byte)(calendar.get(Calendar.DAY_OF_MONTH));
                    data[13] = (byte)(byte)(calendar.get(Calendar.HOUR_OF_DAY));
                    data[14] = (byte)(byte)(calendar.get(Calendar.MINUTE));
                    data[15] = (byte)(byte)(calendar.get(Calendar.SECOND));

                }else{
                if(calendar.get(Calendar.YEAR) > (((data[2] << 8) & 0x0000ff00) | (data[3] & 0x000000ff)))
                {
                    data[2] = (byte)(calendar.get(Calendar.YEAR) >> 8);
                    data[3] = (byte)(calendar.get(Calendar.YEAR)&0x00FF);
                }

                if(calendar.get(Calendar.YEAR) > (((data[9] << 8) & 0x0000ff00) | (data[10] & 0x000000ff)))
                {
                    data[9] = (byte)(calendar.get(Calendar.YEAR) >> 8);
                    data[10] = (byte)(calendar.get(Calendar.YEAR)&0x00FF);
                }
                }
                mKeyPadSwitch.setSwitchCheck(isKeypad);

                if (limitType == BPprotocol.LIMIT_TYPE_NA) {
                    mLimitTypeTV.setValue(getString(R.string.permanent));
                    limitContent = "";
                }else if (limitType == BPprotocol.LIMIT_TYPE_PERIOD) {
                    mLimitTypeTV.setValue(getString(R.string.schedule));
                    int year = ((startTime[0] << 8) & 0x0000ff00) | (startTime[1] & 0x000000ff);

                    calendar.set(year,startTime[2]-1,startTime[3],startTime[4],startTime[5],startTime[6]);
                    Date date = calendar.getTime();
                    String startTimeStr = dateTimeFormat(date); //Util.Convert_Date_Time_Str(calendar);
                    Util.debugMessage(TAG,"start date="+date.toString(),debugFlag);
                    year = ((endTime[0] << 8) & 0x0000ff00) | (endTime[1] & 0x000000ff);
                    calendar.set(year,endTime[2]-1,endTime[3],endTime[4],endTime[5],endTime[6]);
                    date = calendar.getTime();
                    Util.debugMessage(TAG,"end date="+date.toString(),debugFlag);

                    String endTimeStr = dateTimeFormat(date);//Util.Convert_Date_Time_Str(calendar);
                    limitContent = startTimeStr + "~" + endTimeStr;
                }else if (limitType == BPprotocol.LIMIT_TYPE_TIMES) {
                    mLimitTypeTV.setValue(getString(R.string.access_times));
                    limitContent = res.getString(R.string.users_edit_access_control_dialog_type_times_mark)+ String.valueOf(times&0xFF);

                }else if (limitType == BPprotocol.LIMIT_TYPE_WEEKLY) {
                    mLimitTypeTV.setValue(getString(R.string.recurrent));
                    int year = ((startTime[0] << 8) & 0x0000ff00) | (startTime[1] & 0x000000ff);

                    calendar.set(year,startTime[2],startTime[3],startTime[4],startTime[5],startTime[6]);
                    String startTimeStr = Util.Convert_Limit_Time(calendar);
                    year = ((endTime[0] << 8) & 0x0000ff00) | (endTime[1] & 0x000000ff);
                    calendar.set(year,endTime[2],endTime[3],endTime[4],endTime[5],endTime[6]);
                    String endTimeStr = Util.Convert_Limit_Time(calendar);
                    limitContent = bpProtocol.Convert_Limit_Weekly(res,weekly);
                    if(weekly != BPprotocol.WEEKLY_TYPE_NA)
                    limitContent +=  "\r\n" + startTimeStr + "~" +endTimeStr;

                }
                mScheduleTV.setText(limitContent);



                currUserProperty = data;
            }

    public void Users_Edit_Dialog_Name(final List<UserData> mUsersItems, final UserData Current_User, final String ADMIN_USER_NAME1, final String ADMIN_USER_NAME2, LinearLayout layout) {
        final Activity currActivity = this;
        final View item = LayoutInflater.from(this).inflate(R.layout.users_edit_name_dialog, layout, false);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(getResources().getString(R.string.users_id_edit_dialog_title));

        dialog.setView(item);

        EditText tmp_name = (EditText) item.findViewById(R.id.editText_Users_Edit_Dialog_Name);
        tmp_name.setText(Current_User.getId());
        tmp_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                int bytes_len = s.toString().getBytes().length;
                int pos = s.length();

                Util.debugMessage(TAG, "String Len= " + s.length(),debugFlag);
                Util.debugMessage(TAG, "Bytes Len= " + bytes_len,debugFlag);

                if (bytes_len > BPprotocol.UserID_maxLen) {
                    s.delete(pos - 1, pos);
                }

            }
        });
        dialog.setPositiveButton(getResources().getString(R.string.Confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText tmp_name_2 = (EditText) item.findViewById(R.id.editText_Users_Edit_Dialog_Name);

                UserData new_User_Data = new UserData(tmp_name_2.getText().toString(),Current_User.getPasswrod(),Current_User.getUserIndex());

                boolean isAdminName1 = Util.checkUserNameAdmin(new_User_Data.getId().toUpperCase(),ADMIN_USER_NAME1);
                boolean isAdminName2 = Util.checkUserNameAdmin(new_User_Data.getId().toUpperCase(),ADMIN_USER_NAME2);

                boolean isDuplicated_Name = Util.checkUserDuplicateByName(new_User_Data.getId(),mUsersItems);
                Util.debugMessage(TAG,"user edit index="+new_User_Data.getUserIndex(),debugFlag);
                if (isAdminName1 || isAdminName2)

                    GeneralDialog.MessagePromptDialog(currActivity,"", getResources().getString(R.string.users_manage_edit_status_Admin_name));
                else if (isDuplicated_Name)

                    GeneralDialog.MessagePromptDialog(currActivity, "", getResources().getString(R.string.users_manage_edit_status_duplication_name));
                else {
                    byte id_buff[] = Util.convertStringToByteBufferForAddUser(new_User_Data.getId(),BPprotocol.UserID_maxLen);
                    Util.debugMessage(TAG,"user edit id index="+new_User_Data.getUserIndex(),debugFlag);
                    bpProtocol.setUserID(id_buff, new_User_Data.getUserIndex());
                    tmpID = id_buff;
                }
            }
        });

        dialog.setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//
            }
        });

        dialog.show();
    }

    public void Users_Edit_Dialog_Password(final List<UserData> mUsersItems, final UserData Current_User,LinearLayout layout,final String currAdminPW) {
        final View item = LayoutInflater.from(this).inflate(R.layout.users_edit_password_dialog, layout, false);
        final Activity currActivity = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.users_pwd_edit_dialog_title));

        builder.setView(item);

        final EditText password = (EditText) item.findViewById(R.id.editText_Users_Edit_Dialog_Password);
        password.setText(Current_User.getPasswrod());
        password.setRawInputType(currActivity.getResources().getConfiguration().KEYBOARD_12KEY);
        builder.setPositiveButton(getResources().getString(R.string.Confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                UserData new_User_Data = new UserData(Current_User.getId(),password.getText().toString(),Current_User.getUserIndex());

                //Check Duplicated
                boolean isDuplicated_Password = Util.checkUserDuplicateByPassword(password.getText().toString(),mUsersItems);
                boolean isAdminPassword = Util.checkUserPWDAdmin(new_User_Data.getPasswrod(),currAdminPW);
                if (isDuplicated_Password) {
                    //nki_show_toast_msg("Password Duplication!!");
                    GeneralDialog.MessagePromptDialog(currActivity,"", getResources().getString(R.string.users_manage_edit_status_duplication_password));
                }  else if(isAdminPassword)
                    GeneralDialog.MessagePromptDialog(currActivity,"", getResources().getString(R.string.users_manage_edit_status_Admin_pwd));
                else {
                    byte password_buffer[] = Util.convertStringToByteBuffer(new_User_Data.getPasswrod(),new_User_Data.getPasswrod().length());
                    Log.e("sean","user edit pwdindex="+new_User_Data.getUserIndex());
                    bpProtocol.setUserPWD(password_buffer,new_User_Data.getUserIndex());
                    tmpPWD = password_buffer;
                }
            }
        });

        builder.setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                nki_show_toast_msg("Cancel !!");
            }
        });

        final AlertDialog dialog = builder.create();

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 4)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                else
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                try {

                    int isNumeric = Integer.parseInt(password .getText().toString());
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }catch(NumberFormatException e){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });

        dialog.show();
    }


}

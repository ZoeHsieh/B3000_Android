package com.anxell.e3ak.util;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.anxell.e3ak.data.UserData;
import com.anxell.e3ak.transport.BPprotocol;
import com.anxell.e3ak.transport.UserItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sean on 3/14/2017.
 */

public class Util {
private static final boolean debugFlag = false;

    public static byte[] bytesToReverse(byte[] bytes) {
        byte[] result = new byte[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[(bytes.length - 1) - i];
        }

        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6',
                '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String stringToUuidString(String uuid) {
        String uuid_str = "";

        uuid_str += uuid.toUpperCase(Locale.ENGLISH).substring(0, 8);
        uuid_str += "-";
        uuid_str += uuid.toUpperCase(Locale.ENGLISH).substring(8, 12);
        uuid_str += "-";
        uuid_str += uuid.toUpperCase(Locale.ENGLISH).substring(12, 16);
        uuid_str += "-";
        uuid_str += uuid.toUpperCase(Locale.ENGLISH).substring(16, 20);
        uuid_str += "-";
        uuid_str += uuid.toUpperCase(Locale.ENGLISH).substring(20, 32);

        return uuid_str;
    }

    public static String UuidStringToRawString(String uuid) {
        String raw_str = "";

        raw_str = uuid.replace("-", "");

        return raw_str;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    public static byte[] convertStringToByteBufferForAddUser(String data, int len) {
        byte[] data_buf = new byte[len];

        byte[] data_tmp = data.getBytes();

        int max_len = (data_tmp.length > data_buf.length) ? data_buf.length : data_tmp.length;

        //Initial Byte Buffer
        Arrays.fill(data_buf, (byte) BPprotocol.nullData);

       if(data_tmp[data.length()-1] == 0x20)
            data_tmp[data.length()-1] =(byte) 0xFF;

        for (int i = 0; i < max_len; i++){
            data_buf[i] = data_tmp[i];
            Util.debugMessage("Util","util data="+ String.format("%02x",data_buf[i]),debugFlag);
        }

        return data_buf;
    }
    public static byte[] convertStringToByteBuffer(String data, int len) {
        byte[] data_buf = new byte[len];

        byte[] data_tmp = data.getBytes();

        int max_len = (data_tmp.length > data_buf.length) ? data_buf.length : data_tmp.length;

        //Initial Byte Buffer
        Arrays.fill(data_buf, (byte) BPprotocol.nullData);



        for (int i = 0; i < max_len; i++){
            data_buf[i] = data_tmp[i];
            Util.debugMessage("Util","util data="+ String.format("%02x",data_buf[i]),debugFlag);
        }

        return data_buf;
    }
    public static boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    public static boolean checkUserDuplicateByPassword(String password, List<UserData> mUsersItems) {
        int curr_items_size = mUsersItems.size();

        if (curr_items_size > 0) {
            //Check Duplicate
            for (int i = 0; i < curr_items_size; i++) {
                if (mUsersItems.get(i).getPasswrod().trim().toUpperCase().equals(password.trim().toUpperCase())) {

                    return true;
                }
            }
        }

        return false;
    }

    public static boolean checkUserDuplicateByName(String name, List<UserData> mUsersItems) {
        int curr_items_size = mUsersItems.size();

        if (curr_items_size > 0) {
            //Check Duplicate
            for (int i = 0; i < curr_items_size; i++) {
                if (mUsersItems.get(i).getId().trim().toUpperCase().equals(name.trim().toUpperCase())) {
                    //Log.v(TAG, "Name [" + name + "] Duplication");
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean checkUserNameAdmin(String name, String ADMIN_USER_NAME) {

        if(name.trim().toUpperCase().equals(ADMIN_USER_NAME.substring(0,ADMIN_USER_NAME.length()-1).trim().toUpperCase()))
            return true;
        if (name.trim().toUpperCase().equals(ADMIN_USER_NAME.trim().toUpperCase()))
            return true;

        return false;
    }

    public static boolean checkUserPWDAdmin(String password, String currAdminPWD) {

        if(password.equals(currAdminPWD))
            return true;

        return false;
    }
    public static String Convert_Date_Time_Str(Calendar c)
    {
        return String.format(Locale.US, "%04d-%02d-%02d %02d:%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }
    public static String Convert_Limit_Time(Calendar c)
    {
        return String.format(Locale.US, "%02d:%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }


    public static void debugMessage(String tag, String log, boolean enable){
        if(enable)
        Log.e(tag,log);
    }

    public static void debug_vMessage(String tag, String log, boolean enable){
        if(enable)
            Log.v(tag,log);
    }


    public static String padLeft(String src, int len, char ch) {
        int diff = len - src.length();
        if (diff <= 0) {
            return src;
        }

        char[] charr = new char[len];
        System.arraycopy(src.toCharArray(), 0, charr, 0, src.length());
        for (int i = src.length(); i < len; i++) {
            charr[i] = ch;
        }
        return new String(charr);
    }

    public static String padRight(String src, int len, char ch) {
        int diff = len - src.length();
        if (diff <= 0) {
            return src;
        }

        char[] charr = new char[len];
        System.arraycopy(src.toCharArray(), 0, charr, diff, src.length());
        for (int i = 0; i < diff; i++) {
            charr[i] = ch;
        }
        return new String(charr);
    }



    public static void showSoftKeyboard(View view, Context mContext) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }



    public static void closeSoftKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }


}

package com.anxell.e3ak.transport;

import android.app.Activity;

import com.anxell.e3ak.R;
import com.anxell.e3ak.util.Util;


import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Sean on 7/25/2017.
 */

public class Users_Item {
    private final static String TAG = Users_Item.class.getSimpleName();
    private static final long serialVersionUID = BPprotocol.serialVersionUID;
    private boolean debugFlag = false;

    public class info {

        public String mac;
        public String password;
        public String id;
        public int index = 0;

            public info(String mac, String password, String id) {
                this.mac = mac;
                this.password = password;
                this.id = id;
            }

            public info( String password, String id) {
                this.mac = "";
                this.password = password;
                this.id = id;
                Util.debugMessage(TAG, "password=" + password + "\r\nid=" + id + "\r\nindex=" + index,debugFlag);
            }
            public void setIndex(int index){
                this.index =  index;
            }

            public info( String mac,String password, String id,int userIndex) {
                this.mac = mac;
                this.password = password;
                this.id = id;
                this.index = userIndex;
            }

            public int getIndex(){
                return index;
            }
        }

        public static final int E1K_ADM_USERS_LIST_ARRAY_MAX = 4;
        public static final int E1K_ADM_USERS_TOTAL = 52;
        public static final int E1K_ADM_USERS_RANGE_MAX = (E1K_ADM_USERS_TOTAL + (E1K_ADM_USERS_LIST_ARRAY_MAX - 1)) / E1K_ADM_USERS_LIST_ARRAY_MAX;

        public static final byte CODE_UNLOCK_DISABLE = 0;
        public static final byte CODE_UNLOCK_ENABLE	 = 1;

        public static final byte LIMIT_TYPE_NA	  		= 0;  //Limit: Unlimited
        public static final byte LIMIT_TYPE_PERIOD		= 1;  //Limit: Start-Date and End-Date
        public static final byte LIMIT_TYPE_TIMES		= 2;  //Limit: Times
        public static final byte LIMIT_TYPE_WEEKLY   = 3;  //Limit: Weekly + Period

        public static final byte WEEKLY_TYPE_NA     = 0x0;         //Not Available
        public static final byte WEEKLY_TYPE_SUN	    = (0x1 << 0);  //Sunday
        public static final byte WEEKLY_TYPE_MON	= (0x1 << 1);  //Monday
        public static final byte WEEKLY_TYPE_TUE	    = (0x1 << 2);  //Tuesday
        public static final byte WEEKLY_TYPE_WED    = (0x1 << 3);  //Wednesday
        public static final byte WEEKLY_TYPE_THU	    = (0x1 << 4);  //Thursday
        public static final byte WEEKLY_TYPE_FRI	    = (0x1 << 5);  //Friday
        public static final byte WEEKLY_TYPE_SAT	    = (0x1 << 6);  //Saturday
        public static final byte WEEKLY_TYPE_ALL	    = (WEEKLY_TYPE_SUN | WEEKLY_TYPE_MON | WEEKLY_TYPE_TUE | WEEKLY_TYPE_WED | WEEKLY_TYPE_THU | WEEKLY_TYPE_FRI | WEEKLY_TYPE_SAT); //ALL

        public class limit {

            public byte code_unlock;
            public byte type;
            public byte times;
            public byte weekly;
            public Calendar start;
            public Calendar end;

            public limit() {
                this.code_unlock = CODE_UNLOCK_ENABLE;
                this.type = LIMIT_TYPE_NA;
                this.times = 0;
                this.weekly = WEEKLY_TYPE_NA;
                this.start = Calendar.getInstance();
                this.end = Calendar.getInstance();
            }

            public limit(byte code_unlock, byte type, byte times, byte weekly, Calendar start, Calendar end) {
                this.code_unlock = code_unlock;
                this.type = type;
                this.times = times;
                this.weekly = weekly;
                this.start = start;
                this.end = end;
            }
        }



        public info info;
        public limit limit;


        public Users_Item(String mac, String password, String id) {
            info = new info(mac, password, id);
            limit = new limit();

        }

        public Users_Item(String password, String id) {
            info = new info( password, id);
            limit = new limit();

        }



        public Users_Item(String mac, String password, String id,int userIndex, byte code_unlock, byte type, byte times, byte weekly, Calendar start, Calendar end) {
            info = new info(mac, password, id,userIndex);
            limit = new limit(code_unlock, type, times, weekly, start, end);

        }

        public void Set_Limit_All(byte code_unlock, byte type, byte times, byte weekly, Calendar start, Calendar end) {
            this.limit.code_unlock = code_unlock;
            this.limit.type = type;
            this.limit.times = times;
            this.limit.weekly = weekly;
            this.limit.start = start;
            this.limit.end = end;
        }

        public void Set_Limit_Code_Unlock(byte code_unlock) {
            this.limit.code_unlock = code_unlock;
        }

        public static String Convert_Limit_Type_To_String(Activity par, byte type) {
            String str_Type;
            switch(type)
            {
                case LIMIT_TYPE_NA:
                    //str_Type = "Unlimited";
                    str_Type = par.getString(R.string.Permanent);
                    break;
                case LIMIT_TYPE_TIMES:
                    //str_Type = "Access Limit";
                    str_Type = par.getString(R.string.Access_Times);
                    break;
                case LIMIT_TYPE_WEEKLY:
                    //str_Type = "Schedule & Time";
                    str_Type = par.getString(R.string.Recurrent);
                    break;
                case LIMIT_TYPE_PERIOD:
                    //str_Type = "Day & Time";
                    str_Type = par.getString(R.string.Schedule);
                    break;
                default:
                    str_Type = "??";
                    break;
            }

            return str_Type;
        }

        public static String Convert_Limit_Date_Time(Calendar c)
        {
            return String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d",
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
        }

        public static String Convert_Limit_Time(Calendar c)
        {
            return String.format(Locale.US, "%02d:%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
        }

        public static String Convert_Limit_Weekly(Activity par,byte weekly)
        {
            String tmpStr = "";

            if((weekly & WEEKLY_TYPE_SUN)!=0)
                tmpStr = tmpStr + par.getString(R.string.weekly_Sun);
            if((weekly & WEEKLY_TYPE_MON)!=0)
                tmpStr = tmpStr + par.getString(R.string.weekly_Mon);
            if((weekly & WEEKLY_TYPE_TUE)!=0)
                tmpStr = tmpStr + par.getString(R.string.weekly_Tue);
            if((weekly & WEEKLY_TYPE_WED)!=0)
                tmpStr = tmpStr + par.getString(R.string.weekly_Wed);
            if((weekly & WEEKLY_TYPE_THU)!=0)
                tmpStr = tmpStr + par.getString(R.string.weekly_Thu);
            if((weekly & WEEKLY_TYPE_FRI)!=0)
                tmpStr = tmpStr + par.getString(R.string.weekly_Fri);
            if((weekly & WEEKLY_TYPE_SAT)!=0)
                tmpStr = tmpStr + par.getString(R.string.weekly_Sat);

            return tmpStr;
        }



}

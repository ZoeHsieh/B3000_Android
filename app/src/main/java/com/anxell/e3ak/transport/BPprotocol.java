package com.anxell.e3ak.transport;




import android.content.res.Resources;

import com.anxell.e3ak.R;
import com.anxell.e3ak.util.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

/**
 * Created by Sean on 3/7/2017.
 */

public class BPprotocol {
    public static final boolean debugFlag = true;
    private final static String TAG = BPprotocol.class.getSimpleName();
    public static final char cmd_user_enroll = 0x01;
    public static final char cmd_user_indentify = 0x02;
    public static final char cmd_keep_open = 0x03;
    public static final char cmd_device_time = 0x04;
    public static final char cmd_device_config = 0x05;
    public static final char cmd_device_name = 0x06;
    public static final char cmd_device_bd_addr = 0x07;
    public static final char cmd_fw_version = 0x08;
    public static final char cmd_admin_enroll = 0x09;
    public static final char cmd_admin_indentify = 0x0A;
    public static final char cmd_admin_login = 0x0B;
    public static final char cmd_set_admin_pwd = 0x0C;
    public static final char cmd_user_property =0x0D;
    public static final char cmd_user_info = 0x0E;
    public static final char cmd_user_add = 0x0F;
    public static final char cmd_user_del = 0x10;
    public static final char cmd_set_user_id = 0x11;
    public static final char cmd_set_user_pwd = 0x12;
    public static final char cmd_user_data = 0x13;
    public static final char cmd_user_bd_address = 0x014;
    public static final char cmd_history_data = 0x15;
    public static final char cmd_user_counter = 0x16;
    public static final char cmd_history_counter = 0x17;
    public static final char cmd_data_lost = 0x18;
    public static final char cmd_erase_user_list =0x19;
    public static final char cmd_history_next_index = 0x1A;
    public static final char cmd_force_disconnect = 0x1B;
    public static final char result_success = 0x00;
    public static final char result_fail = 0x01;
    public static final String bp_address ="00:12:A1";
    public static final int UserID_maxLen =16;
    public static final int UserPD_maxLen = 8;
    public static final int DeviceNameMaxLen = 16;
    public static final int len_user_enroll = 30;
    public static final int len_User_Identify = 16;
    public static final int len_Admin_Identify = 14;
    public static final int len_Admin_enroll = 20;
    public static final int len_Device_Time = 7;
    public static final int len_Device_Name = 17;
    public static final int len_Admin_login = 14;
    public static final int len_set_Admin_PWD = 8;
    public static final int len_Device_Config = 5;
    public static final int len_UserProperty_read= 2;
    public static final int len_UserProperty_write = 20;
    //public static final int len_UserInfo_data = 24;
    public static final int len_UserInfo = 2;
    public static final int len_history = 2;
    public static final int len_User_Add = 26;
    public static final int len_User_Del = 2;
    public static final int len_User_id = 18;
    public static final int len_User_PWD = 10;
    public static final int len_User_data =50;
    public static final char type_write = 0x01;
    public static final char type_read = 0x00;
    public static final byte OPEN_TYPE_NA = 0x00;  //Not Available
    public static final byte OPEN_TYPE_ANDROID = 0x01;  //Android
    public static final byte OPEN_TYPE_IOS = 0x02;  //IOS
    public static final byte OPEN_TYPE_KEYPAD = 0x03;  //Keypad
    public static final byte OPEN_TYPE_KEEP_ANDROID = 0x11;  //Android - Toggle Keep
    public static final byte OPEN_TYPE_KEEP_IOS = 0x12;  //IOS - Toggle Keep
    public static final byte OPEN_TYPE_KEEP_KEYPAD = 0x13;  //Keypad - Toggle Keep
    public static final byte OPEN_TYPE_TAMPER_ALARM = 0x30;  //IR - Alarm
    public static final byte OPEN_TYPE_EXIT_BUTTON = 0x31;
    public static final char user_limit_def = 0x00;
    public static final char user_keypad_unlock_def = 0x00;
    public static final char door_statis_delayTime = 0x00;
    public static final char door_statis_KeepOpen = 0x01;
    public static final char door_statis_KeepLock = 0x02;
    public static final char user_manage_type_add = 0x00;
    public static final char user_manage_type_del = 0x01;
    public static final char user_manage_type_chg = 0x02;
    public static final int E3AK_Total_User_SIZE = 100;
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
    public static final String indexTag = "userIndex";
    private Queue<Queue_Item> queue = new LinkedList<>();
    public static char nullData =0xFF;
    public static final long serialVersionUID = 4598738130123921552L;


    private byte[] getCmdWrite(char cmdType, byte data[],int datalen) {

        byte cmd[] = new byte[datalen + 4];

        cmd[0] = (byte)cmdType;
        cmd[1] = (byte)type_write;
        cmd[2] = (byte) (datalen >> 8);
        cmd[3] = (byte) (datalen & 0xFF);
        for (int i = 0; i < datalen; i++)
            cmd[4 + i] = data[i];

        return cmd;
    }

    private byte[] getCmdRead(char cmdType) {
        byte cmd[] = new byte[4];

        cmd[0] = (byte)cmdType;
        cmd[1] = (byte)type_read;
        cmd[2] = (byte)0x00;
        cmd[3] = (byte)0x00;

        return cmd;
    }

    private byte[] getCmdRead(char cmdType, byte data[],int datalen ) {
        byte cmd[] = new byte[datalen+4];

        cmd[0] = (byte)cmdType;
        cmd[1] = (byte)type_read;
        cmd[2] = (byte) (datalen >> 8);
        cmd[3] = (byte) (datalen & 0xFF);
        for (int i = 0; i < datalen; i++)
            cmd[4 + i] = data[i];

        return cmd;
    }

    private void queueAdd( byte[] data) {
        Queue_Item item = new Queue_Item( data);

        queue.offer(item);


    }


    private void queueAdd(Queue_Item item) {
        queue.offer(item);
    }
    public int getQueueSize(){
        return queue.size();
    }
    public void queueClear() {

        queue.clear();

    }
    public void reInitQueue(){
        queue = null;
        queue = new LinkedList<>();
    }
    public Queue<Queue_Item> getQueue(){

        return queue;
    }
    public void setDeviceName(byte[] code, int len) {

        ByteBuffer b = ByteBuffer.allocate(BPprotocol.len_Device_Name);

        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Len
        b.put((byte) len);
        //Code
        for (int i = 0; i < len_Device_Name-1; i++)
            b.put(code[i]);


        byte cmd[] =getCmdWrite(cmd_device_name,b.array(), len_Device_Name);

        queueAdd(cmd);
    }
    public void getAdminDeviceName() {
        //mE1K_DB_Data.data_Need_Received.add(NKI_E1K_DB_DATA.E1K_SETTINGS_DEVICE_NAME);
        byte cmd[] = getCmdRead(cmd_device_name);
        queueAdd(cmd);
    }
    public void getFWVersion() {
        //mE1K_DB_Data.data_Need_Received.add(NKI_E1K_DB_DATA.E1K_SETTINGS_FW_VERSION);
        byte cmd [] = getCmdRead(cmd_fw_version);
        queueAdd(cmd);
    }
    public void setDateTime(int year, int month, int day, int hours, int minutes, int seconds) {
        String dbg_String;
        ByteBuffer b = ByteBuffer.allocate(7);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Fill Data

        b.put((byte)(year >> 8));
        b.put((byte)(year & 0xFF));
        b.put((byte) (month+1));
        b.put((byte) day);
        b.put((byte) hours);
        b.put((byte) minutes);
        b.put((byte) seconds);


        dbg_String = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hours, minutes, seconds);

        Util.debugMessage(TAG, "Sync DateDime = " + dbg_String,debugFlag);

        byte cmd[] = getCmdWrite(cmd_device_time,b.array(), len_Device_Time);
        queueAdd(cmd);

    }
    public void setDateTime(byte dateTime[]) {
        String dbg_String;
        ByteBuffer b = ByteBuffer.allocate(7);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Fill Data
        for(byte data:dateTime) {
            Util.debugMessage(TAG, "i="+ String.format("%02x",data),debugFlag);
            b.put(data);
        }

        byte cmd[] = getCmdWrite(cmd_device_time,b.array(), len_Device_Time);
        queueAdd(cmd);

    }

    public void getDateTime() {
        //mE1K_DB_Data.data_Need_Received.add(NKI_E1K_DB_DATA.E1K_SETTINGS_FW_VERSION);
        byte cmd [] = getCmdRead(cmd_device_time);
        queueAdd(cmd);
    }
    public void setEraseUserList(){
        byte data[] ={0};
        byte cmd[] = getCmdWrite(cmd_erase_user_list,data, 0);
        queueAdd(cmd);

    }
    public void setConfig(boolean door_sensor_opt, byte lock_type, int delay_secs, boolean ir_sensor_opt) {
        String dbg_String;
        ByteBuffer b = ByteBuffer.allocate(BPprotocol.len_Device_Config);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Fill Data
        if (door_sensor_opt)
            b.put((byte) 0x01);
        else
            b.put((byte) 0x00);

        b.put(lock_type);

        b.put((byte)(delay_secs>>8));
        b.put((byte)(delay_secs & 0xFF));

        if (ir_sensor_opt)
            b.put((byte) 0x01);
        else
            b.put((byte) 0x00);


        dbg_String = String.format(Locale.US, "Sensor = %d, Lock Type = %d, Delay = %d s", b.get(0), lock_type, delay_secs);

        Util.debugMessage(TAG, "Set Config = " + dbg_String,debugFlag);

        byte cmd[] = getCmdWrite(cmd_device_config,b.array(), len_Device_Config);
        queueAdd(cmd);

    }
    public void setConfig(byte config[]) {
        String dbg_String;
        ByteBuffer b = ByteBuffer.allocate(BPprotocol.len_Device_Config);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        for(byte data:config) {
            Util.debugMessage(TAG, "i="+ String.format("%02x",data),debugFlag);
            b.put(data);
        }

        byte cmd[] = getCmdWrite(cmd_device_config,b.array(), len_Device_Config);
        queueAdd(cmd);

    }
    public void getConfig() {

        byte cmd [] = getCmdRead(cmd_device_config);

        queueAdd(cmd);
    }

    public void getAdminPWD(){


        byte cmd[]= getCmdRead(cmd_set_admin_pwd);
        queueAdd(cmd);

    }

    public void executeForceDisconnect(){

        byte cmd[]= getCmdRead(cmd_force_disconnect);
        queueAdd(cmd);
    }

    public void setAdminPWD(byte password[]){
        Util.debugMessage(TAG,"admin password len= " +password.length,debugFlag);

        ByteBuffer b = ByteBuffer.allocate(BPprotocol.len_set_Admin_PWD);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        for(byte data:password) {
            Util.debugMessage(TAG, "i="+ String.format("%02x",data),debugFlag);
            b.put(data);
        }
        Util.debugMessage(TAG, "admin password len2= " +password.length,debugFlag);
        if(debugFlag) {
            for (byte data : b.array())
                Util.debugMessage(TAG, "admin password = " + String.format("%02x", data), debugFlag);
        }

        byte cmd[]= getCmdWrite(cmd_set_admin_pwd,b.array(), len_set_Admin_PWD);
        queueAdd(cmd);

    }

    public void adminLogin(byte mac[]){

        String dbg_String;
        ByteBuffer b = ByteBuffer.allocate(len_Admin_Identify);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        byte open_type = OPEN_TYPE_KEEP_ANDROID;

        int curr_year, curr_month, curr_day;
        int curr_hours, curr_minutes, curr_seconds;

        Calendar c = Calendar.getInstance();

        curr_year = c.get(Calendar.YEAR);
        curr_month = c.get(Calendar.MONTH) + 1;
        curr_day = c.get(Calendar.DAY_OF_MONTH);
        curr_hours = c.get(Calendar.HOUR_OF_DAY);
        curr_minutes = c.get(Calendar.MINUTE);
        curr_seconds = c.get(Calendar.SECOND);

        //Fill Data
        b.putShort((short) curr_year);
        b.put((byte) curr_month);
        b.put((byte) curr_day);
        b.put((byte) curr_hours);
        b.put((byte) curr_minutes);
        b.put((byte) curr_seconds);

        b.put(mac[0]);
        b.put(mac[1]);
        b.put(mac[2]);
        b.put(mac[3]);
        b.put(mac[4]);
        b.put(mac[5]);

        b.put(open_type);


        dbg_String = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d, mac = [%02X:%02X:%02X:%02X:%02X:%02X], TYPE = %d",
                curr_year, curr_month, curr_day, curr_hours, curr_minutes, curr_seconds,
                mac[0], mac[1], mac[2], mac[3], mac[4], mac[5], open_type);

        Util.debugMessage(TAG, "Admin login = " + dbg_String,debugFlag);


        byte cmd[]= getCmdRead(BPprotocol.cmd_admin_login,b.array(),len_Admin_login);
        queueAdd(cmd);


    }
    public void setEnrollAdmin(byte[] mac, byte[] password, byte[] name) {
        String dbg_String;
        ByteBuffer b = ByteBuffer.allocate(len_Admin_enroll);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Fill Data
        b.put(mac[0]);
        b.put(mac[1]);
        b.put(mac[2]);
        b.put(mac[3]);
        b.put(mac[4]);
        b.put(mac[5]);

        b.put(password[0]);
        b.put(password[1]);
        b.put(password[2]);
        b.put(password[3]);
        b.put(password[4]);
        b.put(password[5]);
        b.put(password[6]);
        b.put(password[7]);

        b.put(name[0]);
        b.put(name[1]);
        b.put(name[2]);
        b.put(name[3]);
        b.put(name[4]);
        b.put(name[5]);


        dbg_String = String.format(Locale.US, "mac = [%x:%x:%x:%x:%x:%x], password: [%x%x%x%x%x%x%x%x], name = [%x%x%x%x%x%x]",
                mac[0], mac[1], mac[2], mac[3], mac[4], mac[5],
                password[0], password[1], password[2], password[3], password[4], password[5],
                password[6],password[7],name[0], name[1], name[2], name[3], name[4], name[5]);

        Util.debugMessage(TAG, "Admin Enroll = " + dbg_String,debugFlag);


        byte cmd [] = getCmdWrite(cmd_admin_enroll,b.array(), len_Admin_enroll);
        queueAdd(cmd);

    }
    public void setEnrollUser(byte[] mac, byte[] password, byte[] name) {
        String dbg_String;
        ByteBuffer b = ByteBuffer.allocate(len_user_enroll);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Fill Data
        for(byte data:mac)
            b.put(data);

        for(byte data:password)
            b.put(data);

        for(byte data:name)
            b.put(data);

        dbg_String = String.format(Locale.US, "mac = [%x:%x:%x:%x:%x:%x], password: [%02x%02x%02x%02x%02x%02x%02x%02x], name = [%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x]",
                mac[0], mac[1], mac[2], mac[3], mac[4], mac[5],
                password[0], password[1], password[2], password[3], password[4], password[5],password[6], password[7],
                name[0], name[1], name[2], name[3], name[4], name[5],name[6], name[7], name[8], name[9], name[10], name[11]
                ,name[12], name[13], name[14], name[15]);

        Util.debugMessage(TAG, "Enroll = " + dbg_String,debugFlag);

        byte cmd[]= getCmdWrite(cmd_user_enroll,b.array(), len_user_enroll);
        queueAdd(cmd);

    }
    public void setUserAdd( byte[] password, byte[] name) {

        ByteBuffer b = ByteBuffer.allocate(BPprotocol.len_User_Add);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Fill Data
        for(byte data:password)
            b.put(data);

        for(byte data:name)
            b.put(data);

        b.put((byte) user_keypad_unlock_def);

        b.put((byte) user_limit_def);
        if(debugFlag) {
            for (byte data : b.array())
                Util.debugMessage(TAG, "Users add = " + String.format("%02x", data), debugFlag);
        }
        byte cmd[] = getCmdWrite(cmd_user_add, b.array(),len_User_Add);

        queueAdd(cmd);

    }
    public void setUserDel(int index){
        ByteBuffer b = ByteBuffer.allocate(BPprotocol.len_User_Del);
        b.put((byte)(index >> 8));
        b.put((byte)(index &0xFF));
        byte cmd[] = getCmdWrite(cmd_user_del, b.array(), len_User_Del);

        queueAdd(cmd);

    }
    public void setUserID(byte[] ID,int index){

        ByteBuffer b = ByteBuffer.allocate(len_User_id);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        b.put((byte)(index >> 8));
        b.put((byte)(index &0xFF));
        //Fill Data
        for(byte data:ID)
            b.put(data);
        if(debugFlag) {
            for (byte data : b.array())
                Util.debugMessage(TAG, "Users property = " + String.format("%02x", data), debugFlag);
        }
        byte cmd[] = getCmdWrite(cmd_set_user_id, b.array(), len_User_id);

        queueAdd(cmd);


    }
    public void setUserPWD(byte[] password,int index){

        ByteBuffer b = ByteBuffer.allocate(len_User_PWD);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        b.put((byte)(index >> 8));
        b.put((byte)(index &0xFF));
        //Fill Data
        for(byte data:password)
            b.put(data);
        if(debugFlag) {
            for (byte data : b.array())
                Util.debugMessage(TAG, "Users property = " + String.format("%02x", data),debugFlag);
        }
        byte cmd[] = getCmdWrite(cmd_set_user_pwd, b.array(), len_User_PWD);

        queueAdd(cmd);


    }
    public void setProperty(byte userPropertyData[]){

        ByteBuffer b = ByteBuffer.allocate(len_UserProperty_write);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Fill Data
        for(byte data:userPropertyData) {
            Util.debugMessage(TAG, "i="+ String.format("%02x",data),debugFlag);
            b.put(data);
        }

        byte cmd[] = getCmdWrite(cmd_user_property,b.array(), len_UserProperty_write);
        queueAdd(cmd);

    }
    public void setProperty(int userIndex, byte keypad_unlock, byte limit_type, Calendar start, Calendar end, byte times, byte weekly){

        ByteBuffer b = ByteBuffer.allocate(len_UserProperty_write);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        b.put((byte)(userIndex>>8));
        b.put((byte)(userIndex&0xFF));
        b.put(keypad_unlock);
        b.put(limit_type);

        b.put((byte) (start.get(Calendar.YEAR) >>8));
        b.put((byte) (start.get(Calendar.YEAR) &0xFF));
        b.put((byte) (start.get(Calendar.MONTH) + 1));
        b.put((byte) start.get(Calendar.DAY_OF_MONTH));
        b.put((byte) start.get(Calendar.HOUR_OF_DAY));
        b.put((byte) start.get(Calendar.MINUTE));
        b.put((byte) start.get(Calendar.SECOND));
        b.put((byte) (end.get(Calendar.YEAR) >>8));
        b.put((byte) (end.get(Calendar.YEAR) &0xFF));
        b.put((byte) (end.get(Calendar.MONTH) + 1));
        b.put((byte) end.get(Calendar.DAY_OF_MONTH));
        b.put((byte) end.get(Calendar.HOUR_OF_DAY));
        b.put((byte) end.get(Calendar.MINUTE));
        b.put((byte) end.get(Calendar.SECOND));
        b.put(times);
        b.put(weekly);

        Util.debugMessage(TAG,"times="+times,debugFlag);

        byte cmd[] = getCmdWrite(cmd_user_property,b.array(), len_UserProperty_write);
        queueAdd(cmd);
    }
    public void getUserProperty(int index){
        ByteBuffer b = ByteBuffer.allocate(len_UserProperty_read);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        b.put((byte)(index >> 8));
        b.put((byte)(index &0xFF));
        byte cmd[] = getCmdRead(cmd_user_property,b.array(), len_UserProperty_read);
        queueAdd(cmd);

    }
    public void getUsersCount(){

        byte cmd[] =getCmdRead(cmd_user_counter);
        queueAdd(cmd);
    }
    public void getHistoryCounter(){
        byte cmd[] = getCmdRead(cmd_history_counter);
        queueAdd(cmd);

    }

    public void getHistoryNextIndex(){
        byte cmd[] = getCmdRead(cmd_history_next_index);
        queueAdd(cmd);

    }

    public void getUserData(int userCount){
        byte cmdData[] ={ (byte)(userCount >>8) , (byte)(userCount & 0xFF)};

        byte cmd[] = getCmdRead(cmd_user_data,cmdData, cmdData.length);
        queueAdd(cmd);
    }
    public void getHistoryAll() {
        byte cmd[] = getCmdRead(cmd_history_data);
        queueAdd( cmd);
    }
    public void getUserByRange(int range) {
        Util.debugMessage(TAG,"range="+range,debugFlag);
        ByteBuffer b = ByteBuffer.allocate(BPprotocol.len_UserInfo);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Fill Data
        b.put((byte)(range >> 8 ));
        b.put((byte)(range & 0xFF));

        byte cmd [] = getCmdRead(cmd_user_info,b.array(),len_UserInfo);
        queueAdd(cmd);



    }
    public void getHistoryByRange(int range) {

        ByteBuffer b = ByteBuffer.allocate(BPprotocol.len_history);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.

        //Fill Data
        b.put((byte)(range >> 8 ));
        b.put((byte)(range & 0xFF));

        byte cmd [] = getCmdRead(cmd_history_data,b.array(),len_history);
        queueAdd(cmd);



    }
    public void setUserDatalost(int count){
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte)(count >> 8));
        buffer.put((byte)(count &0xFF));
        byte cmd []= getCmdWrite(cmd_data_lost,buffer.array(),2);
        queueAdd(cmd);

    }
    public void setAdminIdentify(byte[] mac) {
        String dbg_String;
        ByteBuffer b = ByteBuffer.allocate(len_Admin_Identify);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        byte open_type = OPEN_TYPE_ANDROID;

        int curr_year, curr_month, curr_day;
        int curr_hours, curr_minutes, curr_seconds;

        Calendar c = Calendar.getInstance();

        curr_year = c.get(Calendar.YEAR);
        curr_month = c.get(Calendar.MONTH) + 1;
        curr_day = c.get(Calendar.DAY_OF_MONTH);
        curr_hours = c.get(Calendar.HOUR_OF_DAY);
        curr_minutes = c.get(Calendar.MINUTE);
        curr_seconds = c.get(Calendar.SECOND);

        //Fill Data
        b.put((byte)(curr_year >> 8));
        b.put((byte)(curr_year & 0xFF));
        b.put((byte) curr_month);
        b.put((byte) curr_day);
        b.put((byte) curr_hours);
        b.put((byte) curr_minutes);
        b.put((byte) curr_seconds);

        b.put(mac[0]);
        b.put(mac[1]);
        b.put(mac[2]);
        b.put(mac[3]);
        b.put(mac[4]);
        b.put(mac[5]);

        b.put(open_type);


        dbg_String = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d, mac = [%02X:%02X:%02X:%02X:%02X:%02X], TYPE = %d",
                curr_year, curr_month, curr_day, curr_hours, curr_minutes, curr_seconds,
                mac[0], mac[1], mac[2], mac[3], mac[4], mac[5], open_type);

        Util.debugMessage(TAG, "Admin Identify = " + dbg_String,debugFlag);


        byte cmd[]= getCmdRead(cmd_admin_indentify,b.array(), len_Admin_Identify);
        queueAdd(cmd);

    }
    public void setUserIdentifyDirect(byte[] mac, boolean isKeepOpen,int userIndex) {
        String dbg_String;
        char cmdtype = 0x00;
        ByteBuffer b = ByteBuffer.allocate(len_User_Identify);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        byte open_type = (isKeepOpen) ? OPEN_TYPE_KEEP_ANDROID : OPEN_TYPE_ANDROID; //NA = 0x00, ANDROID = 0x01, IOS = 0x02


        int curr_year, curr_month, curr_day;
        int curr_hours, curr_minutes, curr_seconds;

        Calendar c = Calendar.getInstance();

        curr_year = c.get(Calendar.YEAR);
        curr_month = c.get(Calendar.MONTH) + 1;
        curr_day = c.get(Calendar.DAY_OF_MONTH);
        curr_hours = c.get(Calendar.HOUR_OF_DAY);
        curr_minutes = c.get(Calendar.MINUTE);
        curr_seconds = c.get(Calendar.SECOND);

        //Fill Data
        b.put((byte)(curr_year >> 8));
        b.put((byte)(curr_year & 0xFF));
        b.put((byte) curr_month);
        b.put((byte) curr_day);
        b.put((byte) curr_hours);
        b.put((byte) curr_minutes);
        b.put((byte) curr_seconds);

        b.put(mac[0]);
        b.put(mac[1]);
        b.put(mac[2]);
        b.put(mac[3]);
        b.put(mac[4]);
        b.put(mac[5]);

        b.put(open_type);
        b.put((byte)(userIndex >> 8));
        b.put((byte)(userIndex & 0xFF));
        if (!isKeepOpen)
            cmdtype = cmd_user_indentify;
        else
            cmdtype = cmd_keep_open;
        byte cmd[] =getCmdRead(cmdtype,b.array(), len_User_Identify);



        queueAdd( cmd);



    }
    public void setUserIdentifyOneTimeRAW(byte[] mac, boolean isKeepOpen,int userIndex) {
        String dbg_String;
        ByteBuffer b = ByteBuffer.allocate(len_User_Identify);
        b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        byte open_type = (isKeepOpen) ? OPEN_TYPE_KEEP_ANDROID : OPEN_TYPE_ANDROID; //NA = 0x00, ANDROID = 0x01, IOS = 0x02

        int curr_year, curr_month, curr_day;
        int curr_hours, curr_minutes, curr_seconds;

        Calendar c = Calendar.getInstance();

        curr_year = c.get(Calendar.YEAR);
        curr_month = c.get(Calendar.MONTH) + 1;
        curr_day = c.get(Calendar.DAY_OF_MONTH);
        curr_hours = c.get(Calendar.HOUR_OF_DAY);
        curr_minutes = c.get(Calendar.MINUTE);
        curr_seconds = c.get(Calendar.SECOND);

        //Fill Data
        b.put((byte)(curr_year >> 8));
        b.put((byte)(curr_year & 0xFF));
        b.put((byte) curr_month);
        b.put((byte) curr_day);
        b.put((byte) curr_hours);
        b.put((byte) curr_minutes);
        b.put((byte) curr_seconds);

        b.put(mac[0]);
        b.put(mac[1]);
        b.put(mac[2]);
        b.put(mac[3]);
        b.put(mac[4]);
        b.put(mac[5]);

        b.put(open_type);


        dbg_String = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d, mac = [%02X:%02X:%02X:%02X:%02X:%02X], OPEN = %d",
                curr_year, curr_month, curr_day, curr_hours, curr_minutes, curr_seconds,
                mac[0], mac[1], mac[2], mac[3], mac[4], mac[5], open_type);

        Util.debugMessage(TAG, "Identify = " + dbg_String,debugFlag);
        byte cmd[] = getCmdRead(cmd_user_indentify,b.array(),len_User_Identify);
        //Then Disconnect
        queueAdd(cmd);
    }
    public void restoreUsersData(Users_Item item,int count){

        byte[] mac_buf = Util.hexStringToByteArray(item.info.mac.replaceAll(":", ""));


        byte[] password_buf = Util.convertStringToByteBuffer(item.info.password, BPprotocol.UserPD_maxLen);

        byte[] name_buf = Util.convertStringToByteBuffer(item.info.id, BPprotocol.UserID_maxLen);

        ByteBuffer buffer = ByteBuffer.allocate(BPprotocol.len_User_data);
       /* buffer.put((byte)(count>>8));
        buffer.put((byte)(count & 0xFF));*/

        buffer.put((byte)(item.info.index >>8));
        buffer.put((byte)(item.info.index &0xFF));

        for(byte data:mac_buf)
            buffer.put(data);

        for(byte data:password_buf)
            buffer.put(data);
        for(byte data:name_buf)
            buffer.put(data);

        buffer.put(item.limit.code_unlock);
        buffer.put(item.limit.type);
        Calendar start = item.limit.start;
        Calendar end = item.limit.end;
        buffer.put((byte)(start.get(Calendar.YEAR)>> 8));
        buffer.put((byte)(start.get(Calendar.YEAR)&0xFF));

        buffer.put((byte) (start.get(Calendar.MONTH)));

        buffer.put((byte) start.get(Calendar.DAY_OF_MONTH));

        buffer.put((byte) start.get(Calendar.HOUR_OF_DAY));

        buffer.put((byte) start.get(Calendar.MINUTE));

        buffer.put((byte) start.get(Calendar.SECOND));

        buffer.put((byte)(end.get(Calendar.YEAR)>> 8));
        buffer.put((byte)(end.get(Calendar.YEAR)&0xFF));

        buffer.put((byte) (end.get(Calendar.MONTH)));

        buffer.put((byte) end.get(Calendar.DAY_OF_MONTH));

        buffer.put((byte) end.get(Calendar.HOUR_OF_DAY));

        buffer.put((byte) end.get(Calendar.MINUTE));

        buffer.put((byte) end.get(Calendar.SECOND));

        buffer.put(item.limit.times);

        buffer.put(item.limit.weekly);

        byte cmd[] = getCmdWrite(cmd_user_data, buffer.array(), len_User_data);


        queueAdd(cmd);

    }


    public void restoreUsersData(byte cmdData[]){

        ByteBuffer buffer = ByteBuffer.allocate(BPprotocol.len_User_data);
        /*for(int i=0;i<2;i++)
            buffer.put(cmdData[i]);
        for(int i=0;i<6;i++)
            buffer.put(cmdData[2+i]);
        for(int i=0;i<BPprotocol.UserPD_maxLen;i++)
            buffer.put(cmdData[8+i]);

        for(int i=0;i<BPprotocol.UserID_maxLen;i++)
            buffer.put(cmdData[16+i]);*/


        for(int i=0;i<cmdData.length;i++)
            buffer.put(cmdData[i]);
        byte cmd[] = getCmdWrite(cmd_user_data, buffer.array(), len_User_data);

        queueAdd(cmd);

    }
    public  String Convert_Limit_Weekly(Resources res,byte weekly)
    {
        String tmpStr = "";

        if((weekly & WEEKLY_TYPE_SUN)!=0)
            tmpStr = tmpStr + res.getString(R.string.weekly_Sun);
        if((weekly & WEEKLY_TYPE_MON)!=0)
            tmpStr = tmpStr + res.getString(R.string.weekly_Mon);
        if((weekly & WEEKLY_TYPE_TUE)!=0)
            tmpStr = tmpStr + res.getString(R.string.weekly_Tue);
        if((weekly & WEEKLY_TYPE_WED)!=0)
            tmpStr = tmpStr + res.getString(R.string.weekly_Wed);
        if((weekly & WEEKLY_TYPE_THU)!=0)
            tmpStr = tmpStr + res.getString(R.string.weekly_Thu);
        if((weekly & WEEKLY_TYPE_FRI)!=0)
            tmpStr = tmpStr + res.getString(R.string.weekly_Fri);
        if((weekly & WEEKLY_TYPE_SAT)!=0)
            tmpStr = tmpStr + res.getString(R.string.weekly_Sat);
        if (weekly == WEEKLY_TYPE_ALL)
            tmpStr = res.getString(R.string.every_week);
        return tmpStr;
    }

}

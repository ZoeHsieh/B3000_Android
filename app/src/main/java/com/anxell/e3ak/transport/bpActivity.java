package com.anxell.e3ak.transport;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
//import android.support.v7.app.AppCompatActivity;
import android.content.ServiceConnection;
import android.widget.Toast;

import com.anxell.e3ak.BaseActivity;
import com.anxell.e3ak.data.UserData;
import com.anxell.e3ak.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import static com.anxell.e3ak.transport.APPConfig.E3K_DEVICES_BLE_RSSI_LEVEL_DEFAULT;


public class bpActivity extends BaseActivity {
    private static RBLService bleService;
    private Activity app = null;
    public static String currentClassName = "";
    public String localClassName = "";
    private String tmpBuff="";
    public static bpEncode encode = new bpEncode();
    private BluetoothGattCharacteristic E3AK_ble_ch = null;
    private String TAG = bpActivity.class.getSimpleName().toString();
    private Boolean debugFlag = true;
    private final int btPreDelay =500;
    public static  BPprotocol bpProtocol = new BPprotocol();
    public static SharedPreferences sharedPreferences =null;
    private boolean isMessageProcessing = false;
    private int MessageProcess_timeout_count = 0;
    private static final int MESSAGE_PROCESS_TIMEOUT_LIMIT = 100;
    private boolean isBLE_Service_Ready = false;
    public byte mSYS_BLE_MAC_Address_RAW[] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public String mSYS_BLE_MAC_Address;
    public static List<UserData> mUserDataList = new ArrayList<>();


    public void Initial(String localClassName) {

        this.localClassName = localClassName;
        sharedPreferences = getSharedPreferences("data" , MODE_PRIVATE);

    }

    public int getUserPosition(int userIndex){
        for(int i=0;i<mUserDataList.size();i++){
            if(mUserDataList.get(i).getUserIndex() == userIndex){
                return i;
            }
        }

        return 0;
    }

    public void update_system_ble_mac_addrss() {
        //Get System BLE MAC Address
        mSYS_BLE_MAC_Address = Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");

        //ble_mac_addr_Value.setText(mSYS_BLE_MAC_Address);

        mSYS_BLE_MAC_Address_RAW = Util.hexStringToByteArray(mSYS_BLE_MAC_Address.replaceAll(":", ""));

        Util.debugMessage(TAG, "update_system_ble_mac_addrss(): " + mSYS_BLE_MAC_Address,debugFlag);

        //nki_show_toast_msg(mSYS_BLE_MAC_Address);
    }
    protected void onStop() {
        /*try{
            unregisterReceiver(this.mGattUpdateReceiver);
        }catch( java.lang.IllegalArgumentException e)
        {

        }*/
        super.onStop();
    }

  
	@Override
	protected void onRestart() {
		
		//app.registerReceiver(this.mGattUpdateReceiver, filter);
		super.onRestart();
	}

	protected void onDestroy() {
        //unbindService(this.ServiceConnection);

        super.onDestroy();
    }

    public void scanUpdate(BluetoothDevice device,int rssi,byte scanRecord []) {}
    public void scanStopUpdate() {}
    public void connectUpdate() {}
    public void cmdDataCacheEvent(byte cmd, byte cmdType,byte data[], int datalen){}
    public void update_RSSI(String rssi){}
    public void conTimeOutUpdate() {}
    public void disconnectUpdate() {
        isBLE_Service_Ready = false;
    }
    public void discoverUpdate() {}
    public void getDataUpdate(String rawData) {}
    public void writeSuccess()  {}
    public void getERROREvent(String bdAddress) {}
    public void writeError()  {}
    public void BLEReady(){
        isBLE_Service_Ready = true;
    }
    public void readRSSI(){

         bleService.readRssi();
    }
    public void cmdAnalysis(byte cmd, byte cmdType,byte data[], int datalen){}
    private void protocolCheck(byte rawData[]){

        byte cmd[] =rawData; //encode.SLIP_Decode(rawData,rawData.length);

        if(cmd.length>3) {
            Util.debugMessage(TAG,"cmdPacket=" + encode.BytetoHexString(cmd),debugFlag);
            int datalen = cmd[2] << 8 | cmd[3];
            Util.debugMessage(TAG, "datalen=" + datalen + "length=" + cmd.length,debugFlag);

            String message = "";
            byte data[] = Arrays.copyOfRange(cmd, 4, cmd.length);
             if (datalen == cmd.length - 4) {
                Util.debugMessage(TAG,"current="+currentClassName + "local="+localClassName,debugFlag);
                if(currentClassName.equals(localClassName))
                    cmdAnalysis(cmd[0], cmd[1], data, datalen);
                }
            }else
                Util.debugMessage(TAG,"cmd fail",debugFlag);


}


    public IntentFilter getIntentFilter() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(RBLService.ACTION_GATT_RSSI);
        intentFilter.addAction(RBLService.ACTION_GATT_WRITE_SUCCESS);
        intentFilter.addAction(RBLService.ACTION_GATT_ERROR_133);
        return intentFilter;
    }


    protected final ServiceConnection ServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {

            bpActivity.this.bleService = ((RBLService.LocalBinder) service)
                    .getService();
            bpActivity.this.bleService.initialize();
            Util.debugMessage(TAG,"onServiceConnected",debugFlag);
            update_service_connect();
        }

        public void onServiceDisconnected(ComponentName componentName) {

            bpActivity.this.bleService = null;
        }
    };
    public void update_service_connect()
    {
        Util.debugMessage(TAG,"update_sevice",debugFlag);
    }


    protected final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
                disconnectUpdate();


            } if (RBLService.ACTION_GATT_ERROR_133.equals(action)) {
                String  bdAddress = intent.getStringExtra(RBLService.EXTRA_DATA);

                getERROREvent(bdAddress);


            } else if (RBLService.ACTION_GATT_CONNECTED.equals(action)) {
                tmpBuff ="";

                connectUpdate();
                bleService.getSupportedGattService();
            } else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                discoverUpdate();
                getGattService(bleService.getSupportedGattService());


            } else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(RBLService.EXTRA_DATA);


                /// recvBuff.put(data);
                //Log.e("sean","buffsize="+recvBuff.remaining());
                // int dataSize = recvBuff.remaining() - data.length;
                tmpBuff += encode.BytetoHexString(data);
                Util.debugMessage(TAG, "tmpBuff="+ encode.BytetoHexString(data),debugFlag);
                if(tmpBuff!=null&&tmpBuff.length()>10)
                {
                    int count =0;
                    int start_index = 0;
                    int end_index = 0;
                    boolean parseflag =false;
                    int cmdLen = 0;
                    for(int i=0;i<tmpBuff.length()/2;i++) {
                        //Log.e("Sean","tmpBuff="+ encode.BytetoHexString(data));
                        //  Log.e("sean","byte["+i+"]"+tmpBuff.substring(2 * i, i * 2 + 2)+"count="+count);
                        if(tmpBuff.substring(2 * i, i * 2 + 2).equals("C0"))
                        {
                            count++;
                            if(count == 1) {
                                start_index = i;
                                //   Util.debugMessage(TAG, "cmdlen="+tmpBuff.substring(2 * (start_index+3), (start_index+3)* 2+2),debugFlag);
                                byte datalen[] = encode.HexStringToBytes(tmpBuff.substring(2 * (start_index+3), (start_index+3)* 2+4));
                                cmdLen = encode.getUnsignedTwoByte(datalen);

                            }else
                            if(count == 2) {
                                end_index = i;

                                if(start_index<end_index&& ((end_index-start_index-1)== cmdLen+4))
                                    parseflag = true;
                                else
                                    count--;
                            }

                        }

                    }

                    if(parseflag){


                        String packet = tmpBuff.substring((start_index+1)*2,(end_index)*2);
                        Util.debugMessage(TAG, "recvPacket="+ packet,debugFlag);

                        protocolCheck(encode.HexStringToBytes(packet));
                        tmpBuff =/*tmpBuff.substring(0,start_index*2)+*/tmpBuff.substring((end_index+1)*2);
                    }
                }
                //Log.v(TAG, "nki_Check_Gatt_Write_Result(): " + reply_data);
                //nki_show_toast_msg("Write: " + reply_data + " OK!!");


            } else if (RBLService.ACTION_GATT_RSSI.equals(action)) {
                //displayRssiData(intent.getStringExtra(RBLService.EXTRA_DATA));
                String rssi = intent.getStringExtra(RBLService.EXTRA_DATA);
                update_RSSI(rssi);
            } else if (RBLService.ACTION_GATT_WRITE_SUCCESS.equals(action)) {
                //Util.debugMessage(TAG,"send ok",debugFlag);
                isMessageProcessing = false;
                writeSuccess();

            }else  if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
                try {

                    abortBroadcast();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            }
    };


/*
    public boolean getbtStatus()
    {
        return bleService.getbtStatus();
    }

    public boolean scan(boolean eanble, long SCAN_PERIOD)
    {  
       
      return bleService.BLEScan(eanble,SCAN_PERIOD);
    
    
    }
    public boolean btEnable()
    {
      return  bleService.btON();	
    	
    }
    
    public boolean btDisable()
    {
    	return bleService.btOFF();
    	
    }*/

    private void cmdSendProcess(Queue<Queue_Item> queue) {

        Queue_Item temp;


        //Check Connect
        if (isBLE_Service_Ready) {
            if (!isMessageProcessing) {
                temp = queue.poll();

                if (temp != null) {

                    isMessageProcessing = true;
                    MessageProcess_timeout_count = 0;

                    //Write or With Data
                    if (temp.data != null) {

                        if (E3AK_ble_ch != null) {
                            cmdDataCache(temp.data);
                            byte packet []= encode.SLIP_Encode(temp.data,temp.data.length);
                            Util.debugMessage(TAG,"cmd="+encode.BytetoHexString(packet),debugFlag);
                            bleService.writeData(E3AK_ble_ch,packet);

                            // Log.w(TAG, "QUEUE:" + "UUID_BLE_E1K_USER_ENROLL");
                        }

                    }


                }
            }


        } else {


            MessageProcess_timeout_count++;

            if (MessageProcess_timeout_count > MESSAGE_PROCESS_TIMEOUT_LIMIT) {
                 isMessageProcessing = false;
                MessageProcess_timeout_count = 0;
            }
        }


    }
    public void show_toast_msg(String text) {

        final Toast toastItem = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        if (text.equals(""))
            toastItem.cancel();
        else {
            //toastItem.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toastItem.setText(text);
            toastItem.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toastItem.cancel();
                }
            }, 500);
        }
    }
 private void cmdDataCache(byte rawData[]){
     Util.debugMessage(TAG, "cmdDataCache",debugFlag);
        final byte cmd [] = rawData;
        if(cmd.length>3) {
            Util.debugMessage(TAG, "cmdPacket=" + encode.BytetoHexString(cmd),debugFlag);
            final int datalen = cmd[2] << 8 | cmd[3];
            Util.debugMessage(TAG,"datalen=" + datalen + "length=" + cmd.length,debugFlag);

           final byte data[] = Arrays.copyOfRange(cmd, 4, cmd.length);
            if (datalen == cmd.length - 4) {
                Util.debugMessage(TAG,"cmdDataCacheEvent",debugFlag);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(currentClassName.equals(localClassName))
                        cmdDataCacheEvent(cmd[0],cmd[1],data,datalen);
                    }
                });

            }else
                Util.debugMessage(TAG,"cmd fail",debugFlag);
        }
    }

private void startCmdWorkerThread() {
    new Thread() {
        public void run() {

            Process.setThreadPriority(APPConfig.PROCESS_THREAD_PRIORITY);

            while (true) {
                if (bleService != null) {

                    //cmd send Queue Process
                    cmdSendProcess(bpProtocol.getQueue());

                } else {
                    bpProtocol.queueClear();
                }

                try {
                    Thread.sleep(APPConfig.QUEUE_PROCESS_PERIOD);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }.start();
}
    public boolean checkDeviceLevelExist(String bdAddr){

        return  sharedPreferences.getBoolean(APPConfig.RSSI_DB_EXIST+bdAddr,false);

    }
    public int loadDeviceRSSILevel(String bdAddr){
        int RSSI_Level = E3K_DEVICES_BLE_RSSI_LEVEL_DEFAULT;

        boolean isExist = false;
         isExist = sharedPreferences.getBoolean(APPConfig.RSSI_DB_EXIST+bdAddr,false);
        if(isExist) {

            RSSI_Level = sharedPreferences.getInt(APPConfig.RSSI_LEVEL_Tag + bdAddr, 123);

        }
        return RSSI_Level;
    }

    public void saveDeviceRSSILevel(String bdAddr, int RSSI_Level){
        sharedPreferences.edit().putInt(APPConfig.RSSI_LEVEL_Tag+bdAddr,RSSI_Level).commit();
        sharedPreferences.edit().putBoolean(APPConfig.RSSI_DB_EXIST+bdAddr,true).commit();

    }

    public boolean connect(String  BDaddress)
    {
       return bleService.connect(BDaddress);
   
    }

    public void disconnect()
    { 
       bleService.disconnect();
       bleService.close();
     
    }

    public void BLESocketClose()
    {   if(bleService !=null)
        bleService.close();

    }

     public String getBluetoothDeviceAddress(){

         return bleService.getBluetoothDeviceAddress();
     }
    public void getGattService(BluetoothGattService gattService) {
        if (gattService == null) {
            Util.debugMessage(TAG, "gattService is null !!",debugFlag);
            return;
        }





        //E3AK_ble_ch = gattService.getCharacteristics().get(0);

        E3AK_ble_ch = gattService.getCharacteristic(RBLService.UUID_BLE_E3K_CHAR);
        //Check characteristic
        if (E3AK_ble_ch != null) {

           bleService.setCharacteristicNotification(E3AK_ble_ch,true);
            startCmdWorkerThread();
            Handler delay =new Handler();
            delay.postDelayed(new Runnable() {
                @Override
                public void run() {

                   // sendProcessMessage(MSG_DEV_READY);
                    if(currentClassName.equals(localClassName))
                    BLEReady();
                }
            } ,btPreDelay);

            delay = null;

        }



    }



}

package com.anxell.e3ak.transport;

import android.os.Environment;


import com.anxell.e3ak.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Sean on 6/9/2017.
 */

public class SettingData {
    private String TAG = getClass().getName().toString();
    private boolean debugFlag = true;
    private File SettingFile = null;
    private FileOutputStream output = null;
    private FileInputStream input = null;
    private bpEncode encode = new bpEncode();
    private byte readBuffer[] = new byte[64];
    private int putIndex = 0;
    private int readIndex = 2;
    private String FileName = "";
    public SettingData(String fileName){
        FileName = fileName;



        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/" + "E3AK");
        if (root.canWrite()) {

            if(!dir.exists())
            dir.mkdirs();
            SettingFile = new File(dir, FileName );


Util.debugMessage(TAG,"test1",debugFlag);
        }else
            Util.debugMessage(TAG,"test2",debugFlag);

        readIndex = 2;


    }
    public boolean writeUserSize(int userMax){
        byte userMaxH = (byte)(userMax >>8);
        byte userMaxL = (byte) (userMax &0x00FF);
        try{
            output = new FileOutputStream(SettingFile, true);
           output.write(userMaxH);
           output.write(userMaxL);
            output.close();
            return true;
        }catch(IOException e){

        }
        return  false;
    }
    public boolean writeData(byte data[]){

            try {
                output = new FileOutputStream(SettingFile, true);
                for(int i=0;i<data.length;i++)
                output.write(data[i]);
                output.write(0x0D);
                output.write(0x0A);
                output.close();
            }catch(IOException e){

                return false;
            }
        return true;
    }

    public byte[] readData() {

        boolean readFlag = true;
        int retryCount = 5;
        byte tmp[] = new byte[5];

        for (int i = 0; i < readBuffer.length; i++)
            readBuffer[i] = 0x00;
        try{
        input = new FileInputStream(SettingFile);
            Util.debugMessage(TAG,"Total file size to read (in bytes) : "
                    +input.available(), debugFlag);
           input.skip(readIndex);
        }catch (IOException error){

        }

        while (readFlag) {

            try {
                    int dataInt = input.read();
                if(dataInt != -1) {
                    byte data = (byte)dataInt;

                    //Util.debugMessage(TAG, "read data=" + String.format("%02x", data), debugFlag);

                   readBuffer[putIndex] = data;
                    readIndex++;
                    putIndex++;
                }else
                    readFlag = false;



            } catch (IOException e) {
                Util.debugMessage(TAG, "error="+e.getMessage().toString(), debugFlag);
                if(retryCount>0)
                retryCount--;
                else
                    readFlag = false;

            }
            //Util.debugMessage(TAG, "readBuffer=" + encode.BytetoHexString(readBuffer), debugFlag);
            //.debugMessage(TAG, "putIndex="+putIndex,true);
            if(putIndex > 2) {
             if (readBuffer[putIndex - 2] == 0x0D && readBuffer[putIndex - 1] == 0x0A)
                readFlag = false;


            }


        }
        try{
            input.close();
        }catch(IOException error){

        }

        if(putIndex > 2) {
            Util.debugMessage(TAG, "putIndex="+putIndex,true);
            byte cmdData[] = new byte[putIndex - 2];
            for (int i = 0; i < cmdData.length; i++)
                cmdData[i] = readBuffer[i];
            putIndex = 0;
            Util.debugMessage(TAG, "cmdData=" + encode.BytetoHexString(cmdData), debugFlag);
            return  cmdData;

        }


        return null;
    }

    public void checkOldBackupFileExist(){
        if(SettingFile.exists())
            SettingFile.delete();


        try{
            SettingFile.createNewFile();



        }catch(IOException error){
            Util.debugMessage(TAG, "create file fail", debugFlag);
        }
    }

    public int  GetUserSize() {
        byte userSizeBuffer[] = new byte[2];
       int userSize = -1;

        try {
            input = new FileInputStream(SettingFile);
            input.read(userSizeBuffer, 0, 2);
            userSize = (encode.getUnsignedTwoByte(userSizeBuffer));
            /*Util.debugMessage(TAG, "userSize="+String.format("%02x",(encode.getUnsignedByte(userSizeBuffer[0]) & 0xFF00)), debugFlag);
            Util.debugMessage(TAG, "userSize="+String.format("%02x",(encode.getUnsignedByte(userSizeBuffer[1]))), debugFlag);
            Util.debugMessage(TAG, "userSize="+userSize, debugFlag);*/
            input.close();
        } catch (IOException e) {


        }


        return  userSize;
    }

}

package com.anxell.e3ak.transport;

/**
 * Created by Sean on 7/25/2017.
 */

public class ScanItem {
    public String dev_name;
    public String dev_addr;
    public int rssi;
    public int alive_cnt;

    public ScanItem() {
        clear();
    }

    public ScanItem(String dev_name, String dev_addr, int rssi, int alive_cnt) {
        this.dev_name = dev_name;
        this.dev_addr = dev_addr;
        this.rssi = rssi;
        this.alive_cnt = alive_cnt;
    }

    public ScanItem(String dev_name, String dev_addr) {
        this.dev_name = dev_name;
        this.dev_addr = dev_addr;
        this.rssi = -255;
        this.alive_cnt = 0;
    }

    public ScanItem(String dev_name, String dev_addr, int alive_cnt) {
        this.dev_name = dev_name;
        this.dev_addr = dev_addr;
        this.alive_cnt = alive_cnt;
    }

    public ScanItem(String dev_addr) {
        this.dev_name = "";
        this.dev_addr = dev_addr;
        this.rssi = -255;
        this.alive_cnt = 0;
    }

    public void setAlive_cnt(int alive_cnt) {
        this.alive_cnt = alive_cnt;
    }

    public void clear() {
        this.dev_name = "";
        this.dev_addr = "";
        this.rssi = -255;
        this.alive_cnt = 0;
    }
}

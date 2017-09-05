package com.anxell.e3ak.transport;

import java.util.ArrayList;

/**
 * Created by Sean on 7/25/2017.
 */

public class ScanItemData {
    public ArrayList<ScanItem> scanItems;



    public ScanItemData() {
        this.scanItems = new ArrayList<>();
    }



    public void clear() {
        scanItems.clear();
    }

    public int size() {
        return scanItems.size();
    }

    public int check_device_exist_by_addr(String addr)
    {
        int idx = (-1);

        for(int i=0; i<this.scanItems.size(); i++)
        {
            if(this.scanItems.get(i).dev_addr.equals(addr))
                idx = i;
        }

        return idx;
    }

    public int get_device_rssi_by_addr(String addr)
    {
        for(int i=0; i<this.scanItems.size(); i++)
        {
            if(this.scanItems.get(i).dev_addr.equals(addr))
                return this.scanItems.get(i).rssi;
        }

        return (-255);
    }
}

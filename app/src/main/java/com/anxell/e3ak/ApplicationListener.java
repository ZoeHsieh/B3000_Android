package com.anxell.e3ak;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


public class ApplicationListener implements Application.ActivityLifecycleCallbacks {
    private static Map<String,Activity> destoryMap = new HashMap<>();
    private int foregroundCount = 0; // 位于前台的 Activity 的数目
    @Override
    public void onActivityStarted(Activity activity) {
        if (foregroundCount <= 0) {
            // TODO 这里处理从后台恢复到前台的逻辑
        }
        foregroundCount++;
    }
    @Override
    public void onActivityStopped(Activity activity) {
        foregroundCount--;
        if (foregroundCount <= 0) {
            // TODO 这里处理从前台进入到后台的逻辑

            for (int i=0;i<=destoryMap.size()-1;i++)
            {
                destoryActivity(activity.getLocalClassName());
            }
            int id= android.os.Process.myPid();
            android.os.Process.killProcess(id);
        }
    }
    /*
     * 下面回调，我们都不需要
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        addDestoryActivity(activity,activity.getLocalClassName());
    }
    @Override
    public void onActivityResumed(Activity activity) {}
    @Override
    public void onActivityPaused(Activity activity) {}
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override
    public void onActivityDestroyed(Activity activity) {}


    /**
     * 添加到销毁队列
     *
     * @param activity 要销毁的activity
     */

    public static void addDestoryActivity(Activity activity,String activityName) {
        destoryMap.put(activityName,activity);
    }
    /**
     *销毁指定Activity
     */
    public static void destoryActivity(String activityName) {
        Set<String> keySet=destoryMap.keySet();
        for (String key:keySet){
            destoryMap.get(key).finish();
        }
    }


}
package com.lenovo.smartShop.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.lenovo.smartShop.activity.ShopListActivity;
import com.lenovo.smartShop.adapter.ListViewAdapter;
import com.lenovo.smartShop.adapter.SearchAdapter;
import com.lenovo.smartShop.view.DownLoadButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by linsen3 on 2017/9/15.
 */

public class StateMachine {

    private static final String TAG = "SC-StateMachine";
    private static StateMachine mInstace;
    private int currentState = 0;
    public static final int defaultPercent = 0;
    private HashMap<String, Integer> hashMap;
    private HashMap<String, Integer> percentMap;
    private int initState = DownLoadButton.STATE_NORMAL;

    private StateMachine(){
        hashMap = new HashMap<>();
        percentMap = new HashMap<>();
    }

    public static StateMachine getInstance(){
        if(mInstace == null){
            mInstace = new StateMachine();
        }
        return mInstace;
    }

    public void initState(final ArrayList<String> mList, final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String appPackageName : mList){
                    Log.d(TAG, "appPackageName = " + appPackageName);
                    if(SCPackageManager.queryAppIsExist(context, appPackageName)){
                        Log.d(TAG, "Complete");
                        setDownLoadState(appPackageName, DownLoadButton.STATE_COMPLETE);
                    }else if(SCPackageManager.queryAppInstallPackage(appPackageName)){
                        Log.d(TAG, "Install");
                        setDownLoadState(appPackageName, DownLoadButton.STATE_INSTALL);
                    }else {
                        Log.d(TAG, "Normal");
                        setDownLoadState(appPackageName, DownLoadButton.STATE_NORMAL);
                    }
                }
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*SearchAdapter adapter = (SearchAdapter) ShopListActivity.listView.getAdapter();
                        adapter.notifyDataSetChanged();*/
                        ListViewAdapter adapter = (ListViewAdapter) ShopListActivity.listView.getAdapter();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }

    public void setDownLoadState(String packageName, int state){
        this.currentState = state;
        if(hashMap.containsKey(packageName)){
            hashMap.remove(packageName);
        }
        hashMap.put(packageName, currentState);
    }

    public int getDownLoadState(String packageName) {
        if(hashMap.containsKey(packageName)){
            return hashMap.get(packageName);
        }
        return initState;
    }

    public boolean isCurrentState(int state){
        Iterator iter = hashMap.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next();
            if(entry.getValue().equals(state)){
                return true;
            }
        }
        return false;
    }

    public void setDownloadPercent(String packageName, int percent){
        if(percentMap.containsKey(packageName)){
            percentMap.remove(packageName);
        }
        percentMap.put(packageName, percent);
    }

    public int getDownloadPercent(String packageName){
        Log.d(TAG, "percentMap = " + percentMap);
        if(percentMap.containsKey(packageName)){
            return percentMap.get(packageName);
        }
        return defaultPercent;
    }

    public void releaseState(){
        hashMap.clear();
        percentMap.clear();
    }
}

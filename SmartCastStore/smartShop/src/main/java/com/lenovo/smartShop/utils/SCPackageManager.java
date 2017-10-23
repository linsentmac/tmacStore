package com.lenovo.smartShop.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.List;

/**
 * Created by linsen3 on 2017/9/12.
 */

public class SCPackageManager {

    private static Context mContext;
    private static String appPackageName;
    private final static String TAG = "SC-PackageManager";
    public static String PATH = Environment.getExternalStorageDirectory() + "/SmartShop/";
    private final static int PACKAGE_INDEX = 1;

    /**
     * 查询app是否已安装存在
     * @param context
     * @param appPackageName
     * @return
     */
    public static boolean queryAppIsExist(Context context, String appPackageName)
    {
        mContext = context;
        PackageManager pm = mContext.getPackageManager();
        // 查询所有已经安装的应用程序
        List<ApplicationInfo> listAppcations = pm
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);// GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
        String activityName = "";
        String pkgName = "";
        String appLabel = "";
        for (ApplicationInfo app : listAppcations)
        {
            pkgName = app.packageName; // 获得应用程序的包名
            appLabel = (String) app.loadLabel(pm); // 获得应用程序的Label
            System.out.println("2、" + appLabel + " activityName---"
                    + activityName + " pkgName---" + pkgName);
            // Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
            if (pkgName.equalsIgnoreCase(appPackageName))
            {
                System.out.println(appLabel + " pkgName---" + pkgName);
                //appPackageName = pkgName;
                return true;
            }
        }
        return false;
    }


    public static boolean queryAppInstallPackage(String packageName){
        File specItemDir = new File(PATH);
        File[] files = specItemDir.listFiles();
        for (File file : files){
            Log.d(TAG, "name = " + file.getName());
            if(file.getName().equals(packageName)){
                return true;
            }
        }
        return false;
    }

    public static void deleteInstallPackage(String packageName){
        //String[] sourceStrArray = packageName.split(":");
        File file = new File(PATH, packageName);
        try {
            if(file.exists()){
                file.delete();
                Log.d(TAG, "delete success");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void startActivityByPn(String appPackageName){
        Intent intent = mContext.getPackageManager()
                .getLaunchIntentForPackage(appPackageName);
        if (intent != null)
            // 已安装包 直接启动
            mContext.startActivity(intent);
    }

    private void startActivityByPnAndAn(String appPackageName, String appAcitivityName){
        // 前名一个参数是应用程序的包名,后一个是这个应用程序的Activity名
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(appPackageName,
                appAcitivityName));
        mContext.startActivity(intent);
    }

}

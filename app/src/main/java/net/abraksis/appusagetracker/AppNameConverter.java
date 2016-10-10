package net.abraksis.appusagetracker;


import android.content.Context;
import android.content.pm.PackageManager;

public class AppNameConverter {

    public static String getAppNameFromPackageName(Context context, String packageName) {

        PackageManager packageManager = context.getPackageManager();
        String appName ="";
        try {
            appName = (String) packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }
}

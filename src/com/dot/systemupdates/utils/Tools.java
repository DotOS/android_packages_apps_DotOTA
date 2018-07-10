package com.dot.systemupdates.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tools {
    public static Boolean doesPropExist(String propName) {
        return !Objects.equals(SystemProperties.get(propName), "");
    }
    public static String getProp(String propName) {
        return SystemProperties.get(propName);
    }
    public static boolean recovery(Context context) {
        return rebootPhone(context, "recovery");
    }

    public static boolean rebootPhone(Context context, String type) {
        boolean success = true;
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            powerManager.reboot("recovery");
        } catch (Exception e) {
            success = false;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            Log.e("Tools", "reboot '"+type+"' error: "+e.getMessage());
        }
        return success;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String readChangelogFromSystem() {
        InputStreamReader inputReader = null;
        StringBuilder data = new StringBuilder();
        char tmp[] = new char[2048];
        int numRead;
        String val;
        try {
            inputReader = new FileReader("/system/etc/Changelog.txt");
            while ((numRead = inputReader.read(tmp)) >= 0) {
                data.append(tmp, 0, numRead);
            }
            val = data.toString();
        } catch (IOException e) {
            val = "Changelog not found";
        } finally {
            try {
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (IOException ignored) {
            }
        }
        return val;
    }

    public static List<String> getTextFromWeb() {
        URLConnection feedUrl;
        List<String> placeAddress = new ArrayList<>();
        try {
            feedUrl = new URL("https://raw.githubusercontent.com/DotOS/android_vendor_dot/dot-o/dot.devices").openConnection();
            InputStream is = feedUrl.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                placeAddress.add(line);
            }
            is.close();
            return placeAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

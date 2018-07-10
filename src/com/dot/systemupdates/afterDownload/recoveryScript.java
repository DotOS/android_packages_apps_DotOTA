package com.dot.systemupdates.afterDownload;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dot.systemupdates.R;
import com.dot.systemupdates.utils.Constants;
import com.dot.systemupdates.utils.Prefs;
import com.dot.systemupdates.utils.SystemProperties;
import com.dot.systemupdates.utils.Tools;
import com.dot.systemupdates.utils.xmlParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class recoveryScript extends AsyncTask<Void, String, Boolean> implements Constants {

    private final String TAG = this.getClass().getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private StringBuilder mScript = new StringBuilder();
    private String mFilename;
    private String mScriptOutput;
    private String[] serverNodes;
    private String Url = "https://raw.githubusercontent.com/IacobIonut01/services_apps_ota/dot-o/";

    public recoveryScript(Context context) {
        mContext = context;
        String device = SystemProperties.get("ro.dotOS.device");
        try {
            xmlParser xmlParser = new xmlParser();
            serverNodes = xmlParser.execute(Url+device+".xml").get();
            mFilename = serverNodes[0] + "-" + device + ".zip";
        } catch ( InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    protected void onPreExecute() {

        String NEW_LINE = "\n";
        if (Prefs.getWipeData(mContext)) {
            mScript.append("wipe data").append(NEW_LINE);
        }
        if (Prefs.getWipeCache(mContext)) {
            mScript.append("wipe cache").append(NEW_LINE);
        }
        if (Prefs.getWipeDalvik(mContext)) {
            mScript.append("wipe dalvik").append(NEW_LINE);
        }

        StringBuilder installRom = new StringBuilder()
                .append("install ")
                .append(Constants.SD_CARD)
                .append(File.separator)
                .append(OTA_DOWNLOAD_DIR)
                .append(File.separator)
                .append(mFilename)
                .append(NEW_LINE);
        mScript.append(installRom);
        if (DEBUGGING) Log.d(TAG,installRom.toString());

        File installAfterFlashDir = new File(Constants.SD_CARD
                + File.separator
                + OTA_DOWNLOAD_DIR
                + File.separator
                + INSTALL_AFTER_FLASH_DIR);

        File[] filesArr = installAfterFlashDir.listFiles();
        if(filesArr != null && filesArr.length > 0) {
            for (File aFilesArr : filesArr) {
                StringBuilder installAfterFlash = new StringBuilder()
                        .append(NEW_LINE).append("install ")
                        .append(Constants.SD_CARD)
                        .append(File.separator)
                        .append(OTA_DOWNLOAD_DIR)
                        .append(File.separator)
                        .append(INSTALL_AFTER_FLASH_DIR)
                        .append(File.separator)
                        .append(aFilesArr.getName());
                mScript.append(installAfterFlash);
                if (DEBUGGING)
                    Log.d(TAG,installAfterFlash.toString());
            }
        }

        if (Prefs.getDeleteAfterInstall(mContext)) {
            mScript.append(NEW_LINE)
                    .append("cmd rm -rf ")
                    .append(Constants.SD_CARD)
                    .append(File.separator)
                    .append(OTA_DOWNLOAD_DIR)
                    .append(File.separator)
                    .append(INSTALL_AFTER_FLASH_DIR)
                    .append(File.separator)
                    .append(mFilename)
                    .append(NEW_LINE);
        }

        mScriptOutput = mScript.toString();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean orsWrittenToCache = false;
        File orsDir = new File("/cache/recovery");
        File orsFile = new File("/cache/recovery/openrecoveryscript");
        if (!orsDir.exists()){
            try {
                orsWrittenToCache = orsDir.createNewFile();
                if (!orsFile.exists()){
                    orsWrittenToCache = orsFile.createNewFile();
                    FileWriter fileWriter = new FileWriter(orsFile);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(String.format("\" %s \" \n", mScriptOutput));
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return orsWrittenToCache;
    }
    @Override
    protected void onPostExecute(Boolean value) {
        Tools.recovery(mContext);
    }
}

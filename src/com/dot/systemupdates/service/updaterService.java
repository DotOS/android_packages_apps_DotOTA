package com.dot.systemupdates.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.dot.systemupdates.R;
import com.dot.systemupdates.baseActivity;
import com.dot.systemupdates.utils.SystemProperties;
import com.dot.systemupdates.utils.xmlParser;

import java.util.concurrent.ExecutionException;

@SuppressLint("Registered")
public class updaterService extends JobService {
    String[] serverNodes;
    String Url = "https://raw.githubusercontent.com/DotOS/ota/dot-o/";
    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            String localVersion = SystemProperties.get("ro.dot.version");
            String device = SystemProperties.get("ro.dotOS.device");

            xmlParser xmlParser = new xmlParser();
            serverNodes = xmlParser.execute(Url+device+".xml").get();

            if (!localVersion.equals(serverNodes[0]) && serverNodes[0] != null) {
                int notifyID = 1;
                String CHANNEL_ID = "system_updates_global";
                CharSequence name = "DotOS Updates";
                int importance;
                importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                NotificationCompat.Builder notification =
                        new NotificationCompat.Builder(this,CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_dot)
                                .setContentTitle("System Update Available!")
                                .setContentText("New version of dotOS is available for " + SystemProperties.get("ro.dotOS.device"));
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert mNotificationManager != null;
                mNotificationManager.createNotificationChannel(notificationChannel);
                Intent resultIntent = new Intent(this, baseActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(baseActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(resultPendingIntent);
                mNotificationManager.notify(notifyID , notification.build());
            }
            jobFinished(params,false);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
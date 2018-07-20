package com.dot.systemupdates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dot.systemupdates.afterDownload.prepareToFlash;
import com.dot.systemupdates.service.updaterService;
import com.dot.systemupdates.utils.ObjectToolsAnimator;
import com.dot.systemupdates.utils.SystemProperties;
import com.dot.systemupdates.utils.Tools;
import com.dot.systemupdates.utils.xmlParser;
import com.dot.systemupdates.views.ChangelogLayout;
import com.dot.systemupdates.views.ExpandableLayout;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.dot.systemupdates.utils.Tools.isNetworkAvailable;
import static java.lang.Thread.sleep;

public class baseActivity extends AppCompatActivity {
    String Url = "https://raw.githubusercontent.com/DotOS/ota/dot-o/";
    String[] serverNodes;
    DownloadManager downloadManager;
    String changelog;
    boolean isOff;
    int intervals = 86400000;

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        changelog = Tools.readChangelogFromSystem();
        updateDisplayVersion();
        getUpdateState();
        JobScheduler jobScheduler = new JobScheduler() {
			@Override
            @RequiresPermission(android.Manifest.permission.UPDATE_DEVICE_STATS)
            public int scheduleAsPackage(@NonNull JobInfo job, @NonNull String $,
                                                          int userId, String ta$) {
                return 0;
            }
            @SuppressLint("WrongConstant")
            @Override
            public int schedule(@NonNull JobInfo job) {
                return 0;
            }
            @SuppressLint("WrongConstant")
            @Override
            public int enqueue(@NonNull JobInfo job, @NonNull JobWorkItem work) {
                return 0;
            }

            @Override
            public void cancel(int jobId) {

            }

            @Override
            public void cancelAll() {

            }

            @NonNull
            @Override
            public List<JobInfo> getAllPendingJobs() {
                return null;
            }

            @Nullable
            @Override
            public JobInfo getPendingJob(int jobId) {
                return null;
            }
        };
        jobScheduler.schedule(new JobInfo.Builder(0, new ComponentName(this, updaterService.class)).setPeriodic(intervals).build());
        File installAfterFlashDir = new File(Environment.getExternalStoragePublicDirectory(File.separator + "DotUpdates").toString());
        if (!installAfterFlashDir.mkdirs() && !installAfterFlashDir.exists())
            Log.e("SystemUpdates", "Download directory creation failed");
        CardView go_back = findViewById(R.id.go_back);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final ImageButton info_expander = findViewById(R.id.expand_info);
        final ExpandableLayout exp_info = findViewById(R.id.expandable_info);
        info_expander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (exp_info.getStatus()) {
                    case EXPANDED:
                        ObjectToolsAnimator.rotate(info_expander, 0, 180);
                        exp_info.collapse(true);
                        break;
                    case COLLAPSED:
                        ObjectToolsAnimator.rotate(info_expander, 180, 0);
                        exp_info.expand(true);
                        break;
                }
            }
        });
        updateCheck();
    }

    public void getUpdateState() {
        FloatingActionButton updateCheck = findViewById(R.id.check_updates);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        updateCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCheck();
                ExpandableLayout exp_info = findViewById(R.id.expandable_info);
                ImageButton info_expander = findViewById(R.id.expand_info);
                if (exp_info.isExpanded()) {
                    ObjectToolsAnimator.rotate(info_expander, 0, 180);
                    exp_info.collapse(true);
                }

            }
        });
    }

    public void animateCheck() {
        LinearLayout control = findViewById(R.id.control_layout);
        ObjectToolsAnimator.moveAndAnimate(control, "alpha", 1, 0, 200);
        control.setVisibility(View.GONE);
        ProgressBar prg = findViewById(R.id.check_prg);
        prg.setVisibility(View.VISIBLE);
        prg.setIndeterminate(true);
        try {
            sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        prg.setVisibility(View.GONE);
        control.setVisibility(View.VISIBLE);
        ObjectToolsAnimator.moveAndAnimate(control, "alpha", 0, 1);
    }

    public void updateCheck() {
        animateCheck();
        ChangelogLayout updateChangelog = findViewById(R.id.changelog);
        TextView new_version = findViewById(R.id.update_version);
        TextView size = findViewById(R.id.size_mb);
        TextView no_updates = findViewById(R.id.up_to_date);
        ImageButton down = findViewById(R.id.action_button);
        LinearLayout up1 = findViewById(R.id.update_card);
        try {
            String localVersion = SystemProperties.get("ro.dot.version");
            final String device = SystemProperties.get("ro.dotOS.device");
            xmlParser xmlParser = new xmlParser();
            if (isOfficial()) {
                serverNodes = xmlParser.execute(Url + device + ".xml").get();
                new_version.setText(serverNodes[0]);
                String downloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
                final IntentFilter downloadCompleteIntentFilter = new IntentFilter(downloadCompleteIntentName);
                if (!localVersion.equals(serverNodes[0]) && serverNodes[0] != null) {
                    up1.setVisibility(View.VISIBLE);
                    updateChangelog.setVisibility(View.VISIBLE);
                    no_updates.setVisibility(View.GONE);
                    if (serverNodes[4] != null)
                    updateChangelog.setSystem(serverNodes[4]);
                    if (serverNodes[5] != null)
                    updateChangelog.setSettings(serverNodes[5]);
                    if (serverNodes[6] != null)
                    updateChangelog.setLauncher(serverNodes[6]);
                    if (serverNodes[7] != null)
                    updateChangelog.setSecurityPatch(serverNodes[7]);
                    if (serverNodes[8] != null)
                    updateChangelog.setMisc(serverNodes[8]);
                    size.setText(getFileSize(Long.parseLong(serverNodes[3])));
                    down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.shouldShowRequestPermissionRationale(baseActivity.this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                ActivityCompat.requestPermissions(baseActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        0);
                            }
                            if (ContextCompat.checkSelfPermission(baseActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(serverNodes[2]));
                                request.setTitle("Downloading System Update");
                                request.setDescription(serverNodes[0] + " is available to action_button");
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir("/DotUpdates",  serverNodes[0] + ".zip");
                                final long down_ref = downloadManager.enqueue(request);
                                BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(final Context context, Intent intent) {
                                        long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                                        if (down_ref == reference) {
                                            DownloadManager.Query query = new DownloadManager.Query();
                                            query.setFilterById(reference);
                                            Cursor cursor = downloadManager.query(query);
                                            cursor.moveToFirst();
                                            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                            int status = -9;
                                            if (cursor.getCount() > 0) {
                                                status = cursor.getInt(columnIndex);
                                            }
                                            switch (status) {
                                                case DownloadManager.STATUS_SUCCESSFUL:
                                                    AlertDialog.Builder down_end = new AlertDialog.Builder(baseActivity.this);
                                                    down_end.setTitle("Download Finished")
                                                            .setMessage("Do you want to start autoFlash or to update manually?")
                                                            .setCancelable(false)
                                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    startActivity(new Intent(getApplicationContext(), prepareToFlash.class));
                                                                    dialog.dismiss();
                                                                }
                                                            })
                                                            .setNeutralButton("No, thanks", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                }
                                                            })
                                                            .show();
                                                    break;
                                            }
                                        }
                                    }
                                };
                                v.getContext().registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);
                            }
                        }
                    });
                } else {
                    up1.setVisibility(View.GONE);
                    updateChangelog.setVisibility(View.GONE);
                }
            } else {
                no_updates.setText("System is up to date - Device not official");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("RestrictedApi")
    public void updateDisplayVersion() {
        final TextView dot_version = findViewById(R.id.dotOS_version);
        final TextView up_to_date = findViewById(R.id.up_to_date);
        final FloatingActionButton check_updates = findViewById(R.id.check_updates);
        String Official = SystemProperties.get("ro.dot.releasetype");
        if (Objects.equals(Official, "OFFICIAL")) {
            isOff = true;
        }
        if (SystemProperties.get("ro.dotOS.device").isEmpty()) {
            dot_version.setText("version " + SystemProperties.get("ro.modversion") + "- NOT DOTOS ROM");
            up_to_date.setText("Out of index rom");
        }
        if (isOfficial()) {
            check_updates.setVisibility(View.VISIBLE);
            up_to_date.setText("System is up to date");
            dot_version.setText("version " + SystemProperties.get("ro.modversion"));
        }
        if (!isNetworkAvailable(getApplicationContext())) {
            check_updates.setVisibility(View.VISIBLE);
            up_to_date.setText("System is up to date");
            dot_version.setText("version " + SystemProperties.get("ro.modversion"));
        } else {
            dot_version.setText("version " + SystemProperties.get("ro.modversion"));
            up_to_date.setText("Unofficial build not supported");
        }
        /*new Thread(new Runnable() {
            public void run() {
                final List<String> txt = getTextFromWeb();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String device = SystemProperties.get("ro.dotOS.device");

                    }
                });
                runOnUiThread(new Runnable() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void run() {

                    }
                });
            }
        }).start();*/
    }

    public boolean isOfficial() {
        return isOff;
    }

}

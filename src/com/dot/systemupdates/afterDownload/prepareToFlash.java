package com.dot.systemupdates.afterDownload;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dot.systemupdates.R;
import com.dot.systemupdates.utils.ObjectToolsAnimator;

import java.io.DataOutputStream;
import java.io.IOException;

import static android.os.SystemClock.sleep;

public class prepareToFlash extends AppCompatActivity {

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hide();
        setContentView(R.layout.prepare_to_flash);
        mContentView = findViewById(R.id.fullscreen_layout);
        final TextView status = findViewById(R.id.text_after_stats);
        final ImageView settings_gear = findViewById(R.id.settings_icon_in_update);
        final ProgressBar progressBar = findViewById(R.id.intermintent_bar);
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        disableUI();
                        wait(5000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                status.setVisibility(View.VISIBLE);
                                ObjectToolsAnimator.moveAndAnimate(settings_gear, "translationY", 0, 300, 1000);
                                ObjectToolsAnimator.rotate(settings_gear, 0f, 360f, 1200);
                            }
                        });
                    }
                    synchronized (this) {
                        wait(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                status.setText("Rebooting...");
                                ObjectToolsAnimator.moveAndAnimate(settings_gear, "alpha", 1, 0, 700);
                                settings_gear.setVisibility(View.GONE);
                                ObjectToolsAnimator.moveAndAnimate(status, "translationY", 0, 50, 400);
                                ObjectToolsAnimator.moveAndAnimate(progressBar, "translationY", 0, 50, 400);
                                status.setTextSize(22);
                                new recoveryScript(prepareToFlash.this).execute();
                                enableUI();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });

    }

    private void disableUI(){
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("pm disable com.android.systemui\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
    private void enableUI(){
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("pm enable com.android.systemui\n");
            os.writeBytes("killall com.android.systemui\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }


    @Override
    public void onBackPressed() {

    }
    private void hide() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}

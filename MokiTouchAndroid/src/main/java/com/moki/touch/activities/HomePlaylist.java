package com.moki.touch.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.moki.asm.views.AppSettingsActivity;
import com.moki.sdk.logging.data.model.MokiLogging;
import com.moki.touch.copy.MTAppSettingsActivity;
import com.moki.touch.fragments.IdleResetDialogFragment;
import com.moki.touch.fragments.PlaylistFragment;
import com.moki.touch.fragments.views.ButtonViewBar;
import com.moki.touch.fragments.views.WebContent;
import com.moki.touch.gesturedetectors.IGestureDetected;
import com.moki.touch.gesturedetectors.L_GestureDetector;
import com.moki.touch.interfaces.ScreenController;
import com.moki.touch.util.SettingsUtil;
import com.moki.touch.util.management.HomeContentManager;
import com.moki.touch.R;

/**
 * Copyright (C) 2014 Moki Mobility Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 *
 * You may only use this file in compliance with the license
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class HomePlaylist extends PlaylistActivity implements IGestureDetected{


    public static final String SCREENSAVER_RESET_ACTION = "Screensaver";
    private static final int ONE_SECOND_MILLIS = 1000;
    private static final int ONE_MINUTE_MILLIS = ONE_SECOND_MILLIS * 60;
    private boolean canSwipeToAdvance;
    private long idleResetTime;
    private String idleResetLocation;
    private Handler idleHandler;
    private GestureDetector gestureDetector;
    private ButtonViewBar bar;
    private L_GestureDetector openSettingsDetector;
    private LinearLayout barLayout;
    private String adminPassword;
    private static AudioManager audioManager;
    private String styleColor;
    private boolean isMuted = false;

    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        context = this;
        barLayout = (LinearLayout)findViewById(R.id.button_view);
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());
        openSettingsDetector = new L_GestureDetector(this, this);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        styleColor = SettingsUtil.getThemeColor();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static Context getContext(){
        return context;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter buttonFilter = new IntentFilter(ButtonViewBar.BUTTON_ACTION);
        registerReceiver(buttonReceiver, buttonFilter);
        MokiLogging.addBreadcrumb("HomePlaylist onResume");
    }

    private void configureLinkBar() {
        styleColor = SettingsUtil.getThemeColor();
        if (bar != null) {
            bar.cleanBar();
            bar = null;
            if (barLayout.getChildCount() >= 1) {
                barLayout.removeAllViews();
            }
        }
        if (SettingsUtil.showLinkBar()) {
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            bar = new ButtonViewBar(this, contentManager.getAllContentObjects(), styleColor, screenWidth);
            barLayout.addView(bar);
            if (barLayout.getVisibility() == View.GONE) {
                barLayout.setVisibility(View.VISIBLE);
            }
        }
        else {
            barLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void refreshKiosk() {
        super.refreshKiosk();
        configureLinkBar();
        renewSettings();
        setupIdleReset();
        adminPassword = SettingsUtil.getAdminPassword();
        int volume = SettingsUtil.getDeviceVolume();
        if (volume > 0) {
            isMuted = false;
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
        else {
            if (!isMuted) {
                isMuted = true;

                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (idleHandler != null) {
            idleHandler.removeCallbacksAndMessages(null);
        }
        unregisterReceiver(buttonReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bar != null) {
            bar.cleanBar();
        }
    }

    private void renewSettings() {
        canSwipeToAdvance = SettingsUtil.enableSwiping();
        idleResetTime = SettingsUtil.resetWhenIdleFor() * ONE_MINUTE_MILLIS;
        idleResetLocation = SettingsUtil.resetAction();
    }

    public void setupIdleReset() {

        if (idleHandler != null) {
            idleHandler.removeCallbacksAndMessages(null);
        }
        else {
            idleHandler = new Handler();
        }

        if (idleResetTime > 0) {
            if (idleResetLocation.equals(SCREENSAVER_RESET_ACTION)) {
                idleHandler.postDelayed(saverRunnable, idleResetTime);
            }
            else {
                idleHandler.postDelayed(homeRunnable, idleResetTime);
            }
        }
    }

    Runnable saverRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPaused) {
                startScreenSaver();
            }
        }
    };

    private void startScreenSaver() {
        Intent saverIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
        startActivity(saverIntent);
    }

    Runnable homeRunnable = new Runnable() {
        @Override
        public void run() {
            IdleResetDialogFragment fragment = new IdleResetDialogFragment();
            fragment.show(getFragmentManager(),"idleDialog");
        }
    };

    public void handleIdleReset(){
        for(ScreenController controller: controllers){
            if(controller instanceof WebContent){
                ((WebContent)controller).cleanupHistory();
            }
        }
        setupIdleReset();
        defaultInit();
        if (bar != null) {
            bar.setSelection(0);
        }
    }

    @Override
    protected void configureManager() {
        contentManager = new HomeContentManager();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        openSettingsDetector.detect(ev);
        setupIdleReset();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (canSwipeToAdvance) {
            gestureDetector.onTouchEvent(event);
        }

        return false;
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(this, MTAppSettingsActivity.class);
        startActivityForResult(settingsIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            this.finish();
        }
    }

    private void createPasswordDialog() {
        Dialog passwordDialog;
        final EditText passwordText = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @SuppressWarnings("ConstantConditions") // a check is being made for the null that may be thrown
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (passwordText.getText() != null && passwordText.getText().toString().equals(adminPassword)) {
                    openSettings();
                }
            }
        }).setTitle(getString(R.string.dialog_admin_password)).setView(passwordText);
        passwordDialog = builder.create();
        passwordDialog.show();
    }

    private BroadcastReceiver buttonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra(DISPLAY_POSITION, DEFAULT_POSITION);
            bar.setSelection(position);
            initFromPosition(position);
        }
    };

    @Override
    protected void displayFragmentContent(PlaylistFragment newFragment, int transitionDirection) {
        super.displayFragmentContent(newFragment, transitionDirection);
        if (bar != null) {
            bar.setSelection(newFragment.getScreenController().getContentObject().getPosition());
        }
    }

    @Override
    public void onGestureDetected() {
        if (!adminPassword.isEmpty()) {
            createPasswordDialog();
        }
        else {
            openSettings();
        }
    }

    class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 150;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velX, float velY) {
            if (Math.abs(velY) > SWIPE_THRESHOLD_VELOCITY) {
                Log.i("SwipeGestureDetector", e1.getX() + ",  " + e2.getX());
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                    // A left swipe
                    move(MOVE_TO_PREVIOUS_ITEM);
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                    // A right swipe
                    move(MOVE_TO_NEXT_ITEM);
                }
            }
            return false;
        }
    }

}
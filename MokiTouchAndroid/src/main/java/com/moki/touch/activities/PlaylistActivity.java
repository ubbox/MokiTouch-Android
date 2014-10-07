package com.moki.touch.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;

import com.moki.asm.MokiASM;
import com.moki.follow.me.FollowMeGestureDetector;
import com.moki.manage.api.MokiManage;
import com.moki.sdk.logging.data.model.MokiLogging;
import com.moki.touch.R;
import com.moki.touch.base.BaseScreenController;
import com.moki.touch.models.ContentObject;
import com.moki.touch.fragments.PlaylistFragment;
import com.moki.touch.interfaces.ScreenController;
import com.moki.touch.util.MMManager;
import com.moki.touch.util.SettingsUtil;
import com.moki.touch.util.management.ContentManager;
import com.moki.touch.util.management.SaverContentManager;

import java.util.ArrayList;

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
public class PlaylistActivity extends Activity {

    public static final String SCREENSHOT_INTENT = MokiManage.SCREEN_SHOT_INTENT;
    public static final String HOME_INTENT = "com.moki.touch.GO_HOME";
    public static final int MOVE_TO_NEXT_ITEM = 0;
    public static final int MOVE_TO_PREVIOUS_ITEM = 1;
    public static final String DISPLAY_POSITION = "position";
    public static final int DEFAULT_POSITION = 0;
    protected boolean savedStateValid = true;
    private PlaylistFragment currentFragment;
    protected ArrayList<ScreenController> controllers;
    protected ContentManager contentManager;
    protected boolean isPaused;
    private PowerManager.WakeLock wakelock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        configureManager();
        controllers = new ArrayList<ScreenController>();
        initFragments();
        displayFragmentContent(currentFragment, MOVE_TO_NEXT_ITEM);
        wakelock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, PlaylistActivity.class.getSimpleName());
        wakelock.acquire();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MokiLogging.addBreadcrumb("PlaylistActivity onResume");
        //to detect when an image or video is finished
        IntentFilter contentFilter = new IntentFilter(BaseScreenController.CONTENT_FINISHED);
        registerReceiver(contentFinishedReceiver, contentFilter);
        //detects that asm settings have been saved
        IntentFilter settingsFilter = new IntentFilter(MokiASM.NOTIFICATION_SETTINGS_SAVED);
        registerReceiver(settingsSaveReceiver, settingsFilter);

        //This broadcast receiver receives the intent to take a screenshot
        IntentFilter screenShotFilter = new IntentFilter(SCREENSHOT_INTENT);
        //A category with the apps package name needs to be added to receive this intent
        screenShotFilter.addCategory(this.getPackageName());
        registerReceiver(screenShotReceiver, screenShotFilter);

        //go to the first item in the playlist
        IntentFilter homeFilter = new IntentFilter(HOME_INTENT);
        registerReceiver(homeReceiver, homeFilter);
        //detect that the server wants to stat a follow me session
        IntentFilter followMeFilter = new IntentFilter(MokiManage.FOLLOW_ME_INTENT);
        followMeFilter.addCategory(getPackageName());
        registerReceiver(supportReceiver, followMeFilter);
        refreshKiosk();

        //pausing MokiManage prevents it from aquiring wakelocks and doing processing in the background
        MMManager.getMokiManage(this).resume();
        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(contentFinishedReceiver);
        unregisterReceiver(settingsSaveReceiver);
        unregisterReceiver(screenShotReceiver);
        unregisterReceiver(homeReceiver);
        unregisterReceiver(supportReceiver);
        isPaused = true;

        //If MokiManage is pause it needs to be resumed to continue to receive notifications from the web and
        MMManager.getMokiManage(this).pause();
        for (ScreenController controller : controllers) {
            controller.controlChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contentManager = null;
        wakelock.release();
    }

    protected void configureManager() {
        contentManager = new SaverContentManager();
    }

    //this is for exiting the screen saver
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.finish();
        return true;
    }

    protected void move(int movementDirection) {
        if (!isPaused) {
            if (movementDirection == MOVE_TO_NEXT_ITEM){
                ContentObject content = contentManager.getPreviousObject(previousFragment().getScreenController().getContentObject());
                setupNewFragment(nextFragment(), previousFragment(), content);
            }
            else {
                ContentObject content = contentManager.getNextObject(nextFragment().getScreenController().getContentObject());
                setupNewFragment(previousFragment(), nextFragment(), content);
            }
            displayFragmentContent(currentFragment, movementDirection);
        }
    }

    private void setupNewFragment(PlaylistFragment needsNewContentObject, PlaylistFragment newCurrentFragment, ContentObject content) {
        setFragmentContent(needsNewContentObject, content);
        currentFragment = newCurrentFragment;
    }

    public void addNewController(ScreenController controller) {
        controllers.add(controller);
    }

    public void removeController(ScreenController controller) {
        controllers.remove(controller);
    }


    protected void displayFragmentContent(PlaylistFragment newFragment, int transitionDirection){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.visible_view, newFragment);
        if (!isPaused) {
            transaction.commit();
        }
    }

    private void initFragments() {
        PlaylistFragment prev, current, next;
        prev = new PlaylistFragment();
        current = new PlaylistFragment();
        next = new PlaylistFragment();
        prev.setFragments(current, next);
        current.setFragments(next, prev);
        next.setFragments(prev, current);
        currentFragment = current;
    }

    protected void refreshKiosk() {
        defaultInit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        FollowMeGestureDetector.sharedInstance().handleTouch(ev);
        return super.dispatchTouchEvent(ev);
    }

    protected void defaultInit() {
        if (contentManager.getContentStoreSize() > 0) {
            initFromPosition(DEFAULT_POSITION);
        }
    }

    protected void initFromPosition(int position) {
        for (ScreenController controller : controllers) {
            controller.controlChanged();
        }
        ContentObject contentObject = contentManager.getContentObject(position);
        setFragmentContent(currentFragment, contentObject);
        setFragmentContent(nextFragment(), contentManager.getNextObject(contentObject));
        setFragmentContent(previousFragment(), contentManager.getPreviousObject(contentObject));
    }
    
    private PlaylistFragment previousFragment(){
        return currentFragment.getPreviousFragment();
    }
    
    private PlaylistFragment nextFragment() {
        return currentFragment.getNextFragment();
    }

    public void contentFinished(){
        if (contentManager.getContentStoreSize() > 1 && contentManager instanceof SaverContentManager) {
            move(MOVE_TO_PREVIOUS_ITEM);
        }
    }

    private ScreenController setFragmentContent(PlaylistFragment fragment, ContentObject content){
        return fragment.setContentObject(content, this);
    }

    protected BroadcastReceiver settingsSaveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            savedStateValid = false;
            refreshKiosk();
        }
    };

    protected BroadcastReceiver contentFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            contentFinished();
        }
    };

    /**
     * The receiver that is responding to the screen shot intent
     */
    protected BroadcastReceiver screenShotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MokiManage.takeScreenShot(getWindow(), context);
        }
    };

    protected BroadcastReceiver homeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshKiosk();
        }
    };

    public ContentManager getContentManager() {
        return contentManager;
    }

    /**
     * The receiver that responds to follow me requests
     */
    protected BroadcastReceiver supportReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MokiManage.sharedInstance().openFollowMeDialog(PlaylistActivity.this, intent);
            Log.i("support requested", "requested");
        }
    };

    public boolean isCurrentFragment(PlaylistFragment fragment) {
        return fragment.equals(currentFragment);
    }
}

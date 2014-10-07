// Copyright (c) 2012 MokiMobility. All rights reserved.
package com.moki.touch.copy;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.moki.asm.MokiASM;
import com.moki.asm.views.AppSettingsFragment;
import com.moki.asm.views.AppSettingsHolderActivity;
import com.moki.asm.views.BreadCrumbFragment;
import com.moki.asm.views.EnrollmentFragment;
import com.moki.asm.views.NetworkFragment;
import com.moki.asm.views.OnSettingClickedListener;
import com.moki.views.dialogs.IDialogActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.moki.asm.ASMConstantValues.ASM_KEY_Groups;
import static com.moki.asm.ASMConstantValues.ASM_KEY_Settings;
import static com.moki.asm.ASMConstantValues.ASM_KEY_Title;
import static com.moki.asm.ASMConstantValues.ASM_KEY_Values;
import static com.moki.asm.ASMConstantValues.ASM_Pane;

public class MTAppSettingsActivity extends IDialogActivity implements OnItemSelectedListener, View.OnKeyListener, OnSettingClickedListener {

    public MokiASM asm;
    public static JSONObject settings, schema;
    public MTGroupListFragment groupListFragment;
    public JSONArray groups;
    public boolean isDestroyed = false;
    public final String TAG = "AppSettingsActivity";
    JSONArray settingsArray;
    String header;
    AppSettingsFragment appSettingsFragment;
    ArrayList<AppSettingsFragment> fragments = new ArrayList<AppSettingsFragment>();
    FragmentManager fragmentManager;
    EnrollmentFragment enrollment;
    NetworkFragment networkFragment;
    int containerid;
    boolean enrollmentShowing = false;
    boolean networkInfoShowing = false;

    FrameLayout detailsFrame;
    Activity activity;

    FragmentManager.OnBackStackChangedListener listener = new FragmentManager.OnBackStackChangedListener() {
        boolean firstRun = true;

        public void onBackStackChanged() {
            int count = fragmentManager.getBackStackEntryCount();
            Log.i("AppSettingsHolderFragment", "onBackStackChanged " + this + " " + count + ", " + firstRun + ", " + enrollmentShowing);
            if (count == 0 && !firstRun && !enrollmentShowing) {
                MokiASM.sharedInstance().pushSettings(null);
                finish();
            }
            firstRun = false;
        }
    };
    boolean listenForBackstackChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OnCreate 83");
        isDestroyed = false;
        fragmentManager = getFragmentManager();
        Resources resources = getResources();

        Log.i(TAG, "OnCreate 88");
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int settingsView = resources.getIdentifier("mt_settings_view", "layout", getPackageName());
            setContentView(settingsView);
            int options = resources.getIdentifier("options", "id", getPackageName());
            int details = resources.getIdentifier("details", "id", getPackageName());
            int crumbs = resources.getIdentifier("breadcrumbs", "id", getPackageName());
            groupListFragment = (MTGroupListFragment) fragmentManager.findFragmentById(options);
            BreadCrumbFragment crumbFragment = (BreadCrumbFragment) fragmentManager.findFragmentById(crumbs);
            groupListFragment.setBreadCrumbFragment(crumbFragment);
            detailsFrame = (FrameLayout) findViewById(details);
            containerid = detailsFrame.getId();

        } else {
            int settingsView = resources.getIdentifier("mt_settings_view_portrait", "layout", getPackageName());
            int options = resources.getIdentifier("options", "id", getPackageName());
            setContentView(settingsView);
            groupListFragment = (MTGroupListFragment) fragmentManager.findFragmentById(options);
        }

        Log.i(TAG, "OnCreate 108");

        int mainId = resources.getIdentifier("mainview", "id", getPackageName());
        main = (RelativeLayout) findViewById(mainId);
        groupListFragment.setRetainInstance(true);
        try {
            asm = MokiASM.sharedInstance("", this);
            initSettings();

        } catch (Exception e) {
            Log.e(MTAppSettingsActivity.class.getSimpleName(), e.getMessage(), e);
        }
        FragmentManager.enableDebugLogging(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setBackstackChangedListener();
        }

        groups = schema.optJSONArray(ASM_KEY_Groups);
        buildViews();

        registerReceiver(finishedSaving, new IntentFilter(MokiASM.NOTIFICATION_SETTINGS_SAVED));
    }

    private void initSettings() throws JSONException {
        settings = asm.getSettings().optJSONObject(ASM_KEY_Values);
        schema = asm.getVisibleSchemaSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(finishedSaving);
    }

    public void setBackstackChangedListener() {
        listenForBackstackChange = true;
    }

    protected void buildViews() {
        groupListFragment.setOnItemSelectedlistener(this);
        groupListFragment.setSchema(schema);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(MTAppSettingsActivity.class.getSimpleName(), "onItemSelected");
        try {
            updateHolderFragment(position);
        } catch (JSONException e) {
            Log.e(MTAppSettingsActivity.class.getSimpleName(), e.getMessage(), e);
        }
    }

    protected void updateHolderFragment(int position) throws JSONException {
        if (!isDestroyed) {
            JSONObject group = groups.getJSONObject(position);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (this.getCurrentFocus() != null) {
                    InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                popToFirst();
                setSettingsArray(group.optJSONArray(ASM_KEY_Settings));
                setHeaderText(group.optString(ASM_KEY_Title));
                showSettings();

            } else {
                Intent intent = new Intent(this, AppSettingsHolderActivity.class);
                Log.i(MTAppSettingsActivity.class.getSimpleName(), "intent is " + intent);
                intent.putExtra("group", group.toString());
                intent.putExtra("settings", settings.toString());
                startActivityForResult(intent, 0);
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    protected void updateCurrentView() throws JSONException {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (groupListFragment.schema == null && schema != null) {
                groupListFragment.setSchema(schema);
            }
            for (int i = 0; i < groupListFragment.groupNames.length; i++) {
                String groupTitle = groupListFragment.groupNames[i];
                String fragmentTitle = getHeaderText();
                if (groupTitle.equals(fragmentTitle)) {
                    updateHolderFragment(i);
                    break;
                } else if ((groupTitle.equals("Unenroll") || groupTitle.equals("Enroll")) && fragmentTitle.equals("Enrollment Details")) {
                    showEnrollment();
                    break;
                }
            }
        }
    }

    private BroadcastReceiver finishedSaving = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                initSettings();
                updateCurrentView();
            } catch (JSONException e) {
                Log.e(TAG, "unable to update holder fragment with new settings");
            }
        }
    };


    public void showEnrollment() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showLandscapeEnrollment();
        } else {
            Intent intent = new Intent(this, AppSettingsHolderActivity.class);
            Log.i(MTAppSettingsActivity.class.getSimpleName(), "intent is " + intent);
            intent.putExtra("enrollment", true);
            startActivity(intent);
        }
    }

    public void showNetworkFragment() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showLandscapeNetworkInfo();
        } else {
            Intent intent = new Intent(this, AppSettingsHolderActivity.class);
            Log.i(MTAppSettingsActivity.class.getSimpleName(), "intent is " + intent);
            intent.putExtra("networkinfo", true);
            startActivity(intent);
        }
    }


    public void popToFirst() {
        Log.i("AppSettingsHolderFragment", "popToFirst Start " + this);
        if (fragments.size() > 0) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            for (int i = fragments.size() - 1; i > 0; i--) {
                removeFragment(fragmentManager, fragmentTransaction, i);
            }
            fragmentTransaction.commitAllowingStateLoss();
            appSettingsFragment = fragments.get(0);
        }
        Log.i("AppSettingsHolderFragment", "popToFirst End " + this);
    }

    public void clearFragments() {
        Log.i("AppSettingsHolderFragment", "clearFragments Start " + this);
        if (fragments.size() > 0) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            for (int i = fragments.size() - 1; i > -1; i--) {
                removeFragment(fragmentManager, fragmentTransaction, i);
            }
            fragmentTransaction.commitAllowingStateLoss();
            appSettingsFragment = null;
        }
        Log.i("AppSettingsHolderFragment", "clearFragments End " + this);
    }

    protected void removeFragment(FragmentManager fragmentManager, FragmentTransaction fragmentTransaction, int i) {
        AppSettingsFragment fragment = fragments.get(i);
        fragmentTransaction.remove(fragment);
        fragmentManager.popBackStackImmediate(fragment.getTransactionId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragments.remove(i);
    }

    public void showLandscapeEnrollment() {
        Log.i("AppSettingsHolderFragment", "showEnrollment " + this);
        enrollmentShowing = true;
        setHeaderText("Enrollment Details");
        clearFragments();
        if (enrollment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            enrollment = new EnrollmentFragment();
            fragmentTransaction.replace(containerid, enrollment);
            fragmentTransaction.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
            networkFragment = null;
        }

    }

    public void showLandscapeNetworkInfo() {
        Log.i("AppSettingsHolderFragment", "showNetworkInfo " + this);
        networkInfoShowing = true;
        setHeaderText("Network Info");
        clearFragments();
        if (networkFragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            networkFragment = new NetworkFragment();
            fragmentTransaction.replace(containerid, networkFragment);
            fragmentTransaction.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
            enrollment = null;
        }

    }

    public void setSettingsArray(JSONArray settingsArray) {
        this.settingsArray = settingsArray;
        if (appSettingsFragment != null)
            appSettingsFragment.setSettingsArray(settingsArray);
    }

    public void setSettings(JSONObject settings) {
        this.settings = settings;
        if (appSettingsFragment != null)
            appSettingsFragment.setSettings(settings);
    }

    public void setHeaderText(String header) {
        this.header = header;
    }

    public String getHeaderText() {
        return header;
    }

    public void settingClicked(String type, String title, JSONArray settingsArray, JSONObject settings, Activity context) {
        Log.i("AppSettingsHolderFragment", "settings clicked");
        if (type != null && type.equals(ASM_Pane)) {
            pushSettings(title, settingsArray, settings);
        }
    }

    protected void pushSettings(String title, JSONArray settingsArray, JSONObject settings) {
        int count = fragmentManager.getBackStackEntryCount();
        Log.i(MTAppSettingsActivity.class.getSimpleName(), "backstack count is " + count);
        if (settings != null && settingsArray != null) {
            Log.i(MTAppSettingsActivity.class.getSimpleName(), "pushSettings");
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (enrollment != null) {
                enrollmentShowing = false;
                fragmentTransaction.remove(enrollment);
                enrollment = null;
                fragmentTransaction.commitAllowingStateLoss();
                fragmentTransaction = fragmentManager.beginTransaction();
            }
            if (networkFragment != null) {
                networkInfoShowing = false;
                fragmentTransaction.remove(networkFragment);
                networkFragment = null;
                fragmentTransaction.commitAllowingStateLoss();
                fragmentTransaction = fragmentManager.beginTransaction();
            }
            appSettingsFragment = new AppSettingsFragment();
            appSettingsFragment.setOnSettingClickedListener(this);
            appSettingsFragment.setSettings(settings);
            appSettingsFragment.setSettingsArray(settingsArray);
            fragmentTransaction.replace(containerid, appSettingsFragment, null).addToBackStack(null);

            fragments.add(appSettingsFragment);

            int transactionId = fragmentTransaction.commit();

            appSettingsFragment.setTransactionId(transactionId);
            fragmentManager.executePendingTransactions();
        }

    }

    public void showSettings() {
        Log.i(MTAppSettingsActivity.class.getSimpleName(), "showSettings");
        if (appSettingsFragment == null) {
            pushSettings(header, settingsArray, settings);
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            Log.i(MTAppSettingsActivity.class.getSimpleName(), "on key: fragment size == " + fragments.size() + " and action == " + keyCode);
            if (settings != null) {
                Intent i = new Intent();
                i.putExtra("settings", settings.toString());
                setResult(1, i);
            }
            MokiASM.sharedInstance().pushSettings(null);
            finish();
            activity = null;
        }
        return true;
    }

}
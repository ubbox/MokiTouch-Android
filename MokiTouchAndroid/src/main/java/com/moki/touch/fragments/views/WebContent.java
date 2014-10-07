package com.moki.touch.fragments.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moki.touch.R;
import com.moki.touch.activities.HomePlaylist;
import com.moki.touch.activities.PlaylistActivity;
import com.moki.touch.base.BaseScreenController;
import com.moki.touch.cache.Downloader;
import com.moki.touch.interfaces.DownloadListener;
import com.moki.touch.models.ContentObject;
import com.moki.touch.util.SettingsUtil;
import com.moki.touch.util.UrlUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Copyright (C) 2014 Moki Mobility Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * <p/>
 * You may only use this file in compliance with the license
 * <p/>
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@SuppressWarnings("ConstantConditions")
public class WebContent extends BaseScreenController implements DownloadListener {

    public static final int TO_MILLISECONDS = 1000;
    public static final String DOMAIN_URL = SettingsUtil.DOMAIN_URL;
    public static final String LOGO_LOCATION = "/MokiMDM/CachedItems/";
    public static final String LOGO_DOWNLOAD_COMPLETE = "LogoDownloadComplete";
    private static final String TAG = "WebContent";
    public String userAgent;
    private WebView webView;
    private LinearLayout rootView;
    private String homeUrl;
    private Handler completionHandler = new Handler();
    private boolean showBrowserBar;
    private boolean showNavigationButtons;
    private boolean showNavigationBar;
    private boolean showEndSessionButton;
    private boolean showProgressBar;
    private boolean showLogo;
    private String uploadLogoUrl;
    private EditText addressBar;
    private ArrayList<String> allowedUrls;
    private String styleColor;
    private ProgressBar progressBar;
    private Animation fadeOut;
    private RelativeLayout browserBar;
    private String customAlertMessage;

    public WebContent(Context context) {
        super(context);
        fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                progressBar.setVisibility(View.GONE);
            }
        });
        customAlertMessage = SettingsUtil.getCustomAlertMessage();
        userAgent = SettingsUtil.getUserAgent();
    }

    @Override
    public void startContent() {
        loadHome();
        setTimer();
    }

    @Override
    public View getView() {
        View emptyView = super.getView();
        if (emptyView != null) {
            return emptyView;
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (LinearLayout) inflater.inflate(R.layout.webcontentlayout, null);
        webView = (WebView) rootView.findViewById(R.id.webcontentview);
        configureWebView(context);
        browserBar = (RelativeLayout) rootView.findViewById(R.id.barlayout);
        // this is added so no swipe actions are detected on the browser bar
        browserBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        progressBar = (ProgressBar) rootView.findViewById(R.id.web_progress_bar);
        progressBar.setVisibility(View.GONE);
        rootView.findViewById(R.id.barlayout).setBackgroundColor(Color.parseColor(styleColor));
        configureNavigationBar();

        if (!isHomeMode) {
            // this listener is being added to ensure that touch events bubble up to the correct receivers.
            disableWebView();
        }

        if (!showBrowserBar) {
            browserBar.setVisibility(View.GONE);
        }

        boolean listenForLogo = setLogo();
        if (listenForLogo) {
            registerForLogoDownload();
        }

        return rootView;
    }

    private void disableWebView() {
        webView.setLongClickable(false);
        webView.setClickable(false);
        webView.setFocusableInTouchMode(false);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    private void configureWebView(Context context) {
        if (!userAgent.equals("default")) {
            webView.getSettings().setUserAgentString(userAgent);
        }
        webView.getSettings().setJavaScriptEnabled(true);
        // Set cache size to 32 mb by default. should be more than enough
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 32);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        String appCachePath = context.getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.setWebViewClient(new CustomClient());
        if (showProgressBar) {
            webView.setWebChromeClient(new CustomChromeClient());
        }
    }

    private boolean setLogo() {
        final ImageView logoView = (ImageView) rootView.findViewById(R.id.logo);
        boolean shouldListenForLogo = false;
        if (SettingsUtil.getUploadLogo() != null && !SettingsUtil.getUploadLogo().isEmpty()) {
            final Bitmap bitmapLogo = getLogoBitmap();
            if (bitmapLogo == null) {
                shouldListenForLogo = true;
            } else {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        logoView.setImageBitmap(bitmapLogo);
                    }
                }.execute();
            }
        } else {
            logoView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mt_web_logo));
        }
        return shouldListenForLogo;
    }

    private Bitmap getLogoBitmap() {
        ContentObject logoObject = new ContentObject(uploadLogoUrl);
        return BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + LOGO_LOCATION + logoObject.getContentFileName());
    }

    @Override
    public void controlChanged() {
        cleanupHistory();
        webView = null;
        rootView = null;
        homeUrl = null;
        completionHandler = null;
        allowedUrls = null;
        Downloader.getInstance().removeListener(this);
        super.controlChanged();
    }

    private void configureNavigationBar() {

        Button back = (Button) rootView.findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goBack();
            }
        });
        back.setBackgroundDrawable(createButtonStateList(R.drawable.mt_btn_touch_prev, R.drawable.mt_btn_prev));
        Button forward = (Button) rootView.findViewById(R.id.forwardButton);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goForward();
            }
        });
        forward.setBackgroundDrawable(createButtonStateList(R.drawable.mt_btn_touch_next, R.drawable.mt_btn_next));
        Button home = (Button) rootView.findViewById(R.id.homeButton);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endSession();
            }
        });
        home.setBackgroundDrawable(createButtonStateList(R.drawable.mt_btn_touch_home, R.drawable.mt_btn_home));
        Button refresh = (Button) rootView.findViewById(R.id.refreshButton);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
        refresh.setBackgroundDrawable(createButtonStateList(R.drawable.mt_btn_touch_refresh, R.drawable.mt_btn_refresh));
        Button endSession = (Button) rootView.findViewById(R.id.endSession);
        endSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupEndSessionAlertDialog();
                endSession();
            }
        });
        endSession.setBackgroundDrawable(createButtonStateList(R.drawable.mt_btn_end, R.drawable.mt_btn_end));
        addressBar = (EditText) rootView.findViewById(R.id.urlBar);
        addressBar.setText(homeUrl);
        addressBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String url = addressBar.getText().toString();
                url = UrlUtil.addHttp(url);
                webView.loadUrl(url);
                return true;
            }
        });
        addressBar.setLongClickable(false);
        if (!isHomeMode) {
            addressBar.setClickable(false);
            addressBar.setFocusable(false);
            addressBar.setFocusableInTouchMode(false);
            back.setClickable(false);
            forward.setClickable(false);
            home.setClickable(false);
            refresh.setClickable(false);
            endSession.setClickable(false);
        }
        ImageView logo = (ImageView) rootView.findViewById(R.id.logo);

        boolean allNavigationItemsDisabled = true;

        if (!showNavigationButtons) {
            back.setVisibility(View.GONE);
            forward.setVisibility(View.GONE);
            refresh.setVisibility(View.GONE);
            home.setVisibility(View.GONE);
        } else {
            allNavigationItemsDisabled = false;
        }

        if (!showLogo) {
            logo.setVisibility(View.GONE);
        } else {
            allNavigationItemsDisabled = false;
        }

        if (!showEndSessionButton) {
            endSession.setVisibility(View.GONE);
        } else {
            allNavigationItemsDisabled = false;
        }

        if (!showNavigationBar) {
            addressBar.setVisibility(View.GONE);
        } else {
            allNavigationItemsDisabled = false;
        }

        if (allNavigationItemsDisabled) {
            browserBar.setVisibility(View.GONE);
        }
    }

    private void endSession() {
        Intent homeIntent = new Intent();
        homeIntent.setAction(PlaylistActivity.HOME_INTENT);
        context.sendBroadcast(homeIntent);
    }

    private void setupEndSessionAlertDialog() {
        cleanupHistory();
        loadHome();
    }

    public void cleanupHistory() {
        try {
            if (webView != null) {
                Log.i(TAG, "clean up history");
                webView.clearHistory();
                webView.clearFormData();
                webView.clearCache(true);
                webView.clearSslPreferences();
                if (context != null) {
                    CookieSyncManager.createInstance(context);
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.removeAllCookie();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage(), e);
        }
    }

    private StateListDrawable createButtonStateList(int pressedState, int nonPressedState) {
        StateListDrawable stateList = new StateListDrawable();
        stateList.addState(new int[]{android.R.attr.state_pressed}, context.getResources().getDrawable(pressedState));
        stateList.addState(new int[]{-android.R.attr.state_pressed}, context.getResources().getDrawable(nonPressedState));

        return stateList;
    }

    public void loadHome() {
        webView.loadUrl(UrlUtil.addHttp(homeUrl));
    }

    private void setTimer() {
        int duration = Integer.parseInt(mContentObject.getDuration().isEmpty() ? "10" : mContentObject.getDuration()) * TO_MILLISECONDS;
        if (!isHomeMode) {
            completionHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    contentFinished();
                }
            }, duration);
        }
    }

    private boolean isAllowedUrl(String url) {
        boolean allowed = false;

        //if there are no allowed urls then we are allowed to continue anywhere.
        if (allowedUrls == null || allowedUrls.size() <= 0 || url.equals(homeUrl)) {
            allowed = true;
        }

        if (!allowed) {
            try {
                URI urlToCheck = new URI(UrlUtil.addHttp(url));
                for (String allowedUrl : allowedUrls) {
                    JSONObject urlSettings = new JSONObject(allowedUrl);
                    URI anAllowedUrl = new URI(UrlUtil.addHttp(urlSettings.optString(DOMAIN_URL)));
                    if (compareUrls(urlToCheck.getHost(), anAllowedUrl.getHost())) {
                        allowed = true;
                        break;
                    }
                }
            } catch (JSONException e) {
                Log.e(getClass().getSimpleName(), "Url setting invalid, could not convert to JSONObject");
            } catch (URISyntaxException e) {
                Log.e(getClass().getSimpleName(), "Could not create URI from string");
            }
        }
        return allowed;
    }

    private boolean compareUrls(String check, String allowed) {
        boolean comparison = false;

        if (check == null || allowed == null || (check + allowed).isEmpty()) {
            return comparison;
        }

        try {
            URI checkUri = URI.create(removeWWW(removeEmptyPath(check)));
            URI allowedUri = URI.create(removeWWW(removeEmptyPath(allowed)));
            if (checkUri.equals(allowedUri)) {
                comparison = true;
            }
        } catch (IllegalArgumentException e) {
            Log.e(getClass().getSimpleName(), "url contained an illegal character");
        }

        return comparison;
    }

    private String removeWWW(String url) {
        String returnString = url;
        if (url != null && url.length() > 0) {
            if (url.contains("/www.")) {
                returnString = url.replace("/www.", "/");
            } else if (url.contains("www.")) {
                returnString = url.replace("www.", "");
            }
        }
        return returnString;
    }

    private String removeEmptyPath(String url) {
        String returnString = url;
        if (url != null && url.length() > 0 && url.charAt(url.length() - 1) == '/') {
            returnString = url.substring(0, url.length() - 1);
        }
        return returnString;
    }

    private class CustomClient extends WebViewClient {

        Context context;

        public CustomClient() {
            this.context = HomePlaylist.getContext();
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean stopLoading = false;

            if (isHomeMode) {
                if (!isAllowedUrl(url)) {
                    stopLoading = true;
                }
            }

            if (stopLoading) {
                showUnallowedSitesWarning();
            }

            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (!isAllowedUrl(url)) {
                webView.stopLoading();
                showUnallowedSitesWarning();
            }
        }

        private void showUnallowedSitesWarning() {
            String alertMessage = customAlertMessage;
            if (customAlertMessage == null || customAlertMessage.isEmpty()) {
                alertMessage = "Site not allowed";
            }
            Toast toast = Toast.makeText(context, alertMessage, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            addressBar.setText(url);
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {
            showAuthenticationDialog(handler);
        }

        private void showAuthenticationDialog(final HttpAuthHandler handler) {
            Dialog passwordDialog;
            if (context != null) {
                final EditText passwordText = new EditText(context);
                passwordText.setHint("password");

                final EditText usernameText = new EditText(context);
                usernameText.setHint("username");

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(0, 0, 0, 10);

                usernameText.setLayoutParams(layoutParams);
                passwordText.setLayoutParams(layoutParams);

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(usernameText);
                layout.addView(passwordText);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        handler.cancel();
                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (passwordText.getText() != null && usernameText.getText() != null) {
                            handler.proceed(usernameText.getText().toString(), passwordText.getText().toString());
                        }
                    }
                }).setTitle("Basic Authentication").setView(layout);
                passwordDialog = builder.create();
                passwordDialog.show();
            } else {
                Log.i("HttpAuth", "context is null");
            }
        }
    }

    private class CustomChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            if (newProgress > 0 && newProgress < 100) {

                progressBar.setVisibility(View.VISIBLE);

            } else if (newProgress == 100) {

                progressBar.startAnimation(fadeOut);
            }

            progressBar.setProgress(newProgress);
        }
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = UrlUtil.addHttp(homeUrl);
    }

    public void setAllowedUrls(ArrayList<String> allowedUrls) {
        this.allowedUrls = allowedUrls;
    }

    public void setShowBrowserBar(boolean showBrowserBar) {
        this.showBrowserBar = showBrowserBar;
    }

    public void setBarVisibility(boolean showNavigationButtons, boolean showEndSessionButton, boolean showNavigationBar) {
        this.showNavigationButtons = showNavigationButtons;
        this.showEndSessionButton = showEndSessionButton;
        this.showNavigationBar = showNavigationBar;
    }

    public void setStyleColor(String styleColor) {
        this.styleColor = styleColor;
    }

    public void setShowProgressBar(boolean showProgressBar) {
        this.showProgressBar = showProgressBar;
    }

    public void setShowLogo(boolean showLogo) {
        this.showLogo = showLogo;
    }

    public void setUploadLogoUrl(String uploadLogoUrl) {
        this.uploadLogoUrl = uploadLogoUrl;
    }

    private void registerForLogoDownload() {
        Downloader.getInstance().registerListener(this);
    }

    @Override
    public boolean downloadFinished(String url) {
        if (url.equals(SettingsUtil.getUploadLogo())) {
            setLogo();
            return true;
        }
        return false;
    }
}

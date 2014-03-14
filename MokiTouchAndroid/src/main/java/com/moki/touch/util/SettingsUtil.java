package com.moki.touch.util;

import com.moki.manage.api.MokiManage;

import org.json.JSONArray;
import org.json.JSONObject;

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
 *
 * SettingsUtil manages the interface between the settings held by MokiManage and the individual values for any particular setting.
 * Settings util holds the key for a particular setting then accesses the value by calling {@link com.moki.manage.api.MokiManage}
 */
public class SettingsUtil {

    public static final String VALUES = "Values";
    public static final String ALLOWED_URLS = "domainUrlList";
    public static final String ADMIN_PASSWORD = "adminPassword";
    public static final String GOOGLE_ANALYTICS_ID = "googleAnalyticsId";
    public static final String REFRESH_CACHE = "refreshCache";
    public static final String DEVICE_VOLUME = "deviceVolume";
    public static final String SHOW_NAV_BUTTONS = "showNavButtons";
    public static final String IDLE_TIME = "idleTime";
    public static final String UNALLOWED_SITES_ALERT = "forbiddenSitesAlert";
    public static final String SHOW_LOGO = "showLogo";
    public static final String SHOW_END_SESSION = "showEndSession";
    public static final String SHOW_NAVIGATION_BAR = "showNavigationBar";
    public static final String SHOW_PROGRESS = "showProgress";
    public static final String ENABLE_SWIPING = "enableSwiping";
    public static final String IDLE_TIME_ACTION = "idleTimeAction";
    public static final String SHOW_LINK_BAR = "showBottomBar";
    public static final String BAR_COLOR = "barColor";
    public static final String UPLOAD_LOGO = "uploadLogo";
    public static final String HOME_CONTENT_KEY = "contentItems";
    public static final String SAVER_CONTENT_KEY = "screensaverPlaylistItems";
    public static final String CONTENT = "content";
    public static final String DURATION = "duration";
    public static final String CONTENT_TITLE = "contentTitle";
    public static final String DOMAIN_URL = "domainUrl";
    public static final String CUSTOM_ALERT_MESSAGE = "forbiddenAlertMessage";
    public static final String USER_AGENT = "userAgent";
    private static final String DEFAULT_THEME_COLOR = "#4d4d4d";

    public static String getAdminPassword() {
        return getMokiManage().stringForKey(ADMIN_PASSWORD);
    }

    public static String getGoogleAnalyticsId() {
            return getMokiManage().stringForKey(GOOGLE_ANALYTICS_ID);
    }

    public static boolean refreshCacheOnPush() {
        return getMokiManage().boolForKey(REFRESH_CACHE);
    }

    public static int getDeviceVolume() {
        return Integer.parseInt(getMokiManage().stringForKey(DEVICE_VOLUME));
    }

    public static boolean showNavigationButtons() {
        return getMokiManage().boolForKey(SHOW_NAV_BUTTONS);
    }

    public static int resetWhenIdleFor() {
        return Integer.parseInt(getMokiManage().stringForKey(IDLE_TIME));
    }

    public static String resetAction() {
        return getMokiManage().stringForKey(IDLE_TIME_ACTION);
    }

    public static boolean alertForUnallowedSites() {
        return getMokiManage().boolForKey(UNALLOWED_SITES_ALERT);
    }

    public static boolean showLogo() {
        return getMokiManage().boolForKey(SHOW_LOGO);
    }

    public static boolean showEndSessionButtion() {
        return getMokiManage().boolForKey(SHOW_END_SESSION);
    }

    public static boolean showProgress() {
        return getMokiManage().boolForKey(SHOW_PROGRESS);
    }

    public static boolean enableSwiping() {
        return getMokiManage().boolForKey(ENABLE_SWIPING);
    }

    public static boolean showNavigationBar() {
        return getMokiManage().boolForKey(SHOW_NAVIGATION_BAR);
    }

    public static MokiManage getMokiManage() {
        return MMManager.getMokiManage(null);
    }

    public static JSONObject getSettingsValues(){
        return getMokiManage().getSettings().optJSONObject(VALUES);
    }

    public static boolean showBrowserBar() {
        return showEndSessionButtion() || showLogo() || showNavigationButtons() || showNavigationBar();
    }

    public static boolean showLinkBar() {
        return getMokiManage().boolForKey(SHOW_LINK_BAR);
    }

    public static String getThemeColor() {
        String themeString = DEFAULT_THEME_COLOR;
        String settingsTheme = getMokiManage().stringForKey(BAR_COLOR);

        if (settingsTheme != null && !settingsTheme.isEmpty()) {
            if (settingsTheme.charAt(0) != '#'){
                settingsTheme  = "#" + settingsTheme;
            }

            if (ThemeUtil.isValidColorHex(settingsTheme)) {
                themeString = settingsTheme;
            }
        }

        return themeString;
    }

    public static String getUploadLogo() {
        return getMokiManage().stringForKey(UPLOAD_LOGO);
    }

    public static String getCustomAlertMessage() {
        return getMokiManage().stringForKey(CUSTOM_ALERT_MESSAGE);
    }

    public static ArrayList<String> getAllowedUrls() {
        ArrayList<String> urls = new ArrayList<String>();
        JSONArray settingsUrls = getMokiManage().arrayForKey(ALLOWED_URLS);
        if (settingsUrls != null) {
            for (int i = 0; i < settingsUrls.length(); i++) {
                String settingUrl = settingsUrls.optString(i);
                urls.add(settingUrl);
            }
        }
        return urls;
    }

    public static String getUserAgent() {
        return getMokiManage().stringForKey(USER_AGENT);
    }
}

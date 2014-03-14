package com.moki.touch.cache;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.moki.asm.MokiASM;
import com.moki.touch.models.ContentObject;
import com.moki.touch.util.SettingsUtil;
import com.moki.touch.util.management.ContentManager;
import com.moki.touch.util.management.HomeContentManager;
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
 *
 * The CacheReceiver catches the MokiASM {@link com.moki.asm.MokiASM#NOTIFICATION_PULL_FINISHED}, {@link com.moki.asm.MokiASM#NOTIFICATION_PUSH_FINISHED}, and {@link com.moki.asm.MokiASM#NOTIFICATION_SETTINGS_SAVED}
 * intent broadcasts and updates the apps cache accordingly.
 */
public class CacheReceiver extends BroadcastReceiver {

    @SuppressWarnings("ConstantConditions") // this receiver only catches an intent when it has an action of type FINISHED_PUSH || FINISHED_SAVE || FINISHED_PULL
    @Override
    public void onReceive(Context context, Intent intent) {

        cacheUploadLogo();

        handleCacheRefresh(intent);
    }

    private void cacheUploadLogo() {
        if (SettingsUtil.getUploadLogo() != null && !SettingsUtil.getUploadLogo().isEmpty()){
            ContentObject logo = new ContentObject(SettingsUtil.getUploadLogo());
            Downloader.getInstance().downloadContent(logo, ContentManager.MOKI_STORAGE_LOCATION);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void handleCacheRefresh(Intent intent) {
        boolean refresh = false;
        if (SettingsUtil.refreshCacheOnPush() && intent.getAction().equals(MokiASM.NOTIFICATION_PUSH_FINISHED)) {
            refresh = true;
        }

        HomeContentManager homeContentManager = new HomeContentManager();
        if (homeContentManager.getContentStoreSize() > 0) {
            homeContentManager.deleteInvalidCache();
            cacheObjects(homeContentManager, homeContentManager.getContentLocation(), refresh);
        }

        SaverContentManager saverContentManager = new SaverContentManager();
        if (saverContentManager.getContentStoreSize() > 0) {
            saverContentManager.deleteInvalidCache();
            cacheObjects(saverContentManager, saverContentManager.getContentLocation(), refresh);
        }

    }

    private void cacheObjects(ContentManager contentManager, String location, boolean shouldRefresh) {
        ArrayList<ContentObject> downloads = contentManager.getAllContentObjects();
        Downloader downloader = Downloader.getInstance();
        for (ContentObject download : downloads) {

            if (!download.contentIsImage() && !download.contentIsVideo()){
                continue;
            }

            if (shouldRefresh){
                downloader.downloadContent(download, location);
            }
            else if (!contentManager.contentIsCached(download.getContentFileName())) {
                downloader.downloadContent(download, location);
            }
        }
    }
}
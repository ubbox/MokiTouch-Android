package com.moki.touch.fragments.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.view.View;

import com.moki.touch.base.BaseScreenController;
import com.moki.touch.models.ContentObject;
import com.moki.touch.interfaces.DownloadListener;
import com.moki.touch.cache.Downloader;


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
public class ContentView extends BaseScreenController implements DownloadListener {

    private ProgressDialog dialog;
    protected String contentLocation;
    protected boolean waitingOnContent = false;

    public ContentView(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public void controlChanged() {
        super.controlChanged();
    }

    @Override
    public boolean downloadFinished(String url) {
        boolean handlingNotification = false;
        if (url.equals(getContentObject().getUrl())) {
            handlingNotification = true;
            setContentLocation();
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        waitingOnContent = false;
        return handlingNotification;
    }

    @Override
    public void setContent(ContentObject viewContent) {
        super.setContent(viewContent);
        if (!contentManager.contentIsCached(viewContent.getContentFileName()) || Downloader.getInstance().isDownloading(mContentObject)) {
            Downloader downloader = Downloader.getInstance();
            downloader.registerListener(this);
            downloader.downloadContent(mContentObject, contentManager.getContentLocation());
            waitingOnContent = true;
            dialog = new ProgressDialog(context);
            dialog.show();
        }
        else {
            // content is cached
            setContentLocation();
        }
    }

    @Override
    public void startContent() {

    }

    private void setContentLocation() {
        contentLocation = Environment.getExternalStorageDirectory() + contentManager.getContentLocation() + mContentObject.getContentFileName();
    }
}
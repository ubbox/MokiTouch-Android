package com.moki.touch.fragments.views;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.moki.touch.cache.Downloader;
import com.moki.touch.util.SettingsUtil;

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
public class VideoContent extends ContentView {

    private VideoView videoPane;

    public VideoContent(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View emptyView = super.getView();
        if (emptyView != null) {
            return emptyView;
        }
        videoPane = new VideoView(context);
        videoPane.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (!waitingOnContent){
            setupVideo();
        }
        return videoPane;
    }

    private void setupVideo() {
        videoPane.setVideoURI(Uri.parse(contentLocation));
        videoPane.setOnCompletionListener(videoComplete);
        videoPane.setOnPreparedListener(videoPrepared);
    }

    @Override
    public boolean downloadFinished(String url) {
        boolean handleNotification = super.downloadFinished(url);
        if (handleNotification) {
            setupVideo();
        }
        return handleNotification;
    }

    @Override
    public void controlChanged() {
        super.controlChanged();
    }

    private MediaPlayer.OnCompletionListener videoComplete = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (isHomeMode || contentManager.getContentStoreSize() == 1) {
                videoPane.seekTo(0);
                videoPane.start();
            }
            else {
                contentFinished();
            }
        }
    };

    private MediaPlayer.OnPreparedListener videoPrepared = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }
    };


}

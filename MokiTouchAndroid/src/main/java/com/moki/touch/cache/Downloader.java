package com.moki.touch.cache;

import com.moki.touch.models.ContentObject;
import com.moki.touch.interfaces.DownloadListener;

import java.util.ArrayList;
import java.util.HashMap;

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
public class Downloader {

    private ArrayList<DownloadListener> awaitingDownloads;
    private HashMap<String, DownloadThread> currentDownloads;
    private static Object lock = new Object();
    private static Downloader instance;

    public static Downloader getInstance() {
        if (instance == null){
            synchronized (lock) {
                if (instance == null) {
                    instance = new Downloader();
                }
            }
        }
        return instance;
    }

    public Downloader() {
        awaitingDownloads = new ArrayList<DownloadListener>();
        currentDownloads = new HashMap<String, DownloadThread>();
    }

    private void cleanUp() {
        awaitingDownloads = null;
        currentDownloads = null;
        instance = null;
    }

    public void downloadContent(ContentObject contentObject, String filePath) {
        if (!currentDownloads.containsKey(contentObject.getContentFileName())){
            currentDownloads.put(contentObject.getContentFileName(), new DownloadThread(contentObject, this, filePath));
        }
    }

    public void downloadComplete(ContentObject contentObject) {
        DownloadListener listenerRemoval = null;
        for(DownloadListener listener : awaitingDownloads) {
            if (listener.downloadFinished(contentObject.getUrl())) {
                listenerRemoval = listener;
            }
        }
        currentDownloads.remove(contentObject.getContentFileName());
        removeListener(listenerRemoval);
        if (currentDownloads.size() == 0) {
            cleanUp();
        }
    }

    public boolean isDownloading(ContentObject contentObject) {
        boolean isDownloading = false;
        if (currentDownloads.keySet().contains(contentObject.getContentFileName())) {
            isDownloading = true;
        }
        return isDownloading;
    }

    public void registerListener(DownloadListener listener) {
        awaitingDownloads.add(listener);
    }

    public void removeListener(DownloadListener listener) {
        awaitingDownloads.remove(listener);
    }

}
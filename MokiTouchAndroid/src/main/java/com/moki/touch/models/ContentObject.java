package com.moki.touch.models;

import com.moki.touch.util.SettingsUtil;
import com.moki.touch.util.UrlUtil;

import org.json.JSONObject;
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
public class ContentObject {

    public static final String CONTENT_URL = SettingsUtil.CONTENT;
    public static final String CONTENT_DURATION = SettingsUtil.DURATION;
    public static final String TITLE = SettingsUtil.CONTENT_TITLE;
    private JSONObject contentJson;
    private String url;
    private String duration;
    private String title;
    private int position;

    public ContentObject(String url) {
        this.url = url;
    }

    public ContentObject(JSONObject json) {
        contentJson = json;
        url = json.optString(CONTENT_URL);
        duration = json.optString(CONTENT_DURATION);
        title = json.optString(TITLE);
    }

    public String getContent() {
        return contentJson.toString();
    }

    public boolean contentIsVideo() {
        return UrlUtil.isContentVideo(url);
    }

    public boolean contentIsImage() {
        return UrlUtil.isContentImage(url);
    }

    public String getUrl() {
        return url;
    }

    public String getContentFileName() {
        return UrlUtil.hashUrl(url);
    }

    public String getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

package com.moki.touch.util.management;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;


import com.moki.touch.models.ContentObject;
import com.moki.touch.util.MMManager;
import com.moki.touch.util.SettingsUtil;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

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
 * This is an abstract class that provides methods to obtain content objects, obtain the position of content objects in the settings
 * get the next or previous content object base upon position and other helpful things to do with content objects.
 *
 * To use this class you need to create a HomeContentManager or a SaverContentManager depending on what content objects you need to receive
 * or manipulate
 */
public abstract class ContentManager {

    public static final String MOKI_STORAGE_LOCATION = "/MokiMDM/CachedItems/";
    public static final String PREVIOUS = "Previous";
    public static final String NEXT = "Next";
    protected String CONTENT_KEY;
    protected String CONTENT_LOCATION;

    /**
     * Get the next content object in the list
     * @param contentObject A content object for reference
     * @return The content object that is before the content object passed in
     */
    public ContentObject getPreviousObject(ContentObject contentObject) {
        return getContentObject(contentObject.getPosition(), PREVIOUS);
    }

    /**
     * Get the next content object in the list
     * @param contentObject A content object for reference
     * @return The content object that is before the content object passed in
     */
    public ContentObject getNextObject(ContentObject contentObject) {
        return getContentObject(contentObject.getPosition(), NEXT);
    }

    /**
     * Get the Content object at the specified position.
     * @param position The position of the content object.
     * @return a valid ContentObject instance
     */
    public ContentObject getContentObject(int position) {
        JSONArray contentArray =  SettingsUtil.getSettingsValues().optJSONArray(CONTENT_KEY);
        ContentObject contentObject = new ContentObject(contentArray.optJSONObject(position));
        contentObject.setPosition(position);
        return contentObject;
    }

    /**
     * Get the next or previous content object in the list using an integer as a reference point.  This will return 0 if the last content object in the
     * list is passed in and direction is specified as the next content object and will return getContentStoreSize() if the first content object is passed in and
     * previous is specified as the direction.
     * @param position the position from which to start
     * @param direction ContentManager.NEXT or ContentManager.PREVIOUS
     * @return a valid ContentObject instance
     */
    private ContentObject getContentObject(int position, String direction){
        ContentObject contentObject;
        if (direction.equals(NEXT)){
            if (position == (getContentStoreSize() - 1)) {
                position = 0;
            }
            else {
                position++;
            }
            contentObject = getContentObject(position);
        }
        else {
            if (position == 0 && getContentStoreSize() >= 1) {
                position = getContentStoreSize() - 1; // adjusted for inflation
            }
            else {
                position--;
            }
            contentObject = getContentObject(position);
        }
        return contentObject;
    }

    /**
     * Get the size of the content object settings list
     * @return an integer representing how many content objects are in the settings
     */
    public int getContentStoreSize() {
        JSONArray array = SettingsUtil.getSettingsValues().optJSONArray(CONTENT_KEY);
        return array.length();
    }

    /**
     * Checks the current content cache for the specified string.
     * @param content the filename of the content to check, the url of the content encoded using Base64.encodeToString
     * @return true if the content exists in the content cache, false if it doesnt
     */
    public boolean contentIsCached(String content) {
        File contentFile = getFile(content);
        return contentFile.exists();
    }

    /**
     * Get the file related to a content object
     * @param content the filename returned from ContentObject.getContentFileName()
     * @return the cached content file
     */
    public File getFile(String content){
        return new File(Environment.getExternalStorageDirectory() + CONTENT_LOCATION + content);
    }

    private JSONArray getAllContentSettings() {
        return MMManager.getMokiManage(null).arrayForKey(CONTENT_KEY);
    }

    /**
     * Return a list of ContentObjects for every content object that exists in the settings
     * @return an ArrayList of content objects
     */
    public ArrayList<ContentObject> getAllContentObjects() {
        ArrayList<ContentObject> allContentObjects = new ArrayList<ContentObject>();
        JSONArray settingsList = getAllContentSettings();
        for (int i = 0; i < settingsList.length(); i++) {
            ContentObject contentObject = new ContentObject(settingsList.optJSONObject(i));
            contentObject.setPosition(i);
            allContentObjects.add(contentObject);
        }
        return allContentObjects;
    }

    /**
     * Get all Urls that exist in the content cache. This may be more than exist in the settings in some cases.
     * @return an arraylist of strings indicating what files exist in the content store
     */
    public ArrayList<String> getAllContentUrls() {
        ArrayList<String> contentUrls = new ArrayList<String>();
        File contentDir = new File(Environment.getExternalStorageDirectory() + CONTENT_LOCATION);
        if (contentDir.list() != null) {
            contentUrls.addAll(Arrays.asList(contentDir.list()));
        }
        return contentUrls;
    }

    /**
     * Get a list of urls that indicate what urls exist in the settings of the application
     * @return an arraylist of strings for content urls in the settings
     */
    public ArrayList<String> getAllSettingsUrls() {
        ArrayList<String> settingsUrls = new ArrayList<String>();
        JSONArray urls = SettingsUtil.getSettingsValues().optJSONArray(CONTENT_KEY);
        for (int i = 0; i < urls.length(); i++){
            settingsUrls.add(Base64.encodeToString(urls.optJSONObject(i).optString(ContentObject.CONTENT_URL).getBytes(), Base64.DEFAULT));
        }
        return settingsUrls;
    }

    private void deleteContentFile(String content) {
        File cacheFile = getFile(content);
        boolean deleted = cacheFile.delete();
        if (deleted) {
            Log.d(ContentManager.class.getSimpleName(), "Deleted cache file: " + content);
        }
        else {
            Log.d(ContentManager.class.getSimpleName(), "Unable to delete cache file: " + content);
        }
    }

    /**
     * remove content from the content store that do not exist in the settings
     */
    public void deleteInvalidCache() {
        ArrayList<String> contentList = getAllContentUrls();
        if(contentList.removeAll(getAllSettingsUrls())) {
            for (int i = 0; i < contentList.size(); i++) {
                deleteContentFile(contentList.get(i));
            }
        }
    }

    /**
     * return the sd card location for the content that this class manages
     * @return a path on the deivces sd card
     */
    public String getContentLocation() {
        return CONTENT_LOCATION;
    }
}

package com.moki.touch.util;

import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

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
public class UrlUtil {

    private static String[] videoExtentions = {
            "mp4",
            "3gp",
            "webm",
            "mkv"
    };

    private static String[] imageExtentions = {
            "png",
            "gif",
            "bmp",
            "webp",
            "jpg",
            "jpeg"
    };

    public static String addHttp(String url) {
        if (url != null && !url.contains("http://") && !url.contains("https://")){
            url = "http://" + url;
        }
        return url;
    }

    /**
     * This method is used to create a filename for cached content
     * @param url the url of the content to be cached
     * @return an MD5 hashed string
     */
    public static String hashUrl(String url) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(url.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getUrlExtention(String url) {
        if (url == null || url.isEmpty() || url.length() < 4) {
            return "";
        }
        return url.substring(url.length() - 4).toLowerCase();
    }

    /**
     * This method determines if we are able to download a vimeo video from the current url
     * @param url a url that redirects to a vimeo video
     * @return true if it is a downloadable vimeo link
     */
    public static boolean isCachebleVimeoLink(String url) {
        boolean isDownloadableVimeoLink = false;
        try {
            URL vimeoUrl = new URL(url);
            isDownloadableVimeoLink = vimeoUrl.getHost().contains("vimeo") && vimeoUrl.getPath().contains("download");
        } catch (MalformedURLException e) {
            Log.i(UrlUtil.class.getSimpleName(), "url passed to isCacheblevimeoLink was malformed");
        }
        return isDownloadableVimeoLink;
    }

    /**
     * This method determines if the url passed in is the location of a video that we can download
     * @param url a valid url to a video
     * @return true for valid video urls or false
     */
    public static boolean isContentVideo(String url) {
        boolean isVideo;
        if (isCachebleVimeoLink(url)){
            isVideo = true;
        }
        else {
            isVideo = FilenameUtils.isExtension(url, videoExtentions);
        }
        return isVideo;
    }

    /**
     * This method determines if the url passed in is the location of a image that we can download
     * @param url a valid url to a image
     * @return true for valid image urls or false
     */
    public static boolean isContentImage(String url) {
        return FilenameUtils.isExtension(url, imageExtentions);
    }
}

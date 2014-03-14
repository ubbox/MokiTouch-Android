package com.moki.touch.util.management;

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
 *
 * This is an implementation of the ContentManager class giving its functionality to manage content objects that exist
 * in the playlist settings
 *
 * * The operations of this class affect only files that exists in sdcard/MokiMDM/CachedItems/saverContent
 */
public class SaverContentManager extends ContentManager {

    private final static String SAVER_KEY = SettingsUtil.SAVER_CONTENT_KEY;
    private final static String SAVER_CONTENT_LOCATION = "saverContent/";
    {
        CONTENT_KEY = SAVER_KEY;
        CONTENT_LOCATION = MOKI_STORAGE_LOCATION + SAVER_CONTENT_LOCATION;
    }
}

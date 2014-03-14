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
 * in the home settings
 *
 * The operations of this class affect only files that exists in sdcard/MokiMDM/CachedItems/homeContent
 */
public class HomeContentManager extends ContentManager {

    private final static String HOME_CONTENT_KEY = SettingsUtil.HOME_CONTENT_KEY;
    private final static String HOME_CONTENT_LOCATION = "homeContent/";
    {
        CONTENT_KEY = HOME_CONTENT_KEY;
        CONTENT_LOCATION = MOKI_STORAGE_LOCATION + HOME_CONTENT_LOCATION;
    }
}

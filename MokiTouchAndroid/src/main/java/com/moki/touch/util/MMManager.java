package com.moki.touch.util;

import android.content.Context;

import com.moki.manage.api.MokiManage;

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
 * MMManager handles the initialization of the MokiManage object.
 *
 * With the relevant initialization info held as constants in this class the only parameter that the {@link #getMokiManage(android.content.Context)} function needs is a context
 */
public class MMManager {
    private static final String API_KEY = "bf517fc9-5993-49ea-9d2c-2f719447e5e6";
    private static final boolean ENABLE_AppSettingsManagement = true;
    private static final boolean ENABLE_AppEnvironmentManagement = true;
    private static final boolean ENABLE_Compliance = false;
    private static final String APP_ID = "Touch";

	public static MokiManage getMokiManage(Context context){
        MokiManage mokiManage;
        if (context != null){
		    mokiManage = MokiManage.sharedInstance(API_KEY, APP_ID, context, ENABLE_AppSettingsManagement, ENABLE_AppEnvironmentManagement, ENABLE_Compliance);
            mokiManage.setSupportContactNumber("888-329-8942");
        }
        else {
            mokiManage = MokiManage.sharedInstance();
        }
        return mokiManage;
	}
}
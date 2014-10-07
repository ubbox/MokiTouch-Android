package com.moki.touch.application;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.moki.manage.api.MokiManage;
import com.moki.sdk.core.MokiApplication;
import com.moki.sdk.core.MokiCore;
import com.moki.touch.util.MMManager;

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
public class MokiTouchApplication extends MokiApplication {

    private MokiManage mokiManage;
    static Context context;
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        registerActivityLifecycleCallbacks(this);
        mokiManage = MMManager.getMokiManage(this);
        mokiManage.resume();
    }

    public static Context getContext(){
        return context;
    }
}

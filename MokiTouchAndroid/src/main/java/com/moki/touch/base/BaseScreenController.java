package com.moki.touch.base;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.moki.touch.models.ContentObject;
import com.moki.touch.interfaces.ScreenController;
import com.moki.touch.util.management.ContentManager;

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
public abstract class BaseScreenController implements ScreenController {

    public static final String CONTENT_FINISHED = "content_finished";
    protected ContentObject mContentObject;
    protected Context context;
    protected ContentManager contentManager;
    protected boolean isHomeMode;

    public BaseScreenController(Context context) {
        this.context = context;
    }

    @Override
    public void controlChanged() {
        context = null;
    }

    @Override
    public View getView() {
        if (!isContentObjectValid()){
            return new View(context);
        }
        else {
            return null;
        }
    };

    @Override
    public void setContent(ContentObject viewContent) {
        this.mContentObject = viewContent;
    }

    @Override
    public ContentObject getContentObject() {
        return mContentObject;
    }

    @Override
    public void setContentManager(ContentManager contentManager) {
        this.contentManager = contentManager;
    }

    @Override
    public void isHomeMode(boolean homeMode) {
        this.isHomeMode = homeMode;
    }

    @Override
    public abstract void startContent();

    protected void contentFinished(){
        Intent finished = new Intent(CONTENT_FINISHED);
        if (context != null) {
            context.sendBroadcast(finished);
            context = null;
        }
    }

    private boolean isContentObjectValid(){
        boolean isValid = true;
        if (mContentObject.getUrl() == null || mContentObject.getUrl().isEmpty()) {
            isValid = false;
        }
        if (isHomeMode && mContentObject.getTitle() == null && mContentObject.getTitle().isEmpty()) {
            isValid = false;
        }
        return isValid;
    }

}
package com.moki.touch.fragments.views;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
public class ImageContent extends ContentView {

    public static final int TO_MILLISECONDS = 1000;
    private static final int IMAGE_MAX_SIZE = 1000;
    private ImageView imageContent;
    private Handler completionHandler = new Handler();

    public ImageContent(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View emptyView = super.getView();
        imageContent = new ImageView(context);
        imageContent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        if (!waitingOnContent) {
            imageContent.setImageBitmap(decodeFile());
        }
        return imageContent;
    }

    @Override
    public boolean downloadFinished(String url) {
        boolean handleNotification = super.downloadFinished(url);
        if (handleNotification) {
            imageContent.setImageBitmap(decodeFile());

        }
        return handleNotification;
    }

    @Override
    public void controlChanged() {
        super.controlChanged();
        if (completionHandler != null) {
            completionHandler.removeCallbacksAndMessages(null);
        }
        completionHandler = null;
    }

    @Override
    public void startContent() {
        super.startContent();
        if (!isHomeMode) {
            setTimer();
        }
    }

    private void setTimer() {
        int duration = 10;
        if (!mContentObject.getDuration().isEmpty()) {
            duration = Integer.parseInt(mContentObject.getDuration());
        }
        completionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageComplete();
            }
        }, duration * TO_MILLISECONDS);
    }

    private void imageComplete() {
        contentFinished();
    }

    private Bitmap decodeFile(){
        File file = contentManager.getFile(mContentObject.getContentFileName());
        Bitmap bitmap = null;

        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(file);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE /
                        (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            options.inPurgeable = true;
            options.inInputShareable = true;
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis, null, options);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}

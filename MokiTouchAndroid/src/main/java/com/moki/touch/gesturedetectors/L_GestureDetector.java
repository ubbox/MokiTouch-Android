package com.moki.touch.gesturedetectors;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

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
public class L_GestureDetector {

    int current_index = 0;
    int width, height;
    Rect upperLeft, lowerRight, top, right;
    int lastTouch, lowerDetectionBound, upperDetectionBound;
    boolean outOfBounds = false;

    private IGestureDetected gestureDetected;

    public L_GestureDetector(Activity activity, IGestureDetected detector) {
        gestureDetected = detector;
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        lowerDetectionBound = (int) (height * .9);
        upperDetectionBound = (int) (height * .1);
        upperLeft = new Rect(0, 0, (int) (width * .1), upperDetectionBound);
        lowerRight = new Rect((int) (width * .9), lowerDetectionBound, width, height);
        top = new Rect(0, 0, width, upperDetectionBound);
        right = new Rect((int) (width * .9), 0, width, height);
    }

    public boolean detect(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            outOfBounds = false;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            Point currentTouch = new Point((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
            if (!outOfBounds) {
                if (top.contains(currentTouch.x, currentTouch.y) || right.contains(currentTouch.x, currentTouch.y)) {
                    switch (current_index) {
                        case 0:
                            if (upperLeft.contains(currentTouch.x, currentTouch.y)) {
                                current_index = 1;
                            }
                            break;
                        case 1:
                            if (lowerRight.contains(currentTouch.x, currentTouch.y)) {
                                lastTouch = currentTouch.x;
                                gestureDetected.onGestureDetected();
                                outOfBounds = true;
                            }
                            break;
                    }
                }
            } else {
                outOfBounds = true;
            }
        } else {
            current_index = 0;
            lastTouch = 0;
        }
        return false;
    }

}
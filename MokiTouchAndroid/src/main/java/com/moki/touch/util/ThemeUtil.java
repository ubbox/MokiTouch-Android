package com.moki.touch.util;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;

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
public class ThemeUtil {


    /**
     * This method exists to help in the conversion of the base theme setting to lighter and darker colors needed for different parts of MokiTouch
     * @param styleColor A string representing a hex color. example #ABCDEF
     * @param shift a float representing how much you want the styleColor to be changed, color < 1 for darker colors, color > 1 for lighter colors
     * @return
     */
    public static int getShiftedColor(String styleColor, float shift) {
        float[] hsv = new float[3];
        int color = Color.parseColor(styleColor);
        Color.colorToHSV(color, hsv);
        hsv[2] *= shift;
        color = Color.HSVToColor(hsv);
        return color;
    }

    /**
     * Check if the color string passed in is a valid hex string
     * @param color
     * @return
     */
    public static boolean isValidColorHex(String color) {
        boolean isValid = true;

        if (color == null && color.isEmpty()) {
            isValid = false;
        }

        if (!color.matches("^#(?:[0-9a-fA-F]{6}$)")) {
            isValid = false;
        }

        return isValid;
    }
}
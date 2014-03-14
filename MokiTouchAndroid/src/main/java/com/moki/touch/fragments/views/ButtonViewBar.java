package com.moki.touch.fragments.views;

import android.app.ActionBar;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.moki.touch.models.ContentObject;
import com.moki.touch.util.ThemeUtil;

import java.util.ArrayList;

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
public class ButtonViewBar extends HorizontalScrollView {

    public static final String BUTTON_ACTION = "button_action";
    public static final float LINK_BAR_COLOR_SHIFT_VALUE = 1.2f;
    private LinearLayout buttonView;
    private Button selection;
    private Context context;
    private String styleColor;
    private int windowSize;

    public ButtonViewBar(Context context) {
        super(context);
    }

    public ButtonViewBar(Context context, ArrayList<ContentObject> contentObjects, String styleColor, int windowSize) {
        this(context);
        this.styleColor = styleColor;
        this.windowSize = windowSize;
        configureScrollView(context);
        configureButtonview(contentObjects);
        setButtons(contentObjects);
        addView(buttonView);
        setSelection(0);
    }

    private void configureScrollView(Context context) {
        this.context = context;
        this.generateDefaultLayoutParams();
        this.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.setBackgroundColor(ThemeUtil.getShiftedColor(styleColor, LINK_BAR_COLOR_SHIFT_VALUE));
        this.setFillViewport(true);
    }

    private void configureButtonview(ArrayList<ContentObject> contentObjects) {
        buttonView = new LinearLayout(context);
        buttonView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        buttonView.setOrientation(LinearLayout.HORIZONTAL);
        buttonView.setWeightSum(contentObjects.size());
    }

    public void cleanBar() {
        context = null;
        buttonView = null;
        selection = null;
    }

    public void setButtons(ArrayList<ContentObject> currentContent) {
        if (buttonView.getChildCount() > 0) {
            buttonView.removeAllViews();
        }
        for (ContentObject contentObject : currentContent) {
            ContentButton contentButton = new ContentButton(context, this, styleColor);
            contentButton.setTitle(contentObject.getTitle());
            contentButton.setPosition(contentObject.getPosition());
            buttonView.addView(contentButton);
        }
    }

    public void setSelection(Button button) {
        if (button != null) {
            if (selection != null) {
                selection.setSelected(false);
            }
            selection = button;
            selection.setSelected(true);
            scrollButton(button);
        }
    }

    private void scrollButton(Button button) {
        if (button.getX() > this.getScrollX() + windowSize) {
            int scrollx = (int)(button.getX() - (windowSize - button.getWidth()));
            smoothScrollTo(scrollx, 0);
        }
        else if (button.getX() < this.getScrollX()) {
            smoothScrollTo((int)button.getX(), 0);
        }
    }

    public void setSelection(int position) {
        setSelection((Button) buttonView.getChildAt(position));
    }
}
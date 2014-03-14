package com.moki.touch.fragments.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.moki.touch.R;
import com.moki.touch.util.ThemeUtil;


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
public class ContentButton extends Button {

    private static final String POSITION = "position";
    public static final int EQUAL_WEIGHT_LAYOUT = 1;
    public static final float BUTTON_LIGHTEN_SHIFT_VALUE = 1.2f;
    public static final float BUTTON_DARKEN_SHIFT_VALUE = 0.6f;
    private ButtonViewBar bar;
    private int position;

    @SuppressWarnings("ConstantConditions")// The resource obtained using getResources() will be available upon its call
    public ContentButton(Context context, ButtonViewBar bar, String styleColor) {
        super(context);
        this.bar = bar;
        this.setOnClickListener(contentClicked);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, EQUAL_WEIGHT_LAYOUT);
        params.setMargins(1, 0, 1, 0);
        this.setLayoutParams(params);
        this.setBackgroundColor(Color.parseColor(styleColor));
        // We are using this deprecated method because our app supports pre api level 16 devices
        this.setBackgroundDrawable(createStateList(styleColor));
        this.setTextColor(getResources().getColor(R.color.link_button_color_selector));
        this.setTextSize(14);
        this.setPadding(24,0,24,0);
        this.setSingleLine();
    }

    public void setTitle(String title) {
        setText(title);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public boolean isSelected() {
        return super.isSelected();
    }

    public StateListDrawable createStateList(String styleColor) {
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[]{ android.R.attr.state_pressed}, new ColorDrawable(ThemeUtil.getShiftedColor(styleColor, BUTTON_LIGHTEN_SHIFT_VALUE)));
        selector.addState(new int[]{ -android.R.attr.state_selected}, new ColorDrawable(Color.parseColor(styleColor)));
        selector.addState(new int[]{ android.R.attr.state_selected}, new ColorDrawable(ThemeUtil.getShiftedColor(styleColor, BUTTON_DARKEN_SHIFT_VALUE)));
        return selector;
    }

    OnClickListener contentClicked = new OnClickListener() {
        @SuppressWarnings("ConstantConditions") // getContext() in this method will not be null when it is called.
        @Override
        public void onClick(View v) {
            bar.setSelection(ContentButton.this);
            Intent changeViewingContent = new Intent(ButtonViewBar.BUTTON_ACTION);
            changeViewingContent.putExtra(POSITION, position);
            getContext().sendBroadcast(changeViewingContent);
        }
    };
}
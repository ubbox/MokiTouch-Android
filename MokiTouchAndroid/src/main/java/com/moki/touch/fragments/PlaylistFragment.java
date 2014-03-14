package com.moki.touch.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.moki.touch.activities.PlaylistActivity;
import com.moki.touch.models.ContentObject;
import com.moki.touch.fragments.views.ImageContent;
import com.moki.touch.fragments.views.VideoContent;
import com.moki.touch.fragments.views.WebContent;
import com.moki.touch.interfaces.ScreenController;
import com.moki.touch.util.SettingsUtil;
import com.moki.touch.util.management.HomeContentManager;
import com.moki.touch.view.TransitionLayout;

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
public class PlaylistFragment extends Fragment {

    private ScreenController fragmentView;
    private TransitionLayout contentView;
    private PlaylistFragment nextFragment;
    private PlaylistFragment previousFragment;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        if (contentView == null) {
            contentView = new TransitionLayout(getActivity());
        }
        if (fragmentView != null) {
            fragmentView.startContent();
        }
        return contentView;
    }

    public ScreenController setContentObject(final ContentObject viewContent, PlaylistActivity activity){

        if (contentView != null) {
            contentView.removeAllViews();
        }
        else {
            contentView = new TransitionLayout(activity);
        }

        if (fragmentView != null) {
            fragmentView.controlChanged();
            activity.removeController(fragmentView);
            fragmentView = null;
        }

        // determining view content type
        if (viewContent.contentIsVideo()) {
            fragmentView = new VideoContent(activity);
        }
        else if (viewContent.contentIsImage()){
            fragmentView = new ImageContent(activity);
        }
        else {
            WebContent webContent = new WebContent(activity);
            webContent.setHomeUrl(viewContent.getUrl());
            webContent.setAllowedUrls(SettingsUtil.getAllowedUrls());
            webContent.setShowBrowserBar(SettingsUtil.showBrowserBar());
            webContent.setBarVisibility(SettingsUtil.showNavigationButtons(), SettingsUtil.showEndSessionButtion(), SettingsUtil.showNavigationBar());
            webContent.setStyleColor(SettingsUtil.getThemeColor());
            webContent.setShowProgressBar(SettingsUtil.showProgress());
            webContent.setShowLogo(SettingsUtil.showLogo());
            webContent.setUploadLogoUrl(SettingsUtil.getUploadLogo());
            fragmentView = webContent;
        }

        boolean isHomeMode = activity.getContentManager() instanceof HomeContentManager;
        fragmentView.isHomeMode(isHomeMode);
        fragmentView.setContentManager(activity.getContentManager());
        fragmentView.setContent(viewContent);

        contentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        contentView.setGravity(Gravity.CENTER);
        contentView.addView(fragmentView.getView());

        if (isAdded() && activity.isCurrentFragment(this)) {
            fragmentView.startContent();
        }

        activity.addNewController(fragmentView);
        return fragmentView;
    }

    public ScreenController getScreenController() {
        return fragmentView;
    }

    public PlaylistFragment getNextFragment() {
        return nextFragment;
    }

    public PlaylistFragment getPreviousFragment() {
        return previousFragment;
    }

    public void setFragments(PlaylistFragment next, PlaylistFragment previous){
        previousFragment = previous;
        nextFragment = next;
    }
}

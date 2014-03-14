package com.moki.touch.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import com.moki.touch.activities.HomePlaylist;

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
public class IdleResetDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Handler handler = new Handler();
        final Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Session Ending")
                .setMessage("Are you still here?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                handler.removeCallbacksAndMessages(null);
                                ((HomePlaylist)getActivity()).setupIdleReset();
                                dialog.dismiss();
                            }
                        }
                )
                .create();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                ((HomePlaylist)getActivity()).handleIdleReset();
            }
        }, 1000 * 10);
        return dialog;

    }
}

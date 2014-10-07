// Copyright (c) 2012 MokiMobility. All rights reserved.
package com.moki.touch.copy;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;

import com.moki.appregistration.AppRegistration;
import com.moki.asm.MokiASM;
import com.moki.asm.views.AppSettingsActivity;
import com.moki.asm.views.BreadCrumbFragment;
import com.moki.asm.views.GroupListAdapter;
import com.moki.asm.views.GroupListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import static com.moki.asm.ASMConstantValues.ASM_KEY_Groups;
import static com.moki.asm.ASMConstantValues.ASM_KEY_Title;

public class MTGroupListFragment extends Fragment implements OnItemSelectedListener, OnItemClickListener {
	public static final String BACK = "Back";
	public static final String SAVE = "Save";
	public static final String ENROLL = "Enroll";
	public static final String UNENROLL = "Unenroll";
    public static final String NETWORK_INFO = "Network Info";
    public JSONObject schema;
	public ListView list;
	public OnItemSelectedListener itemSelectedlistener;
	public String[] groupNames;
	public boolean initialized = false;
	public Activity activity;
	public View fragmentView;
	public BreadCrumbFragment crumbsFragment;

    BroadcastReceiver finishedReg = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(AppRegistration.NOTIFICATION_MESSAGE);
            if (!result.equals("FAILED")) {
               MTGroupListFragment.this.setLeftListNames();
            }
        }
    };


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity = getActivity();

		int listFragment = getResources().getIdentifier("group_list_fragment", "layout", activity.getPackageName());
		int groupListId = getResources().getIdentifier("list", "id", activity.getPackageName());
		int gray = getResources().getIdentifier("gray", "color", activity.getPackageName());
		fragmentView = inflater.inflate(getResources().getLayout(listFragment), container, false);
		list = (ListView) fragmentView.findViewById(groupListId);
		list.setCacheColorHint(getResources().getColor(gray));
		list.setBackgroundResource(gray);
		list.setDividerHeight(0);
		list.setVerticalScrollBarEnabled(false);
		list.setFadingEdgeLength(0);


        IntentFilter regReceiverFilter = new IntentFilter(AppRegistration.NOTIFICATION_REG_FINISHED);
        regReceiverFilter.addAction(AppRegistration.NOTIFICATION_UN_REG_FINISHED);
        getActivity().registerReceiver(finishedReg, regReceiverFilter);

		return fragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();
		Log.i("GroupListFragment", "onActivityCreated");
		setSchema(schema);
	}

    @Override
    public void onDestroyView() {
        activity.unregisterReceiver(finishedReg);
        super.onDestroyView();
    }

    public void setSchema(JSONObject schema) {
		Log.i("GroupListFragment", "setSchema is null = " + (schema == null));
		if (schema != null) {
			this.schema = schema;
			if (isAdded()) {
				Log.i("GroupListFragment", "isAdded");
				setLeftListNames();
				if (!initialized) {
					list.setSelection(0);
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						onItemSelected(list, null, 0, 0);
					}
				}
			}
		}
	}

	public String getGroupName(int position) {
		return groupNames[position];
	}

	public void setLeftListNames() {
		Log.i("GroupListFragment", "setLeftListNames");
		if (schema != null && list != null) {
			try {
				Log.i("GroupListFragment", "schema is not null");
				JSONArray groups = schema.optJSONArray(ASM_KEY_Groups);
				groupNames = new String[groups.length() + 4];
				for (int i = 0; i < groups.length(); i++) {
					String title = groups.getJSONObject(i).optString(ASM_KEY_Title);
					groupNames[i] = title;
				}
//				groupNames[groups.length()] = NETWORK_INFO;
				groupNames[groups.length()] = (AppRegistration.sharedInstance("", activity).isRegistered()) ? UNENROLL : ENROLL;
				groupNames[groups.length() + 1] = BACK;
				groupNames[groups.length() + 2] = "Exit App";
				groupNames[groups.length() + 3] = "" ;
				int cellId = getResources().getIdentifier("group_list_cell", "layout", activity.getPackageName());

				ArrayList<String> alist = new ArrayList<String>();
				Collections.addAll(alist, groupNames);
				list.setAdapter(new GroupListAdapter(activity, cellId, alist));
				list.setOnItemSelectedListener(this);
				list.setOnItemClickListener(this);
			} catch (JSONException e) {
				Log.e(this.getClass().getName(), e.getMessage(), e);
			}
		}
	}

	public void setOnItemSelectedlistener(OnItemSelectedListener listener) {
		itemSelectedlistener = listener;
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Log.i(this.getClass().getSimpleName(), "onItemSelected");
		if (groupNames != null) {

			((GroupListAdapter) list.getAdapter()).setSelected(position);
			((GroupListAdapter) list.getAdapter()).notifyDataSetChanged();
            if(crumbsFragment != null){
			    crumbsFragment.clear();
			    crumbsFragment.pushCrumb(groupNames[position]);
            }
			if (position == groupNames.length - 2) {
                activity.setResult(1);
                activity.finish();
            }
            else if (position == groupNames.length - 3) {
                MokiASM.sharedInstance().pushSettings(null);
				activity.finish();
			} else if (position == groupNames.length - 4) {
				((MTAppSettingsActivity) activity).showEnrollment();
			}
//            else if (position == groupNames.length - 5) {
//                ((MTAppSettingsActivity) activity).showNetworkFragment();
//			}
            else if (itemSelectedlistener != null) {
				itemSelectedlistener.onItemSelected(arg0, arg1, position, arg3);
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i("GroupListFragment", "onAttach " + this + ", " + activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i("GroupListFragment", "onDettach " + this);
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		if (itemSelectedlistener != null) {
			itemSelectedlistener.onNothingSelected(arg0);
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.i(this.getClass().getSimpleName(), "onItemClick");
		if (list.getSelectedItemPosition() != position) {
			list.setSelection(position);
			onItemSelected(parent, view, position, id);
		}
	}

	public void setBreadCrumbFragment(BreadCrumbFragment crumbFragment) {
		crumbsFragment = crumbFragment;
	}
}

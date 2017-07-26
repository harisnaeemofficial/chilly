/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adisdurakovic.android.chilly.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v17.preference.LeanbackSettingsFragment;
import android.support.v17.preference.LeanbackSettingsRootView;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.data.ChillyTasks;
import com.adisdurakovic.android.chilly.model.TraktGson;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsFragment extends LeanbackSettingsFragment
        implements DialogPreference.TargetFragment, ChillyTasks.TraktResponse {
    private final static String PREFERENCE_RESOURCE_ID = "preferenceResource";
    private final static String PREFERENCE_ROOT = "root";
    private PreferenceFragment mPreferenceFragment;

    @Override
    public void onResume() {
        super.onResume();
        new ChillyTasks.TraktTask(getActivity().getApplicationContext(), this, "get-user-name").execute();
    }

    @Override
    public void onUserGet(TraktGson.TraktUser user, Drawable icon) {
        Preference pref = findPreference(getString(R.string.pref_key_login));
        if(pref != null && user != null) {
            pref.setTitle("Logged in as: " + user.username);
            pref.setSummary("Click for re-outh");
            if(icon != null) {
                pref.setIcon(icon);
            }

        }
    }



    @Override
    public void onPreferenceStartInitialScreen() {
        mPreferenceFragment = buildPreferenceFragment(R.xml.settings, null);
        startPreferenceFragment(mPreferenceFragment);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment,
        Preference preference) {
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment,
        PreferenceScreen preferenceScreen) {
        PreferenceFragment frag = buildPreferenceFragment(R.xml.settings,
            preferenceScreen.getKey());
        startPreferenceFragment(frag);

        return true;
    }

    @Override
    public Preference findPreference(CharSequence charSequence) {
        return mPreferenceFragment.findPreference(charSequence);
    }

    private PreferenceFragment buildPreferenceFragment(int preferenceResId, String root) {
        PreferenceFragment fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putInt(PREFERENCE_RESOURCE_ID, preferenceResId);
        args.putString(PREFERENCE_ROOT, root);
        fragment.setArguments(args);
        return fragment;
    }

    public static class PrefFragment extends LeanbackPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            String root = getArguments().getString(PREFERENCE_ROOT, null);
            int prefResId = getArguments().getInt(PREFERENCE_RESOURCE_ID);
            if (root == null) {
                addPreferencesFromResource(prefResId);
            } else {
                setPreferencesFromResource(prefResId, root);
            }

        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals(getString(R.string.pref_key_login))) {
                // Open an AuthenticationActivity
//                startActivity(new Intent(getActivity(), DialogExampleActivity.class));

                LoginFragment traktLoginFragment = new LoginFragment();
                getFragmentManager().beginTransaction().add(R.id.settings_preference_fragment_container, traktLoginFragment)
                        .addToBackStack(null).commit();
//                getFragmentManager().beginTransaction().remove(SettingsFragment.this).commit();
//                getFragmentManager().popBackStack();

            }
            return super.onPreferenceTreeClick(preference);
        }

    }
}
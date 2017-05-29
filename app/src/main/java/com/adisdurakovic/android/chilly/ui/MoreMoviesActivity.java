/*
 * Copyright (c) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adisdurakovic.android.chilly.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidanceStylist.Guidance;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v4.app.ActivityOptionsCompat;
import android.widget.Toast;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.data.Chilly;

import java.util.List;


/**
 * Activity that showcases different aspects of GuidedStepFragments.
 */
public class MoreMoviesActivity extends Activity {

    private static String displayList;

    private static final String[] MOVIE_LISTS = {
            "Trending",
            "Popular",
            "Box Office",
            "Collection",
            "Watchlist"
    };

    private static final String[] MOVIE_LISTS_INTENTVARS = {
            "movies_trending",
            "movies_popular",
            "movies_boxoffice",
            "Here's another thing you can do",
            "Here's one more thing you can do"
    };


    private static final int[] OPTION_DRAWABLES = {R.drawable.ic_guidedstep_option_a,
            R.drawable.ic_guidedstep_option_b, R.drawable.ic_guidedstep_option_c};
    private static final boolean[] OPTION_CHECKED = {true, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            GuidedStepFragment.addAsRoot(this, new FirstStepFragment(), android.R.id.content);
        }
        displayList = getIntent().getStringExtra("display-list");

    }

    private static void addAction(List<GuidedAction> actions, long id, String title, String desc) {
        actions.add(new GuidedAction.Builder()
                .id(id)
                .title(title)
                .description(desc)
                .build());
    }



    public static class FirstStepFragment extends GuidedStepFragment {


        @Override
        public int onProvideTheme() {
            return R.style.Theme_Example_Leanback_GuidedStep_First;
        }

        @Override
        @NonNull
        public Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
            String title = getString(R.string.movies_browse);
            String breadcrumb = "";
            String description = getString(R.string.guidedstep_first_description);
            Drawable icon = getActivity().getDrawable(R.drawable.ic_main_icon);
            return new Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {

            for (int i = 0; i < MOVIE_LISTS.length; i++) {
                addAction(actions, i, MOVIE_LISTS[i], "");
            }


        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {

            Intent intent = new Intent(getActivity(), VerticalGridActivity.class);
            int intid = (int) action.getId();
            intent.putExtra("display-list",MOVIE_LISTS_INTENTVARS[intid]);
            Bundle bundle =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
                            .toBundle();
            startActivity(intent, bundle);

        }
    }


}



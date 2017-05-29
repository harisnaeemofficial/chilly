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
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.CursorObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.Toast;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.data.Chilly;
import com.adisdurakovic.android.chilly.data.FetchVideoService;
import com.adisdurakovic.android.chilly.data.VideoContract;
import com.adisdurakovic.android.chilly.model.Video;
import com.adisdurakovic.android.chilly.model.VideoCursorMapper;
import com.adisdurakovic.android.chilly.presenter.CardPresenter;
import com.adisdurakovic.android.chilly.presenter.GridItemPresenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * VerticalGridFragment shows a grid of videos that can be scrolled vertically.
 */
public class VerticalGridFragment extends android.support.v17.leanback.app.VerticalGridFragment {

    private static final int NUM_COLUMNS = 5;
    private final CursorObjectAdapter mVideoCursorAdapter =
            new CursorObjectAdapter(new CardPresenter());
    private static final int ALL_VIDEOS_LOADER = 1;
    private ArrayObjectAdapter mEpisodeadapter;
    private Video mSelectedShow;
    private Video mSelectedSeason;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mVideoCursorAdapter.setMapper(new VideoCursorMapper());

//        setAdapter(mVideoCursorAdapter);

        mSelectedShow = (Video) getActivity().getIntent()
                .getParcelableExtra("tvshow");

        mSelectedSeason = (Video) getActivity().getIntent()
                .getParcelableExtra("season");

        CardPresenter cp = new CardPresenter();

        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(5);


        if(getActivity().getIntent().getStringExtra("display-list").equals("episodes-for-show-season")) {
            cp.isEpisode = true;
            gridPresenter.setNumberOfColumns(4);
            setTitle(mSelectedShow.title + ": " + mSelectedSeason.title);
        } else {
            setTitle(getActivity().getString(getResources().getIdentifier(getActivity().getIntent().getStringExtra("display-list"), "string", "com.adisdurakovic.android.chilly")));
        }

        mEpisodeadapter = new ArrayObjectAdapter(cp);
        setAdapter(mEpisodeadapter);
        setGridPresenter(gridPresenter);

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }




        setupFragment();
    }

    private void setupFragment() {



        new MovieTVShowLoader(this, mEpisodeadapter, getActivity().getIntent().getStringExtra("display-list"), mSelectedShow, mSelectedSeason).execute();


        // After 500ms, start the animation to transition the cards into view.
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startEntranceTransition();
            }
        }, 500);

        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }



    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = (Video) item;

                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                intent.putExtra(VideoDetailsActivity.VIDEO, video);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
        }
    }
}

// Background ASYNC Task to login by making HTTP Request
class MovieTVShowLoader extends AsyncTask<String, String, String> {

    private final Activity activity;
    private final ArrayObjectAdapter mCategoryRowAdapter;
    private final VerticalGridFragment fragment;
    List<Video> video_list;
    private String display_list;
    private Video tvshow;
    private Video tvseason;



    public MovieTVShowLoader(VerticalGridFragment fragment, ArrayObjectAdapter adapter, String display_list, Video show, Video season) {
        this.activity = fragment.getActivity();
        this.mCategoryRowAdapter = adapter;
        this.fragment = fragment;
        this.video_list = new ArrayList<>();
        this.display_list = display_list;
        this.tvshow = show;
        this.tvseason = season;
    }

    // Before starting background thread Show Progress Dialog
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        Toast.makeText(activity, "Loading...", Toast.LENGTH_SHORT).show();
    }

    // Checking login in background
    protected String doInBackground(String... params) {


        Chilly chilly = new Chilly(activity.getApplicationContext());

        try {
            switch(display_list) {
                case "movies_trending":
                    video_list = chilly.getTrendingMovies(40);
                    break;
                case "tvshows-trending":
                    video_list = chilly.getTrendingTVShows(40);
                    break;
                case "episodes-for-show-season":
                    video_list = chilly.getEpisodesForShowSeason(tvshow, tvseason);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    // After completing background task Dismiss the progress dialog
    protected void onPostExecute(String file_url) {
        mCategoryRowAdapter.addAll(mCategoryRowAdapter.size(), video_list);
    }
}
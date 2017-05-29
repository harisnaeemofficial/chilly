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
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.CursorObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.adisdurakovic.android.chilly.data.Chilly;
import com.adisdurakovic.android.chilly.data.StreamGrabber;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.data.FetchVideoService;
import com.adisdurakovic.android.chilly.data.VideoContract;
import com.adisdurakovic.android.chilly.model.Video;
import com.adisdurakovic.android.chilly.model.VideoCursorMapper;
import com.adisdurakovic.android.chilly.presenter.CardPresenter;
import com.adisdurakovic.android.chilly.presenter.GridItemPresenter;
import com.adisdurakovic.android.chilly.presenter.IconHeaderItemPresenter;
import com.adisdurakovic.android.chilly.recommendation.UpdateRecommendationsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Main class to show BrowseFragment with header and rows of videos
 */
public class MainFragment extends BrowseFragment {
    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mCategoryRowAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private Uri mBackgroundURI;
    private BackgroundManager mBackgroundManager;
    private static final int CATEGORY_LOADER = 123; // Unique ID for Category Loader.

    // Maps a Loader Id to its CursorObjectAdapter.
//    private Map<Integer, CursorObjectAdapter> mVideoCursorAdapters;
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        // Create a list to contain all the CursorObjectAdapters.
//        // Each adapter is used to render a specific row of videos in the MainFragment.
//        mVideoCursorAdapters = new HashMap<>();
//
//        // Start loading the categories from the database.
//        getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Final initialization, modifying UI elements.
        super.onCreate(savedInstanceState);

        // Prepare the manager that maintains the same background image between activities.
        prepareBackgroundManager();

        setupUIElements();
        setupEventListeners();
        prepareEntranceTransition();


        // Map category results from the database to ListRow objects.
        // This Adapter is used to render the MainFragment sidebar labels.
        mCategoryRowAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mCategoryRowAdapter);

        loadRows();


//        updateRecommendations();
    }

    @Override
    public void onDestroy() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
            mBackgroundTimer = null;
        }
        mBackgroundManager = null;
        super.onDestroy();
    }

    @Override
    public void onStop() {
        mBackgroundManager.release();
        super.onStop();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(
                getActivity().getResources().getDrawable(R.drawable.app_icon_chilly_transparent, null));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent over title
        setHeadersState(HEADERS_DISABLED);
        setHeadersTransitionOnBackEnabled(true);

        // Set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.fastlane_background));

        // Set search icon color.
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.search_opaque));

//        setHeaderPresenterSelector(new PresenterSelector() {
//            @Override
//            public Presenter getPresenter(Object o) {
//                return new IconHeaderItemPresenter();
//            }
//        });
    }

    private void loadRows() {

        HeaderItem gridHeader = new HeaderItem("");
        GridItemPresenter gridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);
        ListRow row = new ListRow(gridHeader, gridRowAdapter);
        mCategoryRowAdapter.add(row);

        new HomeLoader(this, mCategoryRowAdapter).execute();


    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener(this));
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(this)
                .load(uri)
                .asBitmap()
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<Bitmap>(width, height) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap>
                            glideAnimation) {
                        mBackgroundManager.setBitmap(resource);
                    }
                });
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private void updateRecommendations() {
        Intent recommendationIntent = new Intent(getActivity(), UpdateRecommendationsService.class);
        getActivity().startService(recommendationIntent);
    }


    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI.toString());
                    }
                }
            });
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {

        private Fragment fragment;

        public ItemViewClickedListener(Fragment frag) {
            this.fragment = frag;
        }

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
            } else if (item instanceof String) {


//               new MoreLoader(fragment, (String) item);

//                if (((String) item).contains(getString(R.string.trending))) {
//                    Intent intent = new Intent(getActivity(), VerticalGridActivity.class);
//                    intent.putExtra("display-list","movies_trending");
//                    Bundle bundle =
//                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
//                                    .toBundle();
//                    startActivity(intent, bundle);
//                } else if (((String) item).contains(getString(R.string.guidedstep_first_title))) {
//                    Intent intent = new Intent(getActivity(), GuidedStepActivity.class);
//                    Bundle bundle =
//                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
//                                    .toBundle();
//                    startActivity(intent, bundle);
//                } else if (((String) item).contains(getString(R.string.error_fragment))) {
//                    BrowseErrorFragment errorFragment = new BrowseErrorFragment();
//                    getFragmentManager().beginTransaction().replace(R.id.main_frame, errorFragment)
//                            .addToBackStack(null).commit();
//                } else if(((String) item).contains(getString(R.string.personal_settings))) {
//                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
//                    Bundle bundle =
//                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
//                                    .toBundle();
//                    startActivity(intent, bundle);
//                } else {
//                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
//                            .show();
//                    Intent intent = new Intent(getActivity(), MoreMoviesActivity.class);
//                    Bundle bundle =
//                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
//                                    .toBundle();
//                    startActivity(intent, bundle);
//                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Video) {
                mBackgroundURI = Uri.parse(((Video) item).bgImageUrl);
                startBackgroundTimer();
            }

        }
    }
}


// Background ASYNC Task to login by making HTTP Request
class HomeLoader extends AsyncTask<String, String, String> {

    private final Activity activity;
    private final ArrayObjectAdapter mCategoryRowAdapter;
    private final MainFragment fragment;
    ArrayObjectAdapter start_movies;
    ArrayObjectAdapter start_tvshows;

    public HomeLoader(MainFragment fragment, ArrayObjectAdapter adapter) {
        this.activity = fragment.getActivity();
        this.mCategoryRowAdapter = adapter;
        this.fragment = fragment;
        this.start_movies = new ArrayObjectAdapter(new CardPresenter());
        this.start_tvshows = new ArrayObjectAdapter(new CardPresenter());
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
            List<Video> movies = chilly.getTrendingMovies(10);
            List<Video> tvshows = chilly.getTrendingTVShows(10);

            for(Iterator<Video> i = movies.iterator(); i.hasNext();) {
                Video v = i.next();
                start_movies.add(v);
            }

            for(Iterator<Video> i = tvshows.iterator(); i.hasNext();) {
                Video v = i.next();
                start_tvshows.add(v);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    // After completing background task Dismiss the progress dialog
    protected void onPostExecute(String file_url) {

        HeaderItem header_movies = new HeaderItem(0, "MOVIES");
        HeaderItem header_tvshows = new HeaderItem(1, "TV SHOWS");
        HeaderItem gridHeader_movies = new HeaderItem("MORE MOVIES");
        HeaderItem gridHeader_tvshows = new HeaderItem("MORE TV SHOWS");

        GridItemPresenter gridPresenter_movies = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter_movies = new ArrayObjectAdapter(gridPresenter_movies);
        gridRowAdapter_movies.add("Browse Movies");
        gridRowAdapter_movies.add("Movie Genres");
        ListRow row_more_movies = new ListRow(gridHeader_movies, gridRowAdapter_movies);


        GridItemPresenter gridPresenter_tvshows = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter_tvshows = new ArrayObjectAdapter(gridPresenter_tvshows);
        gridRowAdapter_tvshows.add("Browse TV Shows");
        gridRowAdapter_tvshows.add("TV Show Genres");
        ListRow row_more_tvshows = new ListRow(gridHeader_tvshows, gridRowAdapter_movies);


        mCategoryRowAdapter.add(new ListRow(header_movies, start_movies));
        mCategoryRowAdapter.add(row_more_movies);
        mCategoryRowAdapter.add(new ListRow(header_tvshows, start_tvshows));
        mCategoryRowAdapter.add(row_more_tvshows);


        fragment.startEntranceTransition();


    }
}



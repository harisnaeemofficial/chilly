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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
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
import com.adisdurakovic.android.chilly.data.ChillyTasks;
import com.adisdurakovic.android.chilly.data.ListElem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.model.Video;
import com.adisdurakovic.android.chilly.presenter.CardPresenter;
import com.adisdurakovic.android.chilly.presenter.GridItemPresenter;
import com.adisdurakovic.android.chilly.presenter.IconHeaderItemPresenter;
import com.adisdurakovic.android.chilly.recommendation.UpdateRecommendationsService;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Main class to show BrowseFragment with header and rows of videos
 */
public class MainFragment extends BrowseFragment implements ChillyTasks.HomeLoaderResponse, ChillyTasks.VersionResponse {
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
//        prepareBackgroundManager();


        new ChillyTasks.VersionTask(getActivity().getApplicationContext(), this, getResources().getString(R.string.app_version)).execute();

        // Map category results from the database to ListRow objects.
        // This Adapter is used to render the MainFragment sidebar labels.

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        prepareBackgroundManager();
        setupUIElements();
        setupEventListeners();
        prepareEntranceTransition();

        mCategoryRowAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        loadRows();
        setAdapter(mCategoryRowAdapter);


        updateRecommendations();
    }

    @Override
    public void updateAvailable(String version) {
        Toast.makeText(getActivity(), "New version ready for download: " + version, Toast.LENGTH_LONG).show();
    }



    @Override
    public void onResume() {
        super.onResume();


        // check if valid trakt is found and reset all buttons

        if(mCategoryRowAdapter.size() > 0) {
            boolean reset = true;
            for(int i = 0; i < mCategoryRowAdapter.size(); i++) {
                ListRow lr = (ListRow) mCategoryRowAdapter.get(i);
                if(lr.getHeaderItem().getName().equals("MORE MOVIES")) {
                    for(int j = 0; j < lr.getAdapter().size(); j++) {
                        Object item = lr.getAdapter().get(j);
                        if(item instanceof ListElem) {
                            if(((ListElem) item).slug.contains("trakt")) reset = false;
                        }
                    }
                    if(reset) {
                        mCategoryRowAdapter.removeItems(i, 1);
                        prepareMoreMovieButtons(i);
                    }
                }
                if(lr.getHeaderItem().getName().equals("MORE TV SHOWS") && reset) {
                    mCategoryRowAdapter.removeItems(i, 1);
                    prepareMoreTVShowButtons(i);
                }
            }
        }
    }

    private void prepareMoreMovieButtons(int pos) {
        HeaderItem header_more_movies = new HeaderItem("MORE MOVIES");
        ArrayObjectAdapter more_movies = new ArrayObjectAdapter(new GridItemPresenter());
        more_movies.add(new ListElem("Browse Movies", "trakt-public-list", "movie", "display-list", ""));
        more_movies.add(new ListElem("Movie Genres", "genres", "movie", "display-list", ""));

        if(hasValidTraktToken()) {
            more_movies.add(new ListElem("Collection", "user-collection", "movie", "display-videos", ""));
            more_movies.add(new ListElem("Watchlist", "user-watchlist", "movie", "display-videos", ""));
        }

        mCategoryRowAdapter.add(pos, new ListRow(header_more_movies, more_movies));
    }

    private void prepareMoreTVShowButtons(int pos) {
        HeaderItem header_more_tvshows = new HeaderItem("MORE TV SHOWS");
        ArrayObjectAdapter more_tvshows = new ArrayObjectAdapter(new GridItemPresenter());
        more_tvshows.add(new ListElem("Browse TV Shows", "trakt-public-list", "show", "display-list", ""));
        more_tvshows.add(new ListElem("TV Show Genres", "genres", "show", "display-list", ""));

        if(hasValidTraktToken()) {
            more_tvshows.add(new ListElem("Collection", "user-collection", "show", "display-videos", ""));
            more_tvshows.add(new ListElem("Watchlist", "user-watchlist", "show", "display-videos", ""));
        }

        mCategoryRowAdapter.add(pos, new ListRow(header_more_tvshows, more_tvshows));
    }

    private boolean hasValidTraktToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String string_token = sharedPreferences.getString("trakt_token", "NOTOKEN");

        return !string_token.equals("NOTOKEN");
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

        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {
                return new IconHeaderItemPresenter();
            }
        });
    }

    private void loadRows() {

        mCategoryRowAdapter.clear();

        // HACK - add init row, without this, async-task won't populate adapter when HEADERS_DISABLED
        HeaderItem gridHeader = new HeaderItem("HACKINIT");
        GridItemPresenter gridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);
        ListRow row = new ListRow(gridHeader, gridRowAdapter);
        mCategoryRowAdapter.add(row);


        new ChillyTasks.HomeLoaderTask(getActivity().getApplicationContext(), this).execute();


    }

    @Override
    public void onLoadFinish(List<Video> start_movies, List<Video> start_tvshows) {


        HeaderItem header_movies = new HeaderItem(0, "MOVIES");
        HeaderItem header_tvshows = new HeaderItem(1, "TV SHOWS");
        HeaderItem header_settings = new HeaderItem(1, "SETTINGS");



        ArrayObjectAdapter movies = new ArrayObjectAdapter(new CardPresenter(getActivity()));
        ArrayObjectAdapter tvshows = new ArrayObjectAdapter(new CardPresenter(getActivity()));
        ArrayObjectAdapter settings = new ArrayObjectAdapter(new GridItemPresenter());

        movies.addAll(movies.size(), start_movies);
        tvshows.addAll(tvshows.size(), start_tvshows);

        settings.add("Preferences");

        mCategoryRowAdapter.add(new ListRow(header_movies, movies));
        prepareMoreMovieButtons(mCategoryRowAdapter.size());
        mCategoryRowAdapter.add(new ListRow(header_tvshows, tvshows));
        prepareMoreTVShowButtons(mCategoryRowAdapter.size());

        // HACK - remove init row
        for(int i = 0; i < mCategoryRowAdapter.size(); i++) {
            ListRow lr = (ListRow) mCategoryRowAdapter.get(i);
            if(lr.getHeaderItem().getName().equals("HACKINIT")) {
                mCategoryRowAdapter.removeItems(i, 1);
            }
        }

        mCategoryRowAdapter.add(new ListRow(header_settings, settings));


        startEntranceTransition();
    }

    private void setupEventListeners() {
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
//        Intent recommendationIntent = new Intent(getActivity(), UpdateRecommendationsService.class);
//        getActivity().startService(recommendationIntent);
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

    private final class ItemViewClickedListener implements OnItemViewClickedListener, ChillyTasks.ImageLoaderResponse {

        ImageCardView cardView;

        @Override
        public void onLoadFinish(Map<String, String> images, Video video) {
            Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
            video.cardImageUrl = images.get("poster");
            video.bgImageUrl = images.get("background");
            intent.putExtra(VideoDetailsActivity.VIDEO, video);

            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(),
                    cardView.getMainImageView(),
                    VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
            getActivity().startActivity(intent, bundle);
        }


        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {




            if (item instanceof Video) {
                Video video = (Video) item;
                cardView = (ImageCardView) itemViewHolder.view;
                new ChillyTasks.ImageLoaderTask(getActivity().getApplicationContext(), this, video).execute();
//                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
//                intent.putExtra(VideoDetailsActivity.VIDEO, video);
//
//                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        getActivity(),
//                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
//                        VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
//                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {

                if(item.equals("Preferences")) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    Bundle bundle =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
                                    .toBundle();
                    startActivity(intent, bundle);
                }

            } else if (item instanceof ListElem) {

                System.out.println(((ListElem) item).action);

                if(((ListElem) item).action.equals("display-list")) {
                    Intent intent = new Intent(getActivity(), ListSelectActivity.class);
                    intent.putExtra("listElem",((ListElem) item));
                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle();
                    startActivity(intent, bundle);
                } else if(((ListElem) item).action.equals("display-videos")) {
                    System.out.println("HERE");
                    Intent intent = new Intent(getActivity(), VerticalGridActivity.class);
                    intent.putExtra("listElem",((ListElem) item));
                    Bundle bundle =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
                                    .toBundle();
                    startActivity(intent, bundle);
                }
//                Intent intent = new Intent(getActivity(), MoreMoviesActivity.class);
//                Bundle bundle =
//                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
//                                    .toBundle();
//                startActivity(intent, bundle);


//                LoginFragment loginFragment = new LoginFragment();
//                    getFragmentManager().beginTransaction().replace(R.id.main_frame, loginFragment)
//                            .addToBackStack(null).commit();


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

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener, ChillyTasks.ImageLoaderResponse {

        @Override
        public void onLoadFinish(Map<String, String> images, Video video) {
            video.bgImageUrl = images.get("background");
            if(video.bgImageUrl != null) {
                mBackgroundURI = Uri.parse(video.bgImageUrl);
                startBackgroundTimer();
            }
        }

        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Video) {
                Video video = (Video) item;
                new ChillyTasks.ImageLoaderTask(getActivity().getApplicationContext(), this, video).execute();
            }

        }
    }
}


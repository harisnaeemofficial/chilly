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
import android.content.res.Resources;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.data.Chilly;
import com.adisdurakovic.android.chilly.data.ChillyTasks;
import com.adisdurakovic.android.chilly.data.FetchVideoService;
import com.adisdurakovic.android.chilly.data.ListElem;
import com.adisdurakovic.android.chilly.data.VideoContract;
import com.adisdurakovic.android.chilly.model.Video;
import com.adisdurakovic.android.chilly.model.VideoCursorMapper;
import com.adisdurakovic.android.chilly.presenter.CardPresenter;
import com.adisdurakovic.android.chilly.presenter.GridItemPresenter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * VerticalGridFragment shows a grid of videos that can be scrolled vertically.
 */
public class VerticalGridFragment extends android.support.v17.leanback.app.VerticalGridFragment implements ChillyTasks.VideoLoaderResponse {

    private static final int NUM_COLUMNS = 5;
    private final CursorObjectAdapter mVideoCursorAdapter =
            new CursorObjectAdapter(new CardPresenter());
    private static final int ALL_VIDEOS_LOADER = 1;
    private ArrayObjectAdapter mVideoAdapter;
    private Video mSelectedShow;
    private Video mSelectedSeason;
    private SpinnerFragment mSpinnerFragment;
    ListElem elem;
    private int page = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSpinnerFragment = new SpinnerFragment();
//        getFragmentManager().beginTransaction().add(R.id.vertical_grid_fragment, mSpinnerFragment).commit();

//        mVideoCursorAdapter.setMapper(new VideoCursorMapper());

//        setAdapter(mVideoCursorAdapter);

        mSelectedShow = (Video) getActivity().getIntent()
                .getParcelableExtra("show");

        mSelectedSeason = (Video) getActivity().getIntent()
                .getParcelableExtra("season");

        CardPresenter cp = new CardPresenter(getActivity());

        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(5);

        elem = (ListElem) getActivity().getIntent().getParcelableExtra("listElem");
        if(mSelectedShow != null && mSelectedSeason != null) {
            cp.isEpisode = true;
            gridPresenter.setNumberOfColumns(4);
            setTitle(mSelectedShow.title + ": " + mSelectedSeason.title);
            elem = new ListElem();
            elem.tvshow = mSelectedShow;
            elem.season = mSelectedSeason;
        } else {
            setTitle(elem.videoType.toUpperCase() + "S: " + elem.title);
        }


        mVideoAdapter = new ArrayObjectAdapter(cp);
        setAdapter(mVideoAdapter);
        setGridPresenter(gridPresenter);

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }




        setupFragment();
    }

    @Override
    public void onStop() {
        super.onStop();
//        getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
    }

    private void setupFragment() {

        loadVideos(page);


        // After 500ms, start the animation to transition the cards into view.
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startEntranceTransition();
            }
        }, 500);

//        setOnSearchClickedListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getActivity(), SearchActivity.class);
//                startActivity(intent);
//            }
//        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    @Override
    public void onGrabVideos(List<Video> video_list) {
        mVideoAdapter.addAll(mVideoAdapter.size(), video_list);

//        ArrayObjectAdapter more_tvshows = new ArrayObjectAdapter(new GridItemPresenter());
//        more_tvshows.add(new ListElem("Browse TV Shows", "trakt-public-list", "show", "display-list", ""));
//        mVideoAdapter.add(more_tvshows);

        if(mSpinnerFragment != null) {
            getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
        }
    }


    private final class ItemViewClickedListener implements OnItemViewClickedListener, ChillyTasks.ImageLoaderResponse {

        ImageCardView cardView;

        @Override
        public void onLoadFinish(Map<String, String> images, Video video) {
            video.bgImageUrl = images.get("background");
            video.cardImageUrl = images.get("poster");
            Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
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
            }
        }
    }

    public int getPosition() {
        try {
            Field privatePosition =
                    android.support.v17.leanback.app.VerticalGridFragment.class.getDeclaredField("mSelectedPosition");
            privatePosition.setAccessible(true);

            int pos = (int) privatePosition.get(this);
            return pos+1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;

    }

    public void loadVideos(int page) {
        getFragmentManager().beginTransaction().add(R.id.vertical_grid_fragment, mSpinnerFragment).commit();
        new ChillyTasks.VideoLoaderTask(getActivity().getApplicationContext(), this, elem, page).execute();
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
                Video v = (Video) item;
                long pospage = 40*page;
                if(getPosition() >= pospage-5 && getPosition() <= pospage) {
                    if(page < ((pospage/40) + 1)) {
                        page++;
                        loadVideos(page);
                    }
                }
        }
    }


    public static class SpinnerFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ProgressBar progressBar = new ProgressBar(container.getContext());
            if (container instanceof FrameLayout) {
                Resources res = getResources();
                int width = res.getDimensionPixelSize(R.dimen.spinner_width);
                int height = res.getDimensionPixelSize(R.dimen.spinner_height);
                FrameLayout.LayoutParams layoutParams =
                        new FrameLayout.LayoutParams(width, height, Gravity.CENTER);
                progressBar.setLayoutParams(layoutParams);
            }
            return progressBar;
        }
    }

}


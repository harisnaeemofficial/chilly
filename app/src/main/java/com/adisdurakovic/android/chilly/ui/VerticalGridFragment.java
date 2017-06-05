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
import com.adisdurakovic.android.chilly.data.FetchVideoService;
import com.adisdurakovic.android.chilly.data.ListElem;
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
public class VerticalGridFragment extends android.support.v17.leanback.app.VerticalGridFragment implements VideoLoaderResponse {

    private static final int NUM_COLUMNS = 5;
    private final CursorObjectAdapter mVideoCursorAdapter =
            new CursorObjectAdapter(new CardPresenter());
    private static final int ALL_VIDEOS_LOADER = 1;
    private ArrayObjectAdapter mEpisodeadapter;
    private Video mSelectedShow;
    private Video mSelectedSeason;
    private SpinnerFragment mSpinnerFragment;
    ListElem elem;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSpinnerFragment = new SpinnerFragment();
        getFragmentManager().beginTransaction().add(R.id.vertical_grid_fragment, mSpinnerFragment).commit();

//        mVideoCursorAdapter.setMapper(new VideoCursorMapper());

//        setAdapter(mVideoCursorAdapter);

        mSelectedShow = (Video) getActivity().getIntent()
                .getParcelableExtra("show");

        mSelectedSeason = (Video) getActivity().getIntent()
                .getParcelableExtra("season");

        CardPresenter cp = new CardPresenter();

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


        mEpisodeadapter = new ArrayObjectAdapter(cp);
        setAdapter(mEpisodeadapter);
        setGridPresenter(gridPresenter);

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }




        setupFragment();
    }

    @Override
    public void onStop() {
        super.onStop();
        getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
    }

    private void setupFragment() {

        new VideoLoaderTask(getActivity().getApplicationContext(), this, elem).execute();


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
        mEpisodeadapter.addAll(mEpisodeadapter.size(), video_list);
        getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
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

interface VideoLoaderResponse {
    void onGrabVideos(List<Video> video_list);
}

// Background ASYNC Task to login by making HTTP Request
class VideoLoaderTask extends AsyncTask<String, String, String> {

    VideoLoaderResponse delegate = null;
    List<Video> video_list;
    private ListElem elem;
    Chilly chilly;


    public VideoLoaderTask(Context ctx, VideoLoaderResponse del, ListElem elem) {
        this.delegate = del;
        this.chilly = new Chilly(ctx);
        this.video_list = new ArrayList<>();
        this.elem = elem;
    }

    // Before starting background thread Show Progress Dialog
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        Toast.makeText(activity, "Loading...", Toast.LENGTH_SHORT).show();
    }

    // Checking login in background
    protected String doInBackground(String... params) {


        try {

            if(elem.tvshow != null && elem.season != null) {
                video_list = chilly.getEpisodesForShowSeason(elem.tvshow, elem.season);
            } else {
                if(elem.slug.contains("public")) {
                    video_list = chilly.getPublicVideos(elem.slug.replace("public-", ""), elem.videoType, 40);
                }

                if(elem.slug.contains("user")) {
                    video_list = chilly.getUserVideos(elem.slug.replace("user-", ""), elem.videoType);
                }

                if(!elem.filterType.equals("")) {
                    video_list = chilly.getByFilter(elem, 40);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    // After completing background task Dismiss the progress dialog
    protected void onPostExecute(String file_url) {
        delegate.onGrabVideos(video_list);
    }
}
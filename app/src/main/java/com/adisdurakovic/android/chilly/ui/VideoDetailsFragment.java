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

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.CursorObjectAdapter;
import android.support.v17.leanback.widget.DetailsOverviewLogoPresenter;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.adisdurakovic.android.chilly.data.Chilly;
import com.adisdurakovic.android.chilly.data.ListElem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.model.Video;
import com.adisdurakovic.android.chilly.model.VideoCursorMapper;
import com.adisdurakovic.android.chilly.presenter.CardPresenter;
import com.adisdurakovic.android.chilly.presenter.DetailsDescriptionPresenter;

import java.util.ArrayList;
import java.util.List;

/*
 * VideoDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment implements SeasonResponse {
    private static final int NO_NOTIFICATION = -1;
    private static final int ACTION_PLAY_NOW = 1;
    private static final int ACTION_PLAY_FROM = 2;
    private static final int ACTION_PLAY_TRAILER = 3;
    private static final int ACTION_TRAKT = 4;

    // ID for loader that loads related videos.
    private static final int RELATED_VIDEO_LOADER = 1;

    // ID for loader that loads the video from global search.
    private int mGlobalSearchVideoId = 2;

    private Video mSelectedVideo;
    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;
    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private CursorObjectAdapter mVideoCursorAdapter;
    private FullWidthDetailsOverviewSharedElementHelper mHelper;
    private final VideoCursorMapper mVideoCursorMapper = new VideoCursorMapper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();
        mVideoCursorAdapter = new CursorObjectAdapter(new CardPresenter());
        mVideoCursorAdapter.setMapper(mVideoCursorMapper);

        mSelectedVideo = (Video) getActivity().getIntent()
                .getParcelableExtra(VideoDetailsActivity.VIDEO);

        if (mSelectedVideo != null || !hasGlobalSearchIntent()) {
            removeNotification(getActivity().getIntent()
                    .getIntExtra(VideoDetailsActivity.NOTIFICATION_ID, NO_NOTIFICATION));
            setupAdapter();
            setupDetailsOverviewRow();
            setupMovieListRow();
            updateBackground(mSelectedVideo.bgImageUrl);

            // When a Related Video item is clicked.
            setOnItemViewClickedListener(new ItemViewClickedListener());
        }
    }

    private void removeNotification(int notificationId) {
        if (notificationId != NO_NOTIFICATION) {
            NotificationManager notificationManager = (NotificationManager) getActivity()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
    }

    @Override
    public void onStop() {
        mBackgroundManager.release();
        super.onStop();
    }

    @Override
    public void onGetSeasons(List<Video> seasons) {
        ArrayObjectAdapter seasonAdapter = new ArrayObjectAdapter(new CardPresenter());
        HeaderItem header = new HeaderItem(0, getActivity().getString(R.string.tvshow_seasons));
        seasonAdapter.addAll(seasonAdapter.size(), seasons);
        mAdapter.add(new ListRow(header, seasonAdapter));
    }

    /**
     * Check if there is a global search intent. If there is, load that video.
     */
    private boolean hasGlobalSearchIntent() {
//        Intent intent = getActivity().getIntent();
//        String intentAction = intent.getAction();
//        String globalSearch = getString(R.string.global_search);
//
//        if (globalSearch.equalsIgnoreCase(intentAction)) {
//            Uri intentData = intent.getData();
//            String videoId = intentData.getLastPathSegment();
//
//            Bundle args = new Bundle();
//            args.putString(VideoContract.VideoEntry._ID, videoId);
//            getLoaderManager().initLoader(mGlobalSearchVideoId++, args, this);
//            return true;
//        }
        return false;
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void updateBackground(String uri) {
        Glide.with(this)
                .load(uri)
                .asBitmap()
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<Bitmap>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(Bitmap resource,
                            GlideAnimation<? super Bitmap> glideAnimation) {
                        mBackgroundManager.setBitmap(resource);
                    }
                });
    }

    private void setupAdapter() {
        // Set detail background and style.
        FullWidthDetailsOverviewRowPresenter detailsPresenter =
                new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter(),
                        new MovieDetailsOverviewLogoPresenter(mSelectedVideo));

        detailsPresenter.setBackgroundColor(
                ContextCompat.getColor(getActivity(), R.color.videodetails_background));
        detailsPresenter.setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_HALF);

        // Hook up transition element.
        mHelper = new FullWidthDetailsOverviewSharedElementHelper();
        mHelper.setSharedElementEnterTransition(getActivity(),
                VideoDetailsActivity.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(mHelper);
        detailsPresenter.setParticipatingEntranceTransition(false);
        prepareEntranceTransition();
        final VideoDetailsFragment mFragment = this;
        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_PLAY_NOW) {
                    Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                    intent.putExtra(VideoDetailsActivity.VIDEO, mSelectedVideo);
                    startActivity(intent);
//                    new StreamTask(mFragment, mSelectedVideo).execute();
                } else if (action.getId() == ACTION_TRAKT) {
                    Intent intent = new Intent(getActivity(), ListSelectActivity.class);
                    intent.putExtra("listElem", new ListElem("Trakt", "trakt-actions", "", "", ""));
                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle();
                    startActivity(intent, bundle);
                } else {
                    Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mPresenterSelector = new ClassPresenterSelector();
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
    }

//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        switch (id) {
//            case RELATED_VIDEO_LOADER: {
//                String category = args.getString(VideoContract.VideoEntry.COLUMN_CATEGORY);
//                return new CursorLoader(
//                        getActivity(),
//                        VideoContract.VideoEntry.CONTENT_URI,
//                        null,
//                        VideoContract.VideoEntry.COLUMN_CATEGORY + " = ?",
//                        new String[]{category},
//                        null
//                );
//            }
//            default: {
//                // Loading video from global search.
//                String videoId = args.getString(VideoContract.VideoEntry._ID);
//                return new CursorLoader(
//                        getActivity(),
//                        VideoContract.VideoEntry.CONTENT_URI,
//                        null,
//                        VideoContract.VideoEntry._ID + " = ?",
//                        new String[]{videoId},
//                        null
//                );
//            }
//        }
//
//    }

//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        if (cursor != null && cursor.moveToNext()) {
//            switch (loader.getId()) {
//                case RELATED_VIDEO_LOADER: {
//                    mVideoCursorAdapter.changeCursor(cursor);
//                    break;
//                }
//                default: {
//                    // Loading video from global search.
//                    mSelectedVideo = (Video) mVideoCursorMapper.convert(cursor);
//
//                    setupAdapter();
//                    setupDetailsOverviewRow();
//                    setupMovieListRow();
//                    updateBackground(mSelectedVideo.bgImageUrl);
//
//                    // When a Related Video item is clicked.
//                    setOnItemViewClickedListener(new ItemViewClickedListener());
//                }
//            }
//        }
//    }

//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//        mVideoCursorAdapter.changeCursor(null);
//    }

    static class MovieDetailsOverviewLogoPresenter extends DetailsOverviewLogoPresenter {

        Video mSelectedVideo;

        MovieDetailsOverviewLogoPresenter(Video v) {
            mSelectedVideo = v;
        }

        static class ViewHolder extends DetailsOverviewLogoPresenter.ViewHolder {
            public ViewHolder(View view) {
                super(view);
            }

            public FullWidthDetailsOverviewRowPresenter getParentPresenter() {
                return mParentPresenter;
            }

            public FullWidthDetailsOverviewRowPresenter.ViewHolder getParentViewHolder() {
                return mParentViewHolder;
            }
        }

        @Override
        public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
            ImageView imageView = (ImageView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.lb_fullwidth_details_overview_logo, parent, false);

            Resources res = parent.getResources();
            int width = res.getDimensionPixelSize(R.dimen.detail_thumb_width);
            int height = res.getDimensionPixelSize(R.dimen.detail_thumb_height);

            if(mSelectedVideo.videoType.equals("episode")) {
                height = res.getDimensionPixelSize(R.dimen.detail_thumb_episode_height);
            }

            imageView.setLayoutParams(new ViewGroup.MarginLayoutParams(width, height));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
            DetailsOverviewRow row = (DetailsOverviewRow) item;
            ImageView imageView = ((ImageView) viewHolder.view);
            imageView.setImageDrawable(row.getImageDrawable());
            if (isBoundToImage((ViewHolder) viewHolder, row)) {
                MovieDetailsOverviewLogoPresenter.ViewHolder vh =
                        (MovieDetailsOverviewLogoPresenter.ViewHolder) viewHolder;
                vh.getParentPresenter().notifyOnBindLogo(vh.getParentViewHolder());
            }
        }
    }

    private void setupDetailsOverviewRow() {
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedVideo);

        Glide.with(this)
                .load(mSelectedVideo.cardImageUrl)
                .asBitmap()
                .dontAnimate()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(final Bitmap resource,
                            GlideAnimation glideAnimation) {
                        row.setImageBitmap(getActivity(), resource);
                        startEntranceTransition();
                    }
                });

        SparseArrayObjectAdapter adapter = new SparseArrayObjectAdapter();

        adapter.set(ACTION_PLAY_NOW, new Action(ACTION_PLAY_NOW, getResources()
                .getString(R.string.play_now)));
        adapter.set(ACTION_PLAY_FROM, new Action(ACTION_PLAY_FROM, getResources().getString(R.string.play_from)));
        adapter.set(ACTION_PLAY_TRAILER, new Action(ACTION_PLAY_TRAILER, getResources().getString(R.string.play_trailer)));
        if(Chilly.getInstance(getActivity().getApplicationContext()).userLoggedIn()){
            adapter.set(ACTION_TRAKT, new Action(ACTION_TRAKT, "TRAKT"));
        }
        row.setActionsAdapter(adapter);

        mAdapter.add(row);
    }

    // load seasons
    private void setupMovieListRow() {


        System.out.println(mSelectedVideo);

        if(mSelectedVideo.videoType.equals("show")) {
            new SeasonTask(getActivity().getApplicationContext(), this, mSelectedVideo).execute();
        }

    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = (Video) item;
                Intent intent = new Intent(getActivity(), VerticalGridActivity.class);
                intent.putExtra("season", video);
                intent.putExtra("show", mSelectedVideo);
                Bundle bundle =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
                                .toBundle();
                startActivity(intent, bundle);
            }
        }
    }
}


// Background ASYNC Task to login by making HTTP Request
//class StreamTask extends AsyncTask<String, String, String> {
//
//    private final Activity activity;
//    private final Video mSelectedVideo;
//    private final VideoDetailsFragment fragment;
//    String streamurl = "";
//
//    public StreamTask(VideoDetailsFragment fragment, Video video) {
//        this.activity = fragment.getActivity();
//        this.mSelectedVideo = video;
//        this.fragment = fragment;
//    }
//
//    // Before starting background thread Show Progress Dialog
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        Toast.makeText(activity, fragment.getResources()
//                .getString(R.string.playback_getsources), Toast.LENGTH_SHORT).show();
//    }
//
//    // Checking login in background
//    protected String doInBackground(String... params) {
//
//
//        try {
//            streamurl = StreamGrabber.getLastSource(mSelectedVideo);
//        } catch (IOException e) {
//
//        }
//
//        return streamurl;
//
//    }
//
//    // After completing background task Dismiss the progress dialog
//    protected void onPostExecute(String file_url) {
//        // dismiss the dialog once done
////        System.out.println(streamurl);
//        Toast.makeText(activity, streamurl, Toast.LENGTH_SHORT).show();
//        if(streamurl == "") {
//            Toast.makeText(activity, fragment.getResources()
//                    .getString(R.string.playback_nosources), Toast.LENGTH_SHORT).show();
//        } else {
//            mSelectedVideo.videoUrl = streamurl;
//            Intent intent = new Intent(this.activity, PlaybackOverlayActivity.class);
//            intent.putExtra(VideoDetailsActivity.VIDEO, this.mSelectedVideo);
//            fragment.startActivity(intent);
//        }
//
//
//    }
//}


interface SeasonResponse {
    void onGetSeasons(List<Video> seasons);
}

class SeasonTask extends AsyncTask<String, String, String> {

    private final Video mSelectedVideo;
    ArrayObjectAdapter show_seasons;
    Context ctx;

    SeasonResponse delegate;

    List<Video> video_list = new ArrayList<>();

    public SeasonTask(Context ctx, SeasonResponse del, Video video) {
        this.mSelectedVideo = video;
        this.ctx = ctx;
        this.delegate = del;
    }

    // Before starting background thread Show Progress Dialog
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // Checking login in background
    protected String doInBackground(String... params) {

        try {
            video_list = Chilly.getInstance(ctx).getSeasonsForShow(mSelectedVideo);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    // After completing background task Dismiss the progress dialog
    protected void onPostExecute(String file_url) {
        delegate.onGetSeasons(video_list);
    }
}
/*
 * Copyright (c) 2015 The Android Open Source Project
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

package com.adisdurakovic.android.chilly.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adisdurakovic.android.chilly.data.Chilly;
import com.bumptech.glide.Glide;
import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.model.Video;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;
    public boolean isEpisode = false;
    private static Drawable mDefaultCardImage = null;
    private static Activity ctx;
    private static String bgUri = "";

    public CardPresenter() {

    }


    public CardPresenter(Activity context){
        ctx = context;
        mDefaultCardImage = ctx.getResources().getDrawable(R.drawable.default_background);
    }




    static class ViewHolder extends Presenter.ViewHolder implements ImageLoaderResponse  {
        private ImageCardView mCardView;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
        }

        public ImageCardView getCardView() {
            return mCardView;
        }

        protected void updateCardViewImage(Video video) {
//            mCardView.getMainImageView().setScaleType(ImageView.ScaleType.CENTER);
//            AsyncImageLoader.LoadImage(new CoverFetcher(mediaWrapper), mCardView);
//            System.out.println(bgUri);
            new ImageLoaderTask(ctx.getApplicationContext(), this, video, mCardView).execute();
        }

        @Override
        public void onLoadFinish(String img_url, ImageCardView cardView, Video video) {
                Glide.with(cardView.getContext())
                        .load(img_url)
                        .error(mDefaultCardImage)
                        .into(cardView.getMainImageView());

        }

    }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mDefaultBackgroundColor =
            ContextCompat.getColor(parent.getContext(), R.color.default_background);
        mSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.selected_background);
        mDefaultCardImage = parent.getResources().getDrawable(R.drawable.movie, null);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }


        };


        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);

        return new ViewHolder(cardView);
    }

    private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;

        // Both background colors should be set because the view's
        // background is temporarily visible during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        Video video = (Video) item;

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setTitleText(video.title);
        cardView.setContentText(video.productionYear);


        if (video.cardImageUrl != null) {
            // Set card size from dimension resources.
            Resources res = cardView.getResources();
            int width = res.getDimensionPixelSize(R.dimen.card_width);
            int height = res.getDimensionPixelSize(R.dimen.card_height);

            if(isEpisode) {
                width = res.getDimensionPixelSize(R.dimen.card_episode_width);
                height = res.getDimensionPixelSize(R.dimen.card_episode_height);
            }

            cardView.setMainImageDimensions(width, height);
            if(video.watched) cardView.setBadgeImage(cardView.getResources().getDrawable(R.drawable.watched_check, null));
            ViewHolder holder = ((ViewHolder) viewHolder);
            holder.updateCardViewImage(video);

//            Glide.with(cardView2.getContext())
//                    .load(video.cardImageUrl)
//                    .error(mDefaultCardImage)
//                    .into(cardView2.getMainImageView());
        }
    }



    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        // Remove references to images so that the garbage collector can free up memory.
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}


interface ImageLoaderResponse {
    void onLoadFinish(String img_url, ImageCardView cardView, Video video);
}


// Background ASYNC Task to login by making HTTP Request
class ImageLoaderTask extends AsyncTask<String, String, String> {

    ImageLoaderResponse delegate = null;
    String img_url = "";
    Context ctx;
    Video video;
    ImageCardView cardView;

    public ImageLoaderTask(Context ctx, ImageLoaderResponse del, Video v, ImageCardView cardView) {
        this.delegate = del;
        this.ctx = ctx;
        this.video = v;
        this.cardView = cardView;
    }

    // Before starting background thread Show Progress Dialog
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        Toast.makeText(activity, "Loading...", Toast.LENGTH_SHORT).show();
    }

    // Checking login in background
    protected String doInBackground(String... params) {

        img_url = Chilly.getInstance(ctx).getTMDBImage(video, "poster");
        return "";

    }

    // After completing background task Dismiss the progress dialog
    protected void onPostExecute(String file_url) {
        delegate.onLoadFinish(img_url, cardView, video);

    }
}
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

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.ui.MainFragment;

public class GridItemPresenter extends Presenter {
    private final BrowseFragment mainFragment;
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;

    public GridItemPresenter(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {

        mDefaultBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.default_background);
        mSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.selected_background);

        TextView view = new AppCompatTextView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateGridItemBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        Resources res = parent.getResources();
        int width = res.getDimensionPixelSize(R.dimen.grid_item_width);
        int height = res.getDimensionPixelSize(R.dimen.grid_item_height);

        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setBackgroundColor(ContextCompat.getColor(parent.getContext(),
                R.color.default_background));
        view.setTextColor(Color.WHITE);
        view.setGravity(Gravity.CENTER);
        return new ViewHolder(view);
    }

    private void updateGridItemBackgroundColor(AppCompatTextView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;

        // Both background colors should be set because the view's
        // background is temporarily visible during animations.
        view.setBackgroundColor(color);
//        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((TextView) viewHolder.view).setText((String) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }

}

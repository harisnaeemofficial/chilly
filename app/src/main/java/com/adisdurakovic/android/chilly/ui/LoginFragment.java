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

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v17.leanback.app.BrandedFragment;
import android.support.v17.leanback.app.ErrorFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.data.Chilly;
import com.adisdurakovic.android.chilly.data.ChillyTasks;
import com.adisdurakovic.android.chilly.data.ListElem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
 * This class demonstrates how to extend ErrorFragment to create an error dialog.
 */
public class LoginFragment extends BrandedFragment implements ChillyTasks.RequestCodeResponse, ChillyTasks.RequestTokenResponse, ChillyTasks.RequestUserResponse {
    private static final boolean TRANSLUCENT = true;
    private final String TAG = "LOGIN";

    private final Handler mHandler = new Handler();
    private SpinnerFragment mSpinnerFragment;

    private boolean hasToken = false;
    private int TOKEN_INTERVAL = 1000 * 5;
    private int TOKEN_EXPIRE = 1000*500;
    private int tokenExpired = 0;
    private ViewGroup mErrorFrame;
    private ImageView mImageView;
    private TextView mTextView;
    private TextView mSiteText;
    private TextView mCodeText;
    private Button mButton;
    private Drawable mDrawable;
    private CharSequence mMessage;
    private String mButtonText;
    private View.OnClickListener mButtonClickListener;
    private Drawable mBackgroundDrawable;
    private boolean mIsBackgroundTranslucent = true;

    /**
     * Sets the default background.
     *
     * @param translucent True to set a translucent background.
     */
    public void setDefaultBackground(boolean translucent) {
        mBackgroundDrawable = null;
        mIsBackgroundTranslucent = translucent;
        updateBackground();
        updateMessage();
    }

    /**
     * Returns true if the background is translucent.
     */
    public boolean isBackgroundTranslucent() {
        return mIsBackgroundTranslucent;
    }

    /**
     * Sets a drawable for the fragment background.
     *
     * @param drawable The drawable used for the background.
     */
    public void setBackgroundDrawable(Drawable drawable) {
        mBackgroundDrawable = drawable;
        if (drawable != null) {
            final int opacity = drawable.getOpacity();
            mIsBackgroundTranslucent = (opacity == PixelFormat.TRANSLUCENT ||
                    opacity == PixelFormat.TRANSPARENT);
        }
        updateBackground();
        updateMessage();
    }

    /**
     * Returns the background drawable.  May be null if a default is used.
     */
    public Drawable getBackgroundDrawable() {
        return mBackgroundDrawable;
    }

    /**
     * Sets the drawable to be used for the error image.
     *
     * @param drawable The drawable used for the error image.
     */
    public void setImageDrawable(Drawable drawable) {
        mDrawable = drawable;
        updateImageDrawable();
    }

    /**
     * Returns the drawable used for the error image.
     */
    public Drawable getImageDrawable() {
        return mDrawable;
    }

    /**
     * Sets the error message.
     *
     * @param message The error message.
     */
    public void setMessage(CharSequence message) {
        mMessage = message;
        updateMessage();
    }

    /**
     * Returns the error message.
     */
    public CharSequence getMessage() {
        return mMessage;
    }

    /**
     * Sets the button text.
     *
     * @param text The button text.
     */
    public void setButtonText(String text) {
        mButtonText = text;
        updateButton();
    }

    /**
     * Returns the button text.
     */
    public String getButtonText() {
        return mButtonText;
    }

    /**
     * Set the button click listener.
     *
     * @param clickListener The click listener for the button.
     */
    public void setButtonClickListener(View.OnClickListener clickListener) {
        mButtonClickListener = clickListener;
        updateButton();
    }

    /**
     * Returns the button click listener.
     */
    public View.OnClickListener getButtonClickListener() {
        return mButtonClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trakt_login, container, false);

        mErrorFrame = (ViewGroup) root.findViewById(android.support.v17.leanback.R.id.error_frame);
        updateBackground();

//        installTitleView(inflater, mErrorFrame, savedInstanceState);

        mImageView = (ImageView) root.findViewById(android.support.v17.leanback.R.id.image);
        setImageDrawable((getActivity().getResources().getDrawable(R.drawable.logo_trakt)));
        updateImageDrawable();

        mTextView = (TextView) root.findViewById(android.support.v17.leanback.R.id.message);
        updateMessage();

        mButton = (Button) root.findViewById(android.support.v17.leanback.R.id.button);
        updateButton();

        mSiteText = (TextView) root.findViewById(R.id.text_url);
        mCodeText = (TextView) root.findViewById(R.id.text_code);

        Paint.FontMetricsInt metrics = getFontMetricsInt(mTextView);
        int underImageBaselineMargin = container.getResources().getDimensionPixelSize(
                android.support.v17.leanback.R.dimen.lb_error_under_image_baseline_margin);
        setTopMargin(mTextView, underImageBaselineMargin + metrics.ascent);

        int underMessageBaselineMargin = container.getResources().getDimensionPixelSize(
                android.support.v17.leanback.R.dimen.lb_error_under_message_baseline_margin);
        setTopMargin(mButton, underMessageBaselineMargin - metrics.descent);

        return root;
    }

    private void updateBackground() {
        if (mErrorFrame != null) {
            if (mBackgroundDrawable != null) {
                mErrorFrame.setBackground(mBackgroundDrawable);
            } else {
                mErrorFrame.setBackgroundColor(mErrorFrame.getResources().getColor(
                        mIsBackgroundTranslucent ?
                                android.support.v17.leanback.R.color.lb_error_background_color_translucent :
                                android.support.v17.leanback.R.color.lb_error_background_color_opaque));
            }
        }
    }

    private void updateMessage() {
        if (mTextView != null) {
            mTextView.setText(mMessage);
            mTextView.setVisibility(TextUtils.isEmpty(mMessage) ? View.GONE : View.VISIBLE);
        }
    }

    private void updateImageDrawable() {
        if (mImageView != null) {
            mImageView.setImageDrawable(mDrawable);
            mImageView.setVisibility(mDrawable == null ? View.GONE : View.VISIBLE);
        }
    }

    private void updateButton() {
        if (mButton != null) {
            mButton.setText(mButtonText);
            mButton.setOnClickListener(mButtonClickListener);
//            mButton.setVisibility(TextUtils.isEmpty(mButtonText) ? View.GONE : View.VISIBLE);
            mButton.requestFocus();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mErrorFrame.requestFocus();
        new ChillyTasks.RequestCodeTask(getActivity().getApplicationContext(), this).execute();
    }

    private static Paint.FontMetricsInt getFontMetricsInt(TextView textView) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textView.getTextSize());
        paint.setTypeface(textView.getTypeface());
        return paint.getFontMetricsInt();
    }

    private static void setTopMargin(TextView textView, int topMargin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
        lp.topMargin = topMargin;
        textView.setLayoutParams(lp);
    }

    @Override
    public void onReceiveCode(final JSONObject loginResponse) {

        Log.d(TAG, "loginResponse: " + loginResponse);
//        if(loginResponse == null) return;

//        getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();

        setImageDrawable(getResources().getDrawable(R.drawable.logo_trakt, null));
        try {
            mSiteText.setText(loginResponse.getString("verification_url"));
            mCodeText.setText(loginResponse.getString("user_code"));
        } catch (Exception e) {
            e.printStackTrace();
        }

//            setMessage(loginResponse.getString("user_code"));
            final ChillyTasks.RequestTokenResponse me = this;
            mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!hasToken && tokenExpired < TOKEN_EXPIRE) {
                        new ChillyTasks.RequestTokenTask(getActivity().getApplicationContext(), me, loginResponse.getString("device_code")).execute();
                        mHandler.postDelayed(this, TOKEN_INTERVAL);
                        tokenExpired += TOKEN_INTERVAL;
                    }
                } catch (Exception e) {

                }
            }
        }, TOKEN_INTERVAL);



        setDefaultBackground(TRANSLUCENT);

        setButtonText(getResources().getString(R.string.dismiss_error));
        setButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getFragmentManager().beginTransaction().remove(LoginFragment.this).commit();
                getFragmentManager().popBackStack();
            }
        });


    }

    @Override
    public void onReceiveToken(JSONObject tokenResponse) {

//        if(tokenResponse == null) return;

        if(tokenResponse != null && Chilly.getInstance(getActivity().getApplicationContext()).saveSettings("trakt_token", tokenResponse.toString())) {
            Log.d(TAG, "tokenResponse: " + tokenResponse);
            hasToken = true;
            try {
                new ChillyTasks.RequestUserTask(getActivity().getApplicationContext(), this, tokenResponse.getString("access_token")).execute();
            } catch (JSONException e) {

            }
        }

    }

    @Override
    public void onReceiveUser(JSONObject userResponse) {
        Log.d(TAG, "userResponse: " + userResponse);
//        if(userResponse == null) return;
        Chilly.getInstance(getActivity().getApplicationContext()).saveSettings("trakt_user", userResponse.toString());
        getFragmentManager().beginTransaction().remove(LoginFragment.this).commit();
        getFragmentManager().popBackStack();
        Toast.makeText(getActivity().getApplicationContext(), "Login successfull", Toast.LENGTH_LONG).show();

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


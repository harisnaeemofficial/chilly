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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v17.leanback.app.ErrorFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.data.Chilly;
import com.adisdurakovic.android.chilly.data.ListElem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
 * This class demonstrates how to extend ErrorFragment to create an error dialog.
 */
public class LoginFragment extends ErrorFragment implements RequestCodeResponse, RequestTokenResponse, RequestUserResponse {
    private static final boolean TRANSLUCENT = true;

    private final Handler mHandler = new Handler();
    private SpinnerFragment mSpinnerFragment;

    private boolean hasToken = false;
    private int TOKEN_INTERVAL = 1000 * 5;
    private int TOKEN_EXPIRE = 1000*500;
    private int tokenExpired = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.app_name));

        mSpinnerFragment = new SpinnerFragment();
        getFragmentManager().beginTransaction().add(R.id.main_frame, mSpinnerFragment).commit();
    }

    @Override
    public void onReceiveCode(final JSONObject loginResponse) {
        System.out.println(loginResponse);
        getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();

        setImageDrawable(getResources().getDrawable(R.drawable.lb_ic_sad_cloud, null));

//            setMessage(loginResponse.getString("user_code"));
            final RequestTokenResponse me = this;
            mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!hasToken && tokenExpired < TOKEN_EXPIRE) {
                        new RequestTokenTask(getActivity().getApplicationContext(), me, loginResponse.getString("device_code")).execute();
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


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("trakt_token", tokenResponse.toString());

        if(editor.commit()) {
            System.out.println("COMMIT SUCCESS");
            System.out.println(tokenResponse);
            hasToken = true;
            try {
                new RequestUserTask(getActivity().getApplicationContext(), this, tokenResponse.getString("access_token")).execute();
            } catch (JSONException e) {

            }
        } else {
            System.out.println("ERROR on COMMIT");
        }

    }

    @Override
    public void onReceiveUser(JSONObject userResponse) {

        System.out.println(userResponse);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("trakt_user", userResponse.toString());
        editor.commit();

    }

    @Override
    public void onStart() {
        super.onStart();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
//                setErrorContent();
//            }
//        }, TIMER_DELAY);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String string_token = sharedPreferences.getString("trakt_token", "NOTOKEN");

        if(string_token.equals("")) {
            new RequestCodeTask(getActivity().getApplicationContext(), this).execute();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
        getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
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


interface RequestCodeResponse {
    void onReceiveCode(JSONObject loginResponse);
}


class RequestCodeTask extends AsyncTask<String, String, String> {

    RequestCodeResponse delegate;
    Context ctx;
    JSONObject loginResponse = new JSONObject();

    public RequestCodeTask(Context context, RequestCodeResponse del) {
        this.delegate = del;
        this.ctx = context;
    }

    // Before starting background thread Show Progress Dialog
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // Checking login in background
    protected String doInBackground(String... params) {


        Chilly chilly = new Chilly(ctx);
        try {
            loginResponse = chilly.getCodeFromTrakt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loginResponse.toString();

    }

    // After completing background task Dismiss the progress dialog
    protected void onPostExecute(String somestring) {
        // dismiss the dialog once done
        delegate.onReceiveCode(loginResponse);

    }
}


interface RequestTokenResponse {
    void onReceiveToken(JSONObject loginResponse);
}


class RequestTokenTask extends AsyncTask<String, String, String> {

    RequestTokenResponse delegate;
    Context ctx;
    JSONObject tokenResponse = new JSONObject();
    String code;

    public RequestTokenTask(Context context, RequestTokenResponse del, String device_code) {
        this.delegate = del;
        this.ctx = context;
        this.code = device_code;
    }

    // Before starting background thread Show Progress Dialog
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // Checking login in background
    protected String doInBackground(String... params) {

        final Chilly chilly = new Chilly(ctx);



        try {
            tokenResponse = chilly.getTokenFromTrakt(code);
        } catch (Exception e) {
            e.printStackTrace();
        }




        return tokenResponse.toString();

    }

    protected void onPostExecute(String somestring) {
        try {
            if(tokenResponse.getString("access_token") != null) {
                delegate.onReceiveToken(tokenResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


interface RequestUserResponse {
    void onReceiveUser(JSONObject userResponse);
}


class RequestUserTask extends AsyncTask<String, String, String> {

    RequestUserResponse delegate;
    Context ctx;
    JSONObject userResponse = new JSONObject();
    String code;

    public RequestUserTask(Context context, RequestUserResponse del, String access_token) {
        this.delegate = del;
        this.ctx = context;
        this.code = access_token;
    }

    // Before starting background thread Show Progress Dialog
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // Checking login in background
    protected String doInBackground(String... params) {

        final Chilly chilly = new Chilly(ctx);



        try {
            userResponse = chilly.getUserFromTrakt(code);
        } catch (Exception e) {
            e.printStackTrace();
        }




        return userResponse.toString();

    }

    protected void onPostExecute(String somestring) {
        try {
            delegate.onReceiveUser(userResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
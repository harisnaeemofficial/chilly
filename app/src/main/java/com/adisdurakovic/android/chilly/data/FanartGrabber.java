package com.adisdurakovic.android.chilly.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Created by add on 21/05/2017.
 */

public class FanartGrabber {

    public static JSONObject getMedia(String url) throws IOException, JSONException {

        JSONObject media;


        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("GET");

        media = new JSONObject(HTTPGrabber.getContentFromURL(urlConnection));


        return media;
    }

}

package com.adisdurakovic.android.chilly.data;

import android.content.Context;

import com.adisdurakovic.android.chilly.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Iterator;

/**
 * Created by add on 21/05/2017.
 */

public class TraktGrabber {

    public static JSONObject getInfo(String url, String api_version, String api_key) throws IOException, JSONException {

        JSONObject info;

        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("trakt-api-version", api_version);
        urlConnection.setRequestProperty("trakt-api-key", api_key);

        info = new JSONObject(HTTPGrabber.getContentFromURL(urlConnection));


        return info;

    }

}

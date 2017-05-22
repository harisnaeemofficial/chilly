package com.adisdurakovic.android.chilly.data;

import android.content.Context;

import com.adisdurakovic.android.chilly.R;

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

    public static JSONObject getInfo(String url, String api_version, String api_key) {

        JSONObject info = null;

        BufferedReader reader = null;

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("trakt-api-version", api_version);
            urlConnection.setRequestProperty("trakt-api-key", api_key);
            try {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                        "utf-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                info = new JSONObject(sb.toString());

            } finally {
                urlConnection.disconnect();
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }

        } catch (Exception e) {

        }

        return info;

    }

}

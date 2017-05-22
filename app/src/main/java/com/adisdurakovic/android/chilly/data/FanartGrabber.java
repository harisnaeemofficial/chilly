package com.adisdurakovic.android.chilly.data;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Created by add on 21/05/2017.
 */

public class FanartGrabber {

    public static JSONObject getMedia(String url) {

        JSONObject media = null;

        BufferedReader reader = null;

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
            urlConnection.setRequestMethod("GET");

            try {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                        "utf-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                media = new JSONObject(sb.toString());

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

        return media;
    }

}

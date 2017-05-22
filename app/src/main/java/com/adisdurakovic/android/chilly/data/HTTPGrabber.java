package com.adisdurakovic.android.chilly.data;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Created by add on 22/05/2017.
 */

public class HTTPGrabber {

    public static String getContentFromURL(HttpURLConnection urlConnection) throws IOException {

        String content = "";

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                    "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            content = sb.toString();

        } finally {
            urlConnection.disconnect();
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        return content;

    }

}

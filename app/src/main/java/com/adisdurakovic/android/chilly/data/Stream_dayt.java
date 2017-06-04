package com.adisdurakovic.android.chilly.data;

import android.util.Log;

import com.adisdurakovic.android.chilly.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;

/**
 * Created by add on 22/05/2017.
 */

public class Stream_dayt extends StreamProvider {

    String TAG="DAYT";


    @Override
    public List<StreamSource> getStreamSources(Video video) {

        String videotitle = video.title.toLowerCase().replaceAll("[^a-z0-9A-Z ]", "").replace(" ", "-");
        String url = "http://cyro.se/watch/" + videotitle;

        if(video.videoType.equals("episode")) {
            videotitle = video.episodeShow.title.toLowerCase().replaceAll("[^a-z0-9A-Z ]", "").replace(" ", "-");
            url = "http://cyro.se/watch/" + videotitle + "/s" + video.seasonNumber + "/e" + video.episodeNumber;
        }

        Log.d(TAG, "url: " + url);

        List<StreamSource> list = new ArrayList<>();

        request = new Request.Builder().url(url).build();


        try {



            Document doc = Jsoup.parse(client.newCall(request).execute().body().string());

            Elements link_elems = doc.select("#5throw a");

            if(link_elems == null) return list;

            for(Element link_elem : link_elems) {
                String link = link_elem.attr("href");
                Log.d(TAG, "link: " + link);
                if(link.contains("yadi.sk") || link.contains("mail.ru")) {

                    String src_url = (link.contains("yadi.sk") ? yandex(link) : cldmailru(link));

                    StreamSource ss = new StreamSource();
                    ss.quality = 720;
                    ss.url = src_url;
                    Log.d(TAG, "source: " + ss.url);
                    ss.provider = TAG;
                    ss.videosource = "CDN";
                    list.add(ss);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }






        return list;
    }




}
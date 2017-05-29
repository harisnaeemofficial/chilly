package com.adisdurakovic.android.chilly.data;

import android.graphics.Movie;

import com.adisdurakovic.android.chilly.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by add on 22/05/2017.
 */

public class Stream_123movieshd extends StreamProvider {

    @Override
    public List<StreamSource> getMovieStreamURL(Video video) throws IOException {

        String stream_url = "";
        String videotitle = video.title.toLowerCase().replaceAll("[^a-z0-9A-Z ]", "").replace(" ", "-");
        String search_url_movie = "https://123movieshd.tv/movie/search/" + videotitle;
        String player_url = "";

//        String search_title = movie.getTitle().replace(" ", "-").toLowerCase();

//        String url = base_url + "/movie/search/" + search_title;

        HttpURLConnection urlConnection_1 = (HttpURLConnection) new java.net.URL(search_url_movie).openConnection();
        urlConnection_1.setRequestMethod("GET");

        String movie_list = HTTPGrabber.getContentFromURL(urlConnection_1);
        Document doc = Jsoup.parse(movie_list);
        Element link_1 = doc.select("a.ml-mask").first();

        if(link_1 != null) {
            player_url = "https://123movieshd.tv" + link_1.attr("href") + "/watching.html";
        }


        HttpURLConnection urlConnection_2 = (HttpURLConnection) new java.net.URL(player_url).openConnection();
        urlConnection_2.setRequestMethod("GET");

        String player_info = HTTPGrabber.getContentFromURL(urlConnection_2);

        Document doc2 = Jsoup.parse(player_info);
        Element link_2 = doc2.select("div.les-content a").first();

        String link_url = "";

        if(link_2 != null) {
            link_url = link_2.attr("player-data");
        }



        HttpURLConnection urlConnection_3 = (HttpURLConnection) new java.net.URL(link_url).openConnection();
        urlConnection_3.setRequestMethod("GET");

        String stream_detail = HTTPGrabber.getContentFromURL(urlConnection_3);


        Pattern pattern = Pattern.compile("sources:.*?file.*?[\\]]");
//        Pattern pattern = Pattern.compile("(https:.*?redirector.*?)[\\'\\\"]");
        Matcher matcher = pattern.matcher(stream_detail);

        List<StreamSource> source_list = new ArrayList<>();

        while(matcher.find()) {
            String jsonString = "{" + matcher.group().replace("sources:", "'sources':").replace("file:", "'file':").replace("label:", "'label':") + "}";
            try {
                JSONObject source_object = new JSONObject(jsonString);
                JSONArray sources = source_object.getJSONArray("sources");

                for(int i = 0; i < sources.length(); i++) {
                    JSONObject source = sources.getJSONObject(i);
                    StreamSource ss = new StreamSource();
                    ss.quality = source.optString("label").replace(" P", "");
                    ss.url = source.getString("file");
                    ss.provider = "123MOVIESHD";
                    ss.videosource = "GVIDEO";
                    if(!ss.quality.equals("Auto")) {
                        source_list.add(ss);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            stream_url = matcher.group().replace("'","");
        }


        return source_list;


    }


}
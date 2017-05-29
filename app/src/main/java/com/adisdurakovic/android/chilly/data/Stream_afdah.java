package com.adisdurakovic.android.chilly.data;

import com.adisdurakovic.android.chilly.model.Video;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by add on 22/05/2017.
 */

public class Stream_afdah extends StreamProvider {

    @Override
    public List<StreamSource> getMovieStreamURL(Video video) throws IOException {

        String stream_url = "";
        String videotitle = video.title.toLowerCase().replaceAll("[^a-z0-9A-Z ]", "").replace(" ", "+");
        String search_url_movie = "https://putlockerhd.co/results?q=" + videotitle;
        String player_url = "";


        HttpURLConnection urlConnection_1 = (HttpURLConnection) new java.net.URL(search_url_movie).openConnection();
        urlConnection_1.setRequestMethod("GET");

        String movie_list = HTTPGrabber.getContentFromURL(urlConnection_1);
        Document doc = Jsoup.parse(movie_list);
        Element source = doc.select("div.cell_container a").first();

        if(source != null) {
            player_url = "https://putlockerhd.co" + source.attr("href");
        }


        HttpURLConnection urlConnection_2 = (HttpURLConnection) new java.net.URL(player_url).openConnection();
        urlConnection_2.setRequestMethod("GET");

        String player_info = HTTPGrabber.getContentFromURL(urlConnection_2);

        Document doc2 = Jsoup.parse(player_info);
        Element link = doc2.select("div.les-content a").first();

        String link_url = "";

        if(link != null) {
            link_url = link.attr("player-data");
        }



        HttpURLConnection urlConnection_3 = (HttpURLConnection) new java.net.URL(link_url).openConnection();
        urlConnection_3.setRequestMethod("GET");

        String stream_detail = HTTPGrabber.getContentFromURL(urlConnection_3);


        Pattern pattern = Pattern.compile("(https:.*?redirector.*?)[\\'\\\"]");
        Matcher matcher = pattern.matcher(stream_detail);

        List<StreamSource> source_list = new ArrayList<>();

        while(matcher.find()) {
//            System.out.println(matcher.group());
            stream_url = matcher.group().replace("'","");
        }

        return source_list;


    }


}
package com.adisdurakovic.android.chilly.stream;

import android.util.Log;

import com.adisdurakovic.android.chilly.model.Video;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;

/**
 * Created by add on 22/05/2017.
 */

public class Stream_123movieshd extends StreamProvider {

    String TAG = "123MHD";


    @Override
    public List<StreamSource> getStreamSources(Video video) {


        List<StreamSource> list = new ArrayList<>();

        String title = video.title;

        if(video.videoType.equals("episode")) {
            title = video.episodeShow.title + " season " + video.seasonNumber;
        }

        String videotitle = title.toLowerCase().replaceAll("[^a-z0-9A-Z ]", "").replace(" ", "-");
        String search_url = "https://123movieshd.tv/movie/search/" + videotitle;

        Log.d(TAG, "search_url: " + search_url);

        request = new Request.Builder().url(search_url).build();


        try {


            String movie_list = client.newCall(request).execute().body().string();


            Document doc = Jsoup.parse(movie_list);
            Element link_1 = doc.select("a.ml-mask").first();

            if(link_1 == null) return list;

            String player_url = "https://123movieshd.tv" + link_1.attr("href") + "/watching.html";
            Log.d(TAG, "player_url: " + player_url);

            request = new Request.Builder().url(player_url).build();

            String player_info = client.newCall(request).execute().body().string();

            Document doc2 = Jsoup.parse(player_info);
            Elements link_2_elems = doc2.select("div.les-content a");

            for(Element link_2_elem : link_2_elems) {

                if(video.videoType.equals("episode") && !link_2_elem.attr("episode-data").equals(video.episodeNumber)) continue;

                String link_2_url = link_2_elem.attr("player-data");

                Log.d(TAG, "link: " + link_2_url);

                request = new Request.Builder().url(link_2_url).build();

                String stream_detail = client.newCall(request).execute().body().string();


//                Pattern pattern = Pattern.compile("sources:.*?file.*?[\\]]");
                Pattern pattern = Pattern.compile("(https:.*?redirector.*?)[\\'\\\"]");
                Matcher matcher = pattern.matcher(stream_detail);


                while(matcher.find()) {

                    String src_url = matcher.group();

                    StreamSource ss = new StreamSource();
                    ss.quality = getQualityFromGoogleURL(src_url);
                    ss.url = src_url;
                    Log.d(TAG, "source: " + src_url);
                    ss.provider = TAG;
                    ss.videosource = "GVIDEO";

                }


            }



        } catch (IOException e) {
            e.printStackTrace();
        }




        return list;
    }



}
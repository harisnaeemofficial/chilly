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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by add on 22/05/2017.
 */

public class Stream_miradetodo extends StreamProvider {

    String TAG = "MIRADETODO";

    @Override
    public List<StreamSource> getStreamSources(Video video) {
        List<StreamSource> list = new ArrayList<>();

        String imdb = video.imdb_id;

        if(video.videoType.equals("episode")) {
            imdb = video.episodeShow.imdb_id;
        }


        request = new Request.Builder()
                .url("http://www.imdb.com/title/" + imdb)
                .addHeader("Accept-Language", "es-AR")
                .build();


        try {



            Document doc = Jsoup.parse(client.newCall(request).execute().body().string());
            String videotitle = URLEncoder.encode(doc.title().replaceAll("(?:\\(|\\s)\\d{4}.+", ""), "utf-8");

            if(video.videoType.equals("episode")) {
                videotitle = URLEncoder.encode(doc.title().replaceAll("(?:\\(|\\s)\\(TV Series.+", ""), "utf-8");
            }


//        String videotitle = title.replaceAll("[^a-z0-9A-Z ]", "").replace(" ", "_") + "_" + 2016 + ".html";
            String base_link = "http://miradetodo.io";
            String search_url = base_link + "/?s=" + videotitle;

            Log.d(TAG, "search_url: " + search_url);

            request = new Request.Builder()
                    .url(search_url)
                    .build();


            Element linkelem = Jsoup.parse(client.newCall(request).execute().body().string()).select("div.item a").first();
            if(linkelem == null) return list;

            String link_1 = linkelem.attr("href");

            if(video.videoType.equals("episode") && link_1.endsWith("/")) {
                link_1 = link_1.substring(0, link_1.length() - 1);
                link_1 = link_1.replace("/series/", "/episodio/").replace("-actualidad", "").replaceAll("-[0-9]{4}", "") + "-" + video.seasonNumber + "x" + video.episodeNumber;
            }



            Log.d(TAG, link_1);

            request = new Request.Builder().url(link_1).build();

            Elements frame_link_elems = Jsoup.parse(client.newCall(request).execute().body().string()).select("div.movieplay iframe, div.embed2 div iframe");

            List<String> dupes = new ArrayList<>();

            for(Element frame_link_elem : frame_link_elems) {

                String link_2 = frame_link_elem.attr("data-lazy-src");
                Log.d(TAG, "link_2: " + link_2);

                if(!link_2.contains("id=")) continue;

                String sid = getQueryParams(link_2).get("id").get(0);
                Log.d(TAG, "sid: " + sid);
                if(dupes.contains(sid)) continue;

                dupes.add(sid);


                request = new Request.Builder()
                        .url(link_2)
                        .addHeader("X-Requested-With", "XMLHttpRequest")
                        .addHeader("Referer", link_2)
                        .build();

                Element link_3_elem = Jsoup.parse(client.newCall(request).execute().body().string()).select("a").first();
                if(link_3_elem == null) continue;


                String link_3 = link_3_elem.attr("href");

                if(link_3.startsWith("//")) {
                    link_3 = "http:" + link_3;
                }



                Log.d(TAG, "link_3: " + link_3);

                request = new Request.Builder()
                        .url(link_3)
                        .addHeader("X-Requested-With", "XMLHttpRequest")
                        .addHeader("Referer", link_3)
                        .build();

                Pattern pattern = Pattern.compile("sources\\s*:\\s*\\[(.+?)\\]");
                Matcher matcher = pattern.matcher(client.newCall(request).execute().body().string());

                while(matcher.find()) {
                    String src_url = matcher.group();
                    StreamSource ss = new StreamSource();
                    ss.quality = getQualityFromGoogleURL(src_url);
                    ss.url = src_url;
                    Log.d(TAG, ss.url);
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
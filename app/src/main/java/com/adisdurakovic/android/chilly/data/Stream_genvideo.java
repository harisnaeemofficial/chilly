package com.adisdurakovic.android.chilly.data;

import com.adisdurakovic.android.chilly.model.Video;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by add on 22/05/2017.
 */

public class Stream_genvideo extends StreamProvider {


    @Override
    public List<StreamSource> getStreamSources(Video video) throws IOException {


        List<StreamSource> list = new ArrayList<>();

        String title = "Captain America: Civil War";

        String videotitle = title.replaceAll("[^a-z0-9A-Z ]", "").replace(" ", "_") + "_" + 2016 + ".html";
        String base_link = "http://genvideos.com";
        String search_url = base_link + "/watch_" + videotitle;

//        String search_title = movie.getTitle().replace(" ", "-").toLowerCase();

//        String url = base_url + "/movie/search/" + search_title;
        System.out.println(search_url);
        request = new Request.Builder()
                .url(search_url)
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .build();

        response = client.newCall(request).execute();

        System.out.println(response.headers());
        System.out.println("-------------------");


        request = new Request.Builder()
                .url(base_link + "/av")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .addHeader("Referer", search_url)
                .build();

        response = client.newCall(request).execute();

        System.out.println(response.headers());

        System.out.println("-------------------");

        Map<String, String> params = new HashMap<>();
        params.put("v", videotitle);
        JSONObject parameter = new JSONObject(params);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), parameter.toString());

        request = new Request.Builder()
                .url(base_link + "/video_info/frame")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .addHeader("X-Requested-With", "XMLHttpRequest")
//                .post(body)
                .post(body)
                .build();
        response = client.newCall(request).execute();
        System.out.println(response.headers());
        System.out.println("-------------------");

        try {
            JSONObject resp = new JSONObject(response.body().string());
            System.out.println(resp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Pattern pattern = Pattern.compile("var view_id=\"([0-9]*)\"");
////        Pattern pattern = Pattern.compile("(https:.*?redirector.*?)[\\'\\\"]");
//        Matcher matcher = pattern.matcher(client.newCall(request).execute().body().string());
//
//        String view_id = "";
//
//        while(matcher.find()) {
//            view_id = matcher.group().replace("var view_id=", "").replace("\"", "");
//            if(!view_id.equals("")) break;
//        }
//
//        String player_url = "http://player.dizigold.org/?id=" + view_id + "&s=1&dil=or";
//
//        request = new Request.Builder().url(player_url).build();


//        HttpURLConnection urlConnection_1 = (HttpURLConnection) new java.net.URL(search_url_episode).openConnection();
//        urlConnection_1.setRequestMethod("GET");
//
//        String movie_list = HTTPGrabber.getContentFromURL(urlConnection_1);
//        Document doc = Jsoup.parse(movie_list);
//        Element link_1 = doc.select("a.ml-mask").last();
//
//        if(link_1 != null) {
//            player_url = "https://123movieshd.tv" + link_1.attr("href") + "/watching.html";
//        }
//
//
//        HttpURLConnection urlConnection_2 = (HttpURLConnection) new java.net.URL(player_url).openConnection();
//        urlConnection_2.setRequestMethod("GET");
//
//        String player_info = HTTPGrabber.getContentFromURL(urlConnection_2);
//
//        Document doc2 = Jsoup.parse(player_info);
//        Elements episode_links = doc2.select("div.les-content a");
//
//        Element link_2 = null;
//
//        for(Element episode_link : episode_links) {
//            if(episode_link.attr("episode-data").equals(video.episodeNumber)) {
//                link_2 = episode_link;
//                break;
//            }
//        }
//
//        String link_url = "";
//
//        if(link_2 != null) {
//            link_url = link_2.attr("player-data");
//        }
//
//        System.out.println(link_url);
//
//        HttpURLConnection urlConnection_3 = (HttpURLConnection) new java.net.URL(link_url).openConnection();
//        urlConnection_3.setRequestMethod("GET");
//
//        String stream_detail = HTTPGrabber.getContentFromURL(urlConnection_3);


//        Pattern pattern = Pattern.compile("sources:.*?file.*?[\\]]");
////        Pattern pattern = Pattern.compile("(https:.*?redirector.*?)[\\'\\\"]");
//        Matcher matcher = pattern.matcher(stream_detail);
//
//
//        while(matcher.find()) {
//            String jsonString = "{" + matcher.group().replace("sources:", "'sources':").replace("file:", "'file':").replace("label:", "'label':") + "}";
//            try {
//                JSONObject source_object = new JSONObject(jsonString);
//                JSONArray sources = source_object.getJSONArray("sources");
//
//                for(int i = 0; i < sources.length(); i++) {
//                    JSONObject source = sources.getJSONObject(i);
//                    StreamSource ss = new StreamSource();
//                    ss.quality = source.optString("label").replace(" P", "");
//                    ss.url = source.getString("file");
//                    ss.provider = "123MOVIESHD";
//                    ss.videosource = "GVIDEO";
//                    if(!ss.quality.equals("Auto")) {
//                        list.add(ss);
//                    }
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

        return list;
    }


}
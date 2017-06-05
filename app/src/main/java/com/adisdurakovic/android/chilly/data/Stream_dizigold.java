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
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by add on 22/05/2017.
 */

public class Stream_dizigold extends StreamProvider {


    String TAG = "DIZIGOLD";


    @Override
    public List<StreamSource> getStreamSources(Video video) throws IOException {


        List<StreamSource> list = new ArrayList<>();

        String title = video.episodeShow.title;

        String videotitle = title.toLowerCase().replaceAll("[^a-z0-9A-Z ]", "").replace(" ", "-");

        String base_url = "http://www.dizigold1.com";
        String search_url_episode = base_url + "/" + videotitle + "/" + video.seasonNumber + "-sezon/" + video.episodeNumber + "-bolum";

//        String search_title = movie.getTitle().replace(" ", "-").toLowerCase();

//        String url = base_url + "/movie/search/" + search_title;
        Log.d(TAG, "search_url: " + search_url_episode);
        request = new Request.Builder().url(search_url_episode).build();

        Pattern pattern = Pattern.compile("var view_id=\"([0-9]*)\"");
//        Pattern pattern = Pattern.compile("(https:.*?redirector.*?)[\\'\\\"]");
        Matcher matcher = pattern.matcher(client.newCall(request).execute().body().string());

        String view_id = "";

        while(matcher.find()) {
            view_id = matcher.group().replace("var view_id=", "").replace("\"", "");
            if(!view_id.equals("")) break;
        }


        String player_url = "http://player.dizigold1.com/?id=" + view_id + "&s=1&dil=tr";
        Log.d(TAG, "player_url: " + player_url);

        request = new Request.Builder()
                .url(player_url)
                .addHeader("Referer", base_url)
                .build();

        String respbody = client.newCall(request).execute().body().string();
        Elements iframes = Jsoup.parse(respbody).select("iframe");
        for(Element iframe : iframes) {
            Log.d(TAG, "iframe_url: " + iframe.attr("src"));
        }

        return list;
    }


}
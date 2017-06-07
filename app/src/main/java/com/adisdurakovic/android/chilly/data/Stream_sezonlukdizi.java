package com.adisdurakovic.android.chilly.data;

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

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by add on 22/05/2017.
 */

public class Stream_sezonlukdizi extends StreamProvider {

    String TAG="SEZONLUKDIZI";


    @Override
    public List<StreamSource> getStreamSources(Video video) {

        List<StreamSource> list = new ArrayList<>();
        if(!video.videoType.equals("episode")) return list;

        String videotitle = video.episodeShow.title.toLowerCase().replaceAll("[^a-z0-9A-Z ]", "").replace(" ", "-");
        String base_link = "http://sezonlukdizi.net";
        String url = base_link + "/" + videotitle + "/" + video.seasonNumber + "-sezon-" + video.episodeNumber + "-bolum.html";
        String video_url = base_link + "/ajax/dataEmbed.asp";



        try {

            Elements id_elems = null;

            for(int i = 1; i <= 3; i++) {
                request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                        .build();
                response = client.newCall(request).execute();
                String res = response.body().string();
                Log.d(TAG, "res: " + res);
                Document doc = Jsoup.parse(res);
                Log.d(TAG, "try " + i + ": " + response.request().url());

                id_elems = doc.select("div.mediv[data-id]");
                if(id_elems.size() > 0) break;
            }


            if(id_elems == null) return list;

            for(Element id_elem : id_elems) {

                String post = "id=" + id_elem.attr("data-id");
                Log.d(TAG, "id: " + id_elem.attr("data-id"));

                RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"), post);
                String iframe_src = null;

                Element iframe_elem = null;

                request = new Request.Builder()
                        .url(video_url)
                        .addHeader("Referer", url)
                        .post(body)
                        .build();


                String resp = client.newCall(request).execute().body().string();
                iframe_elem = Jsoup.parse(resp).select("iframe").first();
                if(iframe_elem == null) return list;
                iframe_src = iframe_elem.attr("src");

                if(!iframe_src.startsWith("http")) {
                    iframe_src = "http:" + iframe_src;
                }
                Log.d(TAG, "iframe_src: " + iframe_src);

                request = new Request.Builder().url(iframe_src).build();

                Pattern pattern = Pattern.compile("\"?file\"?\\s*:\\s*\"([^\"]+)\"");
                String respbody = client.newCall(request).execute().body().string();
                Matcher matcher = pattern.matcher(respbody);


                while(matcher.find()) {
                    Log.d(TAG, "match: " + matcher.group());
                    String redirect_link = matcher.group().replace("file:", "").replace("file\":", "").replace("\"", "").replace("\\", "").replace(" ", "");
                    if(!redirect_link.startsWith("http")) {
                        if(!redirect_link.contains("sezonlukdizi.net")) {
                            redirect_link = base_link + redirect_link;
                        } else {
                            redirect_link = "http:" + redirect_link;
                        }
                    }
                    Log.d(TAG, "redirect_link: " + redirect_link);

                    request = new Request.Builder().url(redirect_link).build();
                    response = client.newCall(request).execute();
                    String src_url = response.request().url().toString();

                    if(src_url.contains("google")) {
                        StreamSource ss = new StreamSource();
                        ss.quality = getQualityFromGoogleURL(src_url);
                        ss.url = src_url;
                        Log.d(TAG, "source: " + src_url);
                        ss.provider = TAG;
                        ss.videosource = "GVIDEO";
                        list.add(ss);
                    }

                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }






        return list;
    }




}
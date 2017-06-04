package com.adisdurakovic.android.chilly.data;

import com.adisdurakovic.android.chilly.model.Video;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by add on 22/05/2017.
 */

public class StreamProvider {

    OkHttpClient client;
    Request request;
    Response response;

    public StreamProvider() {
         client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());

                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();
    }



    public List<StreamSource> getStreamSources(Video video) throws IOException {
        List<StreamSource> urllist = new ArrayList<>();


        return urllist;
    }



    public String cldmailru(String link) {

        String url = "";
        String v = link.split("public")[1];

        request = new Request.Builder()
                .url(link)
                .build();

        try {
            String resp = client.newCall(request).execute().body().string();
            String tok = "";
            String url_2 = "";

            Pattern tok_pattern = Pattern.compile("\"tokens\"\\s*:\\s*\\{\\s*\"download\"\\s*:\\s*\"([^\"]+)");
            Matcher tok_matcher = tok_pattern.matcher(resp);

            Pattern url_pattern = Pattern.compile("\"weblink_get\"\\s*:\\s*\\[.+?\"url\"\\s*:\\s*\"([^\"]+)");
            Matcher url_matcher = url_pattern.matcher(resp);

            while(url_matcher.find()) {
                url_2 = "{" + url_matcher.group() + "\"}]}";
            }

            while(tok_matcher.find()) {
                tok = "{"+tok_matcher.group()+"\"}}";
            }

            try {
                JSONObject tok_json = new JSONObject(tok);
                JSONObject url_json = new JSONObject(url_2);

                tok = tok_json.getJSONObject("tokens").getString("download");
                url_2 = url_json.getJSONArray("weblink_get").getJSONObject(0).getString("url");

            } catch (JSONException e) {
                e.printStackTrace();
            }




            url = url_2 + v + "?key=" + tok;


        } catch (IOException e) {

        }


        return url;

    }


    public String yandex(String link) {
        String url = "";

        request = new Request.Builder()
                .url(link)
                .build();

        return url;

    }

    public long getQualityFromGoogleURL(String url) {
        String quality = "";
        Pattern pattern = Pattern.compile("itag=(\\d*)");
        Matcher matcher = pattern.matcher(url);

        String[] q4K = {"266", "272", "313"};
        String[] q2K = {"264", "271"};
        String[] qFHD = {"37", "137", "299", "96", "248", "303", "46"};
        String[] qHD = {"15", "22", "84", "136", "298", "120", "95", "247", "302", "45", "102"};


        while (matcher.find()) {
            quality = matcher.group();
        }

        pattern = Pattern.compile("=m(\\d*)$");
//        Pattern pattern = Pattern.compile("(https:.*?redirector.*?)[\\'\\\"]");
        matcher = pattern.matcher(url);

        while (matcher.find()) {
            quality = quality + matcher.group();
        }

        if(Arrays.asList(q4K).contains(quality)) {
            return 4096;
        }

        if(Arrays.asList(q2K).contains(quality)) {
            return 1440;
        }

        if(Arrays.asList(qFHD).contains(quality)) {
            return 1080;
        }

        if(Arrays.asList(qHD).contains(quality)) {
            return 720;
        }

        return 480;

    }


    public static Map<String, List<String>> getQueryParams(String url) {
        try {
            Map<String, List<String>> params = new HashMap<String, List<String>>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }

                    List<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        params.put(key, values);
                    }
                    values.add(value);
                }
            }
            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }




    class StreamSource {
        public String url;
        public long quality;
        public String provider;
        public String videosource;
    }

}

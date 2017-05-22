package com.adisdurakovic.android.chilly.data;

import android.content.Context;

import com.adisdurakovic.android.chilly.R;

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
import java.util.Iterator;
import java.util.List;

/**
 * Created by add on 21/05/2017.
 */

public class Chilly {

    Context mContext;

    Chilly(Context ctx) {
        mContext = ctx;
    }

    public JSONArray getDataFromIMDB(String category, String i_url, String t_url, String f_url, String f_id) throws JSONException, IOException {


        JSONObject list = new JSONObject();

        JSONArray videolist = new JSONArray();
        JSONObject videoelem = new JSONObject();
        JSONArray videos = new JSONArray();


        List<String> imdb_popular_movies = getIMDBList(i_url);


        int x = 0;
        for(Iterator<String> i = imdb_popular_movies.iterator(); i.hasNext();) {

            x++;

            String imdb_id = i.next();
            String trakt_url = String.format(t_url, imdb_id);
            JSONObject trakt_info = getTraktInfo(trakt_url, mContext.getResources().getString(R.string.trakt_api_version), mContext.getResources().getString(R.string.trakt_api_key));
            String fanart_url = String.format(f_url, trakt_info.getJSONObject("ids").getString(f_id)) + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
            JSONObject fanart_media = getFanart(fanart_url);


            JSONObject elem = new JSONObject();

            elem.put("title", trakt_info.optString("title"));
            elem.put("description", trakt_info.optString("overview"));
            elem.put("card", getPoster(fanart_media));
            elem.put("background", getBackground(fanart_media));
            elem.put("studio", trakt_info.optString("year"));

            System.out.println(getBackground(fanart_media));

            JSONArray sources = new JSONArray();
            sources.put("https://nowhere" + trakt_info.optString("title") + ".mp4");

            elem.put("sources", sources);


            videos.put(elem);

        }

        videoelem.put("category", category);
        videoelem.put("videos", videos);

        videolist.put(videoelem);


        return videolist;


    }


    public List<String> getIMDBList(String url) throws IOException {

        List<String> list = new ArrayList<>();

        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("GET");

        String html = HTTPGrabber.getContentFromURL(urlConnection);

        Document doc = Jsoup.parse(html);
        Elements itemLinks = doc.select("h3.lister-item-header a");

        for(Iterator<Element> i = itemLinks.iterator(); i.hasNext();) {
            Element item = i.next();
            String itemlink = item.attr("href");
            String imdb_id = itemlink.toString().replace("/?ref_=adv_li_tt", "").replace("/title/", "");
            list.add(imdb_id);
        }



        return list;
    }


    public JSONObject getTraktInfo(String url, String api_version, String api_key) throws IOException, JSONException {

        JSONObject info;

        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("trakt-api-version", api_version);
        urlConnection.setRequestProperty("trakt-api-key", api_key);

        info = new JSONObject(HTTPGrabber.getContentFromURL(urlConnection));


        return info;

    }

    private String getPoster(JSONObject fanart) throws JSONException {

        String poster = "";

        JSONArray fa_poster;
        fa_poster = (fanart.optJSONArray("movieposter") != null ? fanart.optJSONArray("movieposter") : fanart.optJSONArray("tvposter"));

        if(fa_poster != null) {

            for(int j = 0; j < fa_poster.length(); j++) {
                // get poster of any language...
                if(!fa_poster.optJSONObject(j).optString("lang").equals("00")) {
                    poster = fa_poster.getJSONObject(j).optString("url");
                }
                // ... except there is one in english take that
                if(fa_poster.optJSONObject(j).optString("lang").equals("en")) {
                    poster = fa_poster.getJSONObject(j).optString("url");
                    break;
                }
            }
        }

        return poster;
    }

    private String getBackground(JSONObject fanart) throws JSONException {
        String background = "";

        JSONArray fa_background;
        fa_background = (fanart.optJSONArray("moviebackground") != null ? fanart.optJSONArray("moviebackground") : fanart.optJSONArray("showbackground"));

        if(fa_background != null) {
            background = fa_background.optJSONObject(0).optString("url");
        }
        return background;
    }

    public static JSONObject getFanart(String url) throws IOException, JSONException {

        JSONObject media = new JSONObject();


        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("GET");

        try {
            media = new JSONObject(HTTPGrabber.getContentFromURL(urlConnection));
        } catch (Exception e) {

        }


        return media;
    }


}

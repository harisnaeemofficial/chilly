package com.adisdurakovic.android.chilly.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.model.Video;
import com.bumptech.glide.disklrucache.DiskLruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.isExternalStorageRemovable;

/**
 * Created by add on 21/05/2017.
 */

public class Chilly {

    private Context mContext;
    private Video tvshow;
    private Video season;

    public Chilly(Context ctx) {
        mContext = ctx;
    }

    public List<Video> getTrendingMovies(int limit) throws JSONException, IOException {
        List<Video> list;
        list = getVideos("movie", mContext.getResources().getString(R.string.trakt_api_url) + "/movies/trending?extended=full&page=1&limit=" + String.valueOf(limit));
        return list;
    }

    public List<Video> getTrendingTVShows(int limit) throws JSONException, IOException {

        List<Video> list;
        list = getVideos("tvshow", mContext.getResources().getString(R.string.trakt_api_url) + "/shows/trending?extended=full&page=1&limit=" + String.valueOf(limit));
        return list;
    }

    public List<Video> getSeasonsForShow(Video stvshow) throws JSONException, IOException {

        List<Video> list;
        tvshow = stvshow;
        list = getVideos("season", mContext.getResources().getString(R.string.trakt_api_url) + "/shows/" + tvshow.id + "/seasons?extended=full");
        return list;
    }

    public List<Video> getEpisodesForShowSeason(Video stvshow, Video sseason) throws JSONException, IOException {

        List<Video> list;
        tvshow = stvshow;
        season = sseason;
        String season_number = season.title.replace("Season ", "");
        list = getVideos("episode", mContext.getResources().getString(R.string.trakt_api_url) + "/shows/" + tvshow.id + "/seasons/" + season_number + "?extended=full");

        return list;
    }


    private List<Video> getVideos(String type, String trakt_list_url) throws JSONException, IOException {

        List<Video> videos = new ArrayList<>();

        JSONArray data_list = getListFromTrakt(trakt_list_url);
        System.out.println(trakt_list_url);

        for(int i = 0; i < data_list.length(); i++) {

            JSONObject trakt_elem = null;
            String fanart_url, tmdb_url = "";
            JSONObject fanart_media, tmdb_media;
            String poster = "";
            String background = "";
            String year = "";

            switch (type) {
                case "movie":
                    trakt_elem = data_list.optJSONObject(i).optJSONObject("movie");
                    fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/movies/" + trakt_elem.optJSONObject("ids").optString("tmdb") + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);;
                    fanart_media = getDataFromFanart(fanart_url);
                    poster = getPoster(fanart_media, "movieposter", "", "");
                    background = getBackground(fanart_media, "moviebackground");
                    year = trakt_elem.optString("year");
                    break;
                case "tvshow":
                    trakt_elem = data_list.optJSONObject(i).optJSONObject("show");
                    fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + trakt_elem.optJSONObject("ids").optString("tvdb") + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);;
                    fanart_media = getDataFromFanart(fanart_url);
                    poster = getPoster(fanart_media, "tvposter", "", "");
                    background = getBackground(fanart_media, "showbackground");
                    year = trakt_elem.optString("year");
                    break;
                case "season":
                    trakt_elem = data_list.optJSONObject(i);
                    if(trakt_elem.optString("number").equals("0")) continue;
                    fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + tvshow.tvdb_id + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
                    fanart_media = getDataFromFanart(fanart_url);
                    poster = getPoster(fanart_media, "seasonposter", "tvposter", trakt_elem.optString("number"));
                    background = getBackground(fanart_media, "showbackground");
                    year = trakt_elem.optString("first_aired").substring(0, 4);
                    break;
                case "episode":
                    trakt_elem = data_list.optJSONObject(i);
                    if(trakt_elem.optString("number").equals("0")) continue;
                    fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + tvshow.tvdb_id + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
                    fanart_media = getDataFromFanart(fanart_url);
                    tmdb_url = mContext.getResources().getString(R.string.tmdb_api_url) + "/tv/" + tvshow.tmdb_id + "/season/" + trakt_elem.optString("season") + "/episode/" + trakt_elem.optString("number") + "/images?language=en-US&api_key=" + mContext.getResources().getString(R.string.tmdb_api_key);;
                    tmdb_media = getDataFromTMDB(tmdb_url);
                    System.out.println(tmdb_url);
                    poster = mContext.getResources().getString(R.string.tmdb_image_url) + tmdb_media.optJSONArray("stills").optJSONObject(0).optString("file_path");
                    background = getBackground(fanart_media, "showbackground");
                    year = "S" + zeroAppended(trakt_elem.optString("season")) + "E" + zeroAppended(trakt_elem.optString("number"));
                    break;
            }



            String network = "";
            if(trakt_elem.optString("network") != null) {
                network = trakt_elem.optString("network");
            }

            Video elem = new Video.VideoBuilder()
                    .id(Long.valueOf(trakt_elem.optJSONObject("ids").optString("trakt")))
                    .tvdb_id(trakt_elem.optJSONObject("ids").optString("tvdb"))
                    .tmdb_id(trakt_elem.optJSONObject("ids").optString("tmdb"))
                    .imdb_id(trakt_elem.optJSONObject("ids").optString("imdb"))
                    .title(trakt_elem.optString("title"))
                    .description(trakt_elem.optString("overview"))
                    .cardImageUrl(poster)
                    .bgImageUrl(background)
                    .studio(network)
                    .videoType(type)
                    .productionYear(year)
                    .airedEpisodes(trakt_elem.optLong("aired_episodes"))
                    .videoUrl("https://placeholder/fake/url/for/android-tv/" +trakt_elem.getString("title") + ".mp4")
                    .build();


            videos.add(elem);

        }



        return videos;


    }

    private String zeroAppended(String value) {
        String ret = value;
        if(Long.valueOf(value) < 10) {
            ret = "0" + value;
        }
        return ret;

    }

    public JSONArray getListFromTrakt(String url) throws JSONException, IOException {

        JSONArray list;

        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("trakt-api-version", mContext.getResources().getString(R.string.trakt_api_version));
        urlConnection.setRequestProperty("trakt-api-key", mContext.getResources().getString(R.string.trakt_api_key));

        list = new JSONArray(HTTPGrabber.getContentFromURL(urlConnection));

        return list;
    }



    private String getPoster(JSONObject fanart, String fanart_object, String fanart_object_fallback, String season_number) throws JSONException {

        String poster = mContext.getResources().getDrawable(R.drawable.movie, null).toString();

        JSONArray fa_poster;
        fa_poster = fanart.optJSONArray(fanart_object);

        if(fanart_object_fallback != "" && fa_poster == null) {
            fa_poster = fanart.optJSONArray(fanart_object_fallback);
        }

        if(fa_poster == null) return "";

        for(int j = 0; j < fa_poster.length(); j++) {

            if(season_number.equals("")) {
                // get poster of any language...
                if(!fa_poster.optJSONObject(j).optString("lang").equals("00")) {
                    poster = fa_poster.getJSONObject(j).optString("url");
                }
                // ... except there is one in english take that
                if(fa_poster.optJSONObject(j).optString("lang").equals("en")) {
                    poster = fa_poster.getJSONObject(j).optString("url");
                    break;
                }
            } else {
                // get poster of any language...
                if(!fa_poster.optJSONObject(j).optString("lang").equals("00") && fa_poster.optJSONObject(j).optString("season").equals(season_number)) {
                    poster = fa_poster.getJSONObject(j).optString("url");
                }
                // ... except there is one in english take that
                if(fa_poster.optJSONObject(j).optString("lang").equals("en") && fa_poster.optJSONObject(j).optString("season").equals(season_number)) {
                    poster = fa_poster.getJSONObject(j).optString("url");
                    break;
                }
            }


        }

        // get smaller version
        poster = poster.replace("/fanart/", "/preview/");

        return poster;
    }

    private String getBackground(JSONObject fanart, String fanart_object) throws JSONException {
        String background = "";

        JSONArray fa_background;
        fa_background = fanart.optJSONArray(fanart_object);

        if(fa_background == null) return "";

        background = fa_background.optJSONObject(0).optString("url");
        return background;
    }

    private JSONObject getDataFromFanart(String url) throws IOException, JSONException {

        JSONObject media = new JSONObject();

        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("GET");

        try {
            media = new JSONObject(HTTPGrabber.getContentFromURL(urlConnection));
        } catch (Exception e) {

        }


        return media;
    }


    private File getDiskCacheDir(String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = mContext.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }


    private JSONObject getDataFromTMDB(String url) throws IOException, JSONException {

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


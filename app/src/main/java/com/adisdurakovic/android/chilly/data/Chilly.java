package com.adisdurakovic.android.chilly.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.model.Video;
import com.bumptech.glide.disklrucache.DiskLruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

    public List<Video> getPublicVideos(String list_type, String videoType, int limit) throws JSONException, IOException {
        List<Video> list;
        list = getVideos(videoType, mContext.getResources().getString(R.string.trakt_api_url) + "/" + videoType + "s/" + list_type + "?extended=full&page=1&limit=" + String.valueOf(limit));
        return list;
    }

    public List<Video> getUserVideos(String list_type, String videoType) throws JSONException, IOException {
        List<Video> list;
        String user_id = getUserSlug();

        list = getVideos(videoType, mContext.getResources().getString(R.string.trakt_api_url) + "/users/" + user_id + "/" + list_type + "/" + videoType + "s" + "?extended=full");
        return list;
    }

    public List<Video> getUserRecommendations() throws JSONException, IOException {
        List<Video> list = new ArrayList<>();

        list.addAll(list.size(), getVideos("movie", mContext.getResources().getString(R.string.trakt_api_url) + "/recommendations/movies?extended=full&limit=3"));
        list.addAll(list.size(), getVideos("show", mContext.getResources().getString(R.string.trakt_api_url) + "/recommendations/shows?extended=full&limit=3"));
        return list;
    }

    public List<Video> getTrendingTVShows(int limit) throws JSONException, IOException {

        List<Video> list;
        list = getVideos("show", mContext.getResources().getString(R.string.trakt_api_url) + "/shows/trending?extended=full&page=1&limit=" + String.valueOf(limit));
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

    public List<ListElem> getGenres(String forElem) throws JSONException, IOException {
        List<ListElem> genres = new ArrayList<>();
        JSONArray trakt_genres = getListFromTrakt(mContext.getResources().getString(R.string.trakt_api_url) + "/genres/" + forElem + "s");

        for(int i = 0; i < trakt_genres.length(); i++) {
            JSONObject trakt_elem = trakt_genres.getJSONObject(i);
            ListElem elem = new ListElem(trakt_elem.getString("name"), trakt_elem.getString("slug"), forElem, "display-list", "genres");
            genres.add(elem);
        }

        return genres;
    }

    public List<ListElem> getPublicList(String forElem) {
        List<ListElem> public_list = new ArrayList<>();

        public_list.add(public_list.size(), new ListElem("Trending now", "public-trending", forElem, "display-videos", ""));
        public_list.add(public_list.size(), new ListElem("Popular", "public-popular", forElem, "display-videos", ""));
        public_list.add(public_list.size(), new ListElem("Mostly played", "public-played", forElem, "display-videos", ""));
        public_list.add(public_list.size(), new ListElem("Mostly watched", "public-watched", forElem, "display-videos", ""));
        if(forElem.equals("movie")) {
            public_list.add(public_list.size(), new ListElem("Box Office", "public-boxoffice", forElem, "display-videos", ""));
        }

        return public_list;
    }

    private String getUserSlug() {

        String ret = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userstring = sharedPreferences.getString("trakt_user", "NOTOKEN");
        try {
            JSONObject trakt_user = new JSONObject(userstring);
            ret = trakt_user.getJSONObject("user").getJSONObject("ids").getString("slug");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;


    }



    public List<Video> getByFilter(ListElem elem, int limit) throws JSONException, IOException {
        List<Video> list;
        String trakt_url = mContext.getResources().getString(R.string.trakt_api_url) + "/search/" + elem.videoType;
        trakt_url += "?extended=full";
        if(elem.filterType.equals("query")) {
            trakt_url += "&" + elem.filterType + "=" + elem.slug;
        } else {
            trakt_url += "&query=&" + elem.filterType + "=" + elem.slug;
        }
        trakt_url += "&limit=" + limit;

        list = getVideos(elem.videoType, trakt_url);
        return list;
    }


    private List<Video> getVideos(String type, String trakt_list_url) throws JSONException, IOException {

        List<Video> videos = new ArrayList<>();

        System.out.println(trakt_list_url);

        JSONArray data_list = getListFromTrakt(trakt_list_url);
        System.out.println(trakt_list_url);
        System.out.println(data_list);

        for(int i = 0; i < data_list.length(); i++) {

            JSONObject trakt_elem = null;
            String fanart_url, tmdb_url = "";
            JSONObject fanart_media, tmdb_media;
            String poster = "";
            String background = "";
            String year = "";

            String seasonNumber = "";
            String episodeNumber = "";

            switch (type) {
                case "movie":
                    trakt_elem = data_list.optJSONObject(i).optJSONObject("movie");
                    if(trakt_elem == null) {
                        trakt_elem = data_list.optJSONObject(i);
                    }
                    fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/movies/" + trakt_elem.getJSONObject("ids").getString("tmdb") + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);;
                    fanart_media = getDataFromFanart(fanart_url);
                    poster = getPoster(fanart_media, "movieposter", "", "");
                    background = getBackground(fanart_media, "moviebackground");
                    year = trakt_elem.optString("year");
                    break;
                case "show":
                    trakt_elem = data_list.optJSONObject(i).optJSONObject("show");
                    if(trakt_elem == null) {
                        trakt_elem = data_list.optJSONObject(i);
                    }
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
                    poster = mContext.getResources().getString(R.string.tmdb_image_url) + tmdb_media.optJSONArray("stills").optJSONObject(0).optString("file_path");
                    background = getBackground(fanart_media, "showbackground");
                    year = "S" + zeroAppended(trakt_elem.optString("season")) + "E" + zeroAppended(trakt_elem.optString("number"));
                    seasonNumber = trakt_elem.getString("season");
                    episodeNumber = trakt_elem.getString("number");
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
                    .videoUrl("")
                    .studio(network)
                    .videoType(type)
                    .productionYear(year)
                    .airedEpisodes(trakt_elem.optLong("aired_episodes"))
                    .seasonNumber(seasonNumber)
                    .episodeNumber(episodeNumber)
                    .episodeShow(tvshow)
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
        urlConnection.setRequestProperty("Content-Type","application/json");


        urlConnection.setRequestProperty("trakt-api-version", mContext.getResources().getString(R.string.trakt_api_version));
        urlConnection.setRequestProperty("trakt-api-key", mContext.getResources().getString(R.string.trakt_api_key));
        String access_token = getAccessToken();
        urlConnection.setRequestProperty("Authorization","Bearer " + access_token);

        list = new JSONArray(HTTPGrabber.getContentFromURL(urlConnection));

        return list;
    }

    private String getAccessToken() {
        String token = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String token_string = sharedPreferences.getString("trakt_token", "NOTOKEN");

        try {
            JSONObject json_token = new JSONObject(token_string);
            token = json_token.getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return token;
    }

    public JSONObject getCodeFromTrakt() throws JSONException, IOException {
        String url = mContext.getResources().getString(R.string.trakt_api_url) + "/oauth/device/code";
        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type","application/json");

        JSONObject cid = new JSONObject();
        cid.put("client_id", mContext.getResources().getString(R.string.trakt_api_key));

        OutputStreamWriter ap_osw= new OutputStreamWriter(urlConnection.getOutputStream());
        ap_osw.write(cid.toString());
        ap_osw.flush();
        ap_osw.close();


        return new JSONObject(HTTPGrabber.getContentFromURL(urlConnection));
    }


    public JSONObject getTokenFromTrakt(String device_code) throws JSONException, IOException {
        String url = mContext.getResources().getString(R.string.trakt_api_url) + "/oauth/device/token";
        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type","application/json");

        JSONObject cid = new JSONObject();
        cid.put("client_id", mContext.getResources().getString(R.string.trakt_api_key));
        cid.put("client_secret", mContext.getResources().getString(R.string.trakt_api_secret));
        cid.put("code", device_code);

        System.out.println(cid);

        OutputStreamWriter ap_osw= new OutputStreamWriter(urlConnection.getOutputStream());
        ap_osw.write(cid.toString());
        ap_osw.flush();
        ap_osw.close();


        return new JSONObject(HTTPGrabber.getContentFromURL(urlConnection));
    }


    public JSONObject getUserFromTrakt(String access_token) throws JSONException, IOException {
        String url = mContext.getResources().getString(R.string.trakt_api_url) + "/users/settings";
        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type","application/json");

        urlConnection.setRequestProperty("Authorization","Bearer " + access_token);
        urlConnection.setRequestProperty("trakt-api-version", mContext.getResources().getString(R.string.trakt_api_version));
        urlConnection.setRequestProperty("trakt-api-key", mContext.getResources().getString(R.string.trakt_api_key));

        return new JSONObject(HTTPGrabber.getContentFromURL(urlConnection));
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


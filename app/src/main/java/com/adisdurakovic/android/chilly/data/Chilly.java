package com.adisdurakovic.android.chilly.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.model.FanartGson;
import com.adisdurakovic.android.chilly.model.TMDBGson;
import com.adisdurakovic.android.chilly.model.TraktGson;
import com.adisdurakovic.android.chilly.model.Video;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by add on 21/05/2017.
 */

public class Chilly {
    private static Chilly mInstance;
    private Context mContext;
    private Video tvshow;
    private Video season;
    private int page;
    private final String TAG = "CHILLY";
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    Video elem = new Video.VideoBuilder().build();

    private Map<String, String> posters = new HashMap<>();

    private Chilly(Context ctx) {
        mContext = ctx;
    }

    public static synchronized Chilly getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Chilly(context);
        }
        return mInstance;
    }

    public List<Video> getTrendingMovies(int limit) throws JSONException, IOException {
        List<Video> list;
        list = getVideos("movie", mContext.getResources().getString(R.string.trakt_api_url) + "/movies/trending?extended=full&page=1&limit=" + String.valueOf(limit));
        return list;
    }

    public List<Video> getPublicVideos(String list_type, String videoType, int limit, int p) throws JSONException, IOException {
        page = p;
        List<Video> list;
        list = getVideos(videoType, mContext.getResources().getString(R.string.trakt_api_url) + "/" + videoType + "s/" + list_type + "?extended=full&page=" + String.valueOf(page) + "&limit=" + String.valueOf(limit));
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

    public List<ListElem> getTraktActions(String forElem, Video video) {
        List<ListElem> trakt_actions = new ArrayList<>();

        trakt_actions.add(trakt_actions.size(), new ListElem("Add to Collection", "trakt-add-to-collection", video));
        trakt_actions.add(trakt_actions.size(), new ListElem("Add to Watchlist", "trakt-add-to-watchlist", video));
        trakt_actions.add(trakt_actions.size(), new ListElem("Mark as watched", "trakt-mark-watched", video));

        return trakt_actions;
    }

    private String getUserSlug() {

        String ret = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userstring = sharedPreferences.getString("trakt_user", "");
        try {
            JSONObject trakt_user = new JSONObject(userstring);
            ret = trakt_user.getJSONObject("user").getJSONObject("ids").getString("slug");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;


    }

    public TraktGson.TraktUser getTraktUser() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userstring = sharedPreferences.getString("trakt_user", "");
        try {
            JSONObject trakt_user = new JSONObject(userstring);
            return  gson.fromJson(trakt_user.getJSONObject("user").toString(), TraktGson.TraktUser.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUserName() {

        String ret = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userstring = sharedPreferences.getString("trakt_user", "");
        try {
            JSONObject trakt_user = new JSONObject(userstring);
            ret = trakt_user.getJSONObject("user").getString("username");


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

    public List<Video> getBySearch(String query) {
        List<Video> list = new ArrayList<>();
        if(query.equals("")) return list;
        try {
            String trakt_url = mContext.getResources().getString(R.string.trakt_api_url) + "/search/movie?extended=full&limit=100&query=" + query;
            list.addAll(getVideos("movie", trakt_url));
            trakt_url = mContext.getResources().getString(R.string.trakt_api_url) + "/search/show?extended=full&limit=100&query=" + query;
            list.addAll(getVideos("show", trakt_url));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    private List<Video> getVideos(String type, String trakt_list_url) throws JSONException, IOException {

        List<Video> videos = new ArrayList<>();

        Log.d(TAG, trakt_list_url);

        JSONArray data_list = getListFromTrakt(trakt_list_url);

        String watchtype = (type.equals("movie") ? "movies" : "shows");
        JSONArray watched_json = getWatched(watchtype);
        TraktGson.TraktWatched[] watched_videos = gson.fromJson(watched_json.toString(), TraktGson.TraktWatched[].class);

        for(int i = 0; i < data_list.length(); i++) {

            JSONObject trakt_object = null;
            String fanart_url, tmdb_url = "";
            JSONObject fanart_media, tmdb_media;

            Video elem = null;


            switch (type) {
                case "movie":
                    trakt_object = data_list.optJSONObject(i).optJSONObject("movie") != null ? data_list.optJSONObject(i).optJSONObject("movie") : data_list.optJSONObject(i);
                    TraktGson.TraktMovie tMovie = gson.fromJson(trakt_object.toString(), TraktGson.TraktMovie.class);


//                    fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/movies/" + trakt_elem.getJSONObject("ids").getString("tmdb") + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
//                    fanart_media = getDataFromFanart(fanart_url);
//                    poster = getPoster(fanart_media, "movieposter", "", "");
//                    background = getBackground(fanart_media, "moviebackground");


                    tMovie.watched = false;
                    for(TraktGson.TraktWatched tw : watched_videos) {
                        if(!tw.movie.ids.get("trakt").equals(tMovie.ids.get("trakt"))) continue;
                        tMovie.watched = true; break;
                    }

                    elem = buildFromGson(tMovie);

                    break;
                case "show":
                    trakt_object = data_list.optJSONObject(i).optJSONObject("show") != null ? data_list.optJSONObject(i).optJSONObject("show") : data_list.optJSONObject(i);
                    TraktGson.TraktShow tShow = gson.fromJson(trakt_object.toString(), TraktGson.TraktShow.class);
//                    fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + trakt_elem.optJSONObject("ids").optString("tvdb") + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);;
//                    fanart_media = getDataFromFanart(fanart_url);
//                    poster = getPoster(fanart_media, "tvposter", "", "");
//                    background = getBackground(fanart_media, "showbackground");

                    tShow.watched = false;
                    for(TraktGson.TraktWatched tw : watched_videos) {
                        if(!tw.show.ids.get("trakt").equals(tShow.ids.get("trakt"))) continue;
                        int watched_episodes = 0;
                        for(TraktGson.TraktWatchedSeason tws : tw.seasons) {
                            watched_episodes += tws.episodes.size();
                        }
                        if(watched_episodes == tShow.aired_episodes) tShow.watched = true; break;
                    }
                    elem = buildFromGson(tShow);


                    break;
                case "season":
                    trakt_object = data_list.optJSONObject(i);
                    TraktGson.TraktSeason tSeason = gson.fromJson(trakt_object.toString(), TraktGson.TraktSeason.class);
                    if(tSeason.number == 0) continue;
//                    fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + tvshow.tvdb_id + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
//                    fanart_media = getDataFromFanart(fanart_url);
//                    poster = getPoster(fanart_media, "seasonposter", "tvposter", trakt_elem.optString("number"));
//                    background = getBackground(fanart_media, "showbackground");

                    tSeason.watched = false;
                    for(TraktGson.TraktWatched tw : watched_videos) {
                        if(!tw.show.ids.get("trakt").equals(String.valueOf(tvshow.id))) continue;
                        for(TraktGson.TraktWatchedSeason tws : tw.seasons) {
                            if(tws.number != tSeason.number) continue;
                            if(tSeason.aired_episodes == tws.episodes.size()) tSeason.watched = true; break;
                        }
                    }
                    elem = buildFromGson(tSeason);


                    break;
                case "episode":
                    trakt_object = data_list.optJSONObject(i);
                    TraktGson.TraktEpisode tEpisode = gson.fromJson(trakt_object.toString(), TraktGson.TraktEpisode.class);
                    if(tEpisode.number == 0) continue;
//                    fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + tvshow.tvdb_id + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
//                    fanart_media = getDataFromFanart(fanart_url);
//                    tmdb_url = mContext.getResources().getString(R.string.tmdb_api_url) + "/tv/" + tvshow.tmdb_id + "/season/" + trakt_elem.optString("season") + "/episode/" + trakt_elem.optString("number") + "/images?language=en-US&api_key=" + mContext.getResources().getString(R.string.tmdb_api_key);;
//                    tmdb_media = getDataFromTMDB(tmdb_url);
//                    poster = mContext.getResources().getString(R.string.tmdb_image_url) + tmdb_media.optJSONArray("stills").optJSONObject(0).optString("file_path");
//                    background = getBackground(fanart_media, "showbackground");

                    tEpisode.watched = false;
                    for(TraktGson.TraktWatched tw : watched_videos) {
                        if(!tw.show.ids.get("trakt").equals(String.valueOf(tvshow.id))) continue;
                        for(TraktGson.TraktWatchedSeason tws : tw.seasons) {
                            if(tws.number != tEpisode.season) continue;
                            for(TraktGson.TraktWatchedEpisode twe : tws.episodes) {
                                if(tEpisode.number != twe.number) continue;
                                tEpisode.watched = true; break;
                            }
                        }
                    }
                    elem = buildFromGson(tEpisode);
                    break;
            }




//            Video elem = new Video.VideoBuilder()
//                    .id(Long.valueOf(trakt_elem.optJSONObject("ids").optString("trakt")))
//                    .tvdb_id(trakt_elem.optJSONObject("ids").optString("tvdb"))
//                    .tmdb_id(trakt_elem.optJSONObject("ids").optString("tmdb"))
//                    .imdb_id(trakt_elem.optJSONObject("ids").optString("imdb"))
//                    .title(trakt_elem.optString("title"))
//                    .description(trakt_elem.optString("overview"))
//                    .cardImageUrl(poster)
//                    .bgImageUrl(background)
//                    .videoUrl("")
//                    .studio(network)
//                    .videoType(type)
//                    .productionYear(year)
//                    .airedEpisodes(trakt_elem.optLong("aired_episodes"))
//                    .seasonNumber(seasonNumber)
//                    .episodeNumber(episodeNumber)
//                    .episodeShow(tvshow)
//                    .runtime(40*(page-1)+(i+1))
//                    .rating(trakt_elem.getLong("rating"))
//                    .trailer(trakt_elem.optString("trailer"))
//                    .watched(watched)
//                    .build();


            videos.add(elem);

        }



        return videos;


    }

    private Video buildFromGson(TraktGson.TraktMovie t) {
        return buildCommon(t)
                .videoType("movie")
                .productionYear(String.valueOf(t.year))
//                .cardImageUrl(getFanartPoster(t, "movie"))
//                .bgImageUrl(getTMDBImage(t, "movie", "backdrop"))
                .build();
    }

    private Video buildFromGson(TraktGson.TraktShow t) {
        return buildCommon(t)
                .airedEpisodes(t.aired_episodes)
                .studio(t.network)
                .videoType("show")
                .productionYear(String.valueOf(t.year))
//                .cardImageUrl(getFanartPoster(t, "show"))
                .build();
    }

    private Video buildFromGson(TraktGson.TraktSeason t) {
        return buildCommon(t)
                .airedEpisodes(t.aired_episodes)
                .videoType("season")
                .productionYear(String.valueOf(t.first_aired).substring(0, 4))
                .videoShow(tvshow)
                .seasonNumber(String.valueOf(t.number))
                .build();
    }

    private Video buildFromGson(TraktGson.TraktEpisode t) {
        return buildCommon(t)
                .seasonNumber(String.valueOf(t.season))
                .episodeNumber(String.valueOf(t.number))
                .videoType("episode")
                .videoShow(tvshow)
                .productionYear(String.valueOf(t.first_aired).substring(0, 4))
                .build();
    }

    private Video.VideoBuilder buildCommon(TraktGson.TraktElem t) {
        return new Video.VideoBuilder()
            .id(Long.valueOf(t.ids.get("trakt")))
            .tmdb_id(t.ids.get("tmdb"))
            .tvdb_id(t.ids.get("tvdb"))
            .imdb_id(t.ids.get("imdb"))
            .title(t.title)
            .description(t.overview)
            .cardImageUrl("http://assets.fanart.tv/fanart/movies/920/movieposter/cars-5214d18d6721b.jpg")
            .bgImageUrl("http://some.fake/image/load.jpg")
            .videoUrl("")
            .runtime(t.runtime)
            .rating(t.rating)
            .trailer(t.trailer)
            .watched(t.watched);
    }



    public Map<String, String> getTMDBImages(Video video) {

        String tmdb_url = mContext.getResources().getString(R.string.tmdb_api_url);
        Map<String, String> images = new HashMap<>();

        switch (video.videoType) {
            case "movie":
                tmdb_url += "/movie/" + video.tmdb_id; break;
            case "show":
                tmdb_url += "/tv/" + video.tmdb_id; break;
            case "season":
                tmdb_url += "/tv/" + video.videoShow.tmdb_id + "/season/" + video.seasonNumber; break;
            case "episode":
                tmdb_url += "/tv/" + video.videoShow.tmdb_id + "/season/" + video.seasonNumber + "/episode/" + video.episodeNumber; break;

        }

        tmdb_url += "/images?language=en&api_key=" + mContext.getResources().getString(R.string.tmdb_api_key);

        int cacheSize = 10 * 1024 * 1024; // 200 MiB
        Cache cache = new Cache(new File(mContext.getCacheDir().getPath()), cacheSize);
        OkHttpClient client = new OkHttpClient.Builder().cache(cache).readTimeout(1, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .url(tmdb_url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            TMDBGson.TMDBElem tmdbElem = gson.fromJson(response.body().string(), TMDBGson.TMDBElem.class);

            if(video.videoType.equals("episode")) {
                images.put("poster", mContext.getResources().getString(R.string.tmdb_image_url) + tmdbElem.stills.get(0).file_path);
            } else {
                images.put("poster", mContext.getResources().getString(R.string.tmdb_image_url) + tmdbElem.posters.get(0).file_path);
            }

            images.put("background", (mContext.getResources().getString(R.string.tmdb_image_url) + tmdbElem.backdrops.get(0).file_path).replace("/w500/", "/w1000/"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return images;

    }


//    public String getFanartPoster(TraktGson.TraktElem t, String videoType) {
//        String poster = "";
//        String fanart_url = "";
//        switch(videoType) {
//            case "movie":
//                fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/movies/" + t.ids.get("tmdb") + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
//                break;
//            case "show":
//                fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + t.ids.get("tvdb") + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
//                break;
//            case "season":
//                fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + tvshow.tvdb_id + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
//                break;
//        }
//
//
//
//
//        int cacheSize = 50 * 1024 * 1024; // 50 MiB
//        Cache cache = new Cache(new File(mContext.getCacheDir().getPath()), cacheSize);
//
//        OkHttpClient client = new OkHttpClient.Builder().cache(cache).readTimeout(1, TimeUnit.SECONDS).build();
//
//        Request request = new Request.Builder().url(fanart_url).build();
//
//
//
//
//        try {
//            Response response = client.newCall(request).execute();
//            System.out.println(response.cacheResponse());
//            FanartGson.FanartElem fanartElem = gson.fromJson(response.body().string(), FanartGson.FanartElem.class);
//            switch(videoType) {
//                case "movie":
//                    for(FanartGson.FanartData f : fanartElem.movieposter) {
//                        if(!f.lang.equals("00")) {
//                            poster = f.url;
//                        }
//                        if(f.lang.equals("en")) {
//                            poster = f.url;
//                            break;
//                        }
//                    }
//                    break;
//                case "show":
//                    for(FanartGson.FanartData f : fanartElem.tvposter) {
//                        if(!f.lang.equals("00")) {
//                            poster = f.url;
//                        }
//                        if(f.lang.equals("en")) {
//                            poster = f.url;
//                            break;
//                        }
//                    }
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        poster = poster.replace("/fanart/", "/preview/");
//        return poster;
//
//    }



//    public String getFanartPoster(Video video) {
//        String poster = "TEST";
//        String fanart_url = "";
//        switch(video.videoType) {
//            case "movie":
//                fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/movies/" + video.tmdb_id + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
//                break;
//            case "show":
//                fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + video.tvdb_id + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
//                break;
//            case "season":
//                fanart_url = mContext.getResources().getString(R.string.fanart_api_url) + "/tv/" + tvshow.tvdb_id + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
//                break;
//        }
//
//        System.out.println(fanart_url);
//
//
//        int cacheSize = 50 * 1024 * 1024; // 50 MiB
//        Cache cache = new Cache(new File(mContext.getCacheDir().getPath()), cacheSize);
//
//        OkHttpClient client = new OkHttpClient.Builder().cache(cache).readTimeout(1, TimeUnit.SECONDS).build();
//
//        Request request = new Request.Builder().url(fanart_url).build();
//
//
//
//
//        try {
//            Response response = client.newCall(request).execute();
//            System.out.println(response.cacheResponse());
//            FanartGson.FanartElem fanartElem = gson.fromJson(response.body().string(), FanartGson.FanartElem.class);
//            switch(video.videoType) {
//                case "movie":
//                    for(FanartGson.FanartData f : fanartElem.movieposter) {
//                        if(!f.lang.equals("00")) {
//                            poster = f.url;
//                        }
//                        if(f.lang.equals("en")) {
//                            poster = f.url;
//                            break;
//                        }
//                    }
//                    break;
//                case "show":
//                    for(FanartGson.FanartData f : fanartElem.tvposter) {
//                        if(!f.lang.equals("00")) {
//                            poster = f.url;
//                        }
//                        if(f.lang.equals("en")) {
//                            poster = f.url;
//                            break;
//                        }
//                    }
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        poster = poster.replace("/fanart/", "/preview/");
//        return poster;
//
//    }


//    private JSONObject getDataFromFanart(String url) {
//
//
//        JSONObject media = new JSONObject();
//
//
//
////        try {
////            media = new JSONObject(client.newCall(request).execute().body().string());
////        } catch (Exception e) {
////
////        }
//
//
//        return media;
//    }


    private JSONArray getWatched(String type) {
        JSONArray watched = new JSONArray();

        String url = mContext.getResources().getString(R.string.trakt_api_url) + "/users/" + getTraktUser().ids.get("slug") + "/watched/" + type;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("trakt-api-version", mContext.getResources().getString(R.string.trakt_api_version))
                .addHeader("trakt-api-key", mContext.getResources().getString(R.string.trakt_api_key))
                .build();

        try {
            watched = new JSONArray(client.newCall(request).execute().body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return watched;
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

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("trakt-api-version", mContext.getResources().getString(R.string.trakt_api_version))
                .addHeader("trakt-api-key", mContext.getResources().getString(R.string.trakt_api_key))
                .build();
        Response response = client.newCall(request).execute();

        list = new JSONArray(response.body().string());

        return list;
    }

    private String getAccessToken() {
        String token = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String token_string = sharedPreferences.getString("trakt_token", "");
        if(token_string.equals("")) return "";
        try {
            JSONObject json_token = new JSONObject(token_string);
            token = json_token.getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return token;
    }

    public boolean saveSettings(String key, String data) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, data);
        return editor.commit();
    }

    public boolean userLoggedIn() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return !sharedPreferences.getString("trakt_user", "").equals("");
    }

    public void addToTrakt(String where, Video video) {
        String url = mContext.getResources().getString(R.string.trakt_api_url) + "/sync/" + where;
        OkHttpClient client = new OkHttpClient();

        JSONObject v = new JSONObject();
        JSONArray va = new JSONArray();
        JSONObject e = new JSONObject();
        JSONObject ids = new JSONObject();

        try {
            ids.put("trakt", video.id);
            e.put("ids", ids);
            va.put(e);
            v.put(video.videoType + "s", va);

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), v.toString());

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + getAccessToken())
                    .addHeader("trakt-api-version", mContext.getResources().getString(R.string.trakt_api_version))
                    .addHeader("trakt-api-key", mContext.getResources().getString(R.string.trakt_api_key))
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            System.out.println(response.body().string());
            System.out.println(response.headers());

        } catch (Exception ex) {
            ex.printStackTrace();
        }



    }

    public void markAsWatched(Video video) {
        scrobble("start", video, 0);
        scrobble("stop", video, 100);

    }

    public void scrobble(String type, Video v, double progress) {
        OkHttpClient client = new OkHttpClient();


        String url = mContext.getResources().getString(R.string.trakt_api_url) + "/scrobble/" + type;
        String b = "{\"" + v.videoType + "\": {\"ids\": {\"trakt\": " + v.id + "}}, \"progress\": " + progress + ", \"app_version\": \"" + mContext.getResources().getString(R.string.app_version) + "\", \"app_date\": \"" + mContext.getResources().getString(R.string.app_date) + "\"}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), b);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("trakt-api-version", mContext.getResources().getString(R.string.trakt_api_version))
                .addHeader("trakt-api-key", mContext.getResources().getString(R.string.trakt_api_key))
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public JSONObject getCodeFromTrakt() {
        String url = mContext.getResources().getString(R.string.trakt_api_url) + "/oauth/device/code";

        OkHttpClient client = new OkHttpClient();
        Map<String, String> params = new HashMap<>();
        params.put("client_id", mContext.getResources().getString(R.string.trakt_api_key));
        JSONObject parameter = new JSONObject(params);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), parameter.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String res = response.body().string().toString();
            return new JSONObject(res);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }



    public JSONObject getTokenFromTrakt(String device_code) {
        String url = mContext.getResources().getString(R.string.trakt_api_url) + "/oauth/device/token";

        OkHttpClient client = new OkHttpClient();
        Map<String, String> params = new HashMap<>();
        params.put("client_id", mContext.getResources().getString(R.string.trakt_api_key));
        params.put("client_secret", mContext.getResources().getString(R.string.trakt_api_secret));

        String dc = device_code;
        if(device_code.equals("reauth")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            try {
                JSONObject existing_token = new JSONObject(sharedPreferences.getString("trakt_token", ""));
                dc = existing_token.getString("refresh_token");
                String at = existing_token.getString("access_token");
                params.put("refresh_token", dc);
                params.put("access_token", dc);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            params.put("code", dc);
        }


        JSONObject parameter = new JSONObject(params);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), parameter.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String res = response.body().string();
            System.out.println(response.headers());
            return new JSONObject(res.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;


    }

    public void reauthTraktToken() {
        JSONObject reauth = getTokenFromTrakt("reauth");
        if(reauth != null) {
            saveSettings("trakt_token", reauth.toString());
        }
        System.out.println(reauth);
    }


    public JSONObject getUserFromTrakt(String access_token){
        String url = mContext.getResources().getString(R.string.trakt_api_url) + "/users/settings";


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + access_token)
                .addHeader("trakt-api-version", mContext.getResources().getString(R.string.trakt_api_version))
                .addHeader("trakt-api-key", mContext.getResources().getString(R.string.trakt_api_key))
                .build();

        try {
            Response response = client.newCall(request).execute();
            String res = response.body().string().toString();
            return new JSONObject(res);
        } catch (Exception e) {
            e.printStackTrace();
        }



        return null;

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





    private JSONObject getDataFromTMDB(String url) throws IOException, JSONException {

        JSONObject media = new JSONObject();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();


        try {
            media = new JSONObject(client.newCall(request).execute().body().string());
        } catch (Exception e) {

        }


        return media;
    }

    public String getVersion() {

        String version = "";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://chilly.adisdurakovic.com/version.txt").build();
        try {
            version = client.newCall(request).execute().body().string();
            System.out.println(version);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return version;

    }



}


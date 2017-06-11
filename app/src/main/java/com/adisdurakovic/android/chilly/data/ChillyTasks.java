package com.adisdurakovic.android.chilly.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ImageCardView;

import com.adisdurakovic.android.chilly.model.TraktGson;
import com.adisdurakovic.android.chilly.model.Video;
import com.adisdurakovic.android.chilly.stream.StreamGrabber;
import com.adisdurakovic.android.chilly.stream.StreamSource;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by add on 10/06/2017.
 */

public class ChillyTasks {

    public interface HomeLoaderResponse {
        void onLoadFinish(List<Video> start_movies, List<Video> start_tvshows);
    }


    // Background ASYNC Task to login by making HTTP Request
    public static class HomeLoaderTask extends AsyncTask<String, String, String> {

        List<Video> start_movies;
        List<Video> start_tvshows;
        HomeLoaderResponse delegate = null;
        Context ctx;

        public HomeLoaderTask(Context ctx, HomeLoaderResponse del) {
            this.delegate = del;
            this.ctx = ctx;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//        Toast.makeText(activity, "Loading...", Toast.LENGTH_SHORT).show();
        }

        // Checking login in background
        protected String doInBackground(String... params) {

            try {
                start_movies = Chilly.getInstance(ctx).getTrendingMovies(10);
                start_tvshows = Chilly.getInstance(ctx).getTrendingTVShows(10);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";

        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String file_url) {
            delegate.onLoadFinish(start_movies, start_tvshows);

        }
    }



    public interface ImageLoaderResponse {
        void onLoadFinish(Map<String, String> images, Video video);
    }


    // Background ASYNC Task to login by making HTTP Request
    public static class ImageLoaderTask extends AsyncTask<String, String, String> {

        ImageLoaderResponse delegate = null;
        Map<String, String> images = new HashMap<>();
        Context ctx;
        Video video;

        public ImageLoaderTask(Context ctx, ImageLoaderResponse del, Video v) {
            this.delegate = del;
            this.ctx = ctx;
            this.video = v;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//        Toast.makeText(activity, "Loading...", Toast.LENGTH_SHORT).show();
        }

        // Checking login in background
        protected String doInBackground(String... params) {

            images = Chilly.getInstance(ctx).getTMDBImages(video);
            return "";

        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String file_url) {
            delegate.onLoadFinish(images, video);

        }
    }




    public interface VideoLoaderResponse {
        void onGrabVideos(List<Video> video_list);
    }

    // Background ASYNC Task to login by making HTTP Request
    public static class VideoLoaderTask extends AsyncTask<String, String, String> {

        VideoLoaderResponse delegate = null;
        List<Video> video_list;
        private ListElem elem;
        Context ctx;
        int page;


        public VideoLoaderTask(Context ctx, VideoLoaderResponse del, ListElem elem, int page) {
            this.delegate = del;
            this.ctx = ctx;
            this.video_list = new ArrayList<>();
            this.elem = elem;
            this.page = page;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//        Toast.makeText(activity, "Loading...", Toast.LENGTH_SHORT).show();
        }

        // Checking login in background
        protected String doInBackground(String... params) {


            try {

                if(elem.tvshow != null && elem.season != null) {
                    video_list = Chilly.getInstance(ctx).getEpisodesForShowSeason(elem.tvshow, elem.season);
                } else {
                    if(elem.slug.contains("public")) {
                        video_list = Chilly.getInstance(ctx).getPublicVideos(elem.slug.replace("public-", ""), elem.videoType, 40, page);
                    }

                    if(elem.slug.contains("user")) {
                        video_list = Chilly.getInstance(ctx).getUserVideos(elem.slug.replace("user-", ""), elem.videoType);
                        Collections.sort(video_list, new Comparator<Video>() {
                            @Override
                            public int compare(Video o1, Video o2) {
                                return o1.title.compareTo(o2.title);
                            }
                        });
                    }

                    if(!elem.filterType.equals("")) {
                        video_list = Chilly.getInstance(ctx).getByFilter(elem, 40);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";

        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String file_url) {
            delegate.onGrabVideos(video_list);
        }
    }



    public interface SeasonResponse {
        void onGetSeasons(List<Video> seasons);
    }

    public static class SeasonTask extends AsyncTask<String, String, String> {

        private final Video mSelectedVideo;
        ArrayObjectAdapter show_seasons;
        Context ctx;

        SeasonResponse delegate;

        List<Video> video_list = new ArrayList<>();

        public SeasonTask(Context ctx, SeasonResponse del, Video video) {
            this.mSelectedVideo = video;
            this.ctx = ctx;
            this.delegate = del;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Checking login in background
        protected String doInBackground(String... params) {

            try {
                video_list = Chilly.getInstance(ctx).getSeasonsForShow(mSelectedVideo);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";

        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String file_url) {
            delegate.onGetSeasons(video_list);
        }
    }


    public interface StreamResponse {
        void onStreamGrab(List<StreamSource> streams);
    }


    public static class StreamTask extends AsyncTask<String, String, String> {

        private final Video mSelectedVideo;
        StreamResponse delegate;
        List<StreamSource> streams;

        public StreamTask(StreamResponse del, Video video) {
            this.mSelectedVideo = video;
            this.delegate = del;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Checking login in background
        protected String doInBackground(String... params) {


            try {
                streams = StreamGrabber.getSources(mSelectedVideo);
            } catch (IOException e) {

            }

            return "";

        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String streamurl) {
            // dismiss the dialog once done
//        System.out.println(streamurl);
            delegate.onStreamGrab(streams);

        }
    }





    public interface RequestCodeResponse {
        void onReceiveCode(JSONObject loginResponse);
    }


    public static class RequestCodeTask extends AsyncTask<String, String, String> {

        RequestCodeResponse delegate;
        Context ctx;
        JSONObject loginResponse = new JSONObject();

        public RequestCodeTask(Context context, RequestCodeResponse del) {
            this.delegate = del;
            this.ctx = context;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Checking login in background
        protected String doInBackground(String... params) {

            loginResponse = Chilly.getInstance(ctx).getCodeFromTrakt();

            return "";
        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String somestring) {
            // dismiss the dialog once done
            delegate.onReceiveCode(loginResponse);

        }
    }


    public interface RequestTokenResponse {
        void onReceiveToken(JSONObject loginResponse);
    }


    public static class RequestTokenTask extends AsyncTask<String, String, String> {

        RequestTokenResponse delegate;
        Context ctx;
        JSONObject tokenResponse = new JSONObject();
        String code;

        public RequestTokenTask(Context context, RequestTokenResponse del, String device_code) {
            this.delegate = del;
            this.ctx = context;
            this.code = device_code;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Checking login in background
        protected String doInBackground(String... params) {

            tokenResponse = Chilly.getInstance(ctx).getTokenFromTrakt(code);
            return "";

        }

        protected void onPostExecute(String somestring) {
            delegate.onReceiveToken(tokenResponse);
        }
    }


    public interface RequestUserResponse {
        void onReceiveUser(JSONObject userResponse);
    }


    public static class RequestUserTask extends AsyncTask<String, String, String> {

        RequestUserResponse delegate;
        Context ctx;
        JSONObject userResponse = new JSONObject();
        String code;

        public RequestUserTask(Context context, RequestUserResponse del, String access_token) {
            this.delegate = del;
            this.ctx = context;
            this.code = access_token;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Checking login in background
        protected String doInBackground(String... params) {

            userResponse = Chilly.getInstance(ctx).getUserFromTrakt(code);
            return "";

        }

        protected void onPostExecute(String somestring) {
            delegate.onReceiveUser(userResponse);
        }
    }




    public interface SearchLoaderResponse {
        void onSearchFinish(List<Video> found_videos);
    }


    // Background ASYNC Task to login by making HTTP Request
    public static class SearchLoaderTask extends AsyncTask<String, String, String> {

        List<Video> found_items;
        SearchLoaderResponse delegate = null;
        Context ctx;
        String query = "";

        public SearchLoaderTask(Context ctx, SearchLoaderResponse del, String q) {
            this.delegate = del;
            this.ctx = ctx;
            this.query = q;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//        Toast.makeText(activity, "Loading...", Toast.LENGTH_SHORT).show();
        }

        // Checking login in background
        protected String doInBackground(String... params) {

            found_items = Chilly.getInstance(ctx).getBySearch(query);
            Collections.sort(found_items, new Comparator<Video>() {
                @Override
                public int compare(Video o1, Video o2) {
                    return o2.productionYear.compareTo(o1.productionYear);
                }
            });

            return "";

        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String file_url) {
            delegate.onSearchFinish(found_items);

        }
    }




    public interface ListResponse {
        void onListGrab(List<ListElem> output, String title, String subtitle, Video video);
    }


    public static class ListTask extends AsyncTask<String, String, String> {

        ListElem item;
        ListResponse delegate;
        List<ListElem> list = new ArrayList<ListElem>();
        Context ctx;
        String title = "";
        String subtitle = "";

        public ListTask(Context ctx, ListResponse del, ListElem item) {
            this.item = item;
            this.delegate = del;
            this.ctx = ctx;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Checking login in background
        protected String doInBackground(String... params) {



            try {
                switch(item.slug) {
                    case "genres":
                        list = Chilly.getInstance(ctx).getGenres(item.videoType);
                        title = "GENRES";
                        subtitle = "Browse " + item.videoType + "s by genres";
                        break;
                    case "trakt-public-list":
                        list = Chilly.getInstance(ctx).getPublicList(item.videoType);
                        title = item.videoType.toUpperCase() + "S";
                        subtitle = "Browse " + item.videoType + "s by categories";
                        break;
                    case "trakt-actions":
                        list = Chilly.getInstance(ctx).getTraktActions(item.videoType, item.video);
                        title = "TRAKT";
                        subtitle = "Perform a Trakt-action.";
                        break;
                    case "get-sources":
                        List<StreamSource> source_list = StreamGrabber.getSources(item.video);

                        for(StreamSource s : source_list) {
                            item.video.videoUrl = s.url;
                            ListElem le = new ListElem(s.quality + "P", "play-video",s.url, s.provider, "");
                            list.add(le);
                        }
                        title = "PLAY FROM";
                        subtitle = "Select a stream provider to play from.";
                        break;


                }

            } catch (Exception e) {

            }
            return "";

        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String somestring) {
            delegate.onListGrab(list, title, subtitle, item.video);
        }
    }

    public interface TraktResponse {
        void onUserGet(TraktGson.TraktUser user, Drawable icon);
    }


    public static class TraktTask extends AsyncTask<String, String, String> {

        Context ctx;
        String action;
        Video v;
        TraktResponse delegate;
        TraktGson.TraktUser user;
        Drawable icon = null;

        public TraktTask(Context ctx, String action, Video video) {
            this.action = action;
            this.v = video;
            this.ctx = ctx;
        }

        public TraktTask(Context ctx, TraktResponse del, String action) {
            this.action = action;
            this.ctx = ctx;
            this.delegate = del;
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Checking login in background
        protected String doInBackground(String... params) {



            switch (action) {
                case "add-to-collection":
                    Chilly.getInstance(ctx).addToTrakt("collection", v);
                    break;
                case "add-to-watchlist":
                    Chilly.getInstance(ctx).addToTrakt("watchlist", v);
                    break;
                case "mark-as-watched":
                    Chilly.getInstance(ctx).markAsWatched(v);
                    break;
                case "get-user-name":
                    user = Chilly.getInstance(ctx).getTraktUser();

                    try {
                        InputStream is = (InputStream)new URL(user.images.get("avatar").get("full")).getContent();
                        icon = Drawable.createFromStream(is, "src name");
                    } catch (Exception e) {
                        System.out.println("Exc=" + e);
                    }


            }

            return "";

        }


        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String somestring) {
            if(delegate != null) {
                delegate.onUserGet(user, icon);
            }
        }
    }




}




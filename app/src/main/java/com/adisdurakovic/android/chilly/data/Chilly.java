package com.adisdurakovic.android.chilly.data;

import android.content.Context;

import com.adisdurakovic.android.chilly.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

    public JSONObject getPopularMovies() throws JSONException, IOException {

        JSONObject list = new JSONObject();

        JSONArray googlevideos = new JSONArray();
        JSONObject googlevideos_obj = new JSONObject();
        JSONArray videos = new JSONArray();


        List<String> imdb_popular_movies = IMDBGrabber.getList(mContext.getResources().getString(R.string.imdb_url_movies_popular));


        int x = 0;
        for(Iterator<String> i = imdb_popular_movies.iterator(); i.hasNext();) {

            x++;

            String imdb_id = i.next();
            String trakt_url = String.format(mContext.getResources().getString(R.string.trakt_api_url_movie_info), imdb_id);
            JSONObject trakt_info = TraktGrabber.getInfo(trakt_url, mContext.getResources().getString(R.string.trakt_api_version), mContext.getResources().getString(R.string.trakt_api_key));
            String fanart_url = String.format(mContext.getResources().getString(R.string.fanart_api_url_movies), trakt_info.getJSONObject("ids").getString("tmdb")) + "?api_key=" + mContext.getResources().getString(R.string.fanart_api_key);
            JSONObject fanart_media = FanartGrabber.getMedia(fanart_url);
            JSONArray moviebackground;
            JSONArray movieposter;

            String poster = "";
            String background = "";

            movieposter = fanart_media.optJSONArray("movieposter");
            moviebackground = fanart_media.optJSONArray("moviebackground");

            if(moviebackground != null) {
                background = moviebackground.optJSONObject(0).optString("url");
            }

            if(movieposter != null) {
                poster = movieposter.getJSONObject(0).optString("url");
                // set english poster
                for(int j = 0; j < movieposter.length(); j++) {
                    if(movieposter.optJSONObject(j).optString("lang") == "en") {
                        poster = movieposter.getJSONObject(j).optString("url");
                        break;
                    }
                }
            }


            JSONObject elem = new JSONObject();

            elem.put("title", trakt_info.optString("title"));
            elem.put("description", trakt_info.optString("overview"));
            elem.put("card", poster);
            elem.put("background", background);
            elem.put("studio", trakt_info.optString("year"));
            JSONArray sources = new JSONArray();
            sources.put("https://nowhere" + x + ".mp4");
            elem.put("sources", sources);


            videos.put(elem);

        }

        googlevideos_obj.put("category", "Movies popular");
        googlevideos_obj.put("videos", videos);

        googlevideos.put(googlevideos_obj);


        list.put("googlevideos", googlevideos);

        return list;


    }

}

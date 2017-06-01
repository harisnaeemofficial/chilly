package com.adisdurakovic.android.chilly.data;

import com.adisdurakovic.android.chilly.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by add on 22/05/2017.
 */

public class StreamProvider {

    public List<StreamSource> getStreamSources(Video video) throws IOException {
        List<StreamSource> urllist = new ArrayList<>();


        switch(video.videoType) {
            case "movie":
                urllist = getMovieSources(video);
                break;
            case "episode":
                urllist = getEpisodeSources(video);
                break;
        }


        return urllist;
    }

    public List<StreamSource> getMovieSources(Video video) throws IOException {
        return new ArrayList<>();
    }

    public List<StreamSource> getEpisodeSources(Video video) throws IOException {
        return new ArrayList<>();
    }

    class StreamSource {
        public String url;
        public String quality;
        public String provider;
        public String videosource;
    }

}

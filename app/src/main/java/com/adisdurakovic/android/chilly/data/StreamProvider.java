package com.adisdurakovic.android.chilly.data;

import com.adisdurakovic.android.chilly.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by add on 22/05/2017.
 */

public class StreamProvider {

    public List<StreamSource> getMovieStreamURL(Video video) throws IOException {
        return new ArrayList<>();
    }

    class StreamSource {
        public String url;
        public String quality;
        public String provider;
        public String videosource;
    }

}

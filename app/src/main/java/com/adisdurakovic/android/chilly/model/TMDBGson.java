package com.adisdurakovic.android.chilly.model;

import java.util.List;

/**
 * Created by add on 09/06/2017.
 */

public class TMDBGson {

    public class TMDBElem {
        public String tmdb_id;
        public String thetvdb_id;
        public List<TMDBData> backdrops;
        public List<TMDBData> posters;
        public List<TMDBData> stills;
        public List<TMDBData> tvposter;
        public List<TMDBData> showbackground;
        public List<TMDBData> seasonposter;
    }


    public class TMDBData {
        public String file_path;
    }

}

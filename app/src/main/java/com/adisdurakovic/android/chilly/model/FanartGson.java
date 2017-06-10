package com.adisdurakovic.android.chilly.model;

import java.util.List;

/**
 * Created by add on 09/06/2017.
 */

public class FanartGson {

    public class FanartElem {
        public List<FanartData> movieposter;
        public List<FanartData> moviebackground;
        public List<FanartData> tvposter;
        public List<FanartData> showbackground;
        public List<FanartData> seasonposter;
    }


    public class FanartData {
        public String url;
        public String lang;
        public String season;
    }

}

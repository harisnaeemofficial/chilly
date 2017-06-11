package com.adisdurakovic.android.chilly.model;

import java.util.List;
import java.util.Map;

/**
 * Created by add on 09/06/2017.
 */

// Movie, Show, Season, Episode

public class TraktGson {

    public class TraktElem {
        public String title;
        public long year;
        public Map<String,String> ids;
        public String overview;
        public long runtime;
        public String trailer;
        public double rating;
        public List<String> genres;
        public String language;
        public boolean watched;

        @Override
        public String toString() {
            return "title = " + title + "; year = " + year + "; ids = " + ids.toString();
        }

    }

    public class TraktMovie extends TraktElem {

    }

    public class TraktShow extends TraktElem {

        public long number;
        public long aired_episodes;
        public String network;

    }

    public class TraktSeason extends TraktElem {
        public long number;
        public long episode_count;
        public long aired_episodes;
        public String first_aired;
    }

    public class TraktEpisode extends TraktElem {
        public long season;
        public long number;
        public String first_aired;
    }

    public class TraktWatchedElem {
        public String title;
        public long year;
        public Map<String,String> ids;

        @Override
        public String toString() {
            return "title = " + title + "; year = " + year + "; ids = " + ids.toString();
        }

    }

    public class TraktWatched extends TraktElem {

        public TraktWatchedElem movie;
        public TraktWatchedElem show;
        public List<TraktWatchedSeason> seasons;

    }


    public class TraktWatchedSeason {
        public long number;
        public List<TraktWatchedEpisode> episodes;
    }

    public class TraktWatchedEpisode {
        public long number;
    }

    public class TraktUser {
        public String username;
        public String name;
        public Map<String, Map<String, String>> images;
        public Map<String, String> ids;
    }

}


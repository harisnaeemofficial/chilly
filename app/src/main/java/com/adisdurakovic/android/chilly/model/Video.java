/*
 * Copyright (c) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adisdurakovic.android.chilly.model;

import android.media.MediaDescription;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Video is an immutable object that holds the various metadata associated with a single video.
 */
public final class Video implements Parcelable {
    public final long id;
    public String tvdb_id;
    public String tmdb_id;
    public String imdb_id;
    public final String title;
    public final String description;
    public final String bgImageUrl;
    public final String cardImageUrl;
    public  String videoUrl;
    public final String studio;
    public final String productionYear;
    public final String videoType;
    public long airedEpisodes;
    public final String seasonNumber;
    public final String episodeNumber;
    public final Video episodeShow;
    public final long runtime;
    public final double rating;
    public final String trailer;
    public final boolean watched;


    private Video(
            final long id,
            final String tvdb_id,
            final String tmdb_id,
            final String imdb_id,
            final String title,
            final String desc,
            final String cardImageUrl,
            final String bgImageUrl,
            final String videoUrl,
            final String studio,
            final String productionYear,
            final String videoType,
            final long airedEpisodes,
            final String seasonNumber,
            final String episodeNumber,
            final Video episodeShow,
            final long runtime,
            final double rating,
            final String trailer,
            final boolean watched
            ) {
        this.id = id;
        this.tvdb_id = tvdb_id;
        this.tmdb_id = tmdb_id;
        this.imdb_id = imdb_id;
        this.title = title;
        this.description = desc;
        this.cardImageUrl = cardImageUrl;
        this.bgImageUrl = bgImageUrl;
        this.videoUrl = videoUrl;
        this.studio = studio;
        this.productionYear = productionYear;
        this.videoType = videoType;
        this.airedEpisodes = airedEpisodes;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.episodeShow = episodeShow;
        this.runtime = runtime;
        this.rating = rating;
        this.trailer = trailer;
        this.watched = watched;
    }

    protected Video(Parcel in) {
        id = in.readLong();
        tvdb_id = in.readString();
        tmdb_id = in.readString();
        imdb_id = in.readString();
        title = in.readString();
        description = in.readString();
        cardImageUrl = in.readString();
        bgImageUrl = in.readString();
        videoUrl = in.readString();
        studio = in.readString();
        productionYear = in.readString();
        videoType = in.readString();
        airedEpisodes = in.readLong();
        seasonNumber = in.readString();
        episodeNumber = in.readString();
        episodeShow = in.readParcelable(Video.class.getClassLoader());
        runtime = in.readLong();
        rating = in.readDouble();
        trailer = in.readString();
        watched = in.readByte() != 0;
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    @Override
    public boolean equals(Object m) {
        return m instanceof Video && id == ((Video) m).id;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(tvdb_id);
        dest.writeString(tmdb_id);
        dest.writeString(imdb_id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(cardImageUrl);
        dest.writeString(bgImageUrl);
        dest.writeString(videoUrl);
        dest.writeString(studio);
        dest.writeString(productionYear);
        dest.writeString(videoType);
        dest.writeLong(airedEpisodes);
        dest.writeString(seasonNumber);
        dest.writeString(episodeNumber);
        dest.writeParcelable(episodeShow, flags);
        dest.writeLong(runtime);
        dest.writeDouble(rating);
        dest.writeString(trailer);
        dest.writeByte(((byte)(watched? 1 : 0)));
    }

    @Override
    public String toString() {

        String es = "";
        if(episodeShow != null) {
            es = episodeShow.toString();
        }

        String s = "Video{";
        s += "id=" + id;
        s += ", tvdb_id='" + tvdb_id + "'";
        s += ", tmdb_id='" + tmdb_id + "'";
        s += ", imdb_id='" + imdb_id + "'";
        s += ", title='" + title + "'";
        s += ", description='" + description + "'";
        s += ", cardImageUrl='" + cardImageUrl + "'";
        s += ", bgImageUrl='" + bgImageUrl + "'";
        s += ", videoUrl='" + videoUrl + "'";
        s += ", studio='" + studio + "'";
        s += ", productionYear='" + productionYear + "'";
        s += ", videoType='" + videoType + "'";
        s += ", airedEpisodes='" + airedEpisodes + "'";
        s += ", seasonNumber='" + seasonNumber + "'";
        s += ", episodeNumber='" + episodeNumber + "'";
        s += ", episodeShow='" + es + "'";
        s += ", runtime='" + runtime + "'";
        s += ", rating='" + rating + "'";
        s += ", trailer='" + trailer + "'";
        s += ", watched='" + watched + "'";
        s += "}";
        return s;
    }

    // Builder for Video object.
    public static class VideoBuilder {
        private long id;
        public String tvdb_id;
        public String tmdb_id;
        public String imdb_id;
        private String title;
        private String desc;
        private String cardImageUrl;
        private String bgImageUrl;
        private String videoUrl;
        private String studio;
        private String productionYear;
        private String videoType;
        public long airedEpisodes;
        private String seasonNumber;
        private String episodeNumber;
        private Video episodeShow;
        public long runtime;
        public double rating;
        private String trailer;
        private boolean watched;

        public VideoBuilder id(long id) {
            this.id = id;
            return this;
        }

        public VideoBuilder tvdb_id(String tvdb_id) {
            this.tvdb_id = tvdb_id;
            return this;
        }

        public VideoBuilder tmdb_id(String tmdb_id) {
            this.tmdb_id = tmdb_id;
            return this;
        }

        public VideoBuilder imdb_id(String imdb_id) {
            this.imdb_id = imdb_id;
            return this;
        }

        public VideoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public VideoBuilder description(String desc) {
            this.desc = desc;
            return this;
        }


        public VideoBuilder cardImageUrl(String cardImageUrl) {
            this.cardImageUrl = cardImageUrl;
            return this;
        }

        public VideoBuilder bgImageUrl(String bgImageUrl) {
            this.bgImageUrl = bgImageUrl;
            return this;
        }

        public VideoBuilder videoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public VideoBuilder studio(String studio) {
            this.studio = studio;
            return this;
        }

        public VideoBuilder productionYear(String productionYear) {
            this.productionYear = productionYear;
            return this;
        }

        public VideoBuilder videoType(String videoType) {
            this.videoType = videoType;
            return this;
        }

        public VideoBuilder airedEpisodes(long airedEpisodes) {
            this.airedEpisodes = airedEpisodes;
            return this;
        }

        public VideoBuilder seasonNumber(String seasonNumber) {
            this.seasonNumber = seasonNumber;
            return this;
        }

        public VideoBuilder episodeNumber(String episodeNumber) {
            this.episodeNumber = episodeNumber;
            return this;
        }

        public VideoBuilder episodeShow(Video episodeShow) {
            this.episodeShow = episodeShow;
            return this;
        }

        public VideoBuilder runtime(long runtime) {
            this.runtime = runtime;
            return this;
        }

        public VideoBuilder rating(double rating) {
            this.rating = rating;
            return this;
        }

        public VideoBuilder trailer(String trailer) {
            this.trailer = trailer;
            return this;
        }

        public VideoBuilder watched(boolean watched) {
            this.watched = watched;
            return this;
        }


        public Video buildFromMediaDesc(MediaDescription desc) {
            return new Video(
                    Long.parseLong(desc.getMediaId()),
                    "",
                    "",
                    "",
                    String.valueOf(desc.getTitle()),
                    String.valueOf(desc.getDescription()),
                    "", // Media URI - not provided by MediaDescription.
                    "", // Background Image URI - not provided by MediaDescription.
                    String.valueOf(desc.getIconUri()),
                    "",
                    String.valueOf(desc.getSubtitle()),
                    "",
                    0,
                    "",
                    "",
                    null,
                    0,
                    0,
                    "",
                    false
            );
        }

        public Video build() {
            return new Video(
                    id,
                    tvdb_id,
                    tmdb_id,
                    imdb_id,
                    title,
                    desc,
                    cardImageUrl,
                    bgImageUrl,
                    videoUrl,
                    studio,
                    productionYear,
                    videoType,
                    airedEpisodes,
                    seasonNumber,
                    episodeNumber,
                    episodeShow,
                    runtime,
                    rating,
                    trailer,
                    watched
            );
        }
    }
}

package com.adisdurakovic.android.chilly.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.adisdurakovic.android.chilly.model.Video;

/**
 * Created by add on 30/05/2017.
 */

public class ListElem implements Parcelable {
    public String title;
    public String slug;
    public String videoType;
    public String filterType;
    public String action;
    public Video video;
    public Video tvshow;
    public Video season;

    public ListElem() {

    }

    public ListElem(String t, String s, String vt, String a, String ft) {
        this.title = t;
        this.slug = s;
        this.videoType = vt;
        this.action = a;
        this.filterType = ft;
        this.video = null;
    }

    public ListElem(String t, String s, Video v) {
        this.title = t;
        this.slug = s;
        this.videoType = "";
        this.action = "";
        this.filterType = "";
        this.video = v;
    }

    public ListElem(Parcel in) {
        title = in.readString();
        slug = in.readString();
        videoType = in.readString();
        action = in.readString();
        filterType = in.readString();
        video = in.readParcelable(Video.class.getClassLoader());
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(slug);
        dest.writeString(videoType);
        dest.writeString(action);
        dest.writeString(filterType);
        dest.writeParcelable(video, flags);
    }

    public static final Creator<ListElem> CREATOR = new Creator<ListElem>() {
        @Override
        public ListElem createFromParcel(Parcel in) {
            return new ListElem(in);
        }

        @Override
        public ListElem[] newArray(int size) {
            return new ListElem[size];
        }
    };

}

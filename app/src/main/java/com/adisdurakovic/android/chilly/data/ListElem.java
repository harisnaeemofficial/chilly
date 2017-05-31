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
        this.season = null;
        this.tvshow = null;
    }

    public ListElem(Parcel in) {
        title = in.readString();
        slug = in.readString();
        videoType = in.readString();
        action = in.readString();
        filterType = in.readString();
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

package com.adisdurakovic.android.chilly.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by add on 30/05/2017.
 */

public class ListElem implements Parcelable {
    public String title;
    public String slug;
    public String videoType;
    public String filterType;

    public ListElem(String t, String s, String vt, String ft) {
        this.title = t;
        this.slug = s;
        this.videoType = vt;
        this.filterType = ft;
    }

    public ListElem(Parcel in) {
        title = in.readString();
        slug = in.readString();
        videoType = in.readString();
        filterType = in.readString();
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(slug);
        dest.writeString(videoType);
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

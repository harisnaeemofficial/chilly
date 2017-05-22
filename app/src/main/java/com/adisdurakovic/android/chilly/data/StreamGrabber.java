package com.adisdurakovic.android.chilly.data;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.adisdurakovic.android.chilly.R;
import com.adisdurakovic.android.chilly.model.Video;
import com.adisdurakovic.android.chilly.ui.PlaybackOverlayActivity;
import com.adisdurakovic.android.chilly.ui.VideoDetailsActivity;
import com.adisdurakovic.android.chilly.ui.VideoDetailsFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by add on 22/05/2017.
 */

public class StreamGrabber {

    public static List<String> getSources(Video video) throws IOException {

        List<String> source_list = new ArrayList<>();

        List<StreamProvider> stream_provider = new ArrayList<>();

        stream_provider.add(new Stream_123movieshd());

        for(Iterator<StreamProvider> i = stream_provider.iterator(); i.hasNext();) {
            StreamProvider provider = i.next();
            source_list.add(provider.getMovieStreamURL(video));
        }

        return source_list;
    }

    public static String getLastSource(Video video) throws IOException {

        String source_url = "";
        List<String> source_list = getSources(video);

        if(source_list.size() > 0) {
            return source_list.get(0);
        }

        return source_url;
    }

}
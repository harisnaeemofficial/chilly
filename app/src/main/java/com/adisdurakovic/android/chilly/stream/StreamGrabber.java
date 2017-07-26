package com.adisdurakovic.android.chilly.stream;

import com.adisdurakovic.android.chilly.stream.StreamSource;
import com.adisdurakovic.android.chilly.model.Video;
import com.adisdurakovic.android.chilly.stream.StreamProvider;
import com.adisdurakovic.android.chilly.stream.Stream_123movieshd;
import com.adisdurakovic.android.chilly.stream.Stream_dayt;
import com.adisdurakovic.android.chilly.stream.Stream_miradetodo;
import com.adisdurakovic.android.chilly.stream.Stream_sezonlukdizi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by add on 22/05/2017.
 */

public class StreamGrabber {

    public static List<StreamSource> getSources(Video video) throws IOException {

        List<StreamSource> source_list = new ArrayList<>();

        List<StreamProvider> stream_provider = new ArrayList<>();

//        stream_provider.add(new Stream_123movieshd());
//        stream_provider.add(new Stream_miradetodo());
//        stream_provider.add(new Stream_dayt());
//        stream_provider.add(new Stream_sezonlukdizi());
//        stream_provider.add(new Stream_istream());
//        stream_provider.add(new Stream_dizigold());
        stream_provider.add(new Stream_putlocker());

        for(Iterator<StreamProvider> i = stream_provider.iterator(); i.hasNext();) {
            StreamProvider provider = i.next();
            List<StreamSource> streamSources = provider.getStreamSources(video);
            if(streamSources.size() == 0) continue;
            for(Iterator<StreamSource> j = streamSources.iterator(); j.hasNext();) {
                StreamSource currentsource = j.next();

                if(source_list.size() > 0 && currentsource.quality > source_list.get(0).quality) {
                    source_list.add(0, currentsource);
                } else {
                    source_list.add(currentsource);
                }
            }
        }

        return source_list;
    }

    public static String getLastSource(Video video) throws IOException {

        String source_url = "";
        List<StreamSource> source_list = getSources(video);


        if(source_list.size() > 0) {
            return source_list.get(0).url;
        }

        return source_url;
    }

}
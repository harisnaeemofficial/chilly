package com.adisdurakovic.android.chilly.data;

import com.adisdurakovic.android.chilly.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by add on 22/05/2017.
 */

public class StreamGrabber {

    public static List<StreamProvider.StreamSource> getSources(Video video) throws IOException {

        List<StreamProvider.StreamSource> source_list = new ArrayList<>();

        List<StreamProvider> stream_provider = new ArrayList<>();

        stream_provider.add(new Stream_123movieshd());
        stream_provider.add(new Stream_miradetodo());
        stream_provider.add(new Stream_dayt());
        stream_provider.add(new Stream_sezonlukdizi());

        for(Iterator<StreamProvider> i = stream_provider.iterator(); i.hasNext();) {
            StreamProvider provider = i.next();
            List<StreamProvider.StreamSource> streamSources = provider.getStreamSources(video);
            if(streamSources.size() == 0) continue;
            for(Iterator<StreamProvider.StreamSource> j = streamSources.iterator(); j.hasNext();) {
                StreamProvider.StreamSource currentsource = j.next();

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
        List<StreamProvider.StreamSource> source_list = getSources(video);


        if(source_list.size() > 0) {
            return source_list.get(0).url;
        }

        return source_url;
    }

}
package com.adisdurakovic.android.chilly.data;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by add on 21/05/2017.
 */

public class IMDBGrabber {


    public static List<String> getList(String url) throws IOException {

        List<String> list = new ArrayList<>();

        HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();
        urlConnection.setRequestMethod("GET");

        String html = HTTPGrabber.getContentFromURL(urlConnection);

        Document doc = Jsoup.parse(html);
        Elements itemLinks = doc.select("h3.lister-item-header a");

        for(Iterator<Element> i = itemLinks.iterator(); i.hasNext();) {
            Element item = i.next();
            String itemlink = item.attr("href");
            String imdb_id = itemlink.toString().replace("/?ref_=adv_li_tt", "").replace("/title/", "");
            list.add(imdb_id);
        }



        return list;
    }

}

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by add on 21/05/2017.
 */

public class IMDBGrabber {


    public static List<String> getList(String url) {

        List<String> list = new ArrayList<>();

        BufferedReader reader = null;

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new java.net.URL(url).openConnection();

            try {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                        "utf-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                Document doc = Jsoup.parse(sb.toString());
                Elements itemLinks = doc.select("h3.lister-item-header a");

                for(Iterator<Element> i = itemLinks.iterator(); i.hasNext();) {
                    Element item = i.next();
                    String itemlink = item.attr("href");
                    String imdb_id = itemlink.toString().replace("/?ref_=adv_li_tt", "").replace("/title/", "");
                    list.add(imdb_id);
                }

            } finally {
                urlConnection.disconnect();
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }

        } catch (Exception e) {

        }

        return list;
    }

}

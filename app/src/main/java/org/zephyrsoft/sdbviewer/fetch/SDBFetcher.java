package org.zephyrsoft.sdbviewer.fetch;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.zephyrsoft.sdbviewer.model.Song;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Security;
import java.util.List;

import static org.zephyrsoft.sdbviewer.Constants.LOG_TAG;

public class SDBFetcher {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public List<Song> fetchSongs(String url) {
        String songsXml = fetchRawDataFromNetwork(url);

        List<Song> songs = deserializeFromXml(songsXml);

        // TODO cache data until next download is possible
        return songs;
    }

    @VisibleForTesting
    String fetchRawDataFromNetwork(String url) {
        String songsXml = null;
        try {
            URL parsedUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) parsedUrl.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                songsXml = result.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "error while fetching raw data from network", e);
            throw new RuntimeException(e);
        }
        return songsXml;
    }

    @VisibleForTesting
    List<Song> deserializeFromXml(String xmlString) {
        return createGsonXml().fromXml(xmlString, new TypeToken<List<Song>>() {
        }.getType());
    }

    private GsonXml createGsonXml() {
        XmlParserCreator parserCreator = new XmlParserCreator() {
            @Override
            public XmlPullParser createParser() {
                try {
                    return XmlPullParserFactory.newInstance().newPullParser();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        return new GsonXmlBuilder()
            .setXmlParserCreator(parserCreator)
            .create();
    }


}

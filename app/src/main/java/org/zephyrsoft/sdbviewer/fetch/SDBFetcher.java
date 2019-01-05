package org.zephyrsoft.sdbviewer.fetch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

import org.xmlpull.v1.XmlPullParserFactory;
import org.zephyrsoft.sdbviewer.Constants;
import org.zephyrsoft.sdbviewer.R;
import org.zephyrsoft.sdbviewer.model.Song;
import org.zephyrsoft.sdbviewer.registry.Registry;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Security;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.zephyrsoft.sdbviewer.Constants.LOG_TAG;

public class SDBFetcher {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public static void createAndRegisterInstance() {
        Registry.register(SDBFetcher.class, new SDBFetcher());
    }

    private boolean shouldUseSavedData(Context context) {
        try {
            if (fileExists(context, Constants.FILE_LAST_UPDATED)
                && fileExists(context, Constants.FILE_SONGS)) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String hoursBetweenReloadsString = sharedPreferences.getString(context.getString(R.string.pref_songs_reload_interval),
                    String.valueOf(context.getResources().getInteger(R.integer.pref_songs_reload_interval_default)));
                int hoursBetweenReloads = Integer.valueOf(hoursBetweenReloadsString);

                String lastUpdatedString = readFile(context, Constants.FILE_LAST_UPDATED).replaceAll("\n", "");
                Date lastUpdated = DateFormat.getDateTimeInstance().parse(lastUpdatedString);
                long millisSinceLastReload = new Date().getTime() - lastUpdated.getTime();
                int hoursSinceLastReload = (int) (millisSinceLastReload / 1000 / 60 / 60);

                Log.i(Constants.LOG_TAG,"songs in local storage are " + hoursSinceLastReload + " hours old");
                return hoursSinceLastReload <= hoursBetweenReloads;
            }
        } catch (Exception e) {
            Log.w(Constants.LOG_TAG, "could not determine if the saved data should be used", e);
        }
        return false;
    }

    public List<Song> fetchSongs(Context context, String url) {
        try {
            if (shouldUseSavedData(context)) {
                String songsXml = readFile(context, Constants.FILE_SONGS);
                Log.i(Constants.LOG_TAG, "loaded songs from local storage");
                return deserializeFromXml(songsXml);
            }
        } catch (Exception e) {
            Log.w(Constants.LOG_TAG, "could not use saved songs data", e);
        }

        return fetchSongsFromNetwork(context, url);
    }

    private List<Song> fetchSongsFromNetwork(Context context, String url) {
        String songsXml = fetchRawDataFromNetwork(url);
        Log.i(Constants.LOG_TAG, "loaded songs from " + url);

        List<Song> songs = deserializeFromXml(songsXml);

        // cache data until next download
        writeFile(context, Constants.FILE_SONGS, songsXml);
        writeFile(context, Constants.FILE_LAST_UPDATED, DateFormat.getDateTimeInstance().format(new Date()));
        Log.i(Constants.LOG_TAG, "wrote songs to local storage");

        return songs;
    }

    public void invalidateSavedSongs(Context context) {
        try {
            if (fileExists(context, Constants.FILE_LAST_UPDATED)) {
                context.deleteFile(Constants.FILE_LAST_UPDATED);
            }
        } catch(Exception e) {
            Log.e(Constants.LOG_TAG, "error while deleting file " + Constants.FILE_LAST_UPDATED, e);
            throw new IllegalStateException("error while deleting file " + Constants.FILE_LAST_UPDATED);
        }
    }

    private boolean fileExists(Context context, String filename) {
        try {
            String[] existingFiles = context.fileList();
            for (String existingFile : existingFiles) {
                if (filename.equals(existingFile)) {
                    return true;
                }
            }
            return false;
        } catch(Exception e) {
            Log.e(Constants.LOG_TAG, "error while checking existence of file " + filename, e);
            throw new IllegalStateException("error while checking existence of file " + filename);
        }
    }

    private String readFile(Context context, String filename) {
        try {
            StringBuilder sb = new StringBuilder();
            FileInputStream inputStream = context.openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            bufferedReader.close();
            return sb.toString();
        } catch(Exception e) {
            Log.e(Constants.LOG_TAG, "error while reading file " + filename, e);
            throw new IllegalStateException("error while reading file " + filename);
        }
    }

    private void writeFile(Context context, String filename, String content) {
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch(Exception e) {
            Log.e(Constants.LOG_TAG, "error while writing to file " + filename, e);
            throw new IllegalStateException("error while writing to file " + filename);
        }
    }

    @VisibleForTesting
    String fetchRawDataFromNetwork(String url) {
        String songsXml;
        try {
            URL parsedUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) parsedUrl.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
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
        List<Song> fromXml = createGsonXml().fromXml(xmlString, new TypeToken<List<Song>>() {
        }.getType());

        for (Iterator<Song> iter = fromXml.iterator(); iter.hasNext();) {
            Song song = iter.next();
            if (song.isEmpty()) {
                iter.remove();
            }
        }

        return fromXml;
    }

    private GsonXml createGsonXml() {
        XmlParserCreator parserCreator = () -> {
            try {
                return XmlPullParserFactory.newInstance().newPullParser();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        return new GsonXmlBuilder()
            .setXmlParserCreator(parserCreator)
            .create();
    }


}

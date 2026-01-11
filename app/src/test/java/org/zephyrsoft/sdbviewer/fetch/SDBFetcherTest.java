package org.zephyrsoft.sdbviewer.fetch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

import android.util.Log;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.zephyrsoft.sdbviewer.db.DatabaseAccess;
import org.zephyrsoft.sdbviewer.model.Song;

import java.util.List;

public class SDBFetcherTest {

    private static final String EXAMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<songs>\n" +
        "<autoSort>true</autoSort>\n" +
        "<song><lyrics>These are a simple test song's lyrics.</lyrics><title>Simple Test Song</title><uuid>9e049d96-2251-4579-9f92-3235e813fab6</uuid></song>\n" +
        "<song><composer>T&amp;M That Guy</composer><authorText>Text Author</authorText><authorTranslation>Translation Author</authorTranslation><publisher>Publisher</publisher><additionalCopyrightNotes>Additional Copyright Notes</additionalCopyrightNotes><language>ENGLISH</language><songNotes>Song Notes</songNotes><tonality>C</tonality><chordSequence>C F G C</chordSequence><lyrics>These are a test song's lyrics.\n" +
        "With multiple lines.\n" +
        "With exactly three lines.</lyrics><title>Test Song</title><uuid>1e48767c-6cd8-48ea-917a-e6e4d93c149b</uuid></song>\n" +
        "<song><title>Test Image</title>\n" +
        "<uuid>2c44c9ac-226a-4478-b2fe-0e8dde3062cf</uuid>\n" +
        "<image>/some/weird/path/to/a/file.jpg</image>\n" +
        "<imageRotation>270</imageRotation></song>\n" +
        "</songs>";

    private SDBFetcher fetcher;

    @BeforeClass
    public static void setupAll() {
        MockedStatic<Log> log = Mockito.mockStatic(Log.class);
        log.when(() -> Log.e(anyString(), anyString())).thenReturn(1);
    }

    @Before
    public void setup() {
        DatabaseAccess databaseAccess = Mockito.mock(DatabaseAccess.class);
        fetcher = new SDBFetcher(databaseAccess);
    }

    @Test
    public void fetchRawDataFromNetwork() {
        String rawData = fetcher.fetchRawDataFromNetwork("https://codeberg.org/mathisdt/sdbviewer/raw/branch/master/songs.xml");

        assertNotNull(rawData);
        assertFalse(rawData.isEmpty());
    }

    @Test
    public void deserializeFromXml() {
        List<Song> songs = fetcher.deserializeFromXml(EXAMPLE_XML);

        assertNotNull(songs);
        assertEquals(3, songs.size());

        assertNotNull(songs.get(0).getLyrics());
        assertNull(songs.get(0).getImage());
        assertNull(songs.get(0).getImageRotation());

        assertNotNull(songs.get(1).getLyrics());
        assertTrue(songs.get(1).getLyrics().contains("\n"));
        assertNull(songs.get(1).getImage());
        assertNull(songs.get(1).getImageRotation());

        assertNotNull(songs.get(2).getTitle());
        assertNotNull(songs.get(2).getImage());
        assertNotNull(songs.get(2).getImageRotation());
    }
}
package org.zephyrsoft.sdbviewer.fetch;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zephyrsoft.sdbviewer.model.Song;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@PowerMockIgnore({"org.spongycastle.*", "org.xmlpull.v1.*"})
@PrepareForTest({Log.class})
@RunWith(PowerMockRunner.class)
public class SDBFetcherTest {

    private static final String EXAMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<songs>\n" +
        "<autoSort>true</autoSort>\n" +
        "<song><lyrics>These are a simple test song's lyrics.</lyrics><title>Simple Test Song</title><uuid>9e049d96-2251-4579-9f92-3235e813fab6</uuid></song>\n" +
        "<song><composer>T&amp;M That Guy</composer><authorText>Text Author</authorText><authorTranslation>Translation Author</authorTranslation><publisher>Publisher</publisher><additionalCopyrightNotes>Additional Copyright Notes</additionalCopyrightNotes><language>ENGLISH</language><songNotes>Song Notes</songNotes><tonality>C</tonality><chordSequence>C F G C</chordSequence><lyrics>These are a test song's lyrics.\n" +
        "With multiple lines.\n" +
        "With exactly three lines.</lyrics><title>Test Song</title><uuid>1e48767c-6cd8-48ea-917a-e6e4d93c149b</uuid></song>\n" +
        "</songs>";

    private SDBFetcher fetcher;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Log.class);
        Mockito.when(Log.e(anyString(), anyString())).thenReturn(1);

        fetcher = new SDBFetcher();
    }

    @Test
    public void fetchRawDataFromNetwork() {
        String rawData = fetcher.fetchRawDataFromNetwork("https://raw.githubusercontent.com/mathisdt/sdbviewer/master/songs.xml");

        assertNotNull(rawData);
        assertTrue(rawData.length() > 0);
    }

    @Test
    public void deserializeFromXml() {
        List<Song> songs = fetcher.deserializeFromXml(EXAMPLE_XML);

        assertNotNull(songs);
        assertTrue(songs.size() > 0);
    }
}
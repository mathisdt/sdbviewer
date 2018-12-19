package org.zephyrsoft.sdbviewer.model;

import java.io.Serializable;
import java.text.Collator;

public class Song implements Serializable, Comparable<Song> {

    private String title;
    private String composer;
    private String authorText;
    private String authorTranslation;
    private String publisher;
    private String additionalCopyrightNotes;
    private String language;
    private String songNotes;
    private String tonality;
    private String uuid;
    private String chordSequence;
    private String lyrics;

    /**
     * Create a song instance. CAUTION: every song has to have a UUID! This constructor is only necessary for
     * unmarshalling from XML.
     */
    public Song() {
        // default constructor
    }

    public Song(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public String getComposer() {
        return composer;
    }

    public String getAuthorText() {
        return authorText;
    }

    public String getAuthorTranslation() {
        return authorTranslation;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getAdditionalCopyrightNotes() {
        return additionalCopyrightNotes;
    }

    public String getLanguage() {
        return language;
    }

    public String getSongNotes() {
        return songNotes;
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getTonality() {
        return tonality;
    }

    public String getChordSequence() {
        return chordSequence;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public void setAuthorText(String authorText) {
        this.authorText = authorText;
    }

    public void setAuthorTranslation(String authorTranslation) {
        this.authorTranslation = authorTranslation;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setAdditionalCopyrightNotes(String additionalCopyrightNotes) {
        this.additionalCopyrightNotes = additionalCopyrightNotes;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setSongNotes(String songNotes) {
        this.songNotes = songNotes;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public void setTonality(String tonality) {
        this.tonality = tonality;
    }

    public void setChordSequence(String chordSequence) {
        this.chordSequence = chordSequence;
    }

    public String getUUID() {
        return uuid;
    }

    @Override
    public int compareTo(Song o) {
        int ret = 0;

        ret = compareLocaleBasedWithNullFirst(getTitle(), o.getTitle());
        if (ret != 0) {
            return ret;
        }

        ret = compareLocaleBasedWithNullFirst(getLyrics(), o.getLyrics());
        if (ret != 0) {
            return ret;
        }

        ret = compareLocaleBasedWithNullFirst(getChordSequence(), o.getChordSequence());
        if (ret != 0) {
            return ret;
        }

        return ret;
    }

    private static int compareLocaleBasedWithNullFirst(String one, String two) {
        if (one == null && two != null) {
            return -1;
        } else if (one != null && two == null) {
            return 1;
        } else if (one == null && two == null) {
            return 0;
        } else if (one != null && two != null) {
            Collator collator = Collator.getInstance();
            return collator.compare(one, two);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return "SONG[" + title + "|" + uuid + "]";
    }

}

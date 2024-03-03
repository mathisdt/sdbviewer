package org.zephyrsoft.sdbviewer.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.io.Serializable;
import java.text.Collator;

public class Song implements Serializable, Comparable<Song>, Parcelable {

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
    private String image;
    private String imageRotation;

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

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

    private Song(Parcel in) {
        title = in.readString();
        composer = in.readString();
        authorText = in.readString();
        authorTranslation = in.readString();
        publisher = in.readString();
        additionalCopyrightNotes = in.readString();
        language = in.readString();
        songNotes = in.readString();
        tonality = in.readString();
        uuid = in.readString();
        chordSequence = in.readString();
        lyrics = in.readString();
        image = in.readString();
        imageRotation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(composer);
        dest.writeString(authorText);
        dest.writeString(authorTranslation);
        dest.writeString(publisher);
        dest.writeString(additionalCopyrightNotes);
        dest.writeString(language);
        dest.writeString(songNotes);
        dest.writeString(tonality);
        dest.writeString(uuid);
        dest.writeString(chordSequence);
        dest.writeString(lyrics);
        dest.writeString(image);
        dest.writeString(imageRotation);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getImage() {
        return image;
    }

    public String getImageRotation() {
        return imageRotation;
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

    public void setImage(String image) {
        this.image = image;
    }

    public void setImageRotation(String imageRotation) {
        this.imageRotation = imageRotation;
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
    public int compareTo(@NonNull Song o) {
        int ret = compareLocaleBasedWithNullFirst(getTitle(), o.getTitle());
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
        } else if (one == null) { // two is always null here
            return 0;
        } else { // one and two are always null here
            Collator collator = Collator.getInstance();
            return collator.compare(one, two);
        }
    }

    @Override
    public String toString() {
        return "SONG[" + title + "|" + uuid + "]";
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public boolean isEmpty() {
        return isEmpty(getTitle())
            && isEmpty(getComposer())
            && isEmpty(getAuthorText())
            && isEmpty(getAuthorTranslation())
            && isEmpty(getPublisher())
            && isEmpty(getAdditionalCopyrightNotes())
            && isEmpty(getLanguage())
            && isEmpty(getSongNotes())
            && isEmpty(getLyrics())
            && isEmpty(getTonality())
            && isEmpty(getChordSequence())
            && isEmpty(getImage())
            && isEmpty(getImageRotation());
    }
}

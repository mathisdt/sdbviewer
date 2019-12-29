package org.zephyrsoft.sdbviewer.fetch;

import org.zephyrsoft.sdbviewer.model.Song;

import java.util.List;

public class FetchException extends RuntimeException {
    /** old data - but as we couldn't fetch the updated songs, we still can use these */
    private List<Song> retainedSongs;

    FetchException(String message, Throwable cause, List<Song> retainedSongs) {
        super(message, cause);
        this.retainedSongs = retainedSongs;
    }

    public boolean hasRetainedSongs() {
        return retainedSongs != null && !retainedSongs.isEmpty();
    }

    public List<Song> getRetainedSongs() {
        return retainedSongs;
    }
}

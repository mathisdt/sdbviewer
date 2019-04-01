package org.zephyrsoft.sdbviewer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import org.zephyrsoft.sdbviewer.Constants;
import org.zephyrsoft.sdbviewer.model.Song;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DatabaseAccess {

    private static final String COL_TITLE = "TITLE";
    private static final String COL_COMPOSER = "COMPOSER";
    private static final String COL_AUTHOR_TEXT = "AUTHOR_TEXT";
    private static final String COL_AUTHOR_TRANSLATION = "AUTHOR_TRANSLATION";
    private static final String COL_PUBLISHER = "PUBLISHER";
    private static final String COL_ADDITIONAL_COPYRIGHT_NOTES = "ADDITIONAL_COPYRIGHT_NOTES";
    private static final String COL_LANGUAGE = "LANGUAGE";
    private static final String COL_SONG_NOTES = "SONG_NOTES";
    private static final String COL_TONALITY = "TONALITY";
    private static final String COL_UUID = "UUID";
    private static final String COL_CHORD_SEQUENCE = "CHORD_SEQUENCE";
    private static final String COL_LYRICS = "LYRICS";

    private static final String DATABASE = "SDBVIEWER";
    private static final String TABLE = "SONGS";
    private static final int DATABASE_VERSION = 1;

    private final DatabaseOpenHelper databaseOpenHelper;

    public DatabaseAccess(Context context) {
        databaseOpenHelper = new DatabaseOpenHelper(context);
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private SQLiteDatabase database;

        private static final String FTS_TABLE_CREATE =
            "CREATE VIRTUAL TABLE " + TABLE +
                " USING fts4 (" +
                COL_TITLE + ", " +
                COL_COMPOSER + ", " +
                COL_AUTHOR_TEXT + ", " +
                COL_AUTHOR_TRANSLATION + ", " +
                COL_PUBLISHER + ", " +
                COL_ADDITIONAL_COPYRIGHT_NOTES + ", " +
                COL_LANGUAGE + ", " +
                COL_SONG_NOTES + ", " +
                COL_TONALITY + ", " +
                COL_UUID + ", " +
                COL_CHORD_SEQUENCE + ", " +
                COL_LYRICS + ")";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE, null, DATABASE_VERSION);

            database = getWritableDatabase();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(FTS_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(Constants.LOG_TAG, "upgrading database from version " + oldVersion + " to "
                + newVersion + ", which destroys all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
        }

        void resetContents(Collection<Song> songs) {
            database.beginTransaction();
            try {
                deleteAll();

                for (Song song : songs) {
                    insert(song);
                }

                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }

        private void deleteAll() {
            database.delete(TABLE, null, null);
        }

        private void insert(Song song) {
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, song.getTitle());
            values.put(COL_COMPOSER, song.getComposer());
            values.put(COL_AUTHOR_TEXT, song.getAuthorText());
            values.put(COL_AUTHOR_TRANSLATION, song.getAuthorTranslation());
            values.put(COL_PUBLISHER, song.getPublisher());
            values.put(COL_ADDITIONAL_COPYRIGHT_NOTES, song.getAdditionalCopyrightNotes());
            values.put(COL_LANGUAGE, song.getLanguage());
            values.put(COL_SONG_NOTES, song.getSongNotes());
            values.put(COL_TONALITY, song.getTonality());
            values.put(COL_UUID, song.getUUID());
            values.put(COL_CHORD_SEQUENCE, song.getChordSequence());
            values.put(COL_LYRICS, song.getLyrics());

            database.insert(TABLE, null, values);
        }

        List<Song> selectFiltered(String filter) {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(TABLE);

            Cursor cursor;

            if (filter == null || filter.trim().length() == 0) {
                cursor = builder.query(getReadableDatabase(), null, null,
                    null, null, null, COL_TITLE + "," + COL_UUID);
            } else {
                cursor = builder.query(getReadableDatabase(), null, TABLE + " MATCH ?",
                    new String[] {"*" + filter + "*"}, null, null, COL_TITLE + "," + COL_UUID);
            }

            return map(cursor);
        }

        private List<Song> map(Cursor cursor) {
            List<Song> result = new LinkedList<>();
            try {
                if (cursor == null) {
                    return result;
                }

                while (cursor.moveToNext())  {
                    result.add(mapSingle(cursor));
                }

                return result;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private Song mapSingle(Cursor cursor) {
            Song song = new Song(cursor.getString(cursor.getColumnIndex(COL_UUID)));

            song.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
            song.setComposer(cursor.getString(cursor.getColumnIndex(COL_COMPOSER)));
            song.setAuthorText(cursor.getString(cursor.getColumnIndex(COL_AUTHOR_TEXT)));
            song.setAuthorTranslation(cursor.getString(cursor.getColumnIndex(COL_AUTHOR_TRANSLATION)));
            song.setPublisher(cursor.getString(cursor.getColumnIndex(COL_PUBLISHER)));
            song.setAdditionalCopyrightNotes(cursor.getString(cursor.getColumnIndex(COL_ADDITIONAL_COPYRIGHT_NOTES)));
            song.setLanguage(cursor.getString(cursor.getColumnIndex(COL_LANGUAGE)));
            song.setSongNotes(cursor.getString(cursor.getColumnIndex(COL_SONG_NOTES)));
            song.setTonality(cursor.getString(cursor.getColumnIndex(COL_TONALITY)));
            song.setChordSequence(cursor.getString(cursor.getColumnIndex(COL_CHORD_SEQUENCE)));
            song.setLyrics(cursor.getString(cursor.getColumnIndex(COL_LYRICS)));

            return song;
        }
    }

    /**
     * Replace all songs locally with the supplied songs.
     */
    public void resetContents(Collection<Song> songs) {
        databaseOpenHelper.resetContents(songs);
    }

    /**
     * Fetch all locally available songs matching the given filter.
     */
    public List<Song> selectFiltered(String filter) {
        return databaseOpenHelper.selectFiltered(filter);
    }
}

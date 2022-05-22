package org.zephyrsoft.sdbviewer;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Constants {
    public static final String LOG_TAG = "sdbviewer";

    public static final String FILE_LAST_UPDATED = "last_updated.txt";

    static final String ARG_SONG = "song";

    public enum AppPreference {
        SONGS_URL((s, c) -> c.getString(R.string.pref_songs_url).equals(s),
            s -> s != null && !s.isEmpty(),
            (s, c) -> PreferenceManager.getDefaultSharedPreferences(c).edit()
                .putString(c.getString(R.string.pref_songs_url), s).commit()),
        SONGS_RELOAD_INTERVAL((s, c) -> c.getString(R.string.pref_songs_reload_interval).equals(s), s -> {
            try {
                Integer.valueOf(s);
                return true;
            } catch (Exception e) {
                return false;
            }
        }, (s, c) -> PreferenceManager.getDefaultSharedPreferences(c).edit()
            .putString(c.getString(R.string.pref_songs_reload_interval), s).commit()),
        SHOW_TRANSLATION((s, c) -> c.getString(R.string.pref_show_translation).equals(s),
            s -> true,
            (s, c) -> PreferenceManager.getDefaultSharedPreferences(c).edit()
                .putBoolean(c.getString(R.string.pref_show_translation), Boolean.parseBoolean(s)).commit()),
        SHOW_CHORDS((s, c) -> c.getString(R.string.pref_show_chords).equals(s),
            s -> true,
            (s, c) -> PreferenceManager.getDefaultSharedPreferences(c).edit()
                .putBoolean(c.getString(R.string.pref_show_chords), Boolean.parseBoolean(s)).commit());

        private final BiPredicate<String, Context> checkName;
        private final Predicate<String> checkValue;
        private final BiConsumer<String, Context> apply;

        AppPreference(BiPredicate<String, Context> checkName, Predicate<String> checkValue, BiConsumer<String, Context> apply) {
            this.checkName = checkName;
            this.checkValue = checkValue;
            this.apply = apply;
        }

        public boolean checkPossibleValue(String str) {
            return checkValue.test(str);
        }

        public void applyValue(String str, Context context) {
            apply.accept(str, context);
        }

        public static AppPreference get(String name, Context context) {
            for (AppPreference pref : AppPreference.values()) {
                if (pref.checkName.test(name, context)) {
                    return pref;
                }
            }
            return null;
        }
    }
}

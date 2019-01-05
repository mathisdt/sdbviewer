package org.zephyrsoft.sdbviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.zephyrsoft.sdbviewer.fetch.SDBFetcher;
import org.zephyrsoft.sdbviewer.model.Song;
import org.zephyrsoft.sdbviewer.parser.SongParser;
import org.zephyrsoft.sdbviewer.registry.Registry;
import org.zephyrsoft.sdbviewer.util.Consumer;

import java.util.List;

/**
 * An activity representing a list of Songs. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SongDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class SongListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;

    /**
     * The topmost item's index in the song list.
     * Used to keep the scroll position after navigation or data reloads.
     */
    private int firstVisiblePosition = 0;

    private SDBFetcher fetcher;

    @Override
    protected void onPause() {
        RecyclerView recyclerView = findViewById(R.id.song_list);
        assert recyclerView != null;

        firstVisiblePosition =
            ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        Log.d(Constants.LOG_TAG, "saved first visible position " + firstVisiblePosition);

        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetcher = Registry.get(SDBFetcher.class);
        setContentView(R.layout.activity_song_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.song_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        RecyclerView recyclerView = findViewById(R.id.song_list);
        assert recyclerView != null;

        loadAndShow(recyclerView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                fetcher.invalidateSavedSongs(getApplicationContext());
                loadAndShow(findViewById(R.id.song_list));
                return true;

            case R.id.action_settings:
                Context context = getApplicationContext();
                Intent intent = new Intent(context, SettingsActivity.class);
                context.startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void loadAndShow(final @NonNull RecyclerView recyclerView) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = sharedPreferences.getString(getApplicationContext().getString(R.string.pref_songs_url), getString(R.string.pref_songs_url_default));

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        final String urlToUse = url;
        Consumer<FetchSongsResult> onDone = result -> {
            if (result.hasException()) {
                Log.w(Constants.LOG_TAG, "could not load songs", result.getException());
                Toast.makeText(getApplicationContext(), "Could not load songs. Is the URL \"" + urlToUse + "\" correct? If not, please go to Settings and edit it.", Toast.LENGTH_LONG).show();
            } else {
                recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(SongListActivity.this, result.getSongs(), mTwoPane));

                recyclerView.getLayoutManager().scrollToPosition(firstVisiblePosition);
                Log.d(Constants.LOG_TAG, "restored first visible position " + firstVisiblePosition);
            }
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        };
        Runnable onAbort = () -> findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

        try {
            new FetchSongsTask(onDone, onAbort, urlToUse).execute();
        } catch (Exception e) {
            onDone.accept(new FetchSongsResult(e, urlToUse));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchSongsTask extends AsyncTask<Void, Integer, FetchSongsResult> {
        private Consumer<FetchSongsResult> onDone;
        private Runnable onAbort;
        private String url;

        FetchSongsTask(Consumer<FetchSongsResult> onDone, Runnable onAbort, String url) {
            this.onDone = onDone;
            this.onAbort = onAbort;
            this.url = url;
        }

        protected FetchSongsResult doInBackground(Void... nothing) {
            try {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try {
                        List<Song> songs = fetcher.fetchSongs(getApplicationContext(), "http://" + url);
                        return new FetchSongsResult(songs);
                    } catch (Exception ex) {
                        Log.w(Constants.LOG_TAG, "unsuccessfully tried URL \"" + url + "\" with http", ex);
                    }
                    try {
                        List<Song> songs = fetcher.fetchSongs(getApplicationContext(), "https://" + url);
                        return new FetchSongsResult(songs);
                    } catch (Exception ex) {
                        Log.w(Constants.LOG_TAG, "unsuccessfully tried URL \"" + url + "\" with https", ex);
                    }
                }
                List<Song> songs = fetcher.fetchSongs(getApplicationContext(), "http://" + url);
                return new FetchSongsResult(songs);
            } catch (Exception e) {
                return(new FetchSongsResult(e, url));
            }
        }

        @Override
        protected void onCancelled(FetchSongsResult songs) {
            onAbort.run();
        }

        protected void onPostExecute(FetchSongsResult result) {
            onDone.accept(result);
        }
    }

    private static class FetchSongsResult {
        private List<Song> songs;
        private Exception exception;
        private String usedUrl;

        FetchSongsResult(List<Song> songs) {
            this.songs = songs;
        }

        FetchSongsResult(Exception exception, String usedUrl) {
            this.exception = exception;
            this.usedUrl = usedUrl;
        }

        boolean hasException() {
            return exception != null;
        }

        List<Song> getSongs() {
            return songs;
        }

        Exception getException() {
            return exception;
        }

        public String getUsedUrl() {
            return usedUrl;
        }

        public void setUsedUrl(String usedUrl) {
            this.usedUrl = usedUrl;
        }
    }

    public static class SimpleItemRecyclerViewAdapter
        extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final SongListActivity mParentActivity;
        private final List<Song> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Song item = (Song) view.getTag();

                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(Constants.ARG_SONG, item);
                    SongDetailFragment fragment = new SongDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.song_detail_container, fragment)
                        .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, SongDetailActivity.class);
                    intent.putExtra(Constants.ARG_SONG, (Parcelable) item);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(SongListActivity parent,
                                      List<Song> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            Song song = mValues.get(position);
            holder.mIdView.setText(song.getTitle());
            holder.mContentView.setText(SongParser.getFirstLyricsLine(song));

            holder.itemView.setTag(song);
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = view.findViewById(R.id.id_text);
                mContentView = view.findViewById(R.id.content);
            }
        }
    }
}

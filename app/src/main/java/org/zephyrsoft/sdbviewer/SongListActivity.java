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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.zephyrsoft.sdbviewer.fetch.SDBFetcher;
import org.zephyrsoft.sdbviewer.model.Song;
import org.zephyrsoft.sdbviewer.parser.SongParser;
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

    private SDBFetcher fetcher;

    private String filter;

    @Override
    protected void onPause() {
        saveFirstVisiblePosition();

        super.onPause();
    }

    private void saveFirstVisiblePosition() {
        RecyclerView recyclerView = findViewById(R.id.song_list);
        assert recyclerView != null;

        int firstVisiblePosition =
            ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        ((SDBViewerApplication) getApplication()).setFirstVisiblePosition(firstVisiblePosition);
        Log.d(Constants.LOG_TAG, "saved first visible position " + firstVisiblePosition);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetcher = ((SDBViewerApplication) getApplication()).getSdbFetcher();
        setContentView(R.layout.activity_song_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_icon);

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

        loadAndShow(recyclerView, null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setOnCloseListener(() -> {
            ((SDBViewerApplication) getApplication()).setFirstVisiblePosition(0);
            return handleSearchText(null);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return handleSearchText(query);
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return handleSearchText(newText);
            }
        });

        return true;
    }

    private boolean handleSearchText(String searchText) {
        filter = searchText == null ? null : searchText.toLowerCase().replaceAll("[\\p{Space}\\p{Punct}]++", " ");
        Log.i(Constants.LOG_TAG, "search text entered: " + searchText + " / filter text used: " + filter);
        applyFilter();
        return filter != null;
    }

    private void applyFilter() {
        RecyclerView recyclerView = findViewById(R.id.song_list);
        ((SimpleItemRecyclerViewAdapter)recyclerView.getAdapter()).filter(filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                saveFirstVisiblePosition();
                fetcher.invalidateSavedSongs(this);
                loadAndShow(findViewById(R.id.song_list), null);
                return true;

            case R.id.action_settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
                return true;

            case R.id.action_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private String getUrl() {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(getString(R.string.pref_songs_url), getString(R.string.pref_songs_url_default));
    }

    private void loadAndShow(final @NonNull RecyclerView recyclerView, String filter) {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        final String urlToUse = getUrl();
        Consumer<FetchSongsResult> onDone = result -> {
            try {
                if (result.hasException()) {
                    Log.w(Constants.LOG_TAG, "could not load songs: " + result.getException().getMessage(), result.getException());
                    Toast.makeText(this, "Could not load songs. Is the URL \"" + urlToUse + "\" correct? If not, please go to Settings and edit it.", Toast.LENGTH_LONG).show();
                } else {
                    recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(SongListActivity.this, fetcher, result.getSongs(), mTwoPane));
                    applyFilter();

                    int firstVisiblePosition = ((SDBViewerApplication) getApplication()).getFirstVisiblePosition();
                    recyclerView.getLayoutManager().scrollToPosition(firstVisiblePosition);
                    Log.d(Constants.LOG_TAG, "restored first visible position " + firstVisiblePosition);
                }
            } finally {
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }
        };
        Runnable onAbort = () -> findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

        try {
            new FetchSongsTask(onDone, onAbort, urlToUse, filter, recyclerView.getContext()).execute();
        } catch (Exception e) {
            onDone.accept(new FetchSongsResult(e));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchSongsTask extends AsyncTask<Void, Integer, FetchSongsResult> {
        private Consumer<FetchSongsResult> onDone;
        private Runnable onAbort;
        private String url;
        private String filter;
        private Context context;

        FetchSongsTask(Consumer<FetchSongsResult> onDone, Runnable onAbort, String url, String filter, Context context) {
            this.onDone = onDone;
            this.onAbort = onAbort;
            this.url = url;
            this.filter = filter;
            this.context = context;
        }

        protected FetchSongsResult doInBackground(Void... nothing) {
            try {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try {
                        List<Song> songs = fetcher.fetchSongs(context, "http://" + url, filter);
                        return new FetchSongsResult(songs);
                    } catch (Exception ex) {
                        Log.w(Constants.LOG_TAG, "unsuccessfully tried URL \"" + url + "\" with http: " + ex.getMessage(), ex);
                    }
                    try {
                        List<Song> songs = fetcher.fetchSongs(context, "https://" + url, filter);
                        return new FetchSongsResult(songs);
                    } catch (Exception ex) {
                        Log.w(Constants.LOG_TAG, "unsuccessfully tried URL \"" + url + "\" with https: " + ex.getMessage(), ex);
                    }
                }
                List<Song> songs = fetcher.fetchSongs(context, url, filter);
                return new FetchSongsResult(songs);
            } catch (Exception e) {
                return(new FetchSongsResult(e));
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

        FetchSongsResult(List<Song> songs) {
            this.songs = songs;
        }

        FetchSongsResult(Exception exception) {
            this.exception = exception;
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
    }

    private class SimpleItemRecyclerViewAdapter
        extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final SongListActivity mParentActivity;
        private final SDBFetcher fetcher;
        private List<Song> mValuesFiltered;
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
                                      SDBFetcher fetcher,
                                      List<Song> items,
                                      boolean twoPane) {
            mValuesFiltered = items;
            mParentActivity = parent;
            this.fetcher = fetcher;
            mTwoPane = twoPane;
        }

        void filter(String filterText) {
            mValuesFiltered = fetcher.fetchSongs(mParentActivity, getUrl(), filterText);
            notifyDataSetChanged();
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
            Song song = mValuesFiltered.get(position);
            holder.mIdView.setText(song.getTitle());
            holder.mContentView.setText(SongParser.getFirstLyricsLine(song));

            holder.itemView.setTag(song);
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValuesFiltered.size();
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

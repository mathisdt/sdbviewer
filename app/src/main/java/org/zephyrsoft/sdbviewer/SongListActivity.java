package org.zephyrsoft.sdbviewer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.zephyrsoft.sdbviewer.fetch.FetchException;
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
        if (recyclerView.getAdapter() != null) {
            ((SimpleItemRecyclerViewAdapter) recyclerView.getAdapter()).filter(filter);
        }
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

            case R.id.action_import_settings:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                    Intent intentImportSettings = new Intent(this, QRScannerActivity.class);
                    startActivity(intentImportSettings);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    AlertDialog.Builder dialog  = new AlertDialog.Builder(this);
                    dialog.setMessage(R.string.camera_explanation);
                    dialog.setPositiveButton(R.string.ok,
                        (d, b) -> qrCodeScannerLauncher.launch(Manifest.permission.CAMERA));
                    dialog.setNegativeButton(R.string.cancel, null);
                    dialog.setCancelable(true);
                    dialog.create().show();
                } else {
                    qrCodeScannerLauncher.launch(Manifest.permission.CAMERA);
                }
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

    private ActivityResultLauncher<String> qrCodeScannerLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Intent intentImportSettings = new Intent(this, QRScannerActivity.class);
                startActivity(intentImportSettings);
            } else {
                AlertDialog.Builder dialog  = new AlertDialog.Builder(this);
                dialog.setMessage(R.string.camera_rejected);
                dialog.setPositiveButton(R.string.ok, null);
                dialog.setCancelable(true);
                dialog.create().show();
            }
        });


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
                if (result.hasExceptionAndNoSongs()) {
                    Log.w(Constants.LOG_TAG, "could not load songs: " + result.getException().getMessage(), result.getException());
                    Toast.makeText(this, "Could not load songs. Is the URL \"" + urlToUse + "\" correct?", Toast.LENGTH_LONG).show();
                } else if (result.hasExceptionButSongs()) {
                    Log.w(Constants.LOG_TAG, "could not load songs (but using old data): " + result.getException().getMessage(), result.getException());
                    Toast.makeText(this, "Could not load songs - using old data for now. Is the URL \"" + urlToUse + "\" correct?", Toast.LENGTH_LONG).show();
                    showSongs(recyclerView, result.getSongs());
                } else {
                    showSongs(recyclerView, result.getSongs());
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

    private void showSongs(@NonNull RecyclerView recyclerView, List<Song> songs) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(SongListActivity.this, recyclerView, fetcher, songs, mTwoPane));
        applyFilter();

        int firstVisiblePosition = ((SDBViewerApplication) getApplication()).getFirstVisiblePosition();
        recyclerView.getLayoutManager().scrollToPosition(firstVisiblePosition);
        Log.d(Constants.LOG_TAG, "restored first visible position " + firstVisiblePosition);

        // select first element by default if in two-pane mode
        if (mTwoPane) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    RecyclerView.ViewHolder firstElement = recyclerView.findViewHolderForAdapterPosition(firstVisiblePosition);
                    if (firstElement != null) {
                        firstElement.itemView.performClick();
                    }
                }
            }, 1);
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
            } catch (FetchException e) {
                return new FetchSongsResult(e.getRetainedSongs(), e);
            } catch (Exception e) {
                return new FetchSongsResult(e);
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

        public FetchSongsResult(List<Song> songs, Exception exception) {
            this.songs = songs;
            this.exception = exception;
        }

        FetchSongsResult(Exception exception) {
            this.exception = exception;
        }

        boolean hasExceptionAndNoSongs() {
            return exception != null && (songs == null || songs.isEmpty());
        }

        boolean hasExceptionButSongs() {
            return exception != null && songs != null && !songs.isEmpty();
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
        private final RecyclerView recyclerView;
        private final SDBFetcher fetcher;
        private List<Song> mValuesFiltered;
        private final boolean mTwoPane;
        private int selectedIndex = -1;

        SimpleItemRecyclerViewAdapter(SongListActivity parent,
                                      final @NonNull RecyclerView recyclerView,
                                      SDBFetcher fetcher,
                                      List<Song> items,
                                      boolean twoPane) {
            mValuesFiltered = items;
            mParentActivity = parent;
            this.recyclerView = recyclerView;
            this.fetcher = fetcher;
            mTwoPane = twoPane;
        }

        void filter(String filterText) {
            try {
                mValuesFiltered = fetcher.fetchSongs(mParentActivity, getUrl(), filterText);
            } catch (FetchException e) {
                if (e.hasRetainedSongs()) {
                    mValuesFiltered = e.getRetainedSongs();
                }
            }
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
        public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            Song song = mValuesFiltered.get(position);
            holder.mIdView.setText(song.getTitle());
            holder.mContentView.setText(SongParser.getFirstLyricsLine(song));

            holder.itemView.setTag(song);
            holder.itemView.setOnClickListener(view -> {
                Song item = (Song) view.getTag();

                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(Constants.ARG_SONG, item);
                    SongDetailFragment fragment = new SongDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.song_detail_container, fragment)
                        .commit();

                    selectedIndex = position;
                    view.setSelected(true);
                    notifyDataSetChanged();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, SongDetailActivity.class);
                    intent.putExtra(Constants.ARG_SONG, (Parcelable) item);

                    context.startActivity(intent);
                }
            });

            // manage selection state on view recycling
            holder.itemView.setSelected(position == selectedIndex);
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

            public int getItemPosition() {
                return getAdapterPosition();
            }
        }
    }
}

package org.zephyrsoft.sdbviewer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.zephyrsoft.sdbviewer.model.Song;
import org.zephyrsoft.sdbviewer.parser.SongParser;

/**
 * A fragment representing a single Song detail screen.
 * This fragment is either contained in a {@link SongListActivity}
 * in two-pane mode (on tablets) or a {@link SongDetailActivity}
 * on handsets.
 */
public class SongDetailFragment extends Fragment {

    /**
     * The song this fragment is presenting.
     */
    private Song song;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(Constants.ARG_SONG)) {
            song = getArguments().getParcelable(Constants.ARG_SONG);

            Activity activity = this.getActivity();
            if (activity == null) {
                throw new IllegalStateException("activity not found");
            }
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(song.getTitle());
            }
        }
    }

    /** TODO see {@link SongParser#parseLyrics} and {@link SongParser#parseCopyright} */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.song_detail, container, false);

        // Show the content as text in a TextView.
        if (song != null) {
            // TODO parse song => display lyrics / translation / chords as selected in preferences!
            ((TextView) rootView.findViewById(R.id.song_detail)).setText(song.getLyrics());
        }

        return rootView;
    }
}

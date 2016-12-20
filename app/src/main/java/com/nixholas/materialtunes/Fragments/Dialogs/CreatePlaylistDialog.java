package com.nixholas.materialtunes.Fragments.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nixholas.materialtunes.Media.Entities.Playlist;
import com.nixholas.materialtunes.Media.Entities.Song;

import java.util.List;

/**
 * Created by nixholas on 20/12/16.
 */

public class CreatePlaylistDialog extends DialogFragment {

    public static CreatePlaylistDialog newInstance(Song song) {
        long[] songs = new long[1];
        songs[0] = song.getId();
        return newInstance(songs);
    }

    public static CreatePlaylistDialog newInstance(long[] songList) {
        CreatePlaylistDialog dialog = new CreatePlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putLongArray("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//        final List<Playlist> playlists;// = PlaylistLoader.getPlaylists(getActivity(), false);
//        CharSequence[] chars = new CharSequence[playlists.size() + 1];
//        chars[0] = "Create new playlist";
//
//        for (int i = 0; i < playlists.size(); i++) {
//            //chars[i + 1] = playlists.get(i).name;
//        }
//        return new MaterialDialog.Builder(getActivity()).title("Add to playlist").items(chars).itemsCallback(new MaterialDialog.ListCallback() {
//            @Override
//            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
//                long[] songs = getArguments().getLongArray("songs");
//                if (which == 0) {
//                    CreatePlaylistDialog.newInstance(songs).show(getActivity().getFragmentManager(), "CREATE_PLAYLIST");
//                    return;
//                }
//
//                //MusicPlayer.addToPlaylist(getActivity(), songs, playlists.get(which - 1).id);
//                dialog.dismiss();
//
//            }
//        }).build();
//    }
}
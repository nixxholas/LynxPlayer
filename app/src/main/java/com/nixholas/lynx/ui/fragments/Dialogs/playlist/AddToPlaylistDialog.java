package com.nixholas.lynx.ui.fragments.Dialogs.playlist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nixholas.lynx.media.entities.Playlist;

import java.util.ArrayList;

import static com.nixholas.lynx.ui.activities.MainActivity.mediaManager;

/**
 * Created by nixholas on 20/12/16.
 */

public class AddToPlaylistDialog extends DialogFragment {
    ArrayList<String> playlistItems = new ArrayList<>();

    public AddToPlaylistDialog() {
        for(Playlist playlist : mediaManager.getPlayLists()) {
            playlistItems.add(playlist.getPlaylistName());
        }
        playlistItems.add("Create a new playlist");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title("Add to playlist")
                .items(playlistItems)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == playlistItems.size() - 1) {
                            //Log.d("AddToPlaylistDialog", "onSelection last item");
                            CreatePlaylistDialog createPlaylistDialog = new CreatePlaylistDialog();
                            createPlaylistDialog.show(getFragmentManager(), "CreatePlaylistDialogFragment");
                            dialog.dismiss();
                        }
                    }
                })
                .show();
    }
}

package com.nixholas.materialtunes.Fragments.Dialogs.Playlist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nixholas.materialtunes.Media.Entities.Playlist;
import com.nixholas.materialtunes.Media.Entities.Song;

import java.util.List;

import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.Media.PlaylistUtil.addNewPlaylist;

/**
 * Created by nixholas on 20/12/16.
 */

public class CreatePlaylistDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // http://stackoverflow.com/questions/30951509/material-dialogs-with-edit-text-and-multi-choice
        final String[] str = {""};

        return new MaterialDialog.Builder(getActivity())
                .title("Create a new playlist")
                .content("Playlist name")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText("Cancel")
                .positiveText("Done")
                .input("Name", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        Log.d("CreatePlaylistDialog", "onInput");
                        str[0] = input.toString();

                        addNewPlaylist(getActivity().getApplicationContext(), str[0]);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build();
    }
}
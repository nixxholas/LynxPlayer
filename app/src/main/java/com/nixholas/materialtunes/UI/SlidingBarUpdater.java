package com.nixholas.materialtunes.UI;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;

import static com.nixholas.materialtunes.MainActivity.mediaControls_PlayPause;
import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.MainActivity.slideAlbumArt;
import static com.nixholas.materialtunes.MainActivity.slideButton;
import static com.nixholas.materialtunes.MainActivity.slideSongArtist;
import static com.nixholas.materialtunes.MainActivity.slideSongTitle;
import static com.nixholas.materialtunes.MainActivity.slidedAlbumArt;
import static com.nixholas.materialtunes.MainActivity.slidedLinearLayout;

/**
 * Created by nixho on 22-Nov-16.
 */

public class SlidingBarUpdater {
    public static void updateSlideBar(Context context) {
        Song currentSong = mediaManager.getCurrent();

        // Update the images first
        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

        //Log.e("Album Art URI", albumArtUri.toString());

        // Collapsed View Layout
        // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
        Glide.with(context)
                .load(albumArtUri)
                .asBitmap()
                .placeholder(R.drawable.untitled_album)
                .into(slideAlbumArt);

        Glide.with(context)
                .load(albumArtUri)
                .asBitmap()
                .placeholder(R.drawable.untitled_album)
                .listener(new RequestListener<Uri, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        // Retrieve the palette of colors from the Bitmap first
                        Palette p = Palette.from(resource).generate();
                        Palette.Swatch swatch = p.getVibrantSwatch();
                        if (swatch != null) {
                            int color = swatch.getRgb();
                            slidedLinearLayout.setBackgroundColor(color);
                        } else {
                            Palette.Swatch mutedSwatch = p.getMutedSwatch();
                            if (mutedSwatch != null) {
                                int color = mutedSwatch.getRgb();
                                slidedLinearLayout.setBackgroundColor(color);
                            }
                        }

                        // Set the images
                        slideAlbumArt.setImageURI(model);
                        slidedAlbumArt.setImageURI(model);

                        return false;
                    }
                })
                .into(slidedAlbumArt);

        slideSongTitle.setText(currentSong.getTitle());
        slideSongArtist.setText(currentSong.getArtistName());

        if (mediaManager.mMediaPlayer.isPlaying()) {
            slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
            mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
        } else {
            slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
        }
    }
}

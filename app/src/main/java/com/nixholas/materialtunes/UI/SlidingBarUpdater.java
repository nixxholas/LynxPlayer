package com.nixholas.materialtunes.UI;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.nixholas.materialtunes.MainActivity.mediaControls_PlayPause;
import static com.nixholas.materialtunes.MainActivity.mediaControls_Shuffle;
import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.MainActivity.mediaSeekText_Maximum;
import static com.nixholas.materialtunes.MainActivity.mediaSeekText_Progress;
import static com.nixholas.materialtunes.MainActivity.preferenceHelper;
import static com.nixholas.materialtunes.MainActivity.slideAlbumArt;
import static com.nixholas.materialtunes.MainActivity.slideButton;
import static com.nixholas.materialtunes.MainActivity.slideSongArtist;
import static com.nixholas.materialtunes.MainActivity.slideSongTitle;
import static com.nixholas.materialtunes.MainActivity.slidedAlbumArt;
import static com.nixholas.materialtunes.MainActivity.slidedRelativeLayout;
import static com.nixholas.materialtunes.MainActivity.slidedSeekBar;
import static com.nixholas.materialtunes.MainActivity.slided_SongArtist;
import static com.nixholas.materialtunes.MainActivity.slided_SongTitle;
import static com.nixholas.materialtunes.MainActivity.slidingSeekBar;

/**
 * Created by nixho on 22-Nov-16.
 */

public class SlidingBarUpdater {
    public static void updateSlideBar(MainActivity mainActivity) {
        try {
            Song currentSong = mediaManager.getCurrent();

            if (currentSong != null) {
                // Update the images first
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

                //Log.d("Album Art URI", albumArtUri.toString());

                // Collapsed View Layout
                // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
                Glide.with(mainActivity.getApplicationContext())
                        .load(albumArtUri)
                        .asBitmap()
                        .placeholder(R.drawable.untitled_album)
                        .into(slideAlbumArt);

                Glide.with(mainActivity.getApplicationContext())
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
                                    slidedRelativeLayout.setBackgroundColor(color);
                                } else {
                                    Palette.Swatch mutedSwatch = p.getMutedSwatch();
                                    if (mutedSwatch != null) {
                                        int color = mutedSwatch.getRgb();
                                        slidedRelativeLayout.setBackgroundColor(color);
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
                slided_SongTitle.setText(currentSong.getTitle());
                slideSongArtist.setText(currentSong.getArtistName());
                slided_SongArtist.setText(currentSong.getArtistName());

                if (mediaManager.mMediaPlayer.isPlaying()) {
                    slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                    mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                } else {
                    slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
                }

                int songDuration = currentSong.getDuration();
                slidingSeekBar.setMax(songDuration);
                slidedSeekBar.setMax(songDuration);

                mediaSeekText_Progress.setText((String.format(Locale.ENGLISH, "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(0),
                        TimeUnit.MILLISECONDS.toSeconds(0) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(0)))));
                // Retrieve the length of the song and set it into the Maximum Text View
                //mediaSeekText_Maximum.setText(getCurrent().getDuration() + "");
                // http://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java
                mediaSeekText_Maximum.setText(String.format(Locale.ENGLISH, "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(songDuration),
                        TimeUnit.MILLISECONDS.toSeconds(songDuration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration))
                ));

                // Let's update the UI elements related to shuffling
                mediaControls_Shuffle.setEnabledUI(preferenceHelper.getShuffle());
            }
        } catch (Exception ex) {
           Log.d("updateSlideBar()", "An error occured");
            ex.printStackTrace();
        }
    }
}

package com.nixholas.lynx.ui;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nixholas.lynx.media.entities.Song;
import com.nixholas.lynx.R;

import static com.nixholas.lynx.ui.activities.MainActivity.getInstance;
import static com.nixholas.lynx.ui.activities.MainActivity.mediaControls_PlayPause;
import static com.nixholas.lynx.ui.activities.MainActivity.mediaManager;
import static com.nixholas.lynx.ui.activities.MainActivity.persistentNotif;
import static com.nixholas.lynx.ui.activities.MainActivity.slideAlbumArt;
import static com.nixholas.lynx.ui.activities.MainActivity.slideButton;
import static com.nixholas.lynx.ui.activities.MainActivity.slideSongArtist;
import static com.nixholas.lynx.ui.activities.MainActivity.slideSongTitle;
import static com.nixholas.lynx.ui.activities.MainActivity.slidedAlbumArt;
import static com.nixholas.lynx.ui.activities.MainActivity.slidedRelativeLayout;
import static com.nixholas.lynx.ui.activities.MainActivity.slided_SongArtist;
import static com.nixholas.lynx.ui.activities.MainActivity.slided_SongTitle;

/**
 * XML-Binded Media Controlling
 *
 * Created by nixho on 22-Nov-16.
 */

public class MediaControlUpdater {

    public static void mediaControlsOnClickPlayPause() {
        Log.d("mCOnClickPlayPause", "Running method");

        if (mediaManager.mLynxMediaPlayer != null) {
                // http://stackoverflow.com/questions/25381624/possible-to-detect-paused-state-of-mediaplayer
                if (mediaManager.mediaPlayerIsPaused) { // If the current song is paused,
                    mediaManager.mLynxMediaPlayer.start();
                    mediaManager.mediaPlayerIsPaused = false;
                    //http://stackoverflow.com/questions/7024881/replace-one-image-with-another-after-clicking-a-button
                    slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                    mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                } else { // Else we pause it
                    mediaManager.mLynxMediaPlayer.pause();
                    mediaManager.mediaPlayerIsPaused = true;
                    slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
                }

                persistentNotif.updateNotification();
        }
    }

    public static void mediaControlsOnClickPrevious(View v) {
        Log.d("mCOnClickPrevious", "Running method");

        try {
            if (mediaManager.mLynxMediaPlayer.getCurrentPosition() > 3000) {
                Log.d("onClickPrevious", "Current Position is > 3000");

                // Since it is playing inwards already, we'll restart it again.
                mediaManager.mLynxMediaPlayer.seekTo(0);
                return; // Exit the method
            }

            final Song prevSong = mediaManager.getPrevious();
            mediaManager.setCurrent(mediaManager.getSongDataset().indexOf(prevSong));

            Uri audioUri = Uri.parse("file://" + prevSong.getDataPath());

            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, prevSong.getAlbumId());

            if (mediaManager.mLynxMediaPlayer.isPlaying() || mediaManager.mediaPlayerIsPaused) {
                mediaManager.mLynxMediaPlayer.stop();
                mediaManager.mLynxMediaPlayer.reset();
                mediaManager.mLynxMediaPlayer.setDataSource(v.getContext(), audioUri);
                mediaManager.mLynxMediaPlayer.prepareAsync();

                /**
                 * User Interface Changes
                 */
                slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                slideSongTitle.setText(prevSong.getTitle());
                slided_SongTitle.setText(prevSong.getTitle());
                slideSongArtist.setText(prevSong.getArtistName());
                slided_SongArtist.setText(prevSong.getArtistName());
                // http://stackoverflow.com/questions/40452192/performing-album-art-checks-on-an-audio-file
                Glide.with(v.getContext())
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

                                return true;
                            }
                        })
                        .into(slideAlbumArt);

                Glide.with(v.getContext())
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

                                return true;
                            }
                        })
                        .into(slidedAlbumArt);

                persistentNotif.updateNotification();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mediaControlsOnClickNext(View v) {
        Log.d("mCOnClickNext", "Running method");

        try {
            //Log.d("onClickNext", "Working");
            //Log.d("mMediaPlayer.isPlaying", mediaManager.mMediaPlayer.isPlaying() + "");
            /**
             * If getInstance().getCurrentFocus() is used, there will be a high probability of
             * com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView{18335a0 VFED..... .F.....D 0,0-1440,2070 #7f0c00a9 app:id/main_RecyclerView}
             * which in other words mean that the UI might fail to load on the right view.
             *
             * Glide is still failing to load on the right context because of the wrong view...
             */
            //View v = getInstance().getCurrentFocus().getRootView();

            // Retrieve the next song
            final Song nextSong = mediaManager.getNext();

            // Set the next song as the current song
            mediaManager.setCurrent(mediaManager.getSongDataset().indexOf(nextSong));

            Uri audioUri = Uri.parse("file://" + nextSong.getDataPath());

            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, nextSong.getAlbumId());

                mediaManager.mLynxMediaPlayer.stop();
                mediaManager.mLynxMediaPlayer.reset();
                mediaManager.mLynxMediaPlayer.setDataSource(getInstance().getApplicationContext(), audioUri);
                mediaManager.mLynxMediaPlayer.prepareAsync();

                /**
                 * User Interface Changes
                 */
                slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                slideSongTitle.setText(nextSong.getTitle());
                slided_SongTitle.setText(nextSong.getTitle());
                slideSongArtist.setText(nextSong.getArtistName());
                slided_SongArtist.setText(nextSong.getArtistName());
                Glide.with(getInstance().getApplicationContext())
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

                                return true;
                            }
                        })
                        .into(slideAlbumArt);

                Glide.with(v.getContext())
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

                                return true;
                            }
                        })
                        .into(slidedAlbumArt);

                persistentNotif.updateNotification();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

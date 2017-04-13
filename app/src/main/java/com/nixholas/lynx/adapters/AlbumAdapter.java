package com.nixholas.lynx.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nixholas.lynx.media.entities.Song;
import com.nixholas.lynx.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.nixholas.lynx.ui.activities.MainActivity.getInstance;
import static com.nixholas.lynx.ui.activities.MainActivity.mediaControls_PlayPause;
import static com.nixholas.lynx.ui.activities.MainActivity.mediaManager;
import static com.nixholas.lynx.ui.activities.MainActivity.preferenceHelper;
import static com.nixholas.lynx.ui.activities.MainActivity.slideAlbumArt;
import static com.nixholas.lynx.ui.activities.MainActivity.slideButton;
import static com.nixholas.lynx.ui.activities.MainActivity.slideSongArtist;
import static com.nixholas.lynx.ui.activities.MainActivity.slideSongTitle;
import static com.nixholas.lynx.ui.activities.MainActivity.slidedAlbumArt;
import static com.nixholas.lynx.ui.activities.MainActivity.slidedRelativeLayout;
import static com.nixholas.lynx.utils.AlbumService.getAlbumArtUri;

/**
 * Created by nixholas on 23/12/16.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>  {
    private ArrayList<Song> mDataset;
    private Context context;
    int color, textColor;

    class ViewHolder extends RecyclerView.ViewHolder {
        public View v;
        TextView title, duration;
        RelativeLayout currentLayout;

        ViewHolder(View v) {
            super(v);
            this.currentLayout = (RelativeLayout) v.findViewById(R.id.albumsfragitem_layout);
            this.title = (TextView) v.findViewById(R.id.albumsfragitem_title);
            this.duration = (TextView) v.findViewById(R.id.albumsfragitem_duration);
        }
    }

    public AlbumAdapter (ArrayList<Song> mDataset, int color, int textColor) {
        this.mDataset = mDataset;
        this.color = color;
        this.textColor = textColor;
    }

    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.albumexpanded_item, parent, false);

        // Set the view's size, margins, paddings and layout parameters
        AlbumAdapter.ViewHolder vh = new AlbumAdapter.ViewHolder(v);

        // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
        context = parent.getContext();

        return vh;
    }

    @Override
    public void onBindViewHolder(AlbumAdapter.ViewHolder holder, int position) {
        final Song currentSong = mDataset.get(position);
        //Log.d("AlbumAdapter", "Current Song: " + currentSong.getTitle());

        holder.title.setText(currentSong.getTitle());
        holder.duration.setText(String.format(Locale.ENGLISH, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentSong.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(currentSong.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentSong.getDuration()))));

        holder.currentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the URI of the selected song
                Uri audioUri = Uri.parse("file://" + currentSong.getDataPath());

                // Let's check if the user is repeating or not
                if (preferenceHelper.getRepeat() == 1) {
                    // Since its repeating all, we add the whole album to the queue
                    mediaManager.putAlbumOnQueue(mDataset, currentSong);
                }

                // Update the currentlyPlayingIndex for mediaManager
                //Log.d("LOG Song Index: ", mediaManager.managerQueue.indexOf(currentSong) + "");
                mediaManager.currentlyPlayingIndex = mDataset.indexOf(currentSong);

            try {
                Uri albumArtUri = getAlbumArtUri(currentSong.getAlbumId());

                if (mediaManager.mLynxMediaPlayer.isPlaying() && !mediaManager.mediaPlayerIsPaused) {
                    /**
                     * Under the hood changes
                     */
                    //stop or pause your media player mediaPlayer.stop(); or mediaPlayer.pause();
                    // http://stackoverflow.com/questions/12266502/android-mediaplayer-stop-and-play
                    //mediaManager.mediaPlayer.stop();
                    mediaManager.mLynxMediaPlayer.reset();
                    mediaManager.mLynxMediaPlayer.setDataSource(context, audioUri);
                    mediaManager.mLynxMediaPlayer.prepareAsync();

                    /**
                     * User Interface Changes
                     */
                    slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                    mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                    slideSongTitle.setText(currentSong.getTitle());
                    slideSongArtist.setText(currentSong.getArtistName());
                    Glide.with(getInstance().getApplicationContext())
                            .load(albumArtUri)
                            .asBitmap()
                            .placeholder(R.drawable.untitled_album)
                            .into(slideAlbumArt);

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

                                    // Set the images
                                    slideAlbumArt.setImageURI(model);
                                    slidedAlbumArt.setImageURI(model);

                                    return false;
                                }
                            })
                            .into(slidedAlbumArt);
                } else {
                    /**
                     * Under the hood changes
                     */

                    //Log.d("SongAdapter", "Working");

                    // http://stackoverflow.com/questions/9008770/media-player-called-in-state-0-error-38-0
                    mediaManager.mLynxMediaPlayer.reset();
                    mediaManager.mLynxMediaPlayer.setDataSource(context, audioUri);
                    mediaManager.mLynxMediaPlayer.prepareAsync();

                    /**
                     * User Interface Changes
                     */
                    slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                    mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                    slideSongTitle.setText(currentSong.getTitle());
                    slideSongArtist.setText(currentSong.getArtistName());
                    Glide.with(getInstance().getApplicationContext())
                            .load(albumArtUri)
                            .placeholder(R.drawable.untitled_album)
                            .into(slideAlbumArt);

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

                                    // Set the images
                                    slideAlbumArt.setImageURI(model);
                                    slidedAlbumArt.setImageURI(model);

                                    return true;
                                }
                            })
                            .into(slidedAlbumArt);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

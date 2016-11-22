package com.nixholas.materialtunes.Media.Adapter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.Preferences;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.MainActivity.slidedProgressBar;
import static com.nixholas.materialtunes.MainActivity.slidingProgressBar;

/**
 * Created by nixho on 03-Nov-16.
 */
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    // Protected Entities
    @BindView(R.id.slide_button) ImageButton slideButton;
    @BindView(R.id.slide_albumart) ImageView slideAlbumArt;
    @BindView(R.id.slide_songtitle) TextView slideSongTitle;
    @BindView(R.id.slide_songartist) TextView slideSongArtist;
    @BindView(R.id.slided_layout) LinearLayout slidedLinearLayout;

    // Expanded Sliding Up Bar Entities
    @BindView(R.id.slided_image) ImageView slidedAlbumArt;

    @BindView(R.id.media_controls_playpause) ImageButton mediaControls_PlayPause;

    private ArrayList<Song> mDataset;
    private Context context;

    @NonNull
    @Override
    public String getSectionName(int position) {
        //this String will be shown in a bubble for specified position
        return mDataset.get(position).getTitle().substring(0, 1);
    }
/*
    @Override
    public String getSectionTitle(int position) {
        //this String will be shown in a bubble for specified position
        return mDataset.get(position).getTitle().substring(0, 1);
    }*/

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        /* each data item is just a string in this case */
        protected View v;
        TextView title, artistName;
        ImageView songArt;
        Palette viewPalette;
        //private boolean isPopupVisible;
        CardView currentCard;
        private final int cardHeight, cardWidth;

        public ViewHolder(View v) {
            super(v);
            //ButterKnife.bind(this, v);
            this.title = (TextView) v.findViewById(R.id.card_title);
            this.artistName = (TextView) v.findViewById(R.id.card_artist);
            this.songArt = (ImageView) v.findViewById(R.id.card_image);
            this.currentCard = (CardView) v.findViewById(R.id.card_view);

            cardHeight = currentCard.getHeight();
            cardWidth = currentCard.getWidth();

            // http://stackoverflow.com/questions/36952436/expanding-cardview-with-a-textview-on-click
            /*currentCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Log.d("OnClick", "CardView");

                    if (currentCard.getHeight() == cardHeight) {
                        // We'll need to animate


                        // Then play the music
                        *//*try {
                            mediaPlayer.setDataSource(this, myUri1);
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(view.getContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
                        } catch (SecurityException e) {
                            Toast.makeText(view.getContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
                        } catch (IllegalStateException e) {
                            Toast.makeText(view.getContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*//*
                    } else {

                    }
                }
            });*/
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SongsAdapter(ArrayList<Song> dataSet) {
        mDataset = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mainfrag_cardview, parent, false);

        // set the view's size, margins, paddings and layout parameters'
        ViewHolder vh = new ViewHolder(v);

        context = parent.getContext();

        // ButterKnife Properly
        //http://stackoverflow.com/questions/37771222/android-butterknife-unable-to-bind-views-into-viewholder
        ButterKnife.bind(this, (Activity) context);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Song currentSong = mDataset.get(position);
        final int currentPosition = position;

        holder.title.setText(currentSong.getTitle());
        holder.artistName.setText(currentSong.getArtistName());

        // http://stackoverflow.com/questions/17168215/seekbar-and-media-player-in-android
        slidingProgressBar.setMax(currentSong.getDuration()); // Set the max duration
        slidedProgressBar.setMax(currentSong.getDuration());

        // Get a handler that can be used to post to the main thread
        // http://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread
        final Handler mainHandler = new Handler(context.getMainLooper());

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaManager.mMediaPlayer != null) {
                        //Log.d("ProgRunnable", "Running"); // Debugging Purposes only
                        int mCurrentPosition = mediaManager.mMediaPlayer.getCurrentPosition() / 1000;
                        slidingProgressBar.setProgress(mCurrentPosition);
                        slidedProgressBar.setProgress(mCurrentPosition);
                    }
                    mainHandler.postDelayed(this, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mainHandler.post(progressRunnable);

        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

        //Log.e("Album Art URI", albumArtUri.toString());

        // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
        Glide.with(context)
                .load(albumArtUri)
                .asBitmap()
                .placeholder(R.drawable.untitled_album)
                .into(holder.songArt);

        holder.currentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Debugging Only
                //Log.e("CardOnClick", "Clicked");
                //Log.d("OnClick", "CardView");

                // Animations
                //view.animate().scaleX()

                // Since it's not enlarged yet
                //view.getRootView().animate().scaleX(1.2f);
                //view.getRootView().animate().scaleY(1.2f);

                try {
                    //Log.e("LOG ", currentSong.getDataPath());
                    mediaManager.currentlyPlayingIndex = currentPosition;
                    Uri audioUri = Uri.parse("file://" + currentSong.getDataPath());

                    /*Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());*/

                    Uri albumArtUri = getAlbumArtUri(currentSong.getAlbumId());

                        if (mediaManager.getmPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                            /**
                             * Under the hood changes
                             */
                            //stop or pause your media player mediaPlayer.stop(); or mediaPlayer.pause();
                            // http://stackoverflow.com/questions/12266502/android-mediaplayer-stop-and-play
                            //mediaManager.mediaPlayer.stop();
                            mediaManager.mMediaPlayer.reset();
                            mediaManager.mMediaPlayer.setDataSource(context, audioUri);
                            mediaManager.mMediaPlayer.prepareAsync();
                            mediaManager.mediaPlayerIsPaused = false;

                            /**
                             * User Interface Changes
                             */
                            slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                            mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                            slideSongTitle.setText(currentSong.getTitle());
                            slideSongArtist.setText(currentSong.getArtistName());
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
                        } else {
                            /**
                             * Under the hood changes
                             */

                            //Log.e("SongsAdapter", "Working");

                            // http://stackoverflow.com/questions/9008770/media-player-called-in-state-0-error-38-0
                            mediaManager.mMediaPlayer.reset();
                            mediaManager.mMediaPlayer.setDataSource(context, audioUri);
                            mediaManager.mMediaPlayer.prepareAsync();
                            mediaManager.mediaPlayerIsPaused = false;

                            /**
                             * User Interface Changes
                             */
                            slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                            mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                            slideSongTitle.setText(currentSong.getTitle());
                            slideSongArtist.setText(currentSong.getArtistName());
                            Glide.with(context)
                                    .load(albumArtUri)
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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public Uri getAlbumArtUri(long albumID) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumID);
    }

    public Bitmap getAlbumart(Long album_id)
    {
        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }
        return bm;
    }

}
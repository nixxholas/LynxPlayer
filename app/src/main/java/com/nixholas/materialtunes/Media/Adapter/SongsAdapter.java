package com.nixholas.materialtunes.Media.Adapter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixho on 03-Nov-16.
 */
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    // Protected Entities
    @BindView(R.id.slide_button) ImageButton slideButton;
    @BindView(R.id.slide_albumart) ImageView slideAlbumArt;
    @BindView(R.id.slide_songtitle) TextView slideSongTitle;
    @BindView(R.id.slide_songartist) TextView slideSongArtist;

    // Expanded Sliding Up Bar Entities
    @BindView(R.id.slided_image) ImageView slidedAlbumArt;

    @BindView(R.id.media_controls_playpause) ImageButton mediaControls_PlayPause;

    private ArrayList<Song> mDataset;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        /* each data item is just a string in this case */
        protected View v;
        TextView title, artistName;
        ImageView songArt;
        private boolean isPopupVisible;
        CardView currentCard;
        private final int cardHeight, cardWidth;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Song currentSong = mDataset.get(position);

        holder.title.setText(currentSong.getTitle());
        holder.artistName.setText(currentSong.getArtistName());

        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

        //Log.e("Album Art URI", albumArtUri.toString());

        // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
        Glide.with(context).load(albumArtUri).into(holder.songArt);

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
                    mediaManager.currentlyPlayingIndex = position;
                    Uri audioUri = Uri.parse("file://" + currentSong.getDataPath());

                    Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

                    if (mediaManager.mediaPlayer.isPlaying())
                    {
                        /**
                         * Under the hood changes
                         */
                        //stop or pause your media player mediaPlayer.stop(); or mediaPlayer.pause();
                        // http://stackoverflow.com/questions/12266502/android-mediaplayer-stop-and-play
                        //mediaManager.mediaPlayer.stop();
                        mediaManager.mediaPlayer.reset();
                        mediaManager.mediaPlayer.setDataSource(context, audioUri);
                        mediaManager.mediaPlayer.prepareAsync();
                        mediaManager.mediaPlayerIsPaused = false;

                        /**
                         * User Interface Changes
                         */
                        slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                        mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                        slideSongTitle.setText(currentSong.getTitle());
                        slideSongArtist.setText(currentSong.getArtistName());
                        Glide.with(context).load(albumArtUri).into(slideAlbumArt);
                        Glide.with(context).load(albumArtUri).into(slidedAlbumArt);
                    } else
                    {
                        /**
                         * Under the hood changes
                         */
                        // http://stackoverflow.com/questions/9008770/media-player-called-in-state-0-error-38-0
                        mediaManager.mediaPlayer.reset();
                        mediaManager.mediaPlayer.setDataSource(context, audioUri);
                        mediaManager.mediaPlayer.prepareAsync();
                        mediaManager.mediaPlayerIsPaused = false;

                        /**
                         * User Interface Changes
                         */
                        slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                        mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                        slideSongTitle.setText(currentSong.getTitle());
                        slideSongArtist.setText(currentSong.getArtistName());
                        Glide.with(context).load(albumArtUri).into(slideAlbumArt);
                        Glide.with(context).load(albumArtUri).into(slidedAlbumArt);
                    }

                } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                    // TODO Auto-generated catch block
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

}
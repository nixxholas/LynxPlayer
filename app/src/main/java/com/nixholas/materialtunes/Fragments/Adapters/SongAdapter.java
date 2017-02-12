package com.nixholas.materialtunes.Fragments.Adapters;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nixholas.materialtunes.Fragments.Dialogs.Playlist.AddToPlaylistDialog;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nixholas.materialtunes.MainActivity.getInstance;
import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.MainActivity.slidedRelativeLayout;
import static com.nixholas.materialtunes.Media.Entities.Utils.SongUtil.removeSong;
import static com.nixholas.materialtunes.Utils.AlbumService.getAlbumArtUri;

/**
 * Created by nixho on 03-Nov-16.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    // Protected Entities
    @BindView(R.id.slide_button) ImageButton slideButton;
    @BindView(R.id.slide_albumart) ImageView slideAlbumArt;
    @BindView(R.id.slide_songtitle) TextView slideSongTitle;
    @BindView(R.id.slide_songartist) TextView slideSongArtist;
    //@BindView(R.id.slided_layout) LinearLayout slidedLinearLayout;

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
    public class ViewHolder extends RecyclerView.ViewHolder {
        /* each data item is just a string in this case */
        protected View v;
        Song song;
        TextView title, artistName;
        ImageView songArt;
        ImageView overflowButton;
        Palette viewPalette;
        LinearLayout layout;

        public ViewHolder(View v) {
            super(v);
            //ButterKnife.bind(this, v);
            this.title = (TextView) v.findViewById(R.id.card_title);
            this.artistName = (TextView) v.findViewById(R.id.card_artist);
            this.songArt = (ImageView) v.findViewById(R.id.card_image);
            this.layout = (LinearLayout) v.findViewById(R.id.mainfrag_layout);
            this.overflowButton = (ImageView) v.findViewById(R.id.card_options);
            final Context mContext = v.getContext();

            // http://stackoverflow.com/questions/37851828/cardview-overflow-menu-in-fragment
            this.overflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(mContext, view);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_song, popup.getMenu());
                    popup.setOnMenuItemClickListener(new SongMenuClickListener());
                    popup.show();
                }
            });
        }

        class SongMenuClickListener implements PopupMenu.OnMenuItemClickListener {

            SongMenuClickListener() {}

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.song_playlist:
                        //Log.e("OnMenuItemClick", "SongMenuClickListener is Working");
                        AddToPlaylistDialog addToPlaylistDialog = new AddToPlaylistDialog();
                        addToPlaylistDialog.show(getInstance().getFragmentManager(), "AddToPlaylistDialogFragment");
                        return true;
                    case R.id.song_delete:
                        // We'll have a method that removes the song
                        // http://stackoverflow.com/questions/3805599/add-delete-view-from-layout
                        //layout.setVisibility(View.GONE); // Unorthodox lol
                        // http://stackoverflow.com/questions/26076965/android-recyclerview-addition-removal-of-items

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                removeSong(song.getId());
                                return null;
                            }
                        }.execute();

                        removeAt(getAdapterPosition());
                        Toast toast = Toast.makeText(context, title.getText() + " Deleted.", Toast.LENGTH_SHORT);
                        toast.show();
                        return true;
                    default:
                }
                return false;
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SongAdapter(ArrayList<Song> dataSet) {
        mDataset = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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
        holder.song = currentSong;
        final int currentPosition = position;

        holder.title.setText(currentSong.getTitle());
        holder.artistName.setText(currentSong.getArtistName());

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

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Debugging Only
                //Log.e("CardOnClick", "Clicked");
                Log.d("OnClick", "CardView");

                // Animations
                //view.animate().scaleX()

                // Since it's not enlarged yet
                //view.getRootView().animate().scaleX(1.2f);
                //view.getRootView().animate().scaleY(1.2f);

                try {
                    //Log.e("LOG ", currentSong.getDataPath());

                    /**
                     * This line breaks the Queue system.
                     */
                    //mediaManager.currentlyPlayingIndex = currentPosition;

                    Uri audioUri = Uri.parse("file://" + currentSong.getDataPath());

                    // Add all the remaining songs to the queue and clear it as well
                    mediaManager.managerQueue.clear();
                    mediaManager.putAllOnQueue(currentSong);

                    //Log.d("LOG Song Index: ", mediaManager.managerQueue.indexOf(currentSong) + "");
                    mediaManager.currentlyPlayingIndex = mediaManager.managerQueue.indexOf(currentSong);

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

                            //Log.e("SongAdapter", "Working");

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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
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

    public void removeAt(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
    }
}
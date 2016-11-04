package com.nixholas.materialtunes.Media.Adapter;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by nixho on 03-Nov-16.
 */
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    private ArrayList<Song> mDataset;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View v;
        protected TextView title, artistName;
        protected ImageView songArt;
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
            currentCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Log.d("OnClick", "CardView");

                    if (currentCard.getHeight() == cardHeight) {

                    } else {

                    }
                }
            });
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


        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Song currentSong = mDataset.get(position);

        holder.title.setText(currentSong.getTitle());
        holder.artistName.setText(currentSong.getArtistName());

        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

        //Log.e("Album Art URI", albumArtUri.toString());

        // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
        Glide.with(context).load(albumArtUri).into(holder.songArt);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
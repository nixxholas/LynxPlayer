package com.nixholas.materialtunes.Media.Adapter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

/**
 * Created by nixho on 03-Nov-16.
 */

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter{
    private ArrayList<Album> mDataset;
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
        return mDataset.get(position).getTitle().substring(0, 1);
    }*/

    static class ViewHolder extends RecyclerView.ViewHolder {
        public View v;
        TextView title, artistName;
        ImageView albumArt;
        CardView currentAlbumCard;

        public ViewHolder(View v) {
            super(v);
            this.albumArt = (ImageView) v.findViewById(R.id.album_thumbnail);
            this.title = (TextView) v.findViewById(R.id.album_cardtitle);
            this.artistName = (TextView) v.findViewById(R.id.album_cardartist);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AlbumsAdapter(ArrayList<Album> dataSet) {
        mDataset = dataSet;
    }

    @Override
    public AlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.albumfrag_cardview, parent, false);

        // Set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);

        context = parent.getContext();

        return vh;
    }

    @Override
    public void onBindViewHolder(final AlbumsAdapter.ViewHolder holder, int position) {
        final Album currentAlbum = mDataset.get(position);

        holder.title.setText(currentAlbum.getTitle());
        holder.artistName.setText(currentAlbum.getArtistName());

        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentAlbum.getId());

        // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
        // http://stackoverflow.com/questions/32038936/how-to-make-glide-display-like-picasso
        Glide.with(context)
                .load(albumArtUri)
                .asBitmap()
                .placeholder(R.drawable.untitled_album)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Uri, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.albumArt);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}

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
import android.util.Log;
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
import com.nixholas.materialtunes.Utils.Preferences;
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
            this.currentAlbumCard = (CardView) v.findViewById(R.id.album_cardview);
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

        // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
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

        // http://stackoverflow.com/questions/32038936/how-to-make-glide-display-like-picasso
        // http://stackoverflow.com/questions/32503327/glide-listener-doesnt-work
        Glide.with(context)
                .load(albumArtUri)
                .asBitmap()
                .placeholder(R.drawable.untitled_album)
                .centerCrop()
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                // http://android-er.blogspot.sg/2015/09/extract-prominent-colors-from-image.html
                .listener(new RequestListener<Uri, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        //int defaultColor = 0x000000;

                        // Learning the ropes for colors..
                        /*Palette.Swatch vibrantSwatch = Palette.from(resource).generate().getVibrantSwatch();
                        Palette.Swatch dominantSwatch = Palette.from(resource).generate().getDominantSwatch();

                        // Assign the various color depths to each variable
                        int vibrantColor = p.getVibrantColor(defaultColor);
                        int darkVibrantColor = p.getDarkVibrantColor(defaultColor);
                        int lightVibrantColor = p.getLightVibrantColor(defaultColor);
                        int mutedColor = p.getMutedColor(defaultColor);
                        int darkMutedColor = p.getDarkMutedColor(defaultColor);
                        int lightMutedColor = p.getLightMutedColor(defaultColor);*/

                        /*Log.e("Light Vibrant Color", lightVibrantColor + "");
                        Log.e("Vibrant Color", vibrantColor + "");*/

                        // Assign the color to the views accordingly
                        /*holder.currentAlbumCard.setBackgroundColor(mutedColor);
                        holder.title.setTextColor(vibrantColor);
                        holder.artistName.setTextColor(vibrantColor);*/

                        // Retrieve the palette of colors from the Bitmap first
                        Palette p = Palette.from(resource).generate();
                        Palette.Swatch swatch = p.getVibrantSwatch();
                        if (swatch != null) {
                            int color = swatch.getRgb();
                            holder.currentAlbumCard.setBackgroundColor(color);
                            int textColor = Preferences.getBlackWhiteColor(swatch.getTitleTextColor());
                            holder.title.setTextColor(textColor);
                            holder.artistName.setTextColor(textColor);
                        } else {
                            Palette.Swatch mutedSwatch = p.getMutedSwatch();
                            if (mutedSwatch != null) {
                                int color = mutedSwatch.getRgb();
                                holder.currentAlbumCard.setBackgroundColor(color);
                                int textColor = Preferences.getBlackWhiteColor(mutedSwatch.getTitleTextColor());
                                holder.title.setTextColor(textColor);
                                holder.artistName.setTextColor(textColor);
                            }
                        }

                        // Set the image
                        holder.albumArt.setImageURI(model);

                        return true;
                    }
                })
                .into(holder.albumArt);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}

package com.nixholas.materialtunes.Media.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.util.Util;
import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.PreferencesExample;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by nixho on 03-Nov-16.
 */

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private ArrayList<Album> mDataset;
    private Context context;
    /*
         * Gets the number of available cores
         * (not always the same as the maximum number of cores)
         */
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    // A queue of Runnables
    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<>();
    // Creates a thread pool manager
    ThreadPoolExecutor mDecodeThreadPool = new ThreadPoolExecutor(
            NUMBER_OF_CORES,       // Initial pool size
            NUMBER_OF_CORES,       // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            mDecodeWorkQueue);


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

        if (currentAlbum.getAlbumArtPath() != null) {
            // http://stackoverflow.com/questions/35997439/palette-using-with-glide-sometimes-fail-to-load-dark-vibrant-color
            Glide.with(context)
                    .fromUri()
                    .asBitmap()
                    .transcode(new PaletteBitmapTranscoder(context), PaletteBitmap.class)
                    .fitCenter().load(Uri.fromFile(new File(currentAlbum.getAlbumArtPath())))
                    .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            holder.albumArt.setImageBitmap(resource.getBitmap());
                            Palette.Swatch swatch = resource.palette.getVibrantSwatch();
                            if (swatch != null) {
                                int color = swatch.getRgb();
                                holder.currentAlbumCard.setBackgroundColor(color);
                                int textColor = PreferencesExample.getBlackWhiteColor(swatch.getTitleTextColor());
                                holder.title.setTextColor(textColor);
                                holder.artistName.setTextColor(textColor);
                            } else {
                                Palette.Swatch mutedSwatch = resource.palette.getMutedSwatch();
                                if (mutedSwatch != null) {
                                    int color = mutedSwatch.getRgb();
                                    holder.currentAlbumCard.setBackgroundColor(color);
                                    int textColor = PreferencesExample.getBlackWhiteColor(mutedSwatch.getTitleTextColor());
                                    holder.title.setTextColor(textColor);
                                    holder.artistName.setTextColor(textColor);
                                }
                            }
                        }
                    });

        } else { // Since it does not have an album art
            Glide.with(context)
                    .load(R.drawable.untitled_album)
                    .into(holder.albumArt);
        }

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}

class PaletteBitmap {
    public final Palette palette;
    public final Bitmap bitmap;

    public PaletteBitmap(@NonNull Bitmap bitmap, @NonNull Palette palette) {
        this.bitmap = bitmap;
        this.palette = palette;
    }

    Bitmap getBitmap() {
        return bitmap;
    }
}

class PaletteBitmapResource implements Resource<PaletteBitmap> {
    private final PaletteBitmap paletteBitmap;
    private final BitmapPool bitmapPool;

    public PaletteBitmapResource(@NonNull PaletteBitmap paletteBitmap, @NonNull BitmapPool bitmapPool) {
        this.paletteBitmap = paletteBitmap;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public PaletteBitmap get() {
        return paletteBitmap;
    }

    @Override
    public int getSize() {
        return Util.getBitmapByteSize(paletteBitmap.bitmap);
    }

    @Override
    public void recycle() {
        if (!bitmapPool.put(paletteBitmap.bitmap)) {
            paletteBitmap.bitmap.recycle();
        }
    }
}

class PaletteBitmapTranscoder implements ResourceTranscoder<Bitmap, PaletteBitmap> {
    private final BitmapPool bitmapPool;

    public PaletteBitmapTranscoder(@NonNull Context context) {
        this.bitmapPool = Glide.get(context).getBitmapPool();
    }

    @Override
    public Resource<PaletteBitmap> transcode(Resource<Bitmap> toTranscode) {
        Bitmap bitmap = toTranscode.get();
        Palette palette = new Palette.Builder(bitmap).generate();
        PaletteBitmap result = new PaletteBitmap(bitmap, palette);
        return new PaletteBitmapResource(result, bitmapPool);
    }

    @Override
    public String getId() {
        return PaletteBitmapTranscoder.class.getName();
    }
}
package com.nixholas.materialtunes.Media.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.util.Util;
import com.nixholas.materialtunes.Fragments.FragmentItem.AlbumActivity;
import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.PaletteBitmap.PaletteBitmap;
import com.nixholas.materialtunes.Utils.PaletteBitmap.PaletteBitmapTranscoder;
import com.nixholas.materialtunes.Utils.PreferencesExample;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by nixho on 03-Nov-16.
 */

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private ArrayList<Album> mDataset;
    private Context context;

    @NonNull
    @Override
    public String getSectionName(int position) {
        //this String will be shown in a bubble for specified position
        return mDataset.get(position).getTitle().substring(0, 1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View v;
        TextView title, artistName;
        ImageView albumArt;
        Palette.Swatch swatch, mutedSwatch;
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
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .fitCenter().load(Uri.fromFile(new File(currentAlbum.getAlbumArtPath())))
                    .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            holder.albumArt.setImageBitmap(resource.getBitmap());
                            holder.swatch = resource.palette.getVibrantSwatch();
                            if (holder.swatch != null) {
                                int color = holder.swatch.getRgb();
                                holder.currentAlbumCard.setBackgroundColor(color);
                                int textColor = PreferencesExample.getBlackWhiteColor(holder.swatch.getTitleTextColor());
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
                    .fromResource()
                    .asBitmap()
                    .transcode(new PaletteBitmapTranscoder(context), PaletteBitmap.class)
                    .load(R.drawable.untitled_album)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            holder.albumArt.setImageBitmap(resource.getBitmap());
                            holder.swatch = resource.palette.getVibrantSwatch();
                            if (holder.swatch != null) {
                                int color = holder.swatch.getRgb();
                                holder.currentAlbumCard.setBackgroundColor(color);
                                int textColor = PreferencesExample.getBlackWhiteColor(holder.swatch.getTitleTextColor());
                                holder.title.setTextColor(textColor);
                                holder.artistName.setTextColor(textColor);
                            } else {
                                holder.mutedSwatch = resource.palette.getMutedSwatch();
                                if (holder.mutedSwatch != null) {
                                    int color = holder.mutedSwatch.getRgb();
                                    holder.currentAlbumCard.setBackgroundColor(color);
                                    int textColor = PreferencesExample.getBlackWhiteColor(holder.mutedSwatch.getTitleTextColor());
                                    holder.title.setTextColor(textColor);
                                    holder.artistName.setTextColor(textColor);
                                }
                            }
                        }
                    });
        }

        holder.currentAlbumCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Implementing Material Motion in Transitions
                 *
                 * http://stackoverflow.com/questions/27736848/cardview-animation-raise-and-expand-on-click
                 */
                Intent intent = new Intent(v.getContext(), AlbumActivity.class);
                int w = v.getWidth();
                int h = v.getHeight();
//                float maxRadius = (float) Math.sqrt(w * w / 4 + h * h / 4);

                // http://stackoverflow.com/questions/2183962/how-to-read-value-from-string-xml-in-android
                String transitionName = v.getResources().getString(R.string.transition_album_cover);

                // Perform the necessary pairing
                // http://xmodulo.com/activity-transition-animations-android.html
                Pair<View, String> imageView = new Pair<View, String>(holder.albumArt, v.getResources().getString(R.string.transition_album_image));
                Pair<View, String> titleView = new Pair<View, String>(holder.title, v.getResources().getString(R.string.transition_album_title));

                // We'll give the intent some data that it requires
                intent.putExtra("albumId", currentAlbum.getId());
                intent.putExtra("albumArtUri", currentAlbum.getAlbumArtPath());
                intent.putExtra("albumName", currentAlbum.getTitle());
                intent.putExtra("albumArtist", currentAlbum.getArtistName());

                /**
                 * We'll have to split apart the AOptions so that we can invoke the methods that
                 * API Level 23 and above can be used for those OSes that are above API Level 23.
                 *
                 * http://stackoverflow.com/questions/38411878/how-do-i-create-the-marshmallow-open-activity-animation
                 */
                ActivityOptions options;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    options = ActivityOptions.makeClipRevealAnimation(v, 0, 0, w, h);
                } else {
                    options =
                        ActivityOptions.makeSceneTransitionAnimation((Activity) context,
                                v,   // The view which starts the transition
                                transitionName    // The transitionName of the view we’re transitioning to
                        );
                }

                startActivity(context, intent, options.toBundle());
        }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}

package com.nixholas.materialtunes.Media.Adapter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
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
import android.widget.TextView;

import com.nixholas.materialtunes.Media.Entities.List;
import com.nixholas.materialtunes.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.FileDescriptor;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nixho on 26-Nov-16.
 */

public class ListsAdapter extends RecyclerView.Adapter<ListsAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    // Protected Entities
    @BindView(R.id.slide_button)
    ImageButton slideButton;
    @BindView(R.id.slide_albumart)
    ImageView slideAlbumArt;
    @BindView(R.id.slide_songtitle)
    TextView slideSongTitle;
    @BindView(R.id.slide_songartist) TextView slideSongArtist;
    //@BindView(R.id.slided_layout) LinearLayout slidedLinearLayout;

    // Expanded Sliding Up Bar Entities
    @BindView(R.id.slided_image) ImageView slidedAlbumArt;

    @BindView(R.id.media_controls_playpause) ImageButton mediaControls_PlayPause;

    private ArrayList<List> mDataset;
    private Context context;

    @NonNull
    @Override
    public String getSectionName(int position) {
        //this String will be shown in a bubble for specified position
        return mDataset.get(position).getListName().substring(0, 1);
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
        TextView title;
        ImageView overflowButton;
        Palette viewPalette;
        //private boolean isPopupVisible;
        CardView currentCard;

        public ViewHolder(View v) {
            super(v);
            //ButterKnife.bind(this, v);
            this.title = (TextView) v.findViewById(R.id.listcard_title);
            this.currentCard = (CardView) v.findViewById(R.id.listfrag_cardview);
            this.overflowButton = (ImageView) v.findViewById(R.id.listcard_options);
            final Context mContext = v.getContext();

            this.overflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(mContext, view);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_song, popup.getMenu());
                    popup.setOnMenuItemClickListener(new ListsAdapter.ViewHolder.ListMenuClickListener());
                    popup.show();
                }
            });
        }

        class ListMenuClickListener implements PopupMenu.OnMenuItemClickListener {

            ListMenuClickListener() {
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.song_playlist:
                        Log.e("OnMenuItemClick", "SongMenuClickListener is Working");
                        //Toast.makeText(mContext, "Action 1", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                }
                return false;
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListsAdapter(ArrayList<List> dataSet) {
        mDataset = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ListsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_cardview, parent, false);

        // set the view's size, margins, paddings and layout parameters'
        ListsAdapter.ViewHolder vh = new ListsAdapter.ViewHolder(v);

        context = parent.getContext();

        // ButterKnife Properly
        //http://stackoverflow.com/questions/37771222/android-butterknife-unable-to-bind-views-into-viewholder
        ButterKnife.bind(this, (Activity) context);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ListsAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final List currentList = mDataset.get(position);
        final int currentPosition = position;

        holder.title.setText(currentList.getListName());

        holder.currentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Debugging Only
                Log.e("CardOnClick", "Clicked on List");
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
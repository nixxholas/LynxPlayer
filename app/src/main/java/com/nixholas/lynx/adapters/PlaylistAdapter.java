package com.nixholas.lynx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nixholas.lynx.media.MediaManager;
import com.nixholas.lynx.media.entities.Playlist;
import com.nixholas.lynx.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.nixholas.lynx.media.entities.utils.PlaylistUtil.removePlaylist;

/**
 * Created by nixho on 26-Nov-16.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    public ArrayList<Playlist> mDataset;
    private Context context;

    @NonNull
    @Override
    public String getSectionName(int position) {
        //this String will be shown in a bubble for specified position
        return mDataset.get(position).getPlaylistName().substring(0, 1);
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
        Playlist playlist;
        TextView title, dateAdded;
        ImageView overflowButton;
        Palette viewPalette;
        CardView cardView;

        public ViewHolder(View v) {
            super(v);
            //ButterKnife.bind(this, v);
            this.v = v;
            this.title = (TextView) v.findViewById(R.id.listcard_title);
            this.dateAdded = (TextView) v.findViewById(R.id.listcard_date_added);
            this.overflowButton = (ImageView) v.findViewById(R.id.listcard_options);
            this.cardView = (CardView) v.findViewById(R.id.list_cardView);
            final Context mContext = v.getContext();

            this.overflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(mContext, view);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_playlist, popup.getMenu());
                    popup.setOnMenuItemClickListener(new ListMenuClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.playlist_play:
                                        //Toast.makeText(mContext, "Action 1", Toast.LENGTH_SHORT).show();
                                        return true;
                                    case R.id.playlist_delete:
                                        //Log.d("onMenuItemClick", title.getText().toString());
                                        //cardView.setVisibility(View.GONE);
                                        // http://stackoverflow.com/questions/26076965/android-recyclerview-addition-removal-of-items

                                        new AsyncTask<Void, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                removePlaylist(context, playlist.getPlaylistId());
                                                return null;
                                            }
                                        }.execute();

                                        removeAt(getAdapterPosition());
                                        Toast toast = Toast.makeText(context, title.getText() + " Deleted", Toast.LENGTH_SHORT);
                                        toast.show();
                                        return true;
                                    default:
                                }
                                return false;
                        }
                    });
                    popup.show();
                }
            });
        }

        abstract class ListMenuClickListener implements PopupMenu.OnMenuItemClickListener {

            ListMenuClickListener() {
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlaylistAdapter(MediaManager mediaManager) {

        mDataset = new ArrayList<>();
        mediaManager.mDataAdapter.updatePlaylistDataset(mDataset);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlaylistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);

        // set the view's size, margins, paddings and layout parameters'
        PlaylistAdapter.ViewHolder vh = new PlaylistAdapter.ViewHolder(v);

        context = parent.getContext();

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final PlaylistAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Playlist currentList = mDataset.get(position);
        holder.playlist = currentList;
        final long playlistId = currentList.getPlaylistId();

        // Convert the Unix Timestamp to a readable date format first
        // http://stackoverflow.com/questions/20654967/convert-unix-epoch-time-to-formatted-date-unexpected-date
        long unixSeconds = Long.parseLong(currentList.getPlaylistDateAdded());
        Date date = new Date(unixSeconds * 1000); // *1000 is to convert minutes to milliseconds
        // https://developer.android.com/reference/java/text/SimpleDateFormat.html
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy"); // the format of your date

        holder.title.setText(currentList.getPlaylistName());
        holder.dateAdded.setText("Created on: " + sdf.format(date));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Debugging Only
                Log.d("playlistOnClick", "Clicked on List Card");
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void removeAt(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
    }
}
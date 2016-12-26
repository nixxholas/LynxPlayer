package com.nixholas.materialtunes.Media.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by nixholas on 23/12/16.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>  {
    private ArrayList<Song> mDataset;
    private Context context;
    int color, textColor;

    class ViewHolder extends RecyclerView.ViewHolder {
        public View v;
        TextView title, duration;
        RelativeLayout currentLayout;

        public ViewHolder(View v) {
            super(v);
            this.currentLayout = (RelativeLayout) v.findViewById(R.id.albumsfragitem_layout);
            this.title = (TextView) v.findViewById(R.id.albumsfragitem_title);
            this.duration = (TextView) v.findViewById(R.id.albumsfragitem_duration);
        }
    }

    public AlbumAdapter (ArrayList<Song> mDataset, int color, int textColor) {
        this.mDataset = mDataset;
        this.color = color;
        this.textColor = textColor;
    }

    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.albumexpanded_item, parent, false);

        // Set the view's size, margins, paddings and layout parameters
        AlbumAdapter.ViewHolder vh = new AlbumAdapter.ViewHolder(v);

        // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
        //context = parent.getContext();

        return vh;
    }

    @Override
    public void onBindViewHolder(AlbumAdapter.ViewHolder holder, int position) {
        final Song currentSong = mDataset.get(position);
        //Log.d("AlbumAdapter", "Current Song: " + currentSong.getTitle());

        holder.title.setText(currentSong.getTitle());
        holder.duration.setText(String.format(Locale.ENGLISH, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentSong.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(currentSong.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentSong.getDuration()))));

        holder.currentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

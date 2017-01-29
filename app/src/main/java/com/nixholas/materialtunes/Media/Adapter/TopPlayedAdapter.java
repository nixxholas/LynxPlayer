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

/**
 * Created by nixholas on 29/1/17.
 */

public class TopPlayedAdapter extends RecyclerView.Adapter<TopPlayedAdapter.ViewHolder> {
    private ArrayList<Song> mDataset;
    private Context context;

    class ViewHolder extends RecyclerView.ViewHolder {
        public View v;
        TextView title, count;
        RelativeLayout currentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.currentLayout = (RelativeLayout) v.findViewById(R.id.topplayeditem_layout);
            this.title = (TextView) v.findViewById(R.id.topplayeditem_title);
            this.count = (TextView) v.findViewById(R.id.topplayeditem_count);
        }
    }

    public TopPlayedAdapter(ArrayList<Song> mDataset) {
        this.mDataset = mDataset;
    }

    @Override
    public TopPlayedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topplayed_item, parent, false);

        // Set the view's size, margins, paddings and layout parameters
        TopPlayedAdapter.ViewHolder vh = new TopPlayedAdapter.ViewHolder(v);

        // http://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
        context = parent.getContext();

        return vh;
    }

    @Override
    public void onBindViewHolder(TopPlayedAdapter.ViewHolder holder, int position) {
        final Song currentSong = mDataset.get(position);

        holder.title.setText(currentSong.getTitle());
        holder.count.setText(currentSong.getCount() + "");

        holder.currentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

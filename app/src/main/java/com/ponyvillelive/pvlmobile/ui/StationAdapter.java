package com.ponyvillelive.pvlmobile.ui;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ponyvillelive.pvlmobile.R;
import com.ponyvillelive.pvlmobile.model.Station;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StationAdapter extends RecyclerView.Adapter<StationHolder> {


    private final Context mContext;
    private List<Station> stationList;

    public StationAdapter(Context context) {
        this.mContext = context;
        this.stationList = new ArrayList<>();
    }

    public void setItems(Station[] stations){
        this.stationList = Arrays.asList(stations);
    }

    public Station[] getItems(){
        return stationList.toArray(new Station[stationList.size()]);
    }

    public Station getItem(int position){
        return stationList.get(position);
    }

    @Override
    public void onBindViewHolder(StationHolder holder, int position) {
        Station station = stationList.get(position);

        holder.title.setText(station.name);
        holder.subtitle.setText(station.genre);
        holder.listeners.setText("44");
        holder.songTitle.setText("Join The Herd");
        holder.songArtist.setText("Forest Rain");

        Picasso.with(mContext).load(station.imageUrl)
                .placeholder(R.drawable.pvl_logo)
                .into(holder.target);
    }

    @Override
    public StationHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.view_station_list_item, viewGroup, false);

        return new StationHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

}

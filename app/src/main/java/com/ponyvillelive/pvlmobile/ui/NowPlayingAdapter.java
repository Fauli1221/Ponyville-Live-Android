package com.ponyvillelive.pvlmobile.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ponyvillelive.pvlmobile.R;
import com.ponyvillelive.pvlmobile.model.NowPlayingMeta;
import com.ponyvillelive.pvlmobile.model.Song;
import com.ponyvillelive.pvlmobile.model.Station;
import com.ponyvillelive.pvlmobile.util.Strings;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tinker on 28/02/16.
 */

public class NowPlayingAdapter extends RecyclerView.Adapter<StationHolder> {


    private final Context mContext;
    private List<NowPlayingMeta> stationList;
    private BaseHolder[] mHolders;

    public NowPlayingAdapter(Context context) {
        this.mContext = context;
        this.stationList = new ArrayList<>();
    }

    public void setItems(NowPlayingMeta[] stations){
        this.stationList = Arrays.asList(stations);
        this.mHolders = new BaseHolder[stationList.size()];
    }

    public NowPlayingMeta[] getItems(){
        return stationList.toArray(new NowPlayingMeta[stationList.size()]);
    }

    public NowPlayingMeta getItem(int position){
        return stationList.get(position);
    }
    public BaseHolder getHolder(int position) { return mHolders[position]; }

    @Override
    public void onBindViewHolder(StationHolder holder, int position) {
        NowPlayingMeta nowPlaying = stationList.get(position);
        Station station = nowPlaying.station;

        holder.title.setText(station.name);
        holder.subtitle.setText(station.genre);
        holder.listeners.setText(Strings.valueOrDefault(Integer.toString(nowPlaying.listeners.get("current")), "0"));
        if (null == nowPlaying.currentSong)
            nowPlaying.currentSong = new Song();
        holder.songTitle.setText(Strings.valueOrDefault(nowPlaying.currentSong.title, ""));
        holder.songArtist.setText(Strings.valueOrDefault(nowPlaying.currentSong.artist, ""));

        Picasso.with(mContext).load(station.imageUrl)
                .placeholder(R.drawable.pvl_logo)
                .into(holder.target);

        mHolders[position] = holder;
    }

    @NonNull
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

package com.ponyvillelive.pvlmobile.ui;

import android.view.View;
import android.widget.TextView;

import com.ponyvillelive.pvlmobile.R;

/**
 * Created by tinker on 30/01/16.
 */
public class StationHolder extends BaseHolder {

    public TextView listeners;
    public TextView songTitle;
    public TextView songArtist;

    public StationHolder(View itemView) {
        super(itemView);
        listeners = (TextView) itemView.findViewById(R.id.station_watchers);
        songTitle = (TextView) itemView.findViewById(R.id.station_title);
        songArtist = (TextView) itemView.findViewById(R.id.station_artist);
    }


}

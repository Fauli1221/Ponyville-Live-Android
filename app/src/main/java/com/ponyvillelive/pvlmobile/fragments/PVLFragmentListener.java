package com.ponyvillelive.pvlmobile.fragments;

import android.graphics.Bitmap;

import com.ponyvillelive.pvlmobile.model.Convention;
import com.ponyvillelive.pvlmobile.model.Episode;
import com.ponyvillelive.pvlmobile.model.Show;
import com.ponyvillelive.pvlmobile.model.Station;

/**
 * Created by tinker on 29/01/16.
 */
public interface PVLFragmentListener {
    /**
     * Handle a station being selected inside of the {@link StationListFragment}
     *
     * @param station The {@link Station} that was selected
     */
    boolean handleStationSelected(Station station, Bitmap thumb); // remember to implement stream selected and then change this back!
    boolean handleStationsLoaded(Station[] stations, boolean explicit);
    boolean handleEpisodeSelected(Show show, Episode episode, Bitmap thumb);
    boolean handleShowSelected(Show show);
    boolean handleConventionSelected(Convention con);
    boolean handleVideoSelected(Convention con, Convention.Video video, Bitmap thumb);
}
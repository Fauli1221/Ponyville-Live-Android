package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.collection.ArrayMap;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tyr on 22/05/2014.
 */
public class NowPlayingMeta implements Parcelable{

    public Station station;
    public Map<String, Integer> listeners;
    @SerializedName("current_song")
    public Song currentSong;
    @SerializedName("song_history")
    public List<SongWrapper> songHistory;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(station, 0);
        dest.writeMap(listeners);
        dest.writeParcelable(currentSong, 0);
        dest.writeList(songHistory);
    }

    public NowPlayingMeta() {
    }

    private NowPlayingMeta(Parcel in) {
        this.station = in.readParcelable(null);
        this.listeners = new ArrayMap<String, Integer>();
        in.readMap(listeners, null);
        this.currentSong = in.readParcelable(null);
        this.songHistory = new ArrayList<SongWrapper>();
        in.readList(songHistory, null);
    }

    public static final Creator<NowPlayingMeta> CREATOR = new Creator<NowPlayingMeta>() {
        public NowPlayingMeta createFromParcel(Parcel source) {
            return new NowPlayingMeta(source);
        }

        public NowPlayingMeta[] newArray(int size) {
            return new NowPlayingMeta[size];
        }
    };

}

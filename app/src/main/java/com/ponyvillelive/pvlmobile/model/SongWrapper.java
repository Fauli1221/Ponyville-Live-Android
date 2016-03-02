package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by berwyn on 21/08/14.
 */
public class SongWrapper implements Parcelable{

    public Date playedAt;
    public Song song;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(playedAt);
        dest.writeParcelable(song, 0);
    }

    public SongWrapper() {
    }

    private SongWrapper(Parcel in) {
        this.playedAt = (java.util.Date) in.readSerializable();
        this.song = in.readParcelable(null);
    }

    public static final Creator<SongWrapper> CREATOR = new Creator<SongWrapper>() {
        public SongWrapper createFromParcel(Parcel source) {
            return new SongWrapper(source);
        }

        public SongWrapper[] newArray(int size) {
            return new SongWrapper[size];
        }
    };
}

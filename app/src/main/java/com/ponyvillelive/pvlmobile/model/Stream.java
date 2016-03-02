package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tinker on 30/01/16.
 */
public class Stream extends Entity {
    public String name;
    public String url;
    public String type;
    @SerializedName("is_default")
    public Boolean isDefault;
    public String status;
    public int bitrate;
    public String format;
    public Listeners listeners;
    @SerializedName("current_song")
    public Song currentSong;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(type);
        dest.writeByte((byte) (isDefault ? 1 : 0));
        dest.writeString(status);
        dest.writeInt(bitrate);
        dest.writeString(format);
        dest.writeParcelable(listeners, 0);
        dest.writeParcelable(currentSong, 0);
    }

    public Stream() {
    }

    private Stream(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.url = in.readString();
        this.type = in.readString();
        this.isDefault = in.readByte() != 0;
        this.status = in.readString();
        this.bitrate = in.readInt();
        this.format = in.readString();
        this.listeners = in.readParcelable(null);
        this.currentSong = in.readParcelable(null);
    }

    public static final Creator<Stream> CREATOR = new Creator<Stream>() {
        public Stream createFromParcel(Parcel source) {
            return new Stream(source);
        }

        public Stream[] newArray(int size) {
            return new Stream[size];
        }
    };

}

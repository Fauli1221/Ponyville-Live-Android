package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tinker on 29/02/16.
 */
public class Episode extends Entity {

    @SerializedName("source_id")
    public int sourceId;
    @SerializedName("guid")
    public String guid;
    @SerializedName("timestamp")
    public int timestamp;
    @SerializedName("title")
    public String title;
    @SerializedName("body")
    public String body;
    @SerializedName("summary")
    public String summary;
    @SerializedName("thumbnail_url")
    public String thumbnailUrl;
    @SerializedName("banner_url")
    public String bannerUrl;
    @SerializedName("is_active")
    public boolean isActive;
    @SerializedName("play_count")
    public int playCount;
    @SerializedName("raw_url")
    public String rawUrl;
    @SerializedName("web_url")
    public String webUrl;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(sourceId);
        dest.writeString(guid);
        dest.writeInt(timestamp);
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(summary);
        dest.writeString(thumbnailUrl);
        dest.writeString(bannerUrl);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeInt(playCount);
        dest.writeString(rawUrl);
        dest.writeString(webUrl);
    }

    public Episode() {
    }

    private Episode(Parcel in) {
        this.id = in.readString();
        this.sourceId = in.readInt();
        this.guid = in.readString();
        this.timestamp = in.readInt();
        this.title = in.readString();
        this.body = in.readString();
        this.summary = in.readString();
        this.thumbnailUrl = in.readString();
        this.bannerUrl = in.readString();
        this.isActive = in.readByte() != 0;
        this.playCount = in.readInt();
        this.rawUrl = in.readString();
        this.webUrl = in.readString();
    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        public Episode createFromParcel(Parcel source) {
            return new Episode(source);
        }

        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };
}

package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Radio station entity as presented by the PonyvilleAPI
 */
public class Station extends Entity {

    public String name;
    public String shortcode;
    public String genre;
    public String category;
    public String affiliation;
    @SerializedName("image_url")
    public String imageUrl;
    @SerializedName("web_url")
    public String webUrl;
    @SerializedName("twitter_url")
    public String twitterUrl;
    public String irc;
    public int sort_order;
    @SerializedName("player_url")
    public String playerUrl;
    @SerializedName("request_url")
    public String requestUrl;
    @SerializedName("stream_url")
    public String streamUrl;
    @SerializedName("default_stream_id")
    public String defaultStreamId;
    public List<Stream> streams;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(shortcode);
        dest.writeString(genre);
        dest.writeString(category);
        dest.writeString(affiliation);
        dest.writeString(imageUrl);
        dest.writeString(webUrl);
        dest.writeString(twitterUrl);
        dest.writeString(irc);
        dest.writeInt(sort_order);
        dest.writeString(playerUrl);
        dest.writeString(requestUrl);
        dest.writeString(streamUrl);
        dest.writeString(defaultStreamId);
        dest.writeList(streams);
    }

    public Station() {
    }

    private Station(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.shortcode = in.readString();
        this.genre = in.readString();
        this.category = in.readString();
        this.affiliation = in.readString();
        this.imageUrl = in.readString();
        this.webUrl = in.readString();
        this.twitterUrl = in.readString();
        this.irc = in.readString();
        this.sort_order = in.readInt();
        this.playerUrl = in.readString();
        this.requestUrl = in.readString();
        this.streamUrl = in.readString();
        this.defaultStreamId = in.readString();
        this.streams = new ArrayList<Stream>();
        in.readList(streams, null);
    }

    public static final Creator<Station> CREATOR = new Creator<Station>() {
        public Station createFromParcel(Parcel source) {
            return new Station(source);
        }

        public Station[] newArray(int size) {
            return new Station[size];
        }
    };
}

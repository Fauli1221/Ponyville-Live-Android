package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents all the properties of PonyvilleAPI show objects
 */
public class Show extends Entity {

    @SerializedName("name")
    public String name;
    @SerializedName("country")
    public String country;
    @SerializedName("description")
    public String description;
    @SerializedName("image_url")
    public String imageUrl;
    @SerializedName("banner_url")
    public String bannerUrl;
    @SerializedName("stations")
    public List<Station> stations;
    @SerializedName("is_adult")
    public boolean isAdult;
    @SerializedName("episodes")
    public List<Episode> episodes;
    @SerializedName("web_url")
    public String webUrl;
    @SerializedName("contact_email")
    public String contactEmail;
    @SerializedName("rss_url")
    public String rssUrl;
    @SerializedName("twitter_url")
    public String twitterUrl;
    @SerializedName("tumblr_url")
    public String tumblrUrl;
    @SerializedName("facebook_url")
    public String facebookUrl;
    @SerializedName("youtube_url")
    public String youtubeUrl;
    @SerializedName("soundcloud_url")
    public String soundcloudUrl;
    @SerializedName("deviantart_url")
    public String deviantartUrl;
    @SerializedName("livestream_url")
    public String livestreamUrl;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeString(country);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(bannerUrl);
        dest.writeList(stations);
        dest.writeByte((byte) (isAdult ? 1 : 0));
        dest.writeList(episodes);
        dest.writeString(webUrl);
        dest.writeString(contactEmail);
        dest.writeString(rssUrl);
        dest.writeString(twitterUrl);
        dest.writeString(tumblrUrl);
        dest.writeString(facebookUrl);
        dest.writeString(youtubeUrl);
        dest.writeString(soundcloudUrl);
        dest.writeString(deviantartUrl);
        dest.writeString(livestreamUrl);
    }

    public Show() {
    }

    private Show(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.country = in.readString();
        this.description = in.readString();
        this.imageUrl = in.readString();
        this.bannerUrl = in.readString();
        this.stations = new ArrayList<Station>();
        in.readList(stations, null);
        this.isAdult = in.readByte() != 0;
        this.episodes = new ArrayList<Episode>();
        in.readList(episodes, null);
        this.webUrl = in.readString();
        this.contactEmail = in.readString();
        this.rssUrl = in.readString();
        this.twitterUrl = in.readString();
        this.tumblrUrl = in.readString();
        this.facebookUrl = in.readString();
        this.youtubeUrl = in.readString();
        this.soundcloudUrl = in.readString();
        this.deviantartUrl = in.readString();
        this.livestreamUrl = in.readString();
    }

    public static final Creator<Show> CREATOR = new Creator<Show>() {
        public Show createFromParcel(Parcel source) {
            return new Show(source);
        }

        public Show[] newArray(int size) {
            return new Show[size];
        }
    };

}

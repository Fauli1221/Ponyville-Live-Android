package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Song extends Entity {
    public String text;
    public String artist;
    public String title;
    @SerializedName("image_url")
    public String imageUrl;
    public long created;
    @SerializedName("play_count")
    public int playCount;
    @SerializedName("last_played")
    public long lastPlayed;
    public int score;
    @SerializedName("sh_id")
    public int shID;
    @SerializedName("vote_urls")
    public VoteURLS voteUrls;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeLong(created);
        dest.writeInt(playCount);
        dest.writeLong(lastPlayed);
        dest.writeInt(score);
        dest.writeInt(shID);
        dest.writeParcelable(voteUrls, 0);
    }

    public Song() {
    }

    private Song(Parcel in) {
        this.id = in.readString();
        this.text = in.readString();
        this.artist = in.readString();
        this.title = in.readString();
        this.imageUrl = in.readString();
        this.created = in.readLong();
        this.playCount = in.readInt();
        this.lastPlayed = in.readLong();
        this.score = in.readInt();
        this.shID = in.readInt();
        this.voteUrls = in.readParcelable(null);
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    private class VoteURLS implements Parcelable {
        public String like;
        public String dislike;
        public String clearvote;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(like);
            dest.writeString(dislike);
            dest.writeString(clearvote);
        }

        public VoteURLS() {
        }

        private VoteURLS(Parcel in) {
            this.like = in.readString();
            this.dislike = in.readString();
            this.clearvote = in.readString();
        }

        public final Creator<VoteURLS> CREATOR = new Creator<VoteURLS>() {
            public VoteURLS createFromParcel(Parcel source) {
                return new VoteURLS(source);
            }

            public VoteURLS[] newArray(int size) {
                return new VoteURLS[size];
            }
        };
    }
}

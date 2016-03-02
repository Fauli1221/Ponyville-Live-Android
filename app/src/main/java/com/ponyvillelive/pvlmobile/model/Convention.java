package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tinker on 30/01/16.
 */
public class Convention extends Entity {
    @SerializedName("name")
    public String name;
    @SerializedName("location")
    public String location;
    @SerializedName("coverage")
    public String coverage;
    @SerializedName("coverage_details")
    public CoverageDetails coverageDetails;
    @SerializedName("date_range")
    public String dateRange;
    @SerializedName("start_date")
    public ConDate startDate;
    @SerializedName("end_date")
    public ConDate endDate;
    @SerializedName("web_url")
    public String webUrl;
    @SerializedName("image_url")
    public String imageUrl;
    @SerializedName("archives_count")
    public int archivesCount;
    @SerializedName("archives")
    public Archives archives;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(coverage);
        dest.writeParcelable(coverageDetails, 0);
        dest.writeString(dateRange);
        dest.writeParcelable(startDate, 0);
        dest.writeParcelable(endDate, 0);
        dest.writeString(webUrl);
        dest.writeString(imageUrl);
        dest.writeInt(archivesCount);
        dest.writeParcelable(archives, 0);
    }

    public Convention() {
    }

    private Convention(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.location = in.readString();
        this.coverage = in.readString();
        this.coverageDetails = in.readParcelable(null);
        this.dateRange = in.readString();
        this.startDate = in.readParcelable(null);
        this.endDate = in.readParcelable(null);
        this.webUrl = in.readString();
        this.imageUrl = in.readString();
        this.archivesCount = in.readInt();
        this.archives = in.readParcelable(null);
    }

    public static final Creator<Convention> CREATOR = new Creator<Convention>() {
        public Convention createFromParcel(Parcel source) {
            return new Convention(source);
        }

        public Convention[] newArray(int size) {
            return new Convention[size];
        }
    };

    public class CoverageDetails implements Parcelable {
        @SerializedName("text")
        public String text;
        @SerializedName("icon")
        public String icon;
        @SerializedName("short")
        public String shortCode;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
            dest.writeString(icon);
            dest.writeString(shortCode);
        }

        public CoverageDetails() {
        }

        private CoverageDetails(Parcel in) {
            this.text = in.readString();
            this.icon = in.readString();
            this.shortCode = in.readString();
        }

        public final Creator<CoverageDetails> CREATOR = new Creator<CoverageDetails>() {
            public CoverageDetails createFromParcel(Parcel source) {
                return new CoverageDetails(source);
            }

            public CoverageDetails[] newArray(int size) {
                return new CoverageDetails[size];
            }
        };
    }

    public class ConDate implements Parcelable {
        @SerializedName("date")
        public String date;
        @SerializedName("timezone_type")
        public int timezoneType;
        @SerializedName("timezone")
        public String timezone;

        @Override
        public String toString() {
            return date;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(date);
            dest.writeInt(timezoneType);
            dest.writeString(timezone);
        }

        public ConDate() {
        }

        private ConDate(Parcel in) {
            this.date = in.readString();
            this.timezoneType = in.readInt();
            this.timezone = in.readString();
        }

        public final Creator<ConDate> CREATOR = new Creator<ConDate>() {
            public ConDate createFromParcel(Parcel source) {
                return new ConDate(source);
            }

            public ConDate[] newArray(int size) {
                return new ConDate[size];
            }
        };
    }

    public class Archives implements Parcelable {

        @SerializedName("videos")
        public List<Video> videos;
        @SerializedName("sources")
        public List<Source> sources;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeList(videos);
            dest.writeList(sources);
        }

        public Archives() {
        }

        private Archives(Parcel in) {
            this.videos = new ArrayList<>();
            in.readList(videos, null);
            this.sources = new ArrayList<>();
            in.readList(videos, null);
        }

        public final Creator<Archives> CREATOR = new Creator<Archives>() {
            public Archives createFromParcel(Parcel source) {
                return new Archives(source);
            }

            public Archives[] newArray(int size) {
                return new Archives[size];
            }
        };

    }

    public class Source implements Parcelable {

        @SerializedName("id")
        public String id;
        @SerializedName("convention_id")
        public String conventionId;
        @SerializedName("playlist_id")
        public String playlistId;
        @SerializedName("type")
        public String type;
        @SerializedName("folder")
        public String folder;
        @SerializedName("name")
        public String name;
        @SerializedName("description")
        public String description;
        @SerializedName("web_url")
        public String webUrl;
        @SerializedName("thumbnail_url")
        public String thumbnailUrl;
        @SerializedName("created_at")
        public String createdAt;
        @SerializedName("synchronized_at")
        public String synchronizedAt;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(conventionId);
            dest.writeString(playlistId);
            dest.writeString(type);
            dest.writeString(folder);
            dest.writeString(name);
            dest.writeString(description);
            dest.writeString(webUrl);
            dest.writeString(thumbnailUrl);
            dest.writeString(createdAt);
            dest.writeString(synchronizedAt);
        }

        public Source() {
        }

        private Source(Parcel in) {
            this.id = in.readString();
            this.conventionId = in.readString();
            this.playlistId = in.readString();
            this.type = in.readString();
            this.folder = in.readString();
            this.name = in.readString();
            this.description = in.readString();
            this.webUrl = in.readString();
            this.thumbnailUrl = in.readString();
            this.createdAt = in.readString();
            this.synchronizedAt = in.readString();
        }

        public final Creator<Source> CREATOR = new Creator<Source>() {
            public Source createFromParcel(Parcel source) {
                return new Source(source);
            }

            public Source[] newArray(int size) {
                return new Source[size];
            }
        };

    }

    public class Video implements Parcelable {

        @SerializedName("id")
        public String id;
        @SerializedName("convention_id")
        public String conventionId;
        @SerializedName("playlist_id")
        public String playlistId;
        @SerializedName("type")
        public String type;
        @SerializedName("folder")
        public String folder;
        @SerializedName("name")
        public String name;
        @SerializedName("description")
        public String description;
        @SerializedName("web_url")
        public String webUrl;
        @SerializedName("thumbnail_url")
        public String thumbnailUrl;
        @SerializedName("created_at")
        public String createdAt;
        @SerializedName("synchronized_at")
        public String synchronizedAt;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(conventionId);
            dest.writeString(playlistId);
            dest.writeString(type);
            dest.writeString(folder);
            dest.writeString(name);
            dest.writeString(description);
            dest.writeString(webUrl);
            dest.writeString(thumbnailUrl);
            dest.writeString(createdAt);
            dest.writeString(synchronizedAt);
        }

        public Video() {
        }

        private Video(Parcel in) {
            this.id = in.readString();
            this.conventionId = in.readString();
            this.playlistId = in.readString();
            this.type = in.readString();
            this.folder = in.readString();
            this.name = in.readString();
            this.description = in.readString();
            this.webUrl = in.readString();
            this.thumbnailUrl = in.readString();
            this.createdAt = in.readString();
            this.synchronizedAt = in.readString();
        }

        public final Creator<Video> CREATOR = new Creator<Video>() {
            public Video createFromParcel(Parcel source) {
                return new Video(source);
            }

            public Video[] newArray(int size) {
                return new Video[size];
            }
        };
    }

}
package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;

/**
 * Created by tinker on 30/01/16.
 */
public class EventUpcoming extends Entity {
    public int station_id;
    public String guid;
    public long start_time;
    public long end_time;
    public boolean is_all_day;
    public String title;
    public String location;
    public String body;
    public String banner_url;
    public String web_url;
    public String range;
    public String image_url;
    public int minutes_until;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(station_id);
        dest.writeString(guid);
        dest.writeLong(start_time);
        dest.writeLong(end_time);
        dest.writeByte((byte) (is_all_day ? 1 : 0));
        dest.writeString(title);
        dest.writeString(location);
        dest.writeString(body);
        dest.writeString(banner_url);
        dest.writeString(web_url);
        dest.writeString(range);
        dest.writeString(image_url);
        dest.writeInt(minutes_until);
    }

    public EventUpcoming() {
    }

    private EventUpcoming(Parcel in) {
        this.id = in.readString();
        this.station_id = in.readInt();
        this.guid = in.readString();
        this.start_time = in.readLong();
        this.end_time = in.readLong();
        this.is_all_day = in.readByte() != 0;
        this.title = in.readString();
        this.location = in.readString();
        this.body = in.readString();
        this.banner_url = in.readString();
        this.web_url = in.readString();
        this.range = in.readString();
        this.image_url = in.readString();
        this.minutes_until = in.readInt();
    }

    public static final Creator<EventUpcoming> CREATOR = new Creator<EventUpcoming>() {
        public EventUpcoming createFromParcel(Parcel source) {
            return new EventUpcoming(source);
        }

        public EventUpcoming[] newArray(int size) {
            return new EventUpcoming[size];
        }
    };
}

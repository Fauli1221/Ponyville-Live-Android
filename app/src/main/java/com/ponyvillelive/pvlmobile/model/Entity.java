package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Presents common properties of all PonyvilleAPI entities
 */
public class Entity implements Parcelable {

    public String id;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
    }

    public Entity() {
    }

    private Entity(Parcel in) {
        this.id = in.readString();
    }

    public static final Creator<Entity> CREATOR = new Creator<Entity>() {
        public Entity createFromParcel(Parcel source) {
            return new Entity(source);
        }

        public Entity[] newArray(int size) {
            return new Entity[size];
        }
    };

}

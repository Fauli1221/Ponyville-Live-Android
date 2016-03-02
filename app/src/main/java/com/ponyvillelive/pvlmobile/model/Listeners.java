package com.ponyvillelive.pvlmobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tinker on 30/01/16.
 */
public class Listeners implements Parcelable {
    public int current;
    public int unique;
    public int total;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(current);
        dest.writeInt(unique);
        dest.writeInt(total);
    }

    public Listeners() {
    }

    private Listeners(Parcel in) {
        this.current = in.readInt();
        this.unique = in.readInt();
        this.total = in.readInt();
    }

    public final Creator<Listeners> CREATOR = new Creator<Listeners>() {
        public Listeners createFromParcel(Parcel source) {
            return new Listeners(source);
        }

        public Listeners[] newArray(int size) {
            return new Listeners[size];
        }
    };
}

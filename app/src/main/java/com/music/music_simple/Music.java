package com.music.music_simple;

import android.os.Parcel;
import android.os.Parcelable;
//音乐实体类
public class Music implements Parcelable {
    public int id;
    public String name;
    public String path;

    public Music(String name, String path, String singer, String duration) {
        this.name = name;
        this.path = path;
        this.singer = singer;
        this.duration = duration;
    }

    public String singer;
    public String duration;

    public Music() {
    }

    protected Music(Parcel in) {
        id = in.readInt();
        name = in.readString();
        path = in.readString();
        singer = in.readString();
        duration = in.readString();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(singer);
        dest.writeString(duration);
    }
}

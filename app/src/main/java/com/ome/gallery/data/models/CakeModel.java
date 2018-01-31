package com.ome.gallery.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Riad on 07/06/2016.
 */

public class CakeModel implements Parcelable {

    public static final Creator<CakeModel> CREATOR
            = new Creator<CakeModel>() {
        public CakeModel createFromParcel(Parcel in) {
            return new CakeModel(in);
        }

        public CakeModel[] newArray(int size) {
            return new CakeModel[size];
        }
    };

    String title;
    String desc;
    String imageUrl;

    public CakeModel() {/**/}

    public CakeModel(JSONObject object) throws JSONException {
        title = object.getString("title");
        desc = object.getString("desc");
        imageUrl = object.getString("image");
    }

    public CakeModel(Parcel in) {
        title = in.readString();
        desc = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeString(imageUrl);
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
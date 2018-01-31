package com.ome.gallery.web;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import okhttp3.Cookie;

/**
 * Created by rgozim on 14/11/2017.
 */

public class SerializableCookie implements Serializable {

    private static final String TAG = SerializableCookie.class.getSimpleName();

    private transient Cookie mCookie;

    public SerializableCookie(Cookie cookie) {
        this.mCookie = cookie;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeBytes(mCookie.name());
        oos.writeBytes(mCookie.value());
        oos.writeBytes(mCookie.path());
        oos.writeBytes(mCookie.domain());
        oos.writeBoolean(mCookie.hostOnly());
        oos.writeBoolean(mCookie.httpOnly());
        oos.writeBoolean(mCookie.secure());
        oos.writeLong(mCookie.persistent() ? mCookie.expiresAt() : -1);;
    }

    public Cookie getCookie() {
        return mCookie;
    }

    public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Cookie.Builder builder = new Cookie.Builder()
                .name((String) ois.readObject())
                .value((String) ois.readObject())
                .path((String) ois.readObject());

        long expiresAt = ois.readLong();
        if (expiresAt != -1) {
            builder.expiresAt(expiresAt);
        }

        final String domain = (String) ois.readObject();
        builder.domain(domain);

        // Host only
        if (ois.readBoolean()) {
            builder.hostOnlyDomain(domain);
        }

        // Http only
        if (ois.readBoolean()) {
            builder.httpOnly();
        }

        // Secure
        if (ois.readBoolean()) {
            builder.secure();
        }

        mCookie = builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SerializableCookie that = (SerializableCookie) o;

        return mCookie != null ? mCookie.equals(that.mCookie) : that.mCookie == null;
    }

    @Override
    public int hashCode() {
        return mCookie != null ? mCookie.hashCode() : 0;
    }
}

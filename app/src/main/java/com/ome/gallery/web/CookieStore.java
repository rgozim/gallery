package com.ome.gallery.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;


public class CookieStore implements CookieJar {
    private static final String TAG = CookieStore.class.getName();
    private final Set<Cookie> mCookies = new HashSet<>();

    // Place to store cookies
    private SharedPreferences mSharedPrefs;

    public CookieStore(Context context) {
        mSharedPrefs = context.getSharedPreferences("Cookies",
                Context.MODE_PRIVATE);

        // Load any existing cookies from disk and place them in set
        for (Map.Entry<String, ?> entry : mSharedPrefs.getAll().entrySet()) {
            String serializedCookie = (String) entry.getValue();
            Cookie cookie = decode(serializedCookie);
            if (cookie != null) {
                mCookies.add(cookie);
            }
        }
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        // Save cookies to the store
        if (mCookies.addAll(cookies)) {
            // Obtain a list of cookies to persist
            List<Cookie> persistentCookies = filterPersistentCookies(cookies);
            if (persistentCookies.isEmpty()) {
                return;
            }

            // Save cookies to persistent store
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            for (Cookie cookie : persistentCookies) {
                editor.putString(createCookieKey(cookie), encode(cookie));
            }
            editor.apply();
        }
    }

    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        ArrayList<Cookie> validCookies = new ArrayList<>();
        for (Iterator<Cookie> it = mCookies.iterator(); it.hasNext(); ) {
            Cookie cookie = it.next();
            if (isExpired(cookie)) {
                mSharedPrefs.edit()
                        .remove(createCookieKey(cookie))
                        .apply();
                it.remove();
            } else {
                validCookies.add(cookie);
            }
        }

        return validCookies;
    }

    private static List<Cookie> filterPersistentCookies(List<Cookie> cookies) {
        List<Cookie> persistentCookies = new ArrayList<>();
        for (Cookie cookie : cookies) {
            if (cookie.persistent()) {
                persistentCookies.add(cookie);
            }
        }
        return persistentCookies;
    }

    private boolean isExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    private String encode(Cookie cookie) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(cookie);
            return Utils.bytesToHex(bos.toByteArray());
        } catch (IOException e) {
            Log.d(TAG, "IOException in encodeCookie", e);
            return null;
        }
    }

    private Cookie decode(String input) {
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.hexToBytes(input));
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ((SerializableCookie) ois.readObject()).getCookie();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    //Print the values of cookies - Useful for testing
    private void LogCookie(Cookie cookie) {
        Log.i(TAG, "String: " + cookie.toString());
        Log.i(TAG, "Expires: " + cookie.expiresAt());
        Log.i(TAG, "Hash: " + cookie.hashCode());
        Log.i(TAG, "Path: " + cookie.path());
        Log.i(TAG, "Domain: " + cookie.domain());
        Log.i(TAG, "Name: " + cookie.name());
        Log.i(TAG, "Value: " + cookie.value());
    }

    private static String createCookieKey(Cookie cookie) {
        return (cookie.secure() ? "https" : "http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name();
    }
}

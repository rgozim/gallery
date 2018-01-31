package com.ome.gallery.data;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;


import com.ome.gallery.data.models.CakeModel;
import com.ome.gallery.ui.StreamUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple loader class for performing the network
 * operation to load the list of cakes.
 */
public class LoadCakesLoader extends AsyncTaskLoader<List<CakeModel>> {

    private static final String TAG = LoadCakesLoader.class.getSimpleName();

    private static final String JSON_URL = "https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/" +
            "raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json";

    private List<CakeModel> mData;

    public LoadCakesLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    public List<CakeModel> loadInBackground() {
        String text = null;
        try {
            text = loadUrlData();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
        }

        // The list models to return
        List<CakeModel> models = null;

        if (!TextUtils.isEmpty(text)) {
            try {
                JSONArray jsonArray = new JSONArray(text);
                int N = jsonArray.length();
                models = new ArrayList<>(N);
                for (int i = 0; i < N; ++i) {
                    CakeModel model = new CakeModel(jsonArray.getJSONObject(i));
                    models.add(model);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        if (models == null) {
            models = Collections.emptyList();
        }

        return models;
    }

    /* Runs on the UI thread */

    @Override
    public void deliverResult(List<CakeModel> data) {
        super.deliverResult(data);

        if (isReset()) {
            // An async query came in while the loader is stopped
            return;
        }

        // Set data internally as a cache
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mData != null && !mData.isEmpty()) {
            mData.clear();
        }
        mData = null;
    }

    private String loadUrlData() throws IOException, InterruptedException {
        URL url = new URL(JSON_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        InputStream inputStream = null;
        try {
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();

            try {
                // Read data from workstation
                inputStream = conn.getInputStream();
            } catch (IOException e) {
                // Read the error from the workstation
                inputStream = conn.getErrorStream();
            }

            String responseMessage = conn.getResponseMessage();
            int response = conn.getResponseCode();
            Log.d(TAG, "Http response message: " + responseMessage);
            Log.d(TAG, "The response code is: " + response);

            byte[] responseData = new byte[0];

            // Check header for HTTP compression. If identity found then we will know the
            // length of the stream. (http://en.wikipedia.org/wiki/HTTP_compression)
            if (inputStream != null) {
                final String compression = conn.getRequestProperty("Accept-Encoding");
                if (compression != null && compression.equals("identity")) {
                    String contentLength = conn.getRequestProperty("Content-Length");
                    if (!TextUtils.isEmpty(contentLength)) {
                        responseData = StreamUtils.readKnownFully(inputStream, Integer.valueOf(contentLength));
                    } else {
                        responseData = StreamUtils.readUnknownFully(inputStream);
                    }
                } else {
                    responseData = StreamUtils.readUnknownFully(inputStream);
                }
            }

            // Read in charset of HTTP content.
            String charset = parseCharset(conn.getRequestProperty("Content-Type"));

            // Convert byte array to appropriate encoded string.
            return new String(responseData, charset);
        } finally {
            // Close the input stream if it exists.
            StreamUtils.close(inputStream);

            // Disconnect the connection
            conn.disconnect();
        }
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(String contentType) {
        if (contentType != null) {
            String[] params = contentType.split(",");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }
        return "UTF-8";
    }
}

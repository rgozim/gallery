package com.ome.gallery.ui;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Riad on 07/06/2016.
 */

public class StreamUtils {

    private static final String TAG = StreamUtils.class.getSimpleName();

    public static byte[] readKnownFully(InputStream inputStream, int length) throws IOException, InterruptedException {
        // Allocate a buffer big enough to hold data.
        byte[] data = new byte[length];
        // Keep tack of how many bytes are actually read in each iteration.
        int bytesRead = 0;
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            int read = inputStream.read(data, bytesRead, length - bytesRead);
            if (read == -1) {
                break;
            }
            bytesRead += read;
        }
        return data;
    }

    public static byte[] readUnknownFully(InputStream inputStream) throws IOException, InterruptedException {
        ByteArrayOutputStream byteOStream = new ByteArrayOutputStream(16384);

        // Allocate a buffer big enough to hold data.
        byte[] data = new byte[16384];

        // Read into our array
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            int read = inputStream.read(data);
            if (read == -1) {
                break;
            }

            // Copy the array to the output stream. Note: it is
            // important to only copy the number bytes read in last read.
            byteOStream.write(data, 0, read);
        }

        // Return the byte array.
        return byteOStream.toByteArray();
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

}

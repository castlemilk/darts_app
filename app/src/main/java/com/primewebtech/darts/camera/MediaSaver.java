package com.primewebtech.darts.camera;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by benebsworth on 2/5/17.
 */

public class MediaSaver extends Thread {

    private static final int SAVE_QUEUE_LIMIT = 3;
    private static final String TAG = "MediaSaver";
    private ArrayList<SaveRequest> mQueue;
    private boolean mStop;
    private ContentResolver mContentResolver;
    private Context mContext;


    public interface OnMediaSavedListener {
        void onMediaSaved(Uri uri);
    }

    public MediaSaver(ContentResolver resolver) {
        mContentResolver = resolver;
        mQueue = new ArrayList<SaveRequest>();
        start();
    }
    // Runs in main thread
    public synchronized boolean queueFull() {
        return (mQueue.size() >= SAVE_QUEUE_LIMIT);
    }
    // Runs in main thread
    public void addImage(Context ctx, final byte[] data, Bitmap logo, Bitmap pin, String title, String score_type,
                         String score, long date, Location loc,
                         int width, int height, int orientation, OnMediaSavedListener l) {
        mContext = ctx;
        SaveRequest r = new SaveRequest();
        r.data = data;
        r.logo = logo;
        r.pin = pin;
        r.score = score;
        r.score_type = score_type;
        r.date = date;
        r.title = title;
        r.loc = (loc == null) ? null : new Location(loc);  // make a copy
        r.width = width;
        r.height = height;
        r.orientation = orientation;
        r.listener = l;
        synchronized (this) {
            while (mQueue.size() >= SAVE_QUEUE_LIMIT) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    // ignore.
                }
            }
            mQueue.add(r);
            notifyAll();  // Tell saver thread there is new work to do.
        }
    }


    // Runs in saver thread
    @Override
    public void run() {
        while (true) {
            SaveRequest r;
            synchronized (this) {
                if (mQueue.isEmpty()) {
                    notifyAll();  // notify main thread in waitDone
                    // Note that we can only stop after we saved all images
                    // in the queue.
                    if (mStop) break;
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // ignore.
                    }
                    continue;
                }
                if (mStop) break;
                r = mQueue.remove(0);
                notifyAll();  // the main thread may wait in addImage
            }
            final long t0 = System.currentTimeMillis();
            Uri uri = storeImage(Util.BitMapToByteArray(Util.combineElements(mContext, r.data, r.logo, r.pin, r.score)), r.title, r.score_type, r.score, r.date, r.loc, r.width, r.height,
                    r.orientation);
            Log.d(TAG, String.format("storeImage took %dms", System.currentTimeMillis() - t0));
            r.listener.onMediaSaved(uri);
        }
        if (!mQueue.isEmpty()) {
            Log.e(TAG, "Media saver thread stopped with " + mQueue.size() + " images unsaved");
            mQueue.clear();
        }
    }

    // Runs in main thread
    public void finish() {
        synchronized (this) {
            mStop = true;
            notifyAll();
        }
    }

    // Runs in saver thread
    private Uri storeImage(final byte[] data, String title, String score_type, String score,
                           long date, Location loc, int width, int height, int orientation) {
        Uri uri = Storage.addImage(mContentResolver, title, score_type, score, date, loc,
                orientation, data, width, height);
        return uri;
    }

    // Each SaveRequest remembers the data needed to save an image.
    private static class SaveRequest {
        byte[] data;
        String title;
        long date;
        Bitmap logo;
        Bitmap pin;
        String score;
        String score_type;
        Location loc;
        int width, height;
        int orientation;
        OnMediaSavedListener listener;
    }

}

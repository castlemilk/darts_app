package com.primewebtech.darts.camera;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by benebsworth on 2/5/17.
 */

public class PhotoHandler implements Camera.PictureCallback {
    private static final String TAG = PhotoHandler.class.getSimpleName();
    private final Context context;
    private Location mLocation;
    private boolean mPaused;
    private long mJpegPictureCallbackTime;
    public long mCaptureStartTime;
    private CameraActivity mActivity;
    private long mOnResumeTime;
    private byte[] mJpegImageData;
    private int mJpegRotation;
    private Camera.Parameters mParameters;
    private MediaSaver mMediaSaver;
    private NamedImages mNamedImages;
    private final Handler mHandler = new MainHandler();

    private static final int SETUP_PREVIEW = 1;
    private static final int FIRST_TIME_INIT = 2;
    private static final int UPDATE_SECURE_ALBUM_ITEM = 13;
    public PhotoHandler(Context context) {
        this.context = context;
    }

    private MediaSaver.OnMediaSavedListener mOnMediaSavedListener = new MediaSaver.OnMediaSavedListener() {
        @Override
        public void onMediaSaved(Uri uri) {
            if (uri != null) {
                mHandler.obtainMessage(UPDATE_SECURE_ALBUM_ITEM, uri).sendToTarget();
                Util.broadcastNewPicture(mActivity, uri);
            }
        }
    };

    @Override
    public void onPictureTaken(byte[] jpegData, Camera camera) {
        mJpegPictureCallbackTime = System.currentTimeMillis();


//
        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(jpegData);
            fos.close();
            Toast.makeText(context, "New Image saved:" + photoFile,
                    Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Log.d(TAG, "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
        Intent reviewPictureIntent = new Intent(this.context, ReviewActivity.class);
        reviewPictureIntent.putExtra("filepath", pictureFile);
        this.context.startActivity(reviewPictureIntent);


    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "CameraAPIDemo");
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SETUP_PREVIEW: {
//                    setupPreview();
                    break;
                }
                case FIRST_TIME_INIT: {
//                    initializeFirstTime();
                    break;
                }
                case UPDATE_SECURE_ALBUM_ITEM: {
//                    mActivity.addSecureAlbumItemIfNeeded(false, (Uri) msg.obj);
                    break;
                }
            }
        }
    }


    private static class NamedImages {
        private ArrayList<NamedEntity> mQueue;
        private boolean mStop;
        private NamedEntity mNamedEntity;
        public NamedImages() {
            mQueue = new ArrayList<NamedEntity>();
        }
        public void nameNewImage(ContentResolver resolver, long date, String score) {
            NamedEntity r = new NamedEntity();
            r.title = Util.createJpegName(date, score);
            r.date = date;
            mQueue.add(r);
        }
        public String getTitle() {
            if (mQueue.isEmpty()) {
                mNamedEntity = null;
                return null;
            }
            mNamedEntity = mQueue.get(0);
            mQueue.remove(0);
            return mNamedEntity.title;
        }
        // Must be called after getTitle().
        public long getDate() {
            if (mNamedEntity == null) return -1;
            return mNamedEntity.date;
        }
        private static class NamedEntity {
            String title;
            long date;
        }
    }


}

package com.primewebtech.darts.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by benebsworth on 2/5/17.
 */

public class PhotoHandler implements Camera.PictureCallback {
    private static final String TAG = PhotoHandler.class.getSimpleName();
    private final Context context;
    Location mLocation;
    private boolean mPaused;
    private long mJpegPictureCallbackTime;
    public long mCaptureStartTime;
    private long mOnResumeTime;
    private byte[] mJpegImageData;
    private int mJpegRotation;
    private Camera.Parameters mParameters;
    private MediaSaver mMediaSaver;
    private NamedImages mNamedImages;
    public PhotoHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] jpegData, Camera camera) {

        Location mLocation;





        mJpegPictureCallbackTime = System.currentTimeMillis();

//        File pictureFileDir = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
//
//            Log.d(TAG, "Can't create directory to save image.");
//            Toast.makeText(context, "Can't create directory to save image.",
//                    Toast.LENGTH_LONG).show();
//            return;
//
//        }
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
//        String date = dateFormat.format(new Date());
//        String photoFile = "Picture_" + date + ".jpg";
//
//        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        // Calculate the width and the height of the jpeg.
        Size s = mParameters.getPictureSize();
        ExifInterface exif = null;
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        int width, height;
        if ((mJpegRotation + orientation) % 180 == 0) {
            width = s.width;
            height = s.height;
        } else {
            width = s.height;
            height = s.width;
        }
        String title = mNamedImages.getTitle();
        long date = mNamedImages.getDate();
        if (title == null) {
            Log.e(TAG, "Unbalanced name/data pair");
        } else {
            if (date == -1) date = mCaptureStartTime;
            mMediaSaver.addImage(jpegData, title, date, mLocation, width, height,
                    orientation, mOnMediaSavedListener);
        }

    }


}

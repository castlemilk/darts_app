package com.primewebtech.darts.camera;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by benebsworth on 2/5/17.
 */

public class Storage {
    private static final String TAG = "CameraStorage";
    public static final String DCIM =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    public static final String DIRECTORY_PICTURES = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).toString();
    public static final String DIRECTORY = DCIM + "/Camera";
    public static final String APP_DIRECTORY = DIRECTORY_PICTURES + "/Darts";
    public static final String BUCKET_ID =
            String.valueOf(DIRECTORY.toLowerCase().hashCode());
    public static final long UNAVAILABLE = -1L;
    public static final long PREPARING = -2L;
    public static final long UNKNOWN_SIZE = -3L;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final long LOW_STORAGE_THRESHOLD= 50000000;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setImageSize(ContentValues values, int width, int height) {
        // The two fields are available since ICS but got published in JB
            values.put(MediaStore.MediaColumns.WIDTH, width);
            values.put(MediaStore.MediaColumns.HEIGHT, height);
    }

    public static void writeFile(String path, byte[] data) {
        final long t0 = System.currentTimeMillis();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);

        } catch (Exception e) {
            Log.e(TAG, "Failed to write data", e);
        } finally {
            try {
                out.close();
                Log.d(TAG, String.format("wrote file in %dms", System.currentTimeMillis() - t0));
            } catch (Exception e) {
            }
        }
    }

    // Save the image and add it to media store.
    public static Uri addImage(ContentResolver resolver, String title,
                                long date, Location location, int orientation, byte[] jpeg,
                                int width, int height) {
        // Save the image.
//        String path = generateFilepath(title);
        final long t1 = System.currentTimeMillis();
        final long t0 = System.currentTimeMillis();
        String path = getOutputMediaFile(MEDIA_TYPE_IMAGE).getPath();
        Log.d(TAG, String.format("getOutputMediafile took %dms", System.currentTimeMillis() - t0));
        Log.d(TAG, "addImage:path:"+path);
        writeFile(path, jpeg);
        Log.d(TAG, String.format("writeFile took %dms", System.currentTimeMillis() - t0));
        return addImage(resolver, title, date, location, orientation,
                jpeg.length, path, width, height);
    }
//    public static Uri addImage(ContentResolver resolver, String title,
//                               long date, Location location, int orientation, Bitmap jpeg,
//                               int width, int height) {
//        // Save the image.
////        String path = generateFilepath(title);
//        final long t0 = System.currentTimeMillis();
//        Util.BitMapToByteArray(jpeg);
//        byte[] jpegBytes = Util.BitMapToByteArray(jpeg);
//        String path = getOutputMediaFile(MEDIA_TYPE_IMAGE).getPath();
//        writeFile(path, Util.BitMapToByteArray(jpeg));
//        Log.d(TAG, String.format("addImageBitmap took %dms", System.currentTimeMillis() - t0));
//        return addImage(resolver, title, date, location, orientation,
//                jpegBytes.length, path, width, height);
//    }

    // Add the image to media store.
    public static Uri addImage(ContentResolver resolver, String title,
                               long date, Location location, int orientation, int jpegLength,
                               String path, int width, int height) {
        // Insert into MediaStore.
        final long t0 = System.currentTimeMillis();
        ContentValues values = new ContentValues(9);
        values.put(ImageColumns.TITLE, title);
        values.put(ImageColumns.DISPLAY_NAME, title + ".jpg");
        values.put(ImageColumns.DATE_TAKEN, date);
        values.put(ImageColumns.MIME_TYPE, "image/jpeg");
        // Clockwise rotation in degrees. 0, 90, 180, or 270.
        values.put(ImageColumns.ORIENTATION, orientation);
        values.put(ImageColumns.DATA, path);
        values.put(ImageColumns.SIZE, jpegLength);
        setImageSize(values, width, height);
        if (location != null) {
            values.put(ImageColumns.LATITUDE, location.getLatitude());
            values.put(ImageColumns.LONGITUDE, location.getLongitude());
        }
        Uri uri = null;
        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Log.d(TAG, String.format("addImage took %dms", System.currentTimeMillis() - t0));
        } catch (Throwable th)  {
            // This can happen when the external volume is already mounted, but
            // MediaScanner has not notify MediaProvider to add that volume.
            // The picture is still safe and MediaScanner will find it and
            // insert it into MediaProvider. The only problem is that the user
            // cannot click the thumbnail to review the picture.
            Log.e(TAG, "Failed to write MediaStore" + th);
        }
        return uri;
    }
    public static void deleteImage(ContentResolver resolver, Uri uri) {
        try {
            resolver.delete(uri, null, null);
        } catch (Throwable th) {
            Log.e(TAG, "Failed to delete image: " + uri);
        }
    }
    public static long getAvailableSpace() {
        String state = Environment.getExternalStorageState();
        Log.d(TAG, "External storage state=" + state);
        if (Environment.MEDIA_CHECKING.equals(state)) {
            return PREPARING;
        }
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return UNAVAILABLE;
        }
        File dir = new File(DIRECTORY);
        dir.mkdirs();
        if (!dir.isDirectory() || !dir.canWrite()) {
            return UNAVAILABLE;
        }
        try {
            StatFs stat = new StatFs(DIRECTORY);
            return stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {
            Log.i(TAG, "Fail to access external storage", e);
        }
        return UNKNOWN_SIZE;
    }

    public static String generateFilepath(String title) {
        return APP_DIRECTORY + '/' + title + ".jpg";
    }
    /**
     * OSX requires plugged-in USB storage to have path /DCIM/NNNAAAAA to be
     * imported. This is a temporary fix for bug#1655552.
     */
    public static void ensureOSXCompatible() {
        File nnnAAAAA = new File(DCIM, "100ANDRO");
        if (!(nnnAAAAA.exists() || nnnAAAAA.mkdirs())) {
            Log.e(TAG, "Failed to create " + nnnAAAAA.getPath());
        }
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Darts");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Darts", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}

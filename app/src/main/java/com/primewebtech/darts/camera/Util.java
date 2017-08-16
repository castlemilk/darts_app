package com.primewebtech.darts.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.primewebtech.darts.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by benebsworth on 3/5/17.
 */

public class Util {

    private static final String TAG = "Util";
    private static ImageFileNamer sImageFileNamer;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static void initialize(Context context) {
        sImageFileNamer = new ImageFileNamer(
                context.getString(R.string.image_file_name_format));
    }

    public static String createJpegName(long dateTaken, String score, String scoreType) {
        synchronized (sImageFileNamer) {
            return sImageFileNamer.generateName(dateTaken, score, scoreType);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    /**
     * Core function which is resposinble for the combining/merging of all assets required for the
     * darts app. We have some very finely tuned float values within the function which represent
     * particular offsets required to move the assets to a desired position within the canvas.
     * Some of these variables include:
     * pictureWidth - Width of photo original taken by camera
     * pictureHeight - Height of original photo taken by camera
     * pinSize - size of the pinboard (is a square)
     * logoWidth - width of the darts logo placed next to pinboard
     * logoHeight - height of the darts logo placed next to pinboard
     *
     * @param picture
     * @param logo
     * @param pin
     * @param score
     * @return
     */

    public static Bitmap combineElements(Context mContext, byte[] picture, Bitmap logo, Bitmap pin, String score) {
        final long t0 = System.currentTimeMillis();

        Bitmap pictureImg = BitmapFactory.decodeByteArray(picture, 0, picture.length);
//        return pictureImg;
        Log.d(TAG, String.format("created bitmap in %dms", System.currentTimeMillis() - t0));
        final long t1 = System.currentTimeMillis();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap rotatedImg = Bitmap.createBitmap(pictureImg, 0, 0, pictureImg.getWidth(), pictureImg.getHeight(), matrix, true);
        Log.d(TAG, String.format("rotated img in %dms", System.currentTimeMillis() - t1));
        final long t2 = System.currentTimeMillis();
        Bitmap combinedImg = null;

        int pictureWidth = rotatedImg.getWidth();
        int pictureHeight = rotatedImg.getHeight();
        Double pinSize = pictureWidth * 0.3;
        int textSize = (int) (pictureHeight * 0.06f);
        int pinSizeInt = pinSize.intValue();
        Double logoWidth = pictureWidth * 0.37;
        Double logoHeight = pictureHeight * 0.13;
        int logoSizeIntWidth = logoWidth.intValue();

        int logoSizeIntHeight = logoHeight.intValue();
        float marginBottom = pinSizeInt + pictureWidth * 0.05f;
        float logoFloatRight = pictureWidth * 0.05f;
        float margin = pictureWidth * 0.05f;
        float logoFloatTop = pictureHeight - logoSizeIntHeight - margin * 0.4f;
        float pinFloatLeft;
        float pinFloatTop;
        if (score.contains("RH")) {
            pinFloatLeft = pictureWidth - pinSizeInt * 0.85f - margin;
            pinFloatTop = pictureHeight - pinSizeInt * 1.1f - margin;
        } else {
            pinFloatLeft = pictureWidth - pinSizeInt * 0.85f - margin;
            pinFloatTop = pictureHeight - pinSizeInt * 0.9f - margin;
        }



        Log.d("bytes:pictureWidth", Integer.toString(pictureWidth));
        Log.d("bytes:pictureHeight", Integer.toString(pictureHeight));
        Log.d("bytes:logoSizeWidth", Integer.toString(logoSizeIntWidth));
        Log.d("bytes:logoSizeHeight", Integer.toString(logoSizeIntHeight));
        Log.d("bytes:pinSize", Integer.toString(pinSizeInt));
        Log.d(TAG, ":logo:Height:"+logo.getHeight());
        Log.d(TAG, ":logo:Width:"+logo.getWidth());
        combinedImg = Bitmap.createBitmap(pictureWidth, pictureHeight, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(combinedImg);
        comboImage.drawBitmap(rotatedImg, 0f, 0f, null);
        comboImage.drawBitmap(BITMAP_RESIZER(logo, logoSizeIntWidth, logoSizeIntHeight), logoFloatRight, logoFloatTop, null);
        comboImage.drawBitmap(BITMAP_RESIZER(pin, pinSizeInt, pinSizeInt), pinFloatLeft, pinFloatTop, null);
        Typeface tf_viewpager = Typeface.createFromAsset(mContext.getAssets(), "fonts/arlrbd.ttf");
        if (score != "RH") {
            comboImage.save();
            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(textSize);
            textPaint.setColor(Color.BLACK);
            Rect bounds = new Rect();
            textPaint.getTextBounds(String.valueOf(score), 0, String.valueOf(score).length(), bounds);
            textPaint.setTypeface(tf_viewpager);
            int textHeight  = bounds.height();
            Log.d("bytes:textHeight", Integer.toString(textHeight));
            Log.d("bytes:textSize", Integer.toString(textSize));
            int textPositionHeight = (int) (pinFloatTop +
                    pinSizeInt / 2 - (textHeight/1.25));
            StaticLayout staticLayout = new StaticLayout(String.valueOf(score),
                    textPaint, pinSize.intValue(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0, false);
            comboImage.translate(pinFloatLeft, textPositionHeight);
            staticLayout.draw(comboImage);
            comboImage.restore();
        }




        Log.d(TAG, String.format("addSelectedIcon took %dms", System.currentTimeMillis() - t2));

        return combinedImg;
    }

    public static Bitmap BITMAP_RESIZER(Bitmap bitmap,int newWidth,int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;

    }


    private static class ImageFileNamer {
        private SimpleDateFormat mFormat;
        // The date (in milliseconds) used to generate the last name.
        private long mLastDate;
        // Number of names generated for the same second.
        private int mSameSecondCount;

        public ImageFileNamer(String format) {
            mFormat = new SimpleDateFormat(format);
        }

        public String generateName(long dateTaken, String score, String scoreType) {
            Date date = new Date(dateTaken);
            String result = mFormat.format(date);
            // If the last name was generated for the same second,
            // we append _1, _2, etc to the name.
            if (dateTaken / 1000 == mLastDate / 1000) {
                mSameSecondCount++;
                result += "_" + mSameSecondCount;
            } else {
                mLastDate = dateTaken;
                mSameSecondCount = 0;
            }
            return result + "_" + scoreType + "_" + score;
        }
    }

    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public static void saveImage(Bitmap image) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Log.d(TAG + "URI:", pictureFile.toURI().toString());
            image.compress(Bitmap.CompressFormat.JPEG, 99, fos);
            Log.d(TAG, "Image saved");
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

    }
    /**
     * @param bitmap
     * @return converting bitmap and return a string
     */
    public static byte[] BitMapToByteArray(Bitmap bitmap) {

        final long t0 = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] b = baos.toByteArray();
        bitmap.recycle();
        Log.d(TAG, String.format("BitMapToByteArray took %dms", System.currentTimeMillis() - t0));
        return b;
    }


    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Darts");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Darts", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    public static int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d(TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public static Camera openBackFacingCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            Log.d(TAG, "Camera Info: " + cameraInfo.facing);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    return Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return null;
    }


}

package com.primewebtech.darts.camera;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.primewebtech.darts.R;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final String TAG = CameraActivity.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    private int cameraId = 0;
    private int THUMBSIZE = 100;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private MediaSaver mMediaSaver;
    private ImageButton mPreviousImageThumbnail;
    private ImageButton mSaveImageButton;
    private ImageButton mBackButton;
    private ImageButton mTakePhotoButton;
    private CustomPagerAdapter mCustomPagerAdapter;
    private Camera.Parameters mParameters;
    private int mDisplayOrientation;
    private int mLayoutOrientation;
    private ViewPager mViewPager;
    private Bitmap mThumbNail;
    private byte[] mJPEGdata;
    private Location mLocation;
    private NamedImages mNamedImages;
    private ContentResolver mContentResolver;
    private Camera.CameraInfo mCameraInfo;
    public long mCaptureStartTime;
    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();
    private final static int ALL_PERMISSIONS_RESULT = 107;

    private MediaSaver.OnMediaSavedListener mOnMediaSavedListener = new MediaSaver.OnMediaSavedListener() {
        @Override
        public void onMediaSaved(Uri uri) {
            if (uri != null) {
                Log.d(TAG + "onMediaSaved:uri:", uri.toString());
            }
        }
    };


    private int[] mResources = {
            R.drawable.first,
            R.drawable.second,
            R.drawable.third,
            R.drawable.fourth,
            R.drawable.fifth,
            R.drawable.sixth
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Util.initialize(this);
        mContentResolver = this.getContentResolver();
        mNamedImages = new NamedImages();
        mMediaSaver = new MediaSaver(mContentResolver);
        Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
        // Create an instance of Camera
        Log.d(TAG, "onCreate:starting");
        // do we have a camera?
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = Util.findBackFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // request permission
                    int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
                    if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {Manifest.permission.CAMERA},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        // no permission so request and return
                        return;
                    }
                    Log.d(TAG, "onCreate:openingCamera");
                    mCamera = Camera.open(cameraId);
                    Camera.getCameraInfo(cameraId, mCameraInfo);
                    mParameters = mCamera.getParameters();
                    Log.d(TAG, "onCreate:openingCamera:done");
                } else {
                    Log.d(TAG, "onCreate:OLD_VERSION:openingCamera");
                    mCamera = Camera.open(cameraId);
                    Log.d(TAG, "onCreate:OLD_VERSION:openingCamera:done");
                    mParameters = mCamera.getParameters();
                    Camera.getCameraInfo(cameraId, mCameraInfo);
                }

            }
        }
        determineDisplayOrientation();
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        mTakePhotoButton = (ImageButton) findViewById(R.id.button_take_photo);
        mPreviousImageThumbnail = (ImageButton) findViewById(R.id.button_previous);
        mSaveImageButton = (ImageButton) findViewById(R.id.button_save_image);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mTakePhotoButton.setVisibility(View.VISIBLE);

        mCustomPagerAdapter = new CustomPagerAdapter(this, mResources);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);
        mViewPager.setVisibility(View.GONE);


    }

    public void onTakePhotoClick(View view) {
        Log.d(TAG, "onCLick:startingPreview");
        mCamera.startPreview();
//        dispatchTakePictureIntent();
        mCamera.takePicture(null, null, jpegCallback);
//        mCamera.takePicture(null, null,
//                        new PhotoHandler(getApplicationContext()));

    }
    public void onSavePhotoClick(View view) {
        Log.d(TAG, "onCLick:Saving photo");
        mCamera.startPreview();
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mTakePhotoButton.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.GONE);
        mNamedImages.nameNewImage(mContentResolver, mCaptureStartTime);
        String title = mNamedImages.getTitle();
        long date = mNamedImages.getDate();
        Camera.Size s = mParameters.getPictureSize();
        int width, height;
        int orientation = mDisplayOrientation;
        Drawable d = getResources().getDrawable(mResources[mViewPager.getCurrentItem()]);
        Bitmap selectedIcon = drawableToBitmap(d);
        boolean result = Util.checkPermission(CameraActivity.this);
        if ((orientation == 90)) {
            width = s.width;
            height = s.height;
        } else {
            width = s.height;
            height = s.width;

        }

        if (title == null) {
            Log.e(TAG, "Unbalanced name/data pair");
        } else {
            if (date == -1) date = mCaptureStartTime;
            if (result) {
                Log.e(TAG, "attempting async save");
//                Bitmap combinedImage = addSelectedIcon(mJPEGdata, selectedIcon);
//                Util.saveImage(combinedImage); //TODO make a async task
                mMediaSaver.addImage(mJPEGdata, selectedIcon, title, date, mLocation, width, height, 0,  mOnMediaSavedListener);

            }
        }

        Bitmap thumbCombined = addSelectedIcon(mThumbNail, selectedIcon);
        mPreviousImageThumbnail.setImageBitmap(thumbCombined);



    }

    public void onBackButtonClick(View view) {
        Log.d(TAG, "onBackButtonClick:resetting camera");
        mCamera.startPreview();
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mViewPager.setVisibility(View.GONE);
        mTakePhotoButton.setVisibility(View.VISIBLE);

    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPreviousImageThumbnail.setImageBitmap(imageBitmap);
        }
    }




    @Override
    protected void onPause() {
        super.onPause();
//        releaseCamera();              // release the camera immediately on pause event
    }
    @Override
    protected void onResume() {
        super.onResume();
        mCamera.startPreview();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCamera();
    }
    private void stopCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }

    }

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
//            new SaveImageTask().execute(data);
//            resetCam();
            Log.d(TAG, "onPictureTaken - jpeg");
            mCaptureStartTime = System.currentTimeMillis();
            mSaveImageButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.VISIBLE);
            mTakePhotoButton.setVisibility(View.GONE);
            mThumbNail = getThumbNail(data);
            mJPEGdata = data;





        }
    };
    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap getThumbNail(byte[] data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(inputStream), THUMBSIZE, THUMBSIZE);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedThumbImage = Bitmap.createBitmap(thumbImage, 0, 0, THUMBSIZE, THUMBSIZE, matrix, true);
        return rotatedThumbImage;
    }
    private Bitmap addSelectedIcon(Bitmap picture, Bitmap icon) {
        Bitmap combinedImg = null;
        int pictureWidth = picture.getWidth();
        int pictureHeight = picture.getHeight();
        float iconFloatLeft = pictureWidth - 50;
        float iconFloatTop = pictureHeight - 50;
        Double iconSize = pictureWidth*0.2;
        int iconSizeInt = iconSize.intValue();

        combinedImg = Bitmap.createBitmap(pictureWidth, pictureHeight, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(combinedImg);

        comboImage.drawBitmap(picture, 0f, 0f, null);
        comboImage.drawBitmap(Util.getResizedBitmap(icon, iconSizeInt, iconSizeInt), iconFloatLeft, iconFloatTop, null);

        return combinedImg;
    }


    private void resetCam() {
        mCamera.startPreview();
    }

    public boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }
    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private static class NamedImages {
        private ArrayList<NamedEntity> mQueue;
        private boolean mStop;
        private NamedEntity mNamedEntity;
        public NamedImages() {
            mQueue = new ArrayList<NamedEntity>();
        }
        public void nameNewImage(ContentResolver resolver, long date) {
            NamedEntity r = new NamedEntity();
            r.title = Util.createJpegName(date);
            r.date = date;
            mQueue.add(r);
            Log.d(TAG +"nameNewImage:title:", r.title);
            Log.d(TAG +"nameNewImage:date:", Float.toString(r.date));

        }
        public String getTitle() {
            if (mQueue.isEmpty()) {
                Log.d(TAG,"getTitle:mQqueu:empty");
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

    public void determineDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees  = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int displayOrientation;

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mDisplayOrientation = displayOrientation;
        mLayoutOrientation  = degrees;

        mCamera.setDisplayOrientation(displayOrientation);
    }



}

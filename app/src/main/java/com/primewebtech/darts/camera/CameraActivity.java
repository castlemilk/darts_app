package com.primewebtech.darts.camera;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.primewebtech.darts.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.primewebtech.darts.camera.Util.openBackFacingCamera;

public class CameraActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final String TAG = CameraActivity.class.getSimpleName();

    private Camera mCamera;
    private CameraPreview mPreview;
    private int cameraId = 0;
    private int THUMBSIZE = 100;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private MediaSaver mMediaSaver;
    private ImageButton mPreviousImageThumbnail;
    private String mRecentlySavedImageFilePath;
    private ImageButton mSaveImageButton;
    private ImageButton mBackButton;
    private ImageButton mTakePhotoButton;
    private CustomPagerAdapter mCustomPagerAdapter;
    private Camera.Parameters mParameters;
    static final String LATEST_PICTRUE = "LATEST_PICTURE";
    private int mDisplayOrientation;
    private int mLayoutOrientation;
    private ViewPager mViewPager;
    private Bitmap mThumbNail;
    private Spinner mScoreType;
    private Spinner mScoreValue;
    private Uri mRecentlySavedImageURI;
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
    private FrameLayout preview;
    private String scoreType;
    private ImageView mLogoText;
    private ImageView mScoreTypeBackground;
    private TextView mScoreNumber;
    public int mScoreNumberValue;


    private MediaSaver.OnMediaSavedListener mOnMediaSavedListener = new MediaSaver.OnMediaSavedListener() {
        @Override
        public void onMediaSaved(Uri uri) {
            if (uri != null) {
                mRecentlySavedImageURI = uri;
                if (uri != null && "content".equals(uri.getScheme())) {
                    Cursor cursor = getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                    cursor.moveToFirst();
                    mRecentlySavedImageFilePath = cursor.getString(0);
                    cursor.close();
                } else {
                    mRecentlySavedImageFilePath = uri.getPath();
                }
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(new File(mRecentlySavedImageFilePath))));
                Log.d(TAG ,"onMediaSaved:getFilePath:"+mRecentlySavedImageFilePath);
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{mRecentlySavedImageFilePath}, new String[]{"image/jpeg"}, null);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPreviousImageThumbnail.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mRecentlySavedImageFilePath),
                                THUMBSIZE, THUMBSIZE));
                    }
                });

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
    private String[] spinnerTypes = {
            "Peg",
            "Score",
    };
    private int[] mScorePins = {
            R.drawable.pin_40s,
            R.drawable.pin_60s,
            R.drawable.pin_70s,
            R.drawable.pin_80s,
            R.drawable.pin_90s,
            R.drawable.pin_100s,
            R.drawable.pin_110sf,
            R.drawable.pin_120s,
            R.drawable.pin_130sf,
            R.drawable.pin_140sf,
            R.drawable.pin_150s,
            R.drawable.pin_160sf,
            R.drawable.pin_170s,
    };
    private int[] mScoreResources = {
            R.drawable.picture_score_image,
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.getString(LATEST_PICTRUE) != null) {
                mRecentlySavedImageURI = Uri.parse(savedInstanceState.getString(LATEST_PICTRUE));
            } else {
                mRecentlySavedImageURI = null;
            }

        }
        setContentView(R.layout.activity_camera);
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
                    Log.d(TAG, "getCameraPermission:openingCamera:done");
                } else {
                    Log.d(TAG, "getCameraPermission:OLD_VERSION:openingCamera:done");

                }

            }
        }
        mScoreType = (Spinner) findViewById(R.id.camera_score_type_spinner);
        mScoreValue = (Spinner) findViewById(R.id.camera_score_spinner);
        mScoreNumber = (TextView) findViewById(R.id.score_number);
        mScoreValue.setVisibility(View.GONE);
        mScoreType.setVisibility(View.GONE);
        initTypeSpinners();
        Util.initialize(this);
        mContentResolver = this.getContentResolver();
        mNamedImages = new NamedImages();
        mMediaSaver = new MediaSaver(mContentResolver);
        mCamera = openBackFacingCamera();

        if (mCamera != null) {
            mParameters = mCamera.getParameters();
        }

        List<String> focusModes = mParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // Autofocus mode is supported
            Log.d(TAG, "SETTING_FOCUS_MODE");
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
        // Create an instance of Camera
        Log.d(TAG, "onCreate:starting");
        // do we have a camera?


        if (Integer.parseInt(Build.VERSION.SDK) >= 8)
            setDisplayOrientation(mCamera, 90);
        else
        {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                mParameters.set("orientation", "portrait");
                mParameters.set("rotation", 90);
            }
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                mParameters.set("orientation", "landscape");
                mParameters.set("rotation", 90);
            }
        }

        preview = (FrameLayout) findViewById(R.id.camera_preview);
        mTakePhotoButton = (ImageButton) findViewById(R.id.button_take_photo);
        mPreviousImageThumbnail = (ImageButton) findViewById(R.id.button_previous);
        mPreviousImageThumbnail.setEnabled(false);
        if (mRecentlySavedImageFilePath != null) {
            mPreviousImageThumbnail.setEnabled(true);
            mPreviousImageThumbnail.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mRecentlySavedImageFilePath),
                    THUMBSIZE, THUMBSIZE));
        }
        mSaveImageButton = (ImageButton) findViewById(R.id.button_save_image);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mTakePhotoButton.setVisibility(View.VISIBLE);

        mLogoText = (ImageView) findViewById(R.id.logo_text);
        mLogoText.setVisibility(View.GONE);

        mScoreTypeBackground = (ImageView) findViewById(R.id.score_type_background);
        mScoreTypeBackground.setVisibility(View.GONE);





        mPreview = new CameraPreview(this, mCamera);
        mCamera.startPreview();

        Camera.getCameraInfo(cameraId, mCameraInfo);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(mParameters);
        Log.d(TAG, "FOCUS_MODE:"+mCamera.getParameters().getFocusMode());
        determineDisplayOrientation();
        preview.addView(mPreview);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                super.onCreate(null);

            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }

    public boolean getCameraPermission() {


        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
            return false;
        } else {
            cameraId = Util.findBackFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
                return false;
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // request permission
                    int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
                    if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {Manifest.permission.CAMERA},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        // no permission so request and return
                        return false;
                    }
                    Log.d(TAG, "getCameraPermission:openingCamera:done");
                } else {
                    Log.d(TAG, "getCameraPermission:OLD_VERSION:openingCamera:done");
                    return true;

                }

            }
            return false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(LATEST_PICTRUE,mRecentlySavedImageFilePath);

    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume");
            if (mCamera == null) {
                setContentView(R.layout.activity_camera);
                mCamera = openBackFacingCamera();
                mPreview = new CameraPreview(this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
                mTakePhotoButton = (ImageButton) findViewById(R.id.button_take_photo);
                mPreviousImageThumbnail = (ImageButton) findViewById(R.id.button_previous);
                mSaveImageButton = (ImageButton) findViewById(R.id.button_save_image);
                mBackButton = (ImageButton) findViewById(R.id.button_back);
                mViewPager = (ViewPager) findViewById(R.id.pager);
                mLogoText = (ImageView) findViewById(R.id.logo_text);
                mScoreTypeBackground = (ImageView) findViewById(R.id.score_type_background);
                mScoreType = (Spinner) findViewById(R.id.camera_score_type_spinner);
                mScoreValue = (Spinner) findViewById(R.id.camera_score_spinner);
                mScoreNumber = (TextView) findViewById(R.id.score_number);
                mScoreValue.setVisibility(View.GONE);
                mScoreType.setVisibility(View.GONE);
                initTypeSpinners();
                mLogoText.setVisibility(View.GONE);
                if (mCustomPagerAdapter != null) {
                    mViewPager.setAdapter(mCustomPagerAdapter);
                } else {
                    mViewPager.setAdapter(new CustomPagerAdapter(this, mScorePins));
                }

                mViewPager.setVisibility(View.GONE);
                mSaveImageButton.setVisibility(View.GONE);
                mBackButton.setVisibility(View.GONE);
                mTakePhotoButton.setVisibility(View.VISIBLE);
                mScoreTypeBackground.setVisibility(View.GONE);
                mScoreNumber.setVisibility(View.GONE);

                if (mRecentlySavedImageFilePath != null) {
                    mPreviousImageThumbnail.setVisibility(View.VISIBLE);
                    mPreviousImageThumbnail.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mRecentlySavedImageFilePath),
                            THUMBSIZE, THUMBSIZE));
                }

            }

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }

    }
    @Override
    protected void onPause() {

        try {
            Log.d(TAG, "onPause");
            if(mCamera != null){
                mCamera.release();
                mCamera = null;
                Log.d(TAG, "onPause:complete");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    public void onTakePhotoClick(View view) {
        Log.d(TAG, "onCLick:startingPreview");
        mCamera.startPreview();
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
        mScoreType.setVisibility(View.GONE);
        mScoreType.setEnabled(false);
        mScoreValue.setVisibility(View.GONE);
        mScoreValue.setEnabled(false);
        mScoreNumber.setVisibility(View.GONE);
        mLogoText.setVisibility(View.GONE);
        mScoreTypeBackground.setVisibility(View.GONE);
        mPreviousImageThumbnail.setEnabled(true);
        mPreviousImageThumbnail.setVisibility(View.VISIBLE);

        mNamedImages.nameNewImage(mContentResolver, mCaptureStartTime, mScoreNumberValue);
        String title = mNamedImages.getTitle();
        long date = mNamedImages.getDate();
        Camera.Size s = mParameters.getPictureSize();
        int width, height;
        int orientation = mDisplayOrientation;
        Bitmap pin = drawableToBitmap(getResources().getDrawable(mScorePins[mViewPager.getCurrentItem()]));
        Bitmap logo = drawableToBitmap(getResources().getDrawable(R.drawable.logotext));
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
                mMediaSaver.addImage(mJPEGdata, logo, pin, mScoreNumberValue, title, date, mLocation, width, height, 0,  mOnMediaSavedListener);

            }
        }



    }

    public void onBackButtonClick(View view) {
        Log.d(TAG, "onBackButtonClick:resetting camera");
        mCamera.startPreview();
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mTakePhotoButton.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.GONE);
        mScoreType.setVisibility(View.GONE);
        mScoreType.setEnabled(false);
        mScoreValue.setVisibility(View.GONE);
        mScoreValue.setEnabled(false);
        mScoreNumber.setVisibility(View.GONE);
        mLogoText.setVisibility(View.GONE);
        mScoreTypeBackground.setVisibility(View.GONE);
        mPreviousImageThumbnail.setEnabled(true);
        mPreviousImageThumbnail.setVisibility(View.VISIBLE);

    }
    public void onReviewLatestPhotoClick(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setDataAndType(mRecentlySavedImageURI, "image/*");
        galleryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(galleryIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPreviousImageThumbnail.setImageBitmap(imageBitmap);
        }
    }





    private void initialise() {
        Log.d(TAG, "initialise:");

        Log.d(TAG, "initialise:completed");
    }


//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        stopCamera();
//    }
//    private void stopCamera() {
//        if (mCamera != null) {
//            mCamera.stopPreview();
//            mCamera.release();
//        }
//
//    }

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
//            new SaveImageTask().execute(data);
//            resetCam();
            Log.d(TAG, "onPictureTaken - jpeg");
            mCaptureStartTime = System.currentTimeMillis();
            mSaveImageButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.VISIBLE);
            setPegDisplay(0);
            mScoreType.setEnabled(true);
            mScoreValue.setEnabled(true);
            mScoreType.setVisibility(View.VISIBLE);
            mScoreValue.setVisibility(View.VISIBLE);
            initTypeSpinners();
            mTakePhotoButton.setVisibility(View.GONE);
            mLogoText.setVisibility(View.VISIBLE);
            mScoreTypeBackground.setVisibility(View.VISIBLE);
            mScoreNumber.setVisibility(View.VISIBLE);
            mPreviousImageThumbnail.setVisibility(View.GONE);
            mPreviousImageThumbnail.setEnabled(false);

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

    /**
     * Manage Spinners
     */

    private void setPegDisplay(int item) {

        mCustomPagerAdapter = new CustomPagerAdapter(this, mScorePins);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);
        mViewPager.setVisibility(View.VISIBLE);
        mViewPager.setCurrentItem(item);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //TODO: set number on scroll as well as the spinner selection action
            }

            @Override
            public void onPageSelected(int position) {
                //TODO: set number on scroll as well as the spinner selection action
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void updatePegDisplay(int score) {
        if ( 40 <= score && score < 50) {
            mViewPager.setCurrentItem(0);
        } else if (50 <= score && score < 60) {
            mViewPager.setCurrentItem(1);
        } else if (70 <= score && score < 80) {
            mViewPager.setCurrentItem(2);
        } else if (80 <= score && score < 90) {
            mViewPager.setCurrentItem(3);
        } else if (90 <= score && score < 100) {
            mViewPager.setCurrentItem(4);
        } else if (100 <= score && score < 110) {
            mViewPager.setCurrentItem(5);
        } else if (110 <= score && score < 120) {
            mViewPager.setCurrentItem(6);
        } else if (120 <= score && score < 130) {
            mViewPager.setCurrentItem(7);
        } else if (130 <= score && score < 140) {
            mViewPager.setCurrentItem(8);
        } else if (140 <= score && score < 150) {
            mViewPager.setCurrentItem(9);
        } else if (150 <= score && score < 160) {
            mViewPager.setCurrentItem(10);
        } else if (160 <= score && score < 170) {
            mViewPager.setCurrentItem(11);
        } else if (170 <= score && score < 180) {
            mViewPager.setCurrentItem(12);
        }

    }
    private List<Integer> makeSequence(int begin, int end) {
        List<Integer> ret = new ArrayList<>(end - begin + 1);
        for (int i=begin; i<=end; i++) {
            ret.add(i);
        }
        return ret;
    }
    private List<Integer> makePegs() {
        List<Integer> ret = new ArrayList<>();
        ret.add(170);
        ret.add(167);
        ret.add(164);
        return ret;
    }

    private void initTypeSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mScoreType.setAdapter(adapter);
        mScoreType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = (String) mScoreType.getItemAtPosition(i);
                Log.d(TAG, "onItemSelected:"+type);
                initScoreSpinner(type);
                if (type.equals("Score")) {
                    scoreType = "SCORE";
                    mViewPager.setEnabled(false);
                    mViewPager.setVisibility(View.GONE);
                    mScoreTypeBackground.setVisibility(View.VISIBLE);

                } else if (type.equals("Peg")) {
                    scoreType = "PEG";
                    if (mViewPager != null) {
                        mViewPager.setEnabled(true);
                        mViewPager.setVisibility(View.VISIBLE);

                    }
                    mCustomPagerAdapter.notifyDataSetChanged();

                    mScoreTypeBackground.setVisibility(View.GONE);

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void initScoreSpinner(String type) {
        ArrayAdapter<Integer> adapter = null;
        Log.d(TAG, "initScoreSpinner:type:"+type);
        switch (type) {
            case "Score":
                Log.d(TAG, "initScoreSpinner:type:Score");
                adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, makeSequence(0, 180));
                scoreType = "SCORE";
                break;
            case "Peg":
                Log.d(TAG, "initScoreSpinner:type:Peg");
                adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, makePegs());
                scoreType = "PEG";
                break;
            default:
                Log.d(TAG, "initScoreSpinner:type:default(score)");
                adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, makeSequence(0, 180));
                scoreType = "SCORE";
                break;
        }
        mScoreValue.setAdapter(adapter);
        mScoreValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "ScoreSpinnerSelect:onItemSelected:");
                int value = (int) mScoreValue.getItemAtPosition(i);
                Log.d(TAG, "ScoreSpinnerSelect:onItemSelected:score:"+value);
                Log.d(TAG, "ScoreSpinnerSelect:onItemSelected:scoreType:"+scoreType);
                if (scoreType.equals("PEG")) {
                    Log.d(TAG, "ScoreSpinnerSelect:onItemSelected:updating:score");
                    updatePegDisplay(value);
//                    mScoreNumber.setText(Integer.toString(value));
                    mScoreNumber.setVisibility(View.GONE);
                    mViewPager.setTag(value);
                    mScoreNumberValue = value;
                    mCustomPagerAdapter.notifyDataSetChanged();


                } else if (scoreType.equals("SCORE")) {
                    mScoreNumber.setVisibility(View.VISIBLE);
                    mScoreNumber.setText(Integer.toString(value));
                    mScoreNumberValue = value;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }



    private static class NamedImages {
        private ArrayList<NamedEntity> mQueue;
        private boolean mStop;
        private NamedEntity mNamedEntity;
        public NamedImages() {
            mQueue = new ArrayList<NamedEntity>();
        }
        public void nameNewImage(ContentResolver resolver, long date, int score) {
            NamedEntity r = new NamedEntity();
            r.title = Util.createJpegName(date, score);
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

    protected void setDisplayOrientation(Camera camera, int angle){
        Method downPolymorphic;
        try
        {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        }
        catch (Exception e1)
        {
        }
    }

    public static Camera getCameraInstance(){

        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }





}

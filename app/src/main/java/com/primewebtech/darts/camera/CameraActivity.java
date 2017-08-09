package com.primewebtech.darts.camera;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aigestudio.wheelpicker.WheelPicker;
import com.primewebtech.darts.BuildConfig;
import com.primewebtech.darts.R;
import com.primewebtech.darts.homepage.HomePageActivity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.primewebtech.darts.camera.Util.openBackFacingCamera;

public class CameraActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    final private int PREVIEW = 111;
    final private int SAVING = 112;
    final private int SAVED = 113;

    private static final String TAG = CameraActivity.class.getSimpleName();

    private int mMode;

    private Camera mCamera;
    private CameraPreview mPreview;
    private int cameraId = 0;
    private int THUMBSIZE = 100;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private MediaSaver mMediaSaver;
    private CircleImageView mPreviousImageThumbnail;
    private String mRecentlySavedImageFilePath;
    private ImageButton mSaveImageButton;
    private ImageButton mBackButton;
    private ImageButton mTakePhotoButton;
    private Camera.Parameters mParameters;
    static final String LATEST_PICTRUE = "LATEST_PICTURE";
    private int mDisplayOrientation;
    private int mLayoutOrientation;
//    private ViewPager mViewPager;
    private Bitmap mThumbNail;
    private WheelPicker mScoreType;
    private WheelPicker mScoreValue;
    private Uri mRecentlySavedImageURI;
    private byte[] mJPEGdata;
    private Location mLocation;
    private NamedImages mNamedImages;
    private ContentResolver mContentResolver;
    public long mCaptureStartTime;
    private boolean cameraPermissionGranted = false;
    private FrameLayout preview;
    private String scoreType;
    private ImageView mLogoText;
    private ImageView mScoreTypeBackground;
    private TextView mScoreNumber;
    public Object mScoreNumberValue;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private SoundPool soundPool;
    private AudioManager audioManager;
    private boolean loaded;
    private float volume;
    // Maximumn sound stream.
    private static final int MAX_STREAMS = 2;
    private int soundIdScrolling;
    private int soundIdClick;


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


    private String[] spinnerTypes = {
            "Peg",
            "Score",
    };
    private int[] mPegResources = {
            R.drawable.score_board_s,
            R.drawable.pin_60ns,
            R.drawable.pin_70ns,
            R.drawable.pin_80ns,
            R.drawable.pin_90ns,
            R.drawable.pin_100ns,
            R.drawable.pin_100ns,
            R.drawable.pin_120ns,
            R.drawable.pin_130ns,
            R.drawable.pin_140ns,
            R.drawable.pin_150ns,
            R.drawable.pin_160ns,
            R.drawable.pin_170ns,
    };
    private int[] mScoreResources = {
            R.drawable.green_hat,
//            R.drawable.score_board_p,
            R.drawable.score_board_s,
            R.drawable.score_board_r,

    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mScoreType = (WheelPicker) findViewById(R.id.score_type);
        mScoreValue = (WheelPicker) findViewById(R.id.score_value);
        mScoreNumber = (TextView) findViewById(R.id.score_number);
        mScoreTypeBackground = (ImageView) findViewById(R.id.score_type_background);
        mTakePhotoButton = (ImageButton) findViewById(R.id.button_take_photo);
        mPreviousImageThumbnail = (CircleImageView) findViewById(R.id.button_previous);
        mPreviousImageThumbnail.setSoundEffectsEnabled(false);
        mLogoText = (ImageView) findViewById(R.id.logo_text);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        mSaveImageButton = (ImageButton) findViewById(R.id.save_photo);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
//        mViewPager = (ViewPager) findViewById(R.id.pager);

//        mViewPager.setVisibility(View.GONE);
        mScoreValue.setVisibility(View.GONE);
        mScoreType.setVisibility(View.GONE);
        mScoreTypeBackground.setVisibility(View.GONE);
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mTakePhotoButton.setVisibility(View.VISIBLE);
        initTypeSpinners();
        initScoreSpinner("Peg");
        initialiseSound();
//        mViewPager.setCurrentItem(0);
        requestCameraPermission();



        Util.initialize(this);
        mContentResolver = this.getContentResolver();
        mNamedImages = new NamedImages();
        mMediaSaver = new MediaSaver(mContentResolver);


        mLogoText.setVisibility(View.GONE);



        if (cameraPermissionGranted) {
            mCamera = openBackFacingCamera();
            if (mCamera != null) {
                mParameters = mCamera.getParameters();
            } else {

            }
            Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, mCameraInfo);
            mPreview = new CameraPreview(this, mCamera);
            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(mParameters);
            determineDisplayOrientation();
            preview.addView(mPreview);
        }




    }

    public void requestCameraPermission() {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // request permission
                    int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
                    if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {Manifest.permission.CAMERA},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        // no permission so request and return
                        return;
                    }
                    Log.d(TAG, "getCameraPermission:openingCamera:done");
                    cameraPermissionGranted = true;
                } else {
                    Log.d(TAG, "getCameraPermission:OLD_VERSION:openingCamera:done");
                    cameraPermissionGranted = true;

                }

            }
        }
    }
    public void initialisePreviousImageThumbail(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.getString(LATEST_PICTRUE) != null) {
                mRecentlySavedImageURI = Uri.parse(savedInstanceState.getString(LATEST_PICTRUE));
            } else {
                File lastPicture = getLastPicture();
                if (lastPicture != null) {
                    mRecentlySavedImageURI = Uri.fromFile(lastPicture);
                } else {
                    mRecentlySavedImageURI = null;
                }
            }
        } else {
            File lastPicture = getLastPicture();
            if (lastPicture != null) {
                Log.d(TAG, "onCreate:lastPicture"+lastPicture.toString());
                mRecentlySavedImageURI = Uri.fromFile(lastPicture);
                Log.d(TAG, "onCreate:mRecentlySavedImageURI:"+mRecentlySavedImageURI);
            } else {
                mRecentlySavedImageURI = null;
            }
        }
        mPreviousImageThumbnail.setEnabled(false);
        if (mRecentlySavedImageURI != null) {
            Log.d(TAG, "onCreate:mPreviewImageThumbnail:path:"+mRecentlySavedImageURI);
            mPreviousImageThumbnail.setEnabled(true);
            mPreviousImageThumbnail.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mRecentlySavedImageURI.getPath()),
                    THUMBSIZE, THUMBSIZE));
        }

    }
    public void initialiseSound() {
        // AudioManager audio settings for adjusting the volume
                audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Current volumn Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volumn (0 --> 1)
        this.volume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.setVolumeControlStream(streamType);

        // For Android SDK >= 21
        if (Build.VERSION.SDK_INT >= 21 ) {

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();
        }
        // for Android SDK < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });
        soundIdClick = soundPool.load(this, R.raw.click, 2);
        soundIdScrolling = soundPool.load(this, R.raw.scrolling, 1);

    }

    public void playSoundSelected(float speed) {
        Log.d(TAG, "playSoundScroll");
        if(loaded)  {
            Log.d(TAG, "playSoundScroll:playing");
            float leftVolumn = volume;
            float rightVolumn = volume;
            int streamId = this.soundPool.play(this.soundIdClick,leftVolumn, rightVolumn, 2, 0, speed);

        }
    }
    public int playSoundScrolling(float speed) {
        Log.d(TAG, "playSoundScroll");
        if(loaded)  {

            float leftVolumn = volume;
            float rightVolumn = volume;
            int scrollingId = this.soundPool.play(this.soundIdScrolling,leftVolumn, rightVolumn, 1, 160, speed);

            Log.d(TAG, "playSoundScroll:playing:id"+scrollingId);
            return scrollingId;
        } else {
            return 0;
        }
    }
    public void stopAllSounds() {
        this.soundPool.autoPause();
    }
    public void stopSoundScrolling(int scrollingId) {
        if (loaded) {
            Log.d(TAG, "stopping:sound:"+scrollingId);
            this.soundPool.stop(scrollingId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                cameraPermissionGranted = true;
                onResume();

            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                Intent homePageIntent = new Intent(CameraActivity.this, HomePageActivity.class);
                startActivity(homePageIntent);

            }

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
            if (mCamera == null && cameraPermissionGranted) {
                Log.d(TAG, "onResume:mCamera:null");
                setContentView(R.layout.activity_camera);
                mCamera = openBackFacingCamera();
                mPreview = new CameraPreview(this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
            }
            mTakePhotoButton = (ImageButton) findViewById(R.id.button_take_photo);
            mPreviousImageThumbnail = (CircleImageView) findViewById(R.id.button_previous);
            mPreviousImageThumbnail.setSoundEffectsEnabled(false);
            mSaveImageButton = (ImageButton) findViewById(R.id.save_photo);
            mBackButton = (ImageButton) findViewById(R.id.button_back);
//            mViewPager = (ViewPager) findViewById(R.id.pager);
            mLogoText = (ImageView) findViewById(R.id.logo_text);
            mScoreTypeBackground = (ImageView) findViewById(R.id.score_type_background);
            mScoreType = (WheelPicker) findViewById(R.id.score_type);
            mScoreValue = (WheelPicker) findViewById(R.id.score_value);
            mScoreNumber = (TextView) findViewById(R.id.score_number);
            mScoreValue.setVisibility(View.GONE);
            mScoreType.setVisibility(View.GONE);
            mScoreTypeBackground.setVisibility(View.GONE);
            mLogoText.setVisibility(View.GONE);
            initTypeSpinners();
            initScoreSpinner("Peg");
            initialiseSound();
//            mViewPager.setCurrentItem(0);
//            mViewPager.setVisibility(View.GONE);
            mSaveImageButton.setVisibility(View.GONE);
            mBackButton.setVisibility(View.GONE);
            mTakePhotoButton.setVisibility(View.VISIBLE);
            mScoreNumber.setVisibility(View.GONE);
            Util.initialize(this);
            mContentResolver = this.getContentResolver();
            mNamedImages = new NamedImages();
            mMediaSaver = new MediaSaver(mContentResolver);
            File lastPicture = getLastPicture();
            if (lastPicture != null) {
                Log.d(TAG, "onCreate:lastPicture"+lastPicture.toString());
                mRecentlySavedImageURI = Uri.fromFile(lastPicture);
                Log.d(TAG, "onCreate:mRecentlySavedImageURI:"+mRecentlySavedImageURI);
            } else {
                mRecentlySavedImageURI = null;
            }

            if (mRecentlySavedImageURI != null) {
                mPreviousImageThumbnail.setVisibility(View.VISIBLE);
                mPreviousImageThumbnail.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mRecentlySavedImageURI.getPath()),
                        THUMBSIZE, THUMBSIZE));
                File file = new File(mRecentlySavedImageFilePath);
                if (file.exists()) {
                    mPreviousImageThumbnail.setSoundEffectsEnabled(true);
                }

            }
            if (cameraPermissionGranted) {
                if (mCamera != null) {
                    mParameters = mCamera.getParameters();
                } else {
                    mCamera = openBackFacingCamera();
                    Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(cameraId, mCameraInfo);
                    mPreview = new CameraPreview(this, mCamera);
                    mCamera.setDisplayOrientation(90);
                    mCamera.setParameters(mParameters);
                    determineDisplayOrientation();
                    preview.addView(mPreview);
                }

            }

        } catch (Exception e) {
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
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
                soundPool.release();
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

    }

    public void onSavePhotoClick(View view) {
        Log.d(TAG, "onCLick:Saving photo");
        mCamera.startPreview();
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mTakePhotoButton.setVisibility(View.VISIBLE);
//        mViewPager.setVisibility(View.GONE);
        mScoreType.setVisibility(View.GONE);
        mScoreType.setEnabled(false);
        mScoreValue.setVisibility(View.GONE);
        mScoreValue.setEnabled(false);
        mScoreNumber.setVisibility(View.GONE);
        mLogoText.setVisibility(View.GONE);
        mScoreTypeBackground.setVisibility(View.GONE);
        mPreviousImageThumbnail.setEnabled(true);
        mPreviousImageThumbnail.setVisibility(View.VISIBLE);
        mPreviousImageThumbnail.setSoundEffectsEnabled(false);
        if (mRecentlySavedImageFilePath != null) {
            File file = new File(mRecentlySavedImageFilePath);
            if (file.exists()) {
                mPreviousImageThumbnail.setSoundEffectsEnabled(true);
            }
        }



        mNamedImages.nameNewImage(mContentResolver, mCaptureStartTime, mScoreNumberValue.toString(),
                spinnerTypes[mScoreType.getSelectedItemPosition()]);
        String title = mNamedImages.getTitle();
        long date = mNamedImages.getDate();
        Camera.Size s = mParameters.getPictureSize();
        int width, height;
        int orientation = mDisplayOrientation;
        Bitmap pin = null;
        String scoreType = spinnerTypes[mScoreType.getCurrentItemPosition()];
        Log.d(TAG, "title: "+title);
        Log.d(TAG, "ScoreType:"+scoreType);
        Log.d(TAG, "ScoreValue:"+mScoreNumberValue);
        if ((scoreType.equals("Score"))) {
            Log.d(TAG, "mScoreTYpe:SCORE");
            pin = drawableToBitmap(getResources().getDrawable(updateScoreDisplay(mScoreNumberValue)));
        } else {
            Log.d(TAG, "mScoreTYpe:PEG");
            pin = drawableToBitmap(getResources().getDrawable(updatePegDisplay(mScoreNumberValue)));
        }

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
                mMediaSaver.addImage(mJPEGdata, logo, pin, title, scoreType,
                        String.valueOf(mScoreNumberValue), date, mLocation,
                        width, height, 0,  mOnMediaSavedListener);
            }
        }
        mMode = PREVIEW;



    }

    public void onBackButtonClick(View view) {
        Log.d(TAG, "onBackButtonClick:resetting camera");
        mCamera.startPreview();
        initTypeSpinners();
        initScoreSpinner("Peg");
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mTakePhotoButton.setVisibility(View.VISIBLE);
//        mViewPager.setVisibility(View.GONE);
        mScoreType.setVisibility(View.GONE);
        mScoreType.setEnabled(false);
        mScoreValue.setVisibility(View.GONE);
        mScoreValue.setEnabled(false);
        mScoreNumber.setVisibility(View.GONE);
        mLogoText.setVisibility(View.GONE);
        mScoreTypeBackground.setVisibility(View.GONE);
        mPreviousImageThumbnail.setEnabled(true);
        mPreviousImageThumbnail.setVisibility(View.VISIBLE);
        stopAllSounds();
        if (mRecentlySavedImageFilePath != null) {
            File file = new File(mRecentlySavedImageFilePath);
            if (file.exists()) {
                mPreviousImageThumbnail.setSoundEffectsEnabled(true);
            }
        }
        mMode = PREVIEW;





    }
    public void onReviewLatestPhotoClick(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        File lastPicture = getLastPicture();
        if (lastPicture != null) {
            Log.d(TAG, "onCreate:lastPicture"+lastPicture.toString());
            mRecentlySavedImageURI = Uri.fromFile(lastPicture);
            Log.d(TAG, "onCreate:mRecentlySavedImageURI:"+mRecentlySavedImageURI);
            Uri photoURI = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    new File(mRecentlySavedImageURI.getPath()));
            galleryIntent.setDataAndType(photoURI, "image/*");
            galleryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            galleryIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(galleryIntent);
        } else {
            mRecentlySavedImageURI = null;
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





    private void initialise() {
        Log.d(TAG, "initialise:");

        Log.d(TAG, "initialise:completed");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCamera();
        soundPool.release();
        soundPool = null;
    }
    private void stopCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            soundPool.release();
        }

    }

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - jpeg");
            mCaptureStartTime = System.currentTimeMillis();
            mSaveImageButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.VISIBLE);
            mScoreType.setEnabled(true);
            mScoreValue.setEnabled(true);
            mScoreType.setVisibility(View.VISIBLE);
            mScoreType.setSelectedItemPosition(0);
            mScoreValue.setVisibility(View.VISIBLE);
            mScoreTypeBackground.setVisibility(View.VISIBLE);
            mScoreNumber.setVisibility(View.VISIBLE);
            initTypeSpinners();
            initScoreSpinner("Peg");

            mTakePhotoButton.setVisibility(View.GONE);
            mLogoText.setVisibility(View.VISIBLE);
            mPreviousImageThumbnail.setVisibility(View.GONE);
            mPreviousImageThumbnail.setEnabled(false);

            mThumbNail = getThumbNail(data);
            mJPEGdata = data;
            mCamera.stopPreview();
            mMode = SAVING;





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


    /**
     * Manage Spinners
     */


    private int updateScoreDisplay(Object item) {
        Log.d(TAG, "updateScoreDisplay:score:"+item);
        if (item == "RH") {
            mScoreTypeBackground.setImageResource(mScoreResources[0]);
            return mScoreResources[0];

        } else if ( 0 <= (int)item && (int)item <= 179 ) {
            mScoreTypeBackground.setImageResource(mScoreResources[1]);
            return mScoreResources[1];

        } else if( ((int) item) == 180 ) {
            mScoreTypeBackground.setImageResource(mScoreResources[2]);
            return mScoreResources[2];
        } else {
            return mScoreResources[0];
        }

    }

    private int updatePegDisplay(Object score) {
        Log.d(TAG, "updatePegDisplay:score:"+score);
        int convertedScore = (int) score;

        if ( 2 <= convertedScore && convertedScore < 60) {
            mScoreTypeBackground.setImageResource(mPegResources[0]);
            return mPegResources[0];
        } else if (60 <= convertedScore && convertedScore < 70) {
            mScoreTypeBackground.setImageResource(mPegResources[1]);
            return mPegResources[1];
        } else if (70 <= convertedScore && convertedScore < 80) {
            mScoreTypeBackground.setImageResource(mPegResources[2]);
            return mPegResources[2];
        } else if (80 <= convertedScore && convertedScore < 90) {
            mScoreTypeBackground.setImageResource(mPegResources[3]);
            return mPegResources[3];
        } else if (90 <= convertedScore && convertedScore < 100) {
            mScoreTypeBackground.setImageResource(mPegResources[4]);
            return mPegResources[4];
        } else if (100 <= convertedScore && convertedScore < 110) {
            mScoreTypeBackground.setImageResource(mPegResources[5]);
            return mPegResources[5];
        } else if (110 <= convertedScore && convertedScore < 120) {
            mScoreTypeBackground.setImageResource(mPegResources[6]);
            return mPegResources[6];
        } else if (120 <= convertedScore && convertedScore < 130) {
            mScoreTypeBackground.setImageResource(mPegResources[7]);
            return mPegResources[7];
        } else if (130 <= convertedScore && convertedScore < 140) {
            mScoreTypeBackground.setImageResource(mPegResources[8]);
            return mPegResources[8];
        } else if (140 <= convertedScore && convertedScore < 150) {
            mScoreTypeBackground.setImageResource(mPegResources[9]);
            return mPegResources[9];
        } else if (150 <= convertedScore && convertedScore < 160) {
            mScoreTypeBackground.setImageResource(mPegResources[10]);
            return mPegResources[10];
        } else if (160 <= convertedScore && convertedScore < 170) {
            mScoreTypeBackground.setImageResource(mPegResources[11]);
            return mPegResources[11];
        } else if (170 <= convertedScore && convertedScore < 180) {
            mScoreTypeBackground.setImageResource(mPegResources[12]);
            return mPegResources[12];
        } else {
            Log.d(TAG, "updatePegDisplay:invalidValue:"+score);
            mScoreTypeBackground.setImageResource(mPegResources[0]);
            return mPegResources[0];
        }

    }
    private List<Object> makeScores() {
        List<Object> ret = new ArrayList<>();
        for (int i=180; i>=0; i--) {
            ret.add(i);
        }
        ret.add("RH");
        return ret;
    }
    private List<Integer> makePegs() {
        List<Integer> ret = new ArrayList<>();
        ret.add(170);
        ret.add(167);
        ret.add(164);
        ret.add(161);
        ret.add(160);
        for (int i=158; i>=2; i--) {
            ret.add(i);
        }
        return ret;
    }

    private void initTypeSpinners() {


        List data = Arrays.asList(spinnerTypes);
        mScoreType.setData(data);
        mScoreType.setVisibleItemCount(2);
        mScoreType.setCyclic(false);
        mScoreType.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                String type = (String) picker.getData().get(position);
                soundPool.autoPause();
                Log.d(TAG, "onItemSelected:"+type);
                initScoreSpinner(type);
                if (type.equals("Score")) {
                    scoreType = "SCORE";
                    updateScoreDisplay(180);
                    mScoreNumberValue = makeScores().get(0);
                    mScoreNumber.setText(String.format(Locale.US, "%s", "180"));
                    mScoreTypeBackground.setVisibility(View.VISIBLE);
                    mScoreNumber.setVisibility(View.VISIBLE);

                } else if (type.equals("Peg")) {
                    scoreType = "PEG";
                    updatePegDisplay(170);
                    mScoreNumberValue = makePegs().get(0);
                    mScoreNumber.setText(String.format(Locale.US, "%s", "170"));
                    mScoreTypeBackground.setVisibility(View.VISIBLE);
                    mScoreNumber.setVisibility(View.VISIBLE);
                }

            }
        });
    }
    private void initScoreSpinner(String type) {
        List data = null;

        Log.d(TAG, "initScoreSpinner:type:"+type);
        switch (type) {
            case "Score":
                data = makeScores();
                Log.d(TAG, "initScoreSpinner:type:Score");
                mScoreNumberValue = makeScores().get(0);
                mScoreValue.setCyclic(false);
                updateScoreDisplay(180);
                mScoreNumber.setText(String.format(Locale.US, "%s", "180"));
                scoreType = "SCORE";
                break;
            case "Peg":
                data = makePegs();
                mScoreNumberValue = makePegs().get(0);
                mScoreValue.setCyclic(false);
                updatePegDisplay(170);
                mScoreNumber.setText(String.format(Locale.US, "%s", "170"));
                scoreType = "PEG";
                break;
            default:
                Log.d(TAG, "initScoreSpinner:type:default(score)");
                scoreType = "SCORE";
                mScoreTypeBackground.setVisibility(View.GONE);
                mScoreNumber.setVisibility(View.GONE);
                break;
        }
        mScoreValue.setData(data);
        mScoreValue.setVisibleItemCount(3);
        mScoreValue.setSelectedItemPosition(0);
        mScoreValue.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            int id = 0;
            boolean playingScrollingSound = false;
            int scrolls = 0;
            @Override
            public void onWheelScrolled(int offset) {
            }

            @Override
            public void onWheelSelected(int position) {
                if (mMode != SAVING) {
                    return;
                }
                stopSoundScrolling(id);
                playSoundSelected(1);
                playingScrollingSound = false;
                scrolls = 0;


            }

            @Override
            public void onWheelScrollStateChanged(int state) {
                if (mMode != SAVING) {
                    return;
                }
                scrolls++;
                Log.d(TAG, "onWheelScrollStateChanged:scrolls:"+scrolls);
                if ( state == mScoreValue.SCROLL_STATE_SCROLLING && !playingScrollingSound && scrolls > 20) {
                    id = playSoundScrolling(1.1f);
                    playingScrollingSound = true;
                } else if (state == mScoreValue.SCROLL_STATE_IDLE ) {
                    playingScrollingSound = false;
                    stopSoundScrolling(id);
                }
            }
        });

        mScoreValue.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {

            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                if (mMode != SAVING) {
                    return;
                }
                Log.d(TAG, "ScoreSpinnerSelect:onItemSelected:");
                Object value = picker.getData().get(position);
                mScoreNumberValue = value;
                Log.d(TAG, "ScoreSpinnerSelect:onItemSelected:score:"+value);
                Log.d(TAG, "ScoreSpinnerSelect:onItemSelected:scoreType:"+scoreType);
                if (scoreType.equals("PEG")) {
                    Log.d(TAG, "ScoreSpinnerSelect:onItemSelected:updating:PEG:score");
                    mScoreTypeBackground.setVisibility(View.VISIBLE);
                    mScoreNumber.setVisibility(View.VISIBLE);
                    mScoreNumber.setText(String.format(Locale.US, "%s", value));
                    updatePegDisplay(value);
                    mScoreValue.setCyclic(false);
                } else if (scoreType.equals("SCORE")) {
                    Log.d(TAG, "ScoreSpinnerSelect:onItemSelected:updating:SCORE:score");
                    updateScoreDisplay(value);
                    mScoreTypeBackground.setVisibility(View.VISIBLE);
                    mScoreNumber.setVisibility(View.VISIBLE);
                    if (value == "RH") {
                        mScoreNumber.setText(String.format(Locale.US, "%s", ""));
                    } else {
                        mScoreNumber.setText(String.format(Locale.US, "%s", value));
                    }
                    mScoreValue.setCyclic(false);

                }
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
        public void nameNewImage(ContentResolver resolver, long date, String score, String scoreType) {
            NamedEntity r = new NamedEntity();
            r.title = Util.createJpegName(date, score, scoreType);
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

    public File getLastPicture() {
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        String selectionClause = MediaStore.Images.ImageColumns.DATA + " LIKE ?";
        String[] selectionArgs = { "%Darts%" };
        final Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selectionClause,
                        selectionArgs, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

// Put it in the image view
        if (cursor.moveToFirst()) {
            String imageLocation = cursor.getString(1);
            Log.d(TAG, "getLastPicture:path:"+imageLocation);
            File imageFile = new File(imageLocation);
            if (imageFile.exists()) {   // TODO: is there a better way to do this?
                return imageFile;
            } else {
                return null;
            }
        }
        return null;
    }

}

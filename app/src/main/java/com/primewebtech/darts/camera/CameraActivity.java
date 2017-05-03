package com.primewebtech.darts.camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.primewebtech.darts.R;

import java.io.ByteArrayInputStream;

public class CameraActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final String TAG = CameraActivity.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    private int cameraId = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageButton mPreviousImageThumbnail;
    private ImageButton mSaveImageButton;
    private ImageButton mBackButton;
    private ImageButton mTakePhotoButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
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
                    Log.d(TAG, "onCreate:openingCamera:done");
                } else {
                    Log.d(TAG, "onCreate:OLD_VERSION:openingCamera");
                    mCamera = Camera.open(cameraId);
                    Log.d(TAG, "onCreate:OLD_VERSION:openingCamera:done");
                }

            }
        }



        // Create our Preview view and set it as the content of our activity.
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
        Log.d(TAG, "onCLick:sSaving photo");
        mCamera.startPreview();
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mTakePhotoButton.setVisibility(View.VISIBLE);

    }
    public void onBackButtonClick(View view) {
        Log.d(TAG, "onBackButtonClick:resetting camera");
        mCamera.startPreview();
        mSaveImageButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
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
        releaseCamera();              // release the camera immediately on pause event
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

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
//            new SaveImageTask().execute(data);
//            resetCam();
            Log.d(TAG, "onPictureTaken - jpeg");
            mSaveImageButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.VISIBLE);
            mTakePhotoButton.setVisibility(View.GONE);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
            mPreviousImageThumbnail.setImageBitmap(imageBitmap);
            saveImage(data);
        }
    };

    private void resetCam() {
        mCamera.startPreview();
    }

    private void saveImage(byte[] data) {
//        File pictureFileDir = this.getDir();

//        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
//
//            Log.d(TAG, "Can't create directory to save image.");
//            Toast.makeText(this, "Can't create directory to save image.",
//                    Toast.LENGTH_LONG).show();
//            return;
//
//        }



    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            Log.d(TAG, "checkCameraHardware:found");
            return true;
        } else {
            // no camera on this device
            Log.d(TAG, "checkCameraHardware:not found");
            return false;
        }
    }
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            cameraId = Util.findFrontFacingCamera();
            c = Camera.open(); // attempt to get a Camera instance
            Log.d(TAG, "getCameraInstance:success");

        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "getCameraInstance:failed");
        }
        return c; // returns null if camera is unavailable
    }



}

package com.primewebtech.darts.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by benebsworth on 1/5/17.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean cameraConfigured;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mCamera.setDisplayOrientation(90);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        setFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            Log.d(TAG, "surfaceCreated:trying");
            mCamera.getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            Log.d(TAG, "surfaceCreated:success");
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
//        mCamera.release();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
//            setFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            initPreview(w, h);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();



        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private void initPreview(int width, int height) {
        if (mCamera != null && mHolder.getSurface() != null) {
            try {
                mCamera.setPreviewDisplay(mHolder);
            }
            catch (Throwable t) {
                Log.e(TAG, ":Exception init)", t);
            }

            if (!cameraConfigured) {
                Camera.Parameters parameters=mCamera.getParameters();
                Camera.Size size=getBestPreviewSize(width, height, parameters);
                Camera.Size pictureSize=getLargestPictureSize(parameters);

                if (size != null && pictureSize != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    parameters.setPictureSize(pictureSize.width,
                            pictureSize.height);
                    List<String> focusModes = parameters.getSupportedFocusModes();
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        // Autofocus mode is supported
                        Log.d(TAG, "SETTING_FOCUS_MODE");
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    mCamera.setParameters(parameters);
                    cameraConfigured=true;
                }
            }
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result=size;
                }
                else {
                    int resultArea=result.width * result.height;
                    int newArea=size.width * size.height;

                    if (newArea > resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }
    private Camera.Size getLargestPictureSize(Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result=size;
            }
            else {
                int resultArea=result.width * result.height;
                int newArea=size.width * size.height;

                if (newArea > resultArea) {
                    result=size;
                }
            }
        }

        return(result);
    }



}
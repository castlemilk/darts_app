package com.primewebtech.darts.gallery;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.util.List;

/**
 * Created by benebsworth on 7/5/17.
 */

public class GalleryActivity2 extends Activity {
    private static final String GOOGLE_PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos";
    private static final String TAG = GalleryActivity2.class.getSimpleName();
    public static final String DIRECTORY_PICTURES = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).toString();
    public String[] allFiles;
    private String SCAN_PATH ;
    private static final String FILE_TYPE="image/*";
    private MediaScannerConnection conn;

    public static final String APP_DIRECTORY = DIRECTORY_PICTURES + "/Darts";
    public static final String BUCKET_ID =
            String.valueOf(APP_DIRECTORY.toLowerCase().hashCode());

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath(), "Darts");
//        File pictures_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        File folder = new File(pictures_dir, "Darts");
//        allFiles = folder.list();
//        Log.d(TAG, "gallery:onCreate:folder:getPath:"+folder.getPath());
//        Log.d(TAG, "gallery:getFileDir:"+getFilesDir().getPath());
//        Log.d(TAG, "gallery:getExternalStoragrDir:"+getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
//        Log.d(TAG, "gallery:getExternalStoragrDirectory:"+Environment.getExternalStorageDirectory().toString());
//        Log.d(TAG, "gallery:allFiles:"+allFiles.toString());
//        for (int i = 0; i < allFiles.length; i++) {
//            Log.d(TAG, "gallery:allFiles:"+allFiles[i].getPath());
//        }
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        String path = folder.getPath();
//        Uri uri;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            uri = FileProvider.getUriForFile(this, "com.primewebtech.darts.fileprovider", folder);
//            Log.d(TAG, "gallery:N+:"+uri.getPath());
//
//            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//            for (ResolveInfo resolveInfo : resInfoList) {
//                String packageName = resolveInfo.activityInfo.packageName;
//                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }
//        } else {
//            uri = Uri.fromFile(new File(path));
//            Log.d(TAG, "gallery:N+:"+uri.getPath());
//        }

//        String bucketId = "";
//
//        final String[] projection = new String[] {"DISTINCT " + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ", " + MediaStore.Images.Media.BUCKET_ID};
//        final Cursor cur = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
//
//        while (cur != null && cur.moveToNext()) {
//            final String bucketName = cur.getString((cur.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)));
//            Log.d(TAG, "bucketID:found:"+bucketName);
//            if (bucketName.equals("Darts")) {
//                bucketId = cur.getString((cur.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)));
//                Log.d(TAG, "bucketID:found:"+bucketId.toString());
//                break;
//            }
//        }
//        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        if (bucketId.length() > 0) {
//            mediaUri = mediaUri.buildUpon()
//                    .authority("media")
//                    .appendQueryParameter("bucketId", bucketId)
//                    .build();
//        }
//        Log.d(TAG, "mediaUri:path:"+mediaUri.getPath());
//        Log.d(TAG, "mediaUri:auth:"+mediaUri.getAuthority());
//        Log.d(TAG, "mediaUri:query:"+mediaUri.getQuery());
////        new SingleMediaScanner(GalleryActivity2.this, allFiles[0]);
//        Intent intent = new Intent(Intent.ACTION_VIEW, mediaUri);
//        startActivity(intent);

//        intent.setDataAndType(uri, "image/*");
//        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        new SingleMediaScanner(GalleryActivity2.this, allFiles[0]);
//        Intent i = new Intent();
//        i.setAction(Intent.ACTION_VIEW);
//        i.setDataAndType(Uri.withAppendedPath(Uri.fromFile(pictures_dir), "/Darts"), "image/*");
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(i);
//        new SingleMediaScanner(GalleryActivity2.this, allFiles[0]);
    }




//    public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
//
//        private MediaScannerConnection mMs;
//        private File mFile;
//
//        public SingleMediaScanner(Context context, File f) {
//            mFile = f;
//            mMs = new MediaScannerConnection(context, this);
//            mMs.connect();
//        }
//
//        public void onMediaScannerConnected() {
//            mMs.scanFile(mFile.getAbsolutePath(), null);
//        }
//
//        public void onScanCompleted(String path, Uri uri) {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            Log.d(TAG, "file:getAbsolutePath:"+mFile.getAbsolutePath());
//
//            intent.setDataAndType(uri, "image/*");
//            startActivity(intent);
//            mMs.disconnect();
//        }
//
//    }
public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private String SCAN_PATH ;
    private static final String FILE_TYPE="*/*";
    private MediaScannerConnection mMs;

    public SingleMediaScanner(Context context, String mFilePath) {
        SCAN_PATH=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/Darts/";
        Log.d("SCAN PATH", "Scan Path " + SCAN_PATH);
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    public void onMediaScannerConnected() {
        Log.d("onMediaScannerConnected","success"+mMs);
        mMs.scanFile(SCAN_PATH, FILE_TYPE);
    }

    public void onScanCompleted(String path, Uri uri) {
        try {
            Log.d("onScanCompleted", uri + "success" + mMs);
            System.out.println("URI " + uri);
            if (uri != null) {
                if (isGooglePhotosInstalled()) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    intent.setType("image/*");
                    List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, 0);
                    for (int i = 0; i < resolveInfoList.size(); i++) {
                        if (resolveInfoList.get(i) != null) {
                            String packageName = resolveInfoList.get(i).activityInfo.packageName;
                            if (GOOGLE_PHOTOS_PACKAGE_NAME.equals(packageName)) {
                                intent.setComponent(new ComponentName(packageName, resolveInfoList.get(i).activityInfo.name));
                                startActivityForResult(intent, 100);
                                return;
                            }
                        }
                    }
                    try {
                        startActivityForResult(intent, 100);
                    } catch (ActivityNotFoundException e) {
//                        showErrorMsgDialog("You don't have Google Photos installed! Download it from the play store today.");
                        e.printStackTrace();
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    startActivity(intent);
                }

            }
        } finally {
            mMs.disconnect();
            mMs = null;
            }
    }

}

    public boolean isGooglePhotosInstalled() {
        PackageManager packageManager = getPackageManager();
        try {
            return packageManager.getPackageInfo(GOOGLE_PHOTOS_PACKAGE_NAME, PackageManager.GET_ACTIVITIES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}

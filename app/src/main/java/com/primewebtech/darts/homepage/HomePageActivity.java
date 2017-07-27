package com.primewebtech.darts.homepage;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.primewebtech.darts.R;
import com.primewebtech.darts.camera.CameraActivity;
import com.primewebtech.darts.gallery.GalleryActivity;
import com.primewebtech.darts.scoring.HundredDartActivity;
import com.primewebtech.darts.scoring.OneDartActivity;
import com.primewebtech.darts.scoring.ThreeDartActivity;
import com.primewebtech.darts.scoring.TwoDartActivity;
import com.primewebtech.darts.statistics.StatsSummaryActivity;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = HomePageActivity.class.getSimpleName();
    private final int GALLERY_STORAGE_REQUEST = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        final Application app = getApplication();
        //Scoring buttons
        Button darts1 = (Button) findViewById(R.id.darts1);
        darts1.setOnClickListener(this);
        Button darts2 = (Button) findViewById(R.id.darts2);
        darts2.setOnClickListener(this);
        Button darts3 = (Button) findViewById(R.id.darts3);
        darts3.setOnClickListener(this);
        Button darts100 = (Button) findViewById(R.id.darts100);
        darts100.setOnClickListener(this);

        //Misc buttons
        ImageButton statistics = (ImageButton) findViewById(R.id.stats_button);
        statistics.setOnClickListener(this);
        ImageButton gallery = (ImageButton) findViewById(R.id.gallery_button);
        gallery.setOnClickListener(this);
        ImageButton camera = (ImageButton) findViewById(R.id.camera_button);
        camera.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Intent oneDartIntent = new Intent(HomePageActivity.this, OneDartActivity.class);
        Intent twoDartIntent = new Intent(HomePageActivity.this, TwoDartActivity.class);
        Intent threeDartIntent = new Intent(HomePageActivity.this, ThreeDartActivity.class);
        Intent hundredDartIntent = new Intent(HomePageActivity.this, HundredDartActivity.class);
        Intent cameraIntent = new Intent(HomePageActivity.this, CameraActivity.class);
        Intent statsOneIntent = new Intent(HomePageActivity.this, StatsSummaryActivity.class);
        Intent galleryIntent = new Intent(HomePageActivity.this, GalleryActivity.class);

        switch (view.getId()) {
            case R.id.darts1:
                // route to darts1 based scoring
                Log.d(TAG, "darts1:selected");
                startActivity(oneDartIntent);
                break;
            case R.id.darts2:
                // route to darts2 based scoring
                Log.d(TAG, "darts2:selected");
                startActivity(twoDartIntent);
                break;
            case R.id.darts3:
                // route to darts3 based scoring
                Log.d(TAG, "darts3:selected");
                startActivity(threeDartIntent);
                break;
            case R.id.darts100:
                // route to darts100 based scoring
                Log.d(TAG, "darts100:selected");
                startActivity(hundredDartIntent);
                break;
            case R.id.stats_button:
                // route to stats based scoring
                Log.d(TAG, "stats:selected");
                startActivity(statsOneIntent);
                break;
            case R.id.gallery_button:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                            PackageManager.PERMISSION_GRANTED) {
                        startActivity(galleryIntent);

                    } else {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                               GALLERY_STORAGE_REQUEST);
                    }
                }

                break;
            case R.id.camera_button:
                // route to camera based scoring
                Log.d(TAG, "camera:selected");
                startActivity(cameraIntent);
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_STORAGE_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent galleryIntent = new Intent(HomePageActivity.this, GalleryActivity.class);
            startActivity(galleryIntent);
        }
    }
}

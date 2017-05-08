package com.primewebtech.darts.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.primewebtech.darts.R;
import com.primewebtech.darts.camera.CameraActivity;
import com.primewebtech.darts.gallery.GalleryActivity;
import com.primewebtech.darts.scoring.ScoringActivity;

import java.io.File;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = HomePageActivity.class.getSimpleName();
    public static final String APP_DIRECTORY = Environment.getExternalStorageDirectory().getPath()+"/Pictures/Darts/";
    private File[] allFiles ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);


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
        Intent scoringIntent = new Intent(HomePageActivity.this, ScoringActivity.class);
        Intent cameraIntent = new Intent(HomePageActivity.this, CameraActivity.class);
        Intent galleryIntent = new Intent(HomePageActivity.this, GalleryActivity.class);
        switch (view.getId()) {
            case R.id.darts1:
                // route to darts1 based scoring
                Log.d(TAG, "darts1:selected");
                scoringIntent.putExtra("SCORING_TYPE", R.id.darts1);
                startActivity(scoringIntent);
                break;
            case R.id.darts2:
                // route to darts2 based scoring
                Log.d(TAG, "darts2:selected");
                scoringIntent.putExtra("SCORING_TYPE", R.id.darts2);
                startActivity(scoringIntent);
                break;
            case R.id.darts3:
                // route to darts3 based scoring
                Log.d(TAG, "darts3:selected");
                scoringIntent.putExtra("SCORING_TYPE", R.id.darts3);
                startActivity(scoringIntent);
                break;
            case R.id.darts100:
                // route to darts100 based scoring
                Log.d(TAG, "darts100:selected");
                scoringIntent.putExtra("SCORING_TYPE", R.id.darts100);
                startActivity(scoringIntent);
                break;
            case R.id.stats_button:
                // route to stats based scoring
                Log.d(TAG, "stats:selected");
                break;
            case R.id.gallery_button:
                startActivity(galleryIntent);
                break;
            case R.id.camera_button:
                // route to camera based scoring
                Log.d(TAG, "camera:selected");
                startActivity(cameraIntent);
                break;
        }

    }
}

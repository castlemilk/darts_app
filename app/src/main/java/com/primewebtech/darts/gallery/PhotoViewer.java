package com.primewebtech.darts.gallery;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.primewebtech.darts.R;

import org.malcdevelop.cyclicview.CyclicAdapter;
import org.malcdevelop.cyclicview.CyclicView;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by benebsworth on 5/8/17.
 */

public class PhotoViewer extends FragmentActivity {
    CyclicView mViewPager;
    private File pictureDirectory;
    private List<File> photos;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pictureDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Darts/");
        photos = sortedFiles();
        setContentView(R.layout.activity_photoviewer);
        Bundle b = getIntent().getExtras();

        int position = 0;
        if (b != null) {
            position = b.getInt("POSITION");
        }

        mViewPager = (CyclicView) findViewById(R.id.pager);
        mViewPager.setChangePositionFactor(4000);

        mViewPager.setAdapter(new CyclicAdapter() {
            @Override
            public int getItemsCount() {
                return photos.size();
            }

            @Override
            public View createView(int i) {
                ImageView photo = new ImageView(PhotoViewer.this);
                Glide.with(getBaseContext()).load("file:///"+photos.get(i).getPath()).crossFade(500).into(photo);
                return photo;
            }

            @Override
            public void removeView(int i, View view) {

            }
        });
        mViewPager.setCurrentPosition(position);
    }

    public List<File> sortedFiles() {
        List<File> files = Arrays.asList(pictureDirectory.listFiles());
        Collections.sort(files, Collections.reverseOrder());
        return files;
    }
}

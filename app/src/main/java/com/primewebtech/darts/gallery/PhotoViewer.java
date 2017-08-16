package com.primewebtech.darts.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.primewebtech.darts.BuildConfig;
import com.primewebtech.darts.R;

import org.malcdevelop.cyclicview.CyclicAdapter;
import org.malcdevelop.cyclicview.CyclicView;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by benebsworth on 5/8/17.
 */

public class PhotoViewer extends FragmentActivity {
    CyclicView mViewPager;
    CyclicAdapter mCyclicAdapter;
    private File pictureDirectory;
    private LinkedList<File> photos;
    private ImageButton mShareButton;
    private ImageButton mDeleteButton;
    public static final String TAG = PhotoViewer.class.getSimpleName();



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pictureDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Darts/");
        photos = new LinkedList<>(sortedFiles());
        setContentView(R.layout.activity_photoviewer);
        mShareButton = (ImageButton) findViewById(R.id.gallery_photoview_share);
        mDeleteButton = (ImageButton) findViewById(R.id.gallery_photoview_delete);
        Bundle b = getIntent().getExtras();

        int position = 0;
        if (b != null) {
            position = b.getInt("POSITION");
        }

        mViewPager = (CyclicView) findViewById(R.id.pager);
        mViewPager.setChangePositionFactor(4000);
        mCyclicAdapter = new CyclicAdapter() {
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
        };
        mViewPager.setAdapter(mCyclicAdapter);
        mViewPager.setCurrentPosition(position);

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent  = shareIntentMaker();
                startActivity(shareIntent);
            }
        });
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = mViewPager.getCurrentPosition();
                Log.d(TAG, "DELETING:ITEM:INDEX:"+position);
                Log.d(TAG, "DELETING:ITEM:PHOTO:LENGTH:"+photos.size());
                File photo;
                if (position >= photos.size()) {
                    photo = photos.get(position-1);
                    photos.remove(position-1);
                } else {
                    photo = photos.get(position);
                    photos.remove(position);
                }
//                    mViewPager.removeViewAt(position);
                Log.d(TAG, "DELETING:PHOTO:"+photo.getPath());
                photo.delete();
                mCyclicAdapter = new CyclicAdapter() {
                    @Override
                    public int getItemsCount() {
                        return photos.size();
                    }

                    @Override
                    public View createView(int i) {
                        ImageView photo = new ImageView(PhotoViewer.this);
                        if (photos.size() == 1) {
                            Glide.with(getBaseContext()).load("file:///"+photos.get(0).getPath()).crossFade(500).into(photo);
                        } else {
                            Glide.with(getBaseContext()).load("file:///"+photos.get(i).getPath()).crossFade(500).into(photo);
                        }

                        return photo;
                    }

                    @Override
                    public void removeView(int i, View view) {


                    }
                };
                if (position < photos.size()) {
                    mViewPager.setAdapter(mCyclicAdapter);
                    mViewPager.setCurrentPosition(position+1);
                }  else if ( photos.size() == 1) {
                    mViewPager.setCurrentPosition(0);
                } else if (photos.size() == 0) {
                    goBackToEmptyGallery();

                }



            }
        });
    }

    private void goBackToEmptyGallery() {
        Intent gallery = new Intent(this, GalleryActivity.class);
        Bundle b = new Bundle();
        b.putString("STATE", "EMPTY");
        gallery.putExtras(b);
        startActivity(gallery);
        finish();
    }

    private Intent shareIntentMaker() {
        Intent shareIntent  = new Intent(Intent.ACTION_SEND);
        File file = photos.get(mViewPager.getCurrentPosition());
        Uri photoURI = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".provider",
                new File(file.getPath()));
        Log.d(TAG, "shareIntentMaker:SHARING:"+file.getPath());
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        shareIntent.setType("image/png");
        return shareIntent;
    }

    public List<File> sortedFiles() {
        List<File> files = Arrays.asList(pictureDirectory.listFiles());
        Collections.sort(files, Collections.reverseOrder());
        return files;
    }
}

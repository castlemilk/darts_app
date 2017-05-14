package com.primewebtech.darts.gallery;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.primewebtech.darts.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GalleryActivityNew
        extends Activity
        {
    private static final String TAG = GalleryActivityNew.class.getSimpleName();
    private File pictureDirectory;
    private List<File> sortedFiles;
    private RecyclerView mRecyclerView;
    private SimpleAdapter mAdapter;
    public ActionMode mActionMode;
    public Util.DateOrganiser mDateOrganiser;
    public boolean mSelectedAll;
    SectionedGridRecyclerViewAdapter mSectionedAdapter;
//    private GalleryActionBarCompat mGalleryActionBarCompat;
    private GalleryActionBar mGalleryActionBar;
//    private ShareActionProvider mShareActionProvider;
//    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        pictureDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Darts/");

//        Util.getHeaderIndexes(pictureDirectory);
        mDateOrganiser = new Util.DateOrganiser(pictureDirectory);
        mDateOrganiser.todaysFiles();
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,4));
        mAdapter = new SimpleAdapter(this);
        mSelectedAll = false;
        List<SectionedGridRecyclerViewAdapter.Section> sections =
                new ArrayList<SectionedGridRecyclerViewAdapter.Section>();
            //Sections
            /*
            Need to implement a top level algorithm here which generates the correct index to insert
            the title corresponding to some date range. For example "Today" can be inserting at 0 only
            if it was found that there are images which were taken today. Otherwise the next ranges
            should be changed and follow a similar principle of only being inserted if images are found
            in that range.

            We have the adapter handling a generic layout of N images which is not concerned with the
            placement of title. Not sure if that is a correct approach.
            psuedo code:

            TODAY:
            if (imagesTakenToday()) {
              insert @ 0
            }
            LASTWEEK:
            int lastWeekIndex = getLastWeekIndex()
            insert @ lastWeekIndex
            LASTMONTH:
            int lastMonthIndex = getLastMonthIndex()
            for month in months:
              int monthIndex = getMonthIndex(month)
              insert @ monthIndex

            ... keep going how far back? after a certain point we could just show the remaining pictures.

            more simplified approach:

            for day in days:
                insert day_header @ day demarcation

             */
//        List<File> sortedFiles = mDateOrganiser.sortedFiles();
//        Date dow = new Date();
//
//        for (File file : sortedFiles) {
//            if (!dow.equals(mDateOrganiser.getDay(file))) {
//                dow = mDateOrganiser.getDay(file);
//                Log.d(TAG, "day: "+dow.toString());
//            }
//            Log.d(TAG, new Date(file.lastModified()).toString());
//        }
        List<Map.Entry<Integer, String>> indices = mDateOrganiser.getDayIndices();
        for ( Map.Entry<Integer, String> index : indices) {
            if (index.getKey() == 0) {
                sections.add(new SectionedGridRecyclerViewAdapter.Section(0,"Today"));
            } else {
                sections.add(new SectionedGridRecyclerViewAdapter.Section(index.getKey(), index.getValue()));
            }
        }

        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter = new
                SectionedGridRecyclerViewAdapter(this,R.layout.section,R.id.section_text,mRecyclerView,mAdapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));
        mRecyclerView.setAdapter(mSectionedAdapter);


        }


    public void onLongPress(int position) {
        if (mActionMode != null) {
            return;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mGalleryActionBar = new GalleryActionBar();
            mActionMode = startActionMode(mGalleryActionBar);

        } else {
//            mGalleryActionBarCompat = new GalleryActionBarCompat();
        }

        Log.d(TAG, "Initialising actionBar:"+mActionMode);
        myToggleSelection();

    }



    public void myToggleSelection() {

        String title = getString(
                R.string.selected_count,
                mAdapter.getSelectedItemCount());
        mActionMode.setTitle(title);
    }




    private Intent shareIntentMaker() {
        Intent shareIntent  = new Intent(Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> imageUris = new ArrayList<>();
        List<File> files = mAdapter.getSelectedFiles();
        for ( File file : files) {
            imageUris.add(FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", file));
        }
        Log.d(TAG, imageUris.toString());
        shareIntent.setType("image/*");
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        return shareIntent;
    }

    private class GalleryActionBar implements android.view.ActionMode.Callback {
        private android.widget.ShareActionProvider mShareActionProvider;

        @Override
        public boolean onCreateActionMode(android.view.ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.gallery_selection_new, menu);
            final MenuItem item = menu.findItem(R.id.menu_item_share_new);
            mShareActionProvider = (android.widget.ShareActionProvider) item.getActionProvider();
//            mShareActionProvider = (android.widget.ShareActionProvider) MenuItemCompat.getActionProvider(item);
            mShareActionProvider.setShareIntent(shareIntentMaker());
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode actionMode, Menu menu) {
            Log.d(TAG, "onPrepareActionMode:preparing:");

            ImageView unselected;


            for (int i =0; i< mRecyclerView.getChildCount(); i++) {
                unselected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.unselected);
                if (unselected != null) {
                    unselected.setVisibility(View.VISIBLE);
                }

            }
            Log.d(TAG, "onPrepareActionMode:preparing:");
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode actionMode, MenuItem menuItem) {
            Log.d(TAG, "onActionItemClicked");
            switch (menuItem.getItemId()) {
                case R.id.menu_delete_new:
                    Log.d(TAG, "onActionItemClicked:menu_delete:");
                    List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
                    int currPos;
                    mActionMode.finish();
                    break;
                case R.id.menu_select_all_new:
                    Log.d(TAG, "onActionItemClicked:menu_select_all:");
                    if (!mSelectedAll) {
                        ImageView unselected;
                        ImageView selected;
                        String[] items = pictureDirectory.list();
                        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
                            unselected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.unselected);
                            selected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.selected);
                            if (unselected != null) {
                                unselected.setVisibility(View.GONE);

                            }
                            if (selected != null) {
                                selected.setVisibility(View.VISIBLE);
                            }

                        }

                        mAdapter.selectAll();
                        myToggleSelection();
                        mSelectedAll = true;

                    } else {
                        ImageView unselected;
                        ImageView selected;
                        String[] items = pictureDirectory.list();
                        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
                            unselected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.unselected);
                            selected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.selected);
                            if (unselected != null) {
                                unselected.setVisibility(View.VISIBLE);

                            }
                            if (selected != null) {
                                selected.setVisibility(View.GONE);
                            }

                        }
                        mAdapter.clearSelections();
                        myToggleSelection();
                        mSelectedAll = false;


                    }
                    break;
                case R.id.menu_item_share_new:
                    Log.d(TAG, "share button clicked");
                    mShareActionProvider.setShareIntent(shareIntentMaker());
                    break;
                default:
                    break;

            }
            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode actionMode) {
            mActionMode = null;
            mAdapter.clearSelections();
            ImageView unselected;
            ImageView selected;
            for (int i =0; i< mRecyclerView.getChildCount(); i++) {
                unselected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.unselected);
                selected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.selected);
                if (unselected != null) {
                    unselected.setVisibility(View.GONE);
                    selected.setVisibility(View.GONE);
                }

            }

        }
    }






        }

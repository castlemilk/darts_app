package com.primewebtech.darts.gallery;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.primewebtech.darts.R;
import com.primewebtech.darts.camera.CameraActivity;
import com.primewebtech.darts.homepage.HomePageActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flipview.FlipView;


/**
 * Created by benebsworth on 20/5/17.
 */

public class GalleryActivity extends AppCompatActivity
        implements ActionMode.Callback,
        FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnItemLongClickListener,
        FlexibleAdapter.OnUpdateListener,
        FlexibleAdapter.OnDeleteCompleteListener, FastScroller.OnScrollStateChangeListener {
    //TODO: add score labelling on gallery items based on filename


    /**
     * RecyclerView and related objects
     */
    private RecyclerView mRecyclerView;
    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    private int mColumnCount = 4;
    public boolean inActionMode = false;
    public ActionMode mActionMode;
    public boolean selectedAll = false;
    private ShareActionProvider mShareActionProvider;
    private String state;



    public static final String TAG = GalleryActivity.class.getSimpleName();

    /* ===================
	 * ACTIVITY MANAGEMENT
	 * =================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Log.d(TAG, "onCreate");
        Bundle b = getIntent().getExtras();
        if (b != null) {
            state = b.getString("STATE", "FULL");
        } else {
            state = "FULL";
        }


        if (!state.contains("EMPTY")) {
            FlexibleAdapter.enableLogs(true);
            if (savedInstanceState == null) {
                GalleryDatabaseService.getInstance(this).createHeadersSectionsGalleryDatasetByMonth();
            }
            initializeRecylerView(savedInstanceState);
            inActionMode = false;
        } else {
            onUpdateEmptyView(0);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "onSaveInstanceState!");
        if (mAdapter != null) {
            mAdapter.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore previous state
        if (savedInstanceState != null && mAdapter != null) {
            // Selection
            mAdapter.onRestoreInstanceState(savedInstanceState);
            if (mAdapter.getSelectedItemCount() > 0) {
                mActionMode = startSupportActionMode(this);
                setContextTitle(mAdapter.getSelectedItemCount());
            }
        }
    }

    /* ======================
	 * INITIALIZATION METHODS
	 * ====================== */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallery_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        switch (item.getItemId())
        {
            case R.id.menu_item_take_photo:
                Intent cameraIntent = new Intent(GalleryActivity.this, CameraActivity.class);
                startActivity(cameraIntent);
                finish();
                return true;
            default:
                return false;
        }
    }

    private void initializeRecylerView(Bundle savedInstanceState) {

        mAdapter = new GalleryAdapter(GalleryDatabaseService.getInstance(this).getDatabaseList(), this);
        mAdapter.setAnimationOnScrolling(false);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(createNewGridLayoutManager());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        FastScroller fastScroller = (FastScroller) findViewById(R.id.fast_scroller);
        mAdapter.setFastScroller(fastScroller,
                Utils.getColorAccent(this), this);

        mAdapter.setDisplayHeadersAtStartUp(true)
                .setStickyHeaders(true)
                .showAllHeaders();

    }



    /* ========================================================================
	 * FLEXIBLE ADAPTER LISTENERS IMPLEMENTATION
	 * Listeners implementation are in MainActivity to easily reuse the common
	 * components like SwipeToRefresh, ActionMode, NavigationView, etc...
	 * ======================================================================== */

    @Override
    public boolean onItemClick(int position) {


        if (mActionMode != null && position != RecyclerView.NO_POSITION) {
            toggleSelection(position);
            return true;
        } else {
            try {
                PhotoItem photoItem = (PhotoItem) mAdapter.getItem(position);
                Log.d(TAG, "onItemClick:Clicked:postition:"+position);

//                Intent intent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoItem.getFile());
                // ROUTE TO SINGULAR GALLERY ITEM
//                intent.setDataAndType(photoURI, "image/*");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                startActivity(intent);
                // ROUTE TO VIEWPAGER and enable swiping through photos.
                int itemPosition = GalleryDatabaseService.getInstance(this).mItems.indexOf(photoItem);
                Log.d(TAG, "onItemClick:Clicked:itemPostition:"+itemPosition);
                Intent viewer = new Intent(this, PhotoViewer.class);
                viewer.putExtra("POSITION", itemPosition);
                viewer.putExtra("PHOTO_PATH", photoURI.getPath());
                startActivity(viewer);
                return false;
            } catch ( Exception e) {
                return true;
            }


        }

        //TODO: navigate to selected image

    }

    @Override
    public void onItemLongClick(int position) {
        Log.d(TAG, "onItemLongClick:longClick:postition:"+position);
        if (mActionMode == null ) {
            mActionMode = startSupportActionMode(this);
        }
        toggleSelection(position);

    }

    private void toggleSelection(int position) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            mActionMode.finish();
        } else {
            setContextTitle(count);
            mShareActionProvider.setShareIntent(shareIntentMaker());
        }
    }

    private void setContextTitle(int count) {
        Log.d(TAG, "SHARING:"+mAdapter.getSelectedPositions().toString());
        if ( mActionMode == null) {
            return;
        }
        mActionMode.setTitle(count == 1 ?
                getString(R.string.action_selected_one, Integer.toString(count)) :
                getString(R.string.action_selected_many, Integer.toString(count)));
    }

    /**
     * Handling RecyclerView when empty.
     * <p><b>Note:</b> The order, how the 3 Views (RecyclerView, EmptyView, FastScroller)
     * are placed in the Layout, is important!</p>
     */
    @Override
    public void onUpdateEmptyView(int size) {
        Log.d(TAG, "onUpdateEmptyView size=" + size);
        FastScroller fastScroller = (FastScroller) findViewById(R.id.fast_scroller);
        View emptyView = findViewById(R.id.empty_view);
        TextView emptyText = (TextView) findViewById(R.id.empty_text);
        if (emptyText != null)
            Log.d(TAG, "onUpdateEmptyView:empty:display:empty_view");
            emptyText.setText(getString(R.string.no_items));

        if (size > 0) {
            fastScroller.setVisibility(View.VISIBLE);
            emptyText.setAlpha(0);
            emptyView.setAlpha(0);


        } else {
            emptyView.setBackgroundColor(Color.BLACK);
            if (mAdapter != null) {
                mAdapter.hideAllHeaders();
            }
            emptyView.setAlpha(1);
            emptyText.setAlpha(1);
            if (mRecyclerView != null) {
                mRecyclerView.setAlpha(0);
            }
            fastScroller.setVisibility(View.GONE);
        }
    }

    public List<IHeader> getReferenceList() {
        return mAdapter.getHeaderItems();
    }

     /* ====================================
	 * OPTION MENU PREPARATION & MANAGEMENT
	 * ==================================== */
     protected GridLayoutManager createNewGridLayoutManager() {
         GridLayoutManager gridLayoutManager = new SmoothScrollGridLayoutManager(this, mColumnCount);
         if (mAdapter == null) {
             return gridLayoutManager;
         }

         gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
             @Override
             public int getSpanSize(int position) {
                 //noinspection ConstantConditions
                 switch (mAdapter.getItemViewType(position)) {
//                     case R.layout.recycler_scrollable_usecase_item:
//                     case R.layout.recycler_scrollable_header_item:
//                     case R.layout.recycler_scrollable_footer_item:
//                     case R.layout.recycler_scrollable_layout_item:
//                     case R.layout.recycler_scrollable_uls_item:
                     case R.layout.holder_header: return 4;
                     default:
                         return 1;
                 }
             }
         });
         return gridLayoutManager;
     }




    @Override
    public void onDeleteConfirmed() {
        //TODO: implement deletion
        if (mAdapter == null) {
            return;
        }
        for (AbstractFlexibleItem adapterItem : mAdapter.getDeletedItems()) {
            for (Integer pos : mAdapter.getSelectedPositions()) {
                Log.d(TAG, "onDeleteConfirmed:"+pos);

            }
        }
    }


    /* ==========================
	 * ACTION MODE IMPLEMENTATION
	 * ========================== */



    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (Utils.hasMarshmallow()) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlack, this.getTheme()));
        } else if (Utils.hasLollipop()) {
            //noinspection deprecation
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlack));
        }
        mode.getMenuInflater().inflate(R.menu.gallery_selection, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(shareIntentMaker());
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        FlipView flipView;
        if (mRecyclerView == null) {
            return false;
        }
        for (int i =0; i< mRecyclerView.getChildCount(); i++) {
            flipView = (FlipView) mRecyclerView.getChildAt(i).findViewById(R.id.image);
            if (flipView != null) {
                flipView.setVisibility(View.VISIBLE);
            }

        }
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Log.d(TAG, "onActionItemClicked:SHARING:"+mAdapter.getSelectedPositions().toString());
        Log.d(TAG, "onActionItemClicked:item.getItemId:"+item.getItemId());
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                Log.d(TAG, "onActionItemClicked:SHARING:");
                mShareActionProvider.setShareIntent(shareIntentMaker());
                return true;
            case R.id.menu_select_all:
                toggleSelectAll();
                return true;
            case R.id.menu_delete:
                Log.d(TAG, "onActionItemClicked:DELETE");
                if (mAdapter == null) {
                    return false;
                }
                for ( Integer position: mAdapter.getSelectedPositions()) {
                    if ( mAdapter.getItem(position) != null &&
                            mAdapter.getItem(position).isSelectable()) {
                            GalleryDatabaseService.getInstance(this).removeItem(mAdapter.getItem(position));
                            File file = ((PhotoItem) mAdapter.getItem(position)).getFile();
                            if (file.delete()) {
                                Log.d(TAG, "onActionItemClicked:DELETE:file:done");
                            } else {
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                    }
                }
                mAdapter.removeAllSelectedItems();
                mode.finish();
                return false;
            default:
                return false;

        }
    }

    public void toggleSelectAll() {
        if ( mAdapter == null ) {
            return;
        }
        if (selectedAll) {
            mAdapter.clearSelection();
            setContextTitle(mAdapter.getSelectedItemCount());
            selectedAll = false;

        } else {
            selectedAll = true;
            mAdapter.selectAll();
            setContextTitle(mAdapter.getSelectedItemCount());
        }

    }

    private Intent shareIntentMaker() {
        //TODO: fix this so it actually shares all selected photos
        Intent shareIntent  = new Intent(Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> imageUris = new ArrayList<>();
        Log.d(TAG, "shareIntentMaker:SHARING:"+mAdapter.getSelectedPositions().toString());
        List<File> files = new ArrayList<>();
        for ( Integer position: mAdapter.getSelectedPositions()) {
            files.add(((PhotoItem) mAdapter.getItem(position)).getFile());
        }

        for ( File file : files) {
            Log.d(TAG, "shareIntentMaker:file:"+file.getPath());
            imageUris.add(FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", file));
        }
        Log.d(TAG, imageUris.toString());
        shareIntent.setType("image/*");
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        return shareIntent;
    }

    /* ======
	 * EXTRAS
	 * ====== */

    @Override
    public void onBackPressed() {
        if (mAdapter != null) {
            mAdapter.setMode(FlexibleAdapter.MODE_IDLE);
        }
        mActionMode = null;
        GalleryDatabaseService.onDestroy();
        super.onBackPressed();
        Intent homepage = new Intent(this, HomePageActivity.class);
        startActivity(homepage);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        mRecyclerView = null;
    }



    @Override
    public void onDestroyActionMode(ActionMode mode) {
        inActionMode = false;
        mActionMode = null;
        inActionMode = true;
        if (mAdapter == null || mRecyclerView == null) {
            return;
        }
        mAdapter.setMode(FlexibleAdapter.MODE_IDLE);

        mAdapter.clearSelection();
        Log.d(TAG, "onDestroyActionMode:itemsCount:"+mAdapter.getItemCount());
        Log.d(TAG, "onDestroyActionMode:items:"+mAdapter.getDeletedItems());
        Log.d(TAG, "onDestroyActionMode:items:DB:"+GalleryDatabaseService.getInstance(this).getDatabaseList().toString());
        Log.d(TAG, "onDestroyActionMode:adapter:isEmpty:"+mAdapter.isEmpty());
        Log.d(TAG, "onDestroyActionMode:db:isEmpty:"+GalleryDatabaseService.getInstance(this).isEmpty());

        if (GalleryDatabaseService.getInstance(this).isEmpty()) {
            this.onUpdateEmptyView(0);
        }
        FlipView flipView;

        if (Utils.hasMarshmallow()) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlack, this.getTheme()));
        } else if (Utils.hasLollipop()) {
            //noinspection deprecation
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlack));
        }

        for (int i =0; i< mRecyclerView.getChildCount(); i++) {
            flipView = (FlipView) mRecyclerView.getChildAt(i).findViewById(R.id.image);

            if (flipView != null) {
                flipView.setVisibility(View.GONE);
            }
        }

    }


    @Override
    public void onFastScrollerStateChange(boolean scrolling) {

    }

}

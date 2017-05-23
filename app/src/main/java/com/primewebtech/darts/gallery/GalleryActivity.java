package com.primewebtech.darts.gallery;

import android.content.Intent;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.primewebtech.darts.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flipview.FlipView;

//import android.widget.ShareActionProvider;

/**
 * Created by benebsworth on 20/5/17.
 */

public class GalleryActivity extends AppCompatActivity
        implements ActionMode.Callback,
        FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnItemLongClickListener,
        FlexibleAdapter.OnUpdateListener,
        FlexibleAdapter.OnDeleteCompleteListener, FastScroller.OnScrollStateChangeListener {


    /**
     * RecyclerView and related objects
     */
    private RecyclerView mRecyclerView;
    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    private ActionModeHelper mActionModeHelper;
    private Toolbar mToolbar;
    private int mColumnCount = 4;
    public boolean inActionMode = false;
    public ActionMode mActionMode;
    public boolean selectedAll = false;
    private ShareActionProvider mShareActionProvider;



    public static final String TAG = GalleryActivity.class.getSimpleName();

    /* ===================
	 * ACTIVITY MANAGEMENT
	 * =================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Log.d(TAG, "onCreate");
        FlexibleAdapter.enableLogs(true);
        if (savedInstanceState == null) {
            GalleryDatabaseService.getInstance().createHeadersSectionsGalleryDataset();
        }
        initializeRecylerView(savedInstanceState);
//        initializeActionModeHelper(0);
        inActionMode = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "onSaveInstanceState!");
        mAdapter.onSaveInstanceState(outState);
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
//            mActionModeHelper.restoreSelection(this);
        }
    }

    /* ======================
	 * INITIALIZATION METHODS
	 * ====================== */

    private void initializeActionModeHelper(int mode) {

        mActionModeHelper = new ActionModeHelper(mAdapter, R.menu.gallery_selection, this) {
            @Override
            public void updateContextTitle(int count) {
                if (mActionMode != null) {//You can use the internal ActionMode instance
                    mActionMode.setTitle(count == 1 ?
                            getString(R.string.action_selected_one, Integer.toString(count)) :
                            getString(R.string.action_selected_many, Integer.toString(count)));
                }
            }
        }.withDefaultMode(mode);
    }

    private void initializeRecylerView(Bundle savedInstanceState) {

        mAdapter = new GalleryAdapter(GalleryDatabaseService.getInstance().getDatabaseList(), this);
        mAdapter.setAnimationOnScrolling(false);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(createNewGridLayoutManager());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setFastScroller((FastScroller) findViewById(R.id.fast_scroller),
                Utils.getColorAccent(this), this);
        mAdapter.setDisplayHeadersAtStartUp(true)
                .setStickyHeaders(true)
                .showAllHeaders();

    }


//    private void initializeToolbar() {
//        Log.d(TAG, "initializeToolbar as actionBar");
//        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mHeaderView = (HeaderView) findViewById(R.id.toolbar_header_view);
//        mHeaderView.bindTo(getString(R.string.app_name), getString(R.string.overall));
//        //mToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
//        // Toolbar will now take on default Action Bar characteristics
//        setSupportActionBar(mToolbar);
//    }


    /* ========================================================================
	 * FLEXIBLE ADAPTER LISTENERS IMPLEMENTATION
	 * Listeners implementation are in MainActivity to easily reuse the common
	 * components like SwipeToRefresh, ActionMode, NavigationView, etc...
	 * ======================================================================== */

    @Override
    public boolean onItemClick(int position) {
        PhotoItem photoItem = (PhotoItem) mAdapter.getItem(position);
        Log.d(TAG, "onItemClick:Clicked:postition:"+position);
        if (mActionMode != null && position != RecyclerView.NO_POSITION) {
            toggleSelection(position);
            return true;
        } else {

            Intent intent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Uri photoURI = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoItem.getFile());
            intent.setDataAndType(photoURI, "image/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            return false;
        }

        //TODO: navigate to selected image


    }

    @Override
    public void onItemLongClick(int position) {
        Log.d(TAG, "onItemLongClick:longClick:postition:"+position);
//        mActionModeHelper.onLongClick(this, position);
        if (mActionMode == null ) {
            mActionMode = startSupportActionMode(this);
        }
        toggleSelection(position);

    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();
//        ImageView unselected;
//        ImageView selected;
//        PhotoItem item = (PhotoItem) mAdapter.getItem(position);
//        Log.d(TAG, "toggleActivaition:position:"+position);
//
//        unselected = (ImageView) mRecyclerView.getChildAt(position).findViewById(R.id.unselected);
//        selected = (ImageView) mRecyclerView.getChildAt(position).findViewById(R.id.selected);
//        if (mAdapter.isSelected(position)) {
//            Log.d(TAG, "toggleActivaition:selecting");
//            selected.setVisibility(View.VISIBLE);
//            unselected.setVisibility(View.GONE);
//        } else {
//            Log.d(TAG, "toggleActivaition:unselecting");
//            selected.setVisibility(View.GONE);
//            unselected.setVisibility(View.VISIBLE);
//        }

        if (count == 0) {
            mActionMode.finish();
        } else {
            setContextTitle(count);
            mShareActionProvider.setShareIntent(shareIntentMaker());
        }
    }

    private void setContextTitle(int count) {
//        mActionMode.setTitle(String.valueOf(count) + " " + (count == 1 ?
//                getString(R.string.action_selected_one) :
//                getString(R.string.action_selected_many)));
        Log.d(TAG, "SHARING:"+mAdapter.getSelectedPositions().toString());
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
            emptyText.setText(getString(R.string.no_items));
        if (size > 0) {
            fastScroller.setVisibility(View.VISIBLE);
//            mRefreshHandler.removeMessages(2);
            emptyView.setAlpha(0);
        } else {
            emptyView.setAlpha(0);
//            mRefreshHandler.sendEmptyMessage(2);
            fastScroller.setVisibility(View.GONE);
        }
        if (mAdapter != null) {
            String message = (mAdapter.hasSearchText() ? "Filtered " : "Refreshed ");
            message += size + " items in " + mAdapter.getTime() + "ms";
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
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccentDark_light, this.getTheme()));
        } else if (Utils.hasLollipop()) {
            //noinspection deprecation
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccentDark_light));
        }
        mode.getMenuInflater().inflate(R.menu.gallery_selection, menu);
//        mAdapter.setMode(FlexibleAdapter.MODE_MULTI);
        MenuItem item = menu.findItem(R.id.menu_item_share);
//        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
//        mShareActionProvider.setShareIntent(shareIntentMaker());
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(shareIntentMaker());
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//        ImageView unselected;
//        ImageView selected;
        FlipView flipView;
        for (int i =0; i< mRecyclerView.getChildCount(); i++) {
//            unselected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.unselected);
//            selected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.selected);
            flipView = (FlipView) mRecyclerView.getChildAt(i).findViewById(R.id.image);
            if (flipView != null) {
//                unselected.setVisibility(View.VISIBLE);
//                selected.setVisibility(View.GONE);
                flipView.setVisibility(View.VISIBLE);
            }

        }
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Log.d(TAG, "onActionItemClicked:SHARING:"+mAdapter.getSelectedPositions().toString());
        Log.d(TAG, "onActionItemClicked:item.getItemId:"+item.getItemId());
//        mShareActionProvider.setShareIntent(shareIntentMaker());
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                Log.d(TAG, "onActionItemClicked:SHARING:");
                mShareActionProvider.setShareIntent(shareIntentMaker());
                return true;
            case R.id.menu_select_all:
                toggleSelectAll();
                return true;
            case R.id.menu_delete:
//                StringBuilder message = new StringBuilder();
//                message.append(getString(R.string.action_deleted)).append(" ");
//
//                for (Integer pos : mAdapter.getSelectedPositions()) {
////                    message.append(extractTitleFrom(mAdapter.getItem(pos)));
//                    if (mAdapter.getSelectedItemCount() > 1)
//                        message.append(", ");
//                }
                Log.d(TAG, "onActionItemClicked:DELETE");

                List<File> files = GalleryDatabaseService.getInstance().getSelectedItems(mAdapter.getSelectedPositions());
                for (File file : files) {
                    Log.d(TAG, "onActionItemClicked:DELETE:file:"+file.getPath());
                    file.delete();
                    Log.d(TAG, "onActionItemClicked:DELETE:file:done");
                }
                mAdapter.removeItems(mAdapter.getSelectedPositions());
//                Log.d(TAG, "onActionItemClicked:deleted_items:"+mAdapter.getDeletedItems().toString());
//                for ( AbstractFlexibleItem photo : mAdapter.getDeletedItems()) {
//                    Log.d(TAG, "onActionItemClicked:DELETE:file:"+((PhotoItem) photo).getFile());
//                    ((PhotoItem) photo).getFile().delete();
//                }
                return true;

        }
        return false;
    }

    public void toggleSelectAll() {

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
        Intent shareIntent  = new Intent(Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> imageUris = new ArrayList<>();
        Log.d(TAG, "shareIntentMaker:SHARING:"+mAdapter.getSelectedPositions().toString());
        List<File> files = GalleryDatabaseService.getInstance().getSelectedItems(mAdapter.getSelectedPositions());

        for ( File file : files) {
            Log.d(TAG, "shareIntentMaker:file:"+file.getPath());
            imageUris.add(FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", file));
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
        mAdapter.setMode(FlexibleAdapter.MODE_IDLE);
        mActionMode = null;
        GalleryDatabaseService.onDestroy();
        super.onBackPressed();
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
        mAdapter.setMode(FlexibleAdapter.MODE_IDLE);
        mActionMode = null;
        mAdapter.clearSelection();
        ImageView unselected;
        ImageView selected;
        FlipView flipView;
        inActionMode = true;
        for (int i =0; i< mRecyclerView.getChildCount(); i++) {
            unselected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.unselected);
            selected = (ImageView) mRecyclerView.getChildAt(i).findViewById(R.id.selected);
            flipView = (FlipView) mRecyclerView.getChildAt(i).findViewById(R.id.image);

            if (unselected != null) {
                unselected.setVisibility(View.GONE);
                selected.setVisibility(View.GONE);
                flipView.setVisibility(View.GONE);
            }
        }

    }


    @Override
    public void onFastScrollerStateChange(boolean scrolling) {
//        if (scrolling) {
//            hideFab();
//        } else {
//            showFab();
//        }
    }

}

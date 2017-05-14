package com.primewebtech.darts.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.primewebtech.darts.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benebsworth on 7/5/17.
 */

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder> {
    private static final String TAG = GalleryActivity.class.getSimpleName();
    private static final int COUNT = 100;
    private File pictureDirectory;
    private List<File> sortedFiles;
    private int THUMBSIZE_W = 30;
    private int THUMBSIZE_H = 30;
    private ImageLoader mImageLoader;
    private final ImageLoaderConfiguration mConfig;

    private final Context mContext;
    private final List<GalleryItem> mItems;
    private int mCurrentItemId = 0;
    private SparseBooleanArray selectedItems;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
//        public final TextView title;
        public final ImageView thumbnail;
        public final ImageView selected;
        public final ImageView unselected;

        public SimpleViewHolder(View view) {
            // initialises the viewholder for view hydration.
            super(view);
//            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            selected = (ImageView) view.findViewById(R.id.selected);
            unselected = (ImageView) view.findViewById(R.id.unselected);


        }
    }

    public SimpleAdapter(Context context) {
        mContext = context;

        mItems = new ArrayList<>(COUNT);
        mConfig = new ImageLoaderConfiguration.Builder(mContext).build();
        ImageLoader.getInstance().init(mConfig);
        selectedItems = new SparseBooleanArray();
        /*
        The adapter will initialise with 100 items. The sectionViewAdapter will then be responsible
        for sectioning these items according to the firstPosition????
        TODO: work that out
         */
        pictureDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Darts/");

        sortedFiles = Util.sortByLastModified(pictureDirectory);
        int index = 0;
        for ( File file : sortedFiles) {
            Log.d(TAG, ":file:"+file.getPath());
            addItem(new GalleryItem(index, file));
            index++;
        }

    }



    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
        return new SimpleViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder:"+Integer.toString(mItems.get(position).mPosition));
//        holder.title.setText(Integer.toString(mItems.get(position).mPosition));
        GalleryActivity activity = (GalleryActivity) mContext;
        final int adjustedPosition = activity.mSectionedAdapter.positionToSectionedPosition(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                GalleryActivity activity = (GalleryActivity) mContext;
                if (activity.actionMode == null) {
                    Log.d(TAG, "onClick:mItems:mPosition:"+mItems.get(position).mPosition);
                    Log.d(TAG, "onClick:onBIndViewHolder:position:"+position);
                    Intent intent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Uri photoURI = FileProvider.getUriForFile(mContext,
                            mContext.getApplicationContext().getPackageName() + ".fileprovider",
                            mItems.get(position).mItemPath);
                    intent.setDataAndType(photoURI, "image/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mContext.startActivity(intent);
                } else if (activity.actionMode != null) {
                    Log.d(TAG, "actionMenu:onClick:mItems:mPosition:"+mItems.get(position).mPosition);
                    Log.d(TAG, "actionMenu:onClick:onBIndViewHolder:position:"+position);
                    toggleSelection(position, holder);
                    activity.myToggleSelection();
                    if (selectedItems.get(position, false)) {
//                        holder.selected.setVisibility(View.VISIBLE);
//                        holder.unselected.setVisibility(View.GONE);
                    } else {
//                        holder.unselected.setVisibility(View.VISIBLE);
//                        holder.selected.setVisibility(View.GONE);
                    }

                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                GalleryActivity activity = (GalleryActivity) mContext;
                toggleSelection(position, holder);
//                selectedItems.put(position, true);
//                holder.selected.setVisibility(View.VISIBLE);
                activity.onLongPress(position);
                holder.unselected.setVisibility(View.GONE);
                holder.selected.setVisibility(View.VISIBLE);

                return true;
            }
        });
        ImageSize targetSize = new ImageSize(80, 50);
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.loadImage("file:///"+mItems.get(position).mItemPath.getPath(), targetSize, null, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                // Do whatever you want with Bitmap
                holder.thumbnail.setImageBitmap(loadedImage);
            }
        });
        if (activity.actionMode != null) {
            if (selectedItems.get(position, false)) {
                holder.selected.setVisibility(View.VISIBLE);
                holder.unselected.setVisibility(View.GONE);
            } else {
                holder.unselected.setVisibility(View.VISIBLE);
                holder.selected.setVisibility(View.GONE);
            }

        } else {
            holder.unselected.setVisibility(View.GONE);
            holder.selected.setVisibility(View.GONE);
        }
    }

    public void addItem(GalleryItem item) {
        final int id = mCurrentItemId++;
        mItems.add(item);
        notifyItemInserted(item.getPosition());
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void toggleSelection(int pos, final SimpleViewHolder holder) {
        if (selectedItems.get(pos, false)) {
            Log.d(TAG, "toggleSelection:unselecting");
            Log.d(TAG, "toggleSelection:unselecting:array:"+selectedItems.toString());
            holder.selected.setVisibility(View.GONE);
            holder.unselected.setVisibility(View.VISIBLE);
            selectedItems.delete(pos);

        }
        else {
            Log.d(TAG, "toggleSelection:selecting");
            holder.selected.setVisibility(View.VISIBLE);
            holder.unselected.setVisibility(View.GONE);
            Log.d(TAG, "toggleSelection:selecting:pos:"+Integer.toString(pos));
            selectedItems.put(pos, true);
            Log.d(TAG, "toggleSelection:selecting:selectedItems:"+selectedItems.toString());

        }

//        notifyItemChanged(pos);
    }
    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            Log.d(TAG, "toggleSelection:unselecting");
            Log.d(TAG, "toggleSelection:unselecting:array:"+selectedItems.toString());
            selectedItems.delete(pos);

        }
        else {
            Log.d(TAG, "toggleSelection:selecting");
            Log.d(TAG, "toggleSelection:selecting:pos:"+Integer.toString(pos));
            selectedItems.put(pos, true);
            Log.d(TAG, "toggleSelection:selecting:selectedItems:"+selectedItems.toString());

        }

//        notifyItemChanged(pos);
    }
    public void selectAll() {
        int index = 0;
        for ( GalleryItem item : mItems ) {
            selectedItems.put(index, true);
            index++;
        }
//        notifyItemChanged(pos);
    }
    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }
    public int getSelectedItemCount() {
        Log.d(TAG, "selectedItemCount:"+selectedItems.size());
        return selectedItems.size();
    }
    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    public class GalleryItem {
        private int mPosition;
        private File mItemPath;
        public GalleryItem(int position, File itemPath) {
            mPosition = position;
            mItemPath = itemPath;

        }
        public void setPosition(int position) {
            mPosition = position;
        }
        public int getPosition() {
            return mPosition;
        }
        public void setItemPath(File path) {
            mItemPath = path;
        }
        public File getItemPath() {
            return mItemPath;
        }
    }
}
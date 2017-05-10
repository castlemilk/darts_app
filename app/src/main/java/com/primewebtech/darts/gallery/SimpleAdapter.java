package com.primewebtech.darts.gallery;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    private int THUMBSIZE_W = 90;
    private int THUMBSIZE_H = 130;

    private final Context mContext;
    private final List<GalleryItem> mItems;
    private int mCurrentItemId = 0;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final ImageView thumbnail;

        public SimpleViewHolder(View view) {
            // initialises the viewholder for view hydration.
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

        }
    }

    public SimpleAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>(COUNT);
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


//        for (int i = 0; i < COUNT; i++) {
//            addItem(new GalleryItem(i, null));
//        }
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder:"+Integer.toString(mItems.get(position).mPosition));
        holder.title.setText(Integer.toString(mItems.get(position).mPosition));
        holder.thumbnail.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mItems.get(position).mItemPath.getPath()),
                THUMBSIZE_W, THUMBSIZE_H));
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
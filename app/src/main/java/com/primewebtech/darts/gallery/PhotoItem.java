package com.primewebtech.darts.gallery;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.primewebtech.darts.R;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flipview.FlipView;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by benebsworth on 21/5/17.
 */

public class PhotoItem extends AbstractItem<PhotoItem.PhotoViewHolder>
        implements ISectionable<PhotoItem.PhotoViewHolder, HeaderItem>, Serializable {


    private static final String TAG = PhotoItem.class.getSimpleName();
    HeaderItem header;
    private File file;
    private String description;


    public PhotoItem(String id) {
        super(id);
    }
    public PhotoItem(String id, HeaderItem header) {
        this(id);
        this.header = header;
    }
    public PhotoItem(String id, File file, HeaderItem header) {
        this(id);
        this.header = header;
        this.file = file;
    }

    @Override
    public String getSubtitle() {
        return getId()
                + (getHeader() != null ? " - " + getHeader().getId() : "")
                + (getUpdates() > 0 ? " - u" + getUpdates() : "");
    }

    @Override
    public HeaderItem getHeader() {
        return header;
    }

    @Override
    public void setHeader(HeaderItem header) {
        this.header = header;

    }
    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;

    }

    public void setDescription(String description) {
        this.description = description;

    }
    public String getDescription() {
        return description;
    }

    public int getSpanSize(int spanCount, int position) {
        return spanCount;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item;
    }

    @Override
    public PhotoViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new PhotoViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void bindViewHolder(final FlexibleAdapter adapter, PhotoViewHolder holder, int position, List payloads) {
        final Context context = holder.itemView.getContext();
        final GalleryActivity activity = (GalleryActivity) context;
        if (activity.mActionMode != null) {
            holder.mFlipView.flipSilently(adapter.isSelected(position));
            holder.mFlipView.setVisibility(View.VISIBLE);
        } else {
            holder.mFlipView.setVisibility(View.GONE);
        }
        Log.d(TAG, "filepath:"+"file:///"+file.getPath());
        holder.description.setText(description);
        Glide.clear(holder.thumbnail);
        Glide.with(context).load("file:///"+file.getPath()).crossFade(500).into(holder.thumbnail);

    }

    final class PhotoViewHolder extends FlexibleViewHolder {

        Context mContext;
        ImageView thumbnail;
        FlipView mFlipView;
        TextView description;
        public boolean swiped = false;
        private boolean inActionMode = false;

        PhotoViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mContext = view.getContext();

            final GalleryActivity activity = (GalleryActivity) mContext;
            inActionMode = activity.inActionMode;
            this.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            this.description = (TextView) view.findViewById(R.id.image_description);
            mFlipView = (FlipView) view.findViewById(R.id.image);
            mFlipView.setVisibility(View.GONE);

        }

        @Override
        public void onClick(View view) {
            Toast.makeText(mContext, "Click on position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            super.onClick(view);
        }

        @Override
        public boolean onLongClick(View view) {
            Toast.makeText(mContext, "LongClick on position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            return super.onLongClick(view);
        }

        @Override
        public void toggleActivation() {
            super.toggleActivation();
            Log.d(TAG, "toggleActivaition");
            mFlipView.flip(mAdapter.isSelected(getAdapterPosition()));
//            if (mAdapter.isSelected(getAdapterPosition())) {
//                selected.setVisibility(View.VISIBLE);
//
//            }

        }

        @Override
        protected boolean shouldAddSelectionInActionMode() {
            return false;//default=false
        }

//        @Override
//        public View getFrontView() {
//            return frontView;
//        }
//
//        @Override
//        public View getRearLeftView() {
//            return rearLeftView;
//        }
//
//        @Override
//        public View getRearRightView() {
//            return rearRightView;
//        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            if (mAdapter.getRecyclerView().getLayoutManager() instanceof GridLayoutManager ||
                    mAdapter.getRecyclerView().getLayoutManager() instanceof StaggeredGridLayoutManager) {
                if (position % 2 != 0)
                    AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
                else
                    AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
            } else {
                //Linear layout
                if (mAdapter.isSelected(position))
                    AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
                else
                    AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
            }
        }

        @Override
        public void onItemReleased(int position) {
            swiped = (mActionState == ItemTouchHelper.ACTION_STATE_SWIPE);
            super.onItemReleased(position);
        }



    }

    @Override
    public String toString() {
        return "PhotoItem[" + super.toString() + "]";
    }


}

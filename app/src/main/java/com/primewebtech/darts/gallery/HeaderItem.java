package com.primewebtech.darts.gallery;

import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.primewebtech.darts.R;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by benebsworth on 21/5/17.
 */

public class HeaderItem extends AbstractHeaderItem<HeaderItem.HeaderViewHolder> implements IFilterable{
    public static final String TAG = HeaderItem.class.getSimpleName();

    private String id;
    private String title;
//    private String subtitle;

    public HeaderItem(String id) {
        super();
        this.id = id;
        setDraggable(false);
        setSelectable(false);
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof HeaderItem) {
            HeaderItem inItem = (HeaderItem) o;
            return this.getId().equals(inItem.getId());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public int getLayoutRes() {
        return R.layout.holder_header;
    }

    @Override
    public HeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new HeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
        if (payloads.size() > 0) {
            Log.d(this.getClass().getSimpleName(), "HeaderItem " + id + " Payload " + payloads);
        } else {
            holder.mTitle.setText(getTitle());
        }
//        List<ISectionable> sectionableList = adapter.getSectionItems(this);
//        String subTitle = (sectionableList.isEmpty() ? "Empty section" :
//                sectionableList.size() + " section items");
//        holder.mSubtitle.setText(subTitle);
    }

    @Override
    public boolean filter(String constraint) {
        return false;
    }

    static class HeaderViewHolder extends FlexibleViewHolder {

        TextView mTitle;
        ImageView mHandleView;

        HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//True for sticky
            mTitle = (TextView) view.findViewById(R.id.title);
            mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("HeaderTitle", "Registered internal click on Header TitleTextView! " + mTitle.getText() + " position=" + getFlexibleAdapterPosition());
                }
            });
            mHandleView = (ImageView) view.findViewById(R.id.row_handle);
            Log.d(TAG, "mHandleView:"+mHandleView);
            if (adapter.isHandleDragEnabled()) {
                mHandleView.setVisibility(View.VISIBLE);
                setDragHandleView(mHandleView);
            } else {
                mHandleView.setVisibility(View.GONE);
            }

            //Support for StaggeredGridLayoutManager
            if (itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams()).setFullSpan(true);
            }
        }
    }

    @Override
    public String toString() {
        return "HeaderItem[id=" + id +
                ", title=" + title + "]";
    }

}

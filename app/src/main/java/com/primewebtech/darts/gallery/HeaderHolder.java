package com.primewebtech.darts.gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.primewebtech.darts.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by benebsworth on 21/5/17.
 */

public class HeaderHolder extends AbstractHeaderItem<HeaderHolder.HeaderViewHolder>
        implements IHolder<HeaderModel>{

    private HeaderModel model;

    public HeaderHolder(HeaderModel model) {
        this.model = model;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HeaderHolder) {
            HeaderHolder inItem = (HeaderHolder) o;
            return model.equals(inItem.getModel());
        }
        return false;
    }

    /**
     * @return the model object
     */
    @Override
    public HeaderModel getModel() {
        return model;
    }


    @Override
    public int hashCode() {
        return model.hashCode();
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
    public void bindViewHolder(final FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
        holder.mTitle.setText(model.getTitle());
        List sectionableList = adapter.getSectionItems(this);
        String subTitle = (sectionableList.isEmpty() ? "Empty section" :
                sectionableList.size() + " section items");
        holder.mSubtitle.setText(subTitle);
    }

    static class HeaderViewHolder extends FlexibleViewHolder {

        @BindView(R.id.title)
        public TextView mTitle;
        @BindView(R.id.subtitle)
        public TextView mSubtitle;

        /**
         * Default constructor.
         */
        public HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//true only for header items when will be sticky
            ButterKnife.bind(this, view);
        }
    }
}

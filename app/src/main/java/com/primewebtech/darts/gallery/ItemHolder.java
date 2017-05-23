package com.primewebtech.darts.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.primewebtech.darts.R;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by benebsworth on 21/5/17.
 */

public class ItemHolder extends AbstractSectionableItem<ItemHolder.ItemViewHolder, HeaderHolder>
                implements IHolder<ItemModel> {
    private static final String TAG = ItemHolder.class.getSimpleName();
    private ItemModel model;
    private File file;


    public ItemHolder(ItemModel model, HeaderHolder header) {

        super(header);

    }

    public ItemHolder withImageUrl(File file) {
        this.file = file;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ItemHolder) {
            ItemHolder inItem = (ItemHolder) o;
            return model.equals(inItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    /**
     * @return the model object
     */
    @Override
    public ItemModel getModel() {
        return model;
    }
    @Override
    public int getLayoutRes() {
        return R.layout.item;
    }

    @Override
    public ItemViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ItemViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, final ItemViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();

    }


    static class ItemViewHolder extends FlexibleViewHolder {

        @BindView(R.id.selected)
        public ImageView selected;
        @BindView(R.id.unselected)
        public ImageView unselected;
        @BindView(R.id.thumbnail)
        public ImageView thumbnail;

        /**
         * Default constructor.
         */
        public ItemViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            ButterKnife.bind(this, view);
        }
    }
}

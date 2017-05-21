package com.primewebtech.darts.gallery2;

import java.io.File;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by benebsworth on 21/5/17.
 */

public abstract class AbstractItem <VH extends FlexibleViewHolder>
        extends AbstractFlexibleItem<VH> {

    protected String id;
    protected String title;
    protected String subtitle = "";
    /* number of times this item has been refreshed */
    protected int updates;

    public AbstractItem(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof AbstractItem) {
            AbstractItem inItem = (AbstractItem) inObject;
            return this.id.equals(inItem.id);
        }
        return false;
    }

    /**
     * Override this method too, when using functionalities like StableIds, Filter or CollapseAll.
     * FlexibleAdapter is making use of HashSet to improve performance, especially in big list.
     */
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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getUpdates() {
        return updates;
    }

    public void increaseUpdates() {
        this.updates++;
    }

    public abstract File getFile();

    public abstract void setFile(File file);

    @Override
    public String toString() {
        return "id=" + id +
                ", title=" + title;
    }

}
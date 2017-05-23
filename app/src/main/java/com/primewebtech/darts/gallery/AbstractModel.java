package com.primewebtech.darts.gallery;

import java.io.File;
import java.io.Serializable;

/**
 * Created by benebsworth on 21/5/17.
 */

public class AbstractModel implements Serializable {

    private static final long serialVersionUID = -7385882749119849060L;

    private String id;
    private String title;
    private String subtitle;
    private String date;
    private File file;

    public AbstractModel(String id) {
        this.id = id;
    }
    public AbstractModel(String id, String date) {

        this.id = id;
        this.date = date;
    }
    public AbstractModel(String id, File file) {

        this.id = id;
        this.file = file;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof AbstractModel) {
            AbstractModel inItem = (AbstractModel) inObject;
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

    public String getDate() {
        return date;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public File getFile() {
        return file;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", title=" + title;
    }

}
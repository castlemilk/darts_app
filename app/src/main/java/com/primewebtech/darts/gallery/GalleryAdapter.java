package com.primewebtech.darts.gallery;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Created by benebsworth on 20/5/17.
 */

public class GalleryAdapter  extends FlexibleAdapter<AbstractFlexibleItem> {


    private static final String TAG = GalleryAdapter.class.getSimpleName();
    private Context mContext;

    public GalleryAdapter(@Nullable List<AbstractFlexibleItem> items, @Nullable Object listeners) {
        //stableIds ? true = Items implement hashCode() so they can have stableIds!
        super(items, listeners, true);
        mHandler = new Handler(Looper.getMainLooper(), new MyHandlerCallback());
    }
    @Override
    public void updateDataSet(List<AbstractFlexibleItem> items, boolean animate) {
        // NOTE: To have views/items not changed, set them into "items" before passing the final
        // list to the Adapter.

        // Overwrite the list and fully notify the change, pass false to not animate changes.
        // Watch out! The original list must a copy.
        super.updateDataSet(items, animate);

        // onPostUpdate() will automatically be called at the end of the Asynchronous update
        // process. Manipulate the list inside that method only or you won't see the changes.
    }

    /*
	 * You can override this method to define your own concept of "Empty".
	 * This method is never called internally.
	 */
    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    private class MyHandlerCallback extends HandlerCallback {
        @Override
        public boolean handleMessage(Message message) {
            boolean done = super.handleMessage(message);
            switch (message.what) {
                // Currently reserved (you don't need to check these numbers!)
                case 1: //async updateDataSet
                case 2: //async filterItems
                case 3: //confirm delete
                case 8: //onLoadMore remove progress item
                    return done;

                // Free to use, example:
                case 10:
                case 11:
                    return true;
            }
            return false;
        }
    }
}

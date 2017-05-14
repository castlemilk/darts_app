package com.primewebtech.darts.gallery;

import android.view.View;

/**
 * Created by benebsworth on 13/5/17.
 */

public interface RecyclerViewClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}

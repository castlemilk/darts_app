package com.primewebtech.darts.scoring;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.primewebtech.darts.R;

/**
 * Created by benebsworth on 11/8/17.
 */

public class ScoreAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    private int[] mResources;
    public TextView scoreNumber;


    public ScoreAdapter(Context context, int[] resources) {
        mContext = context;
        mResources = resources;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item_one_dart, container, false);
        scoreNumber = (TextView) itemView.findViewById(R.id.score_number_one_dart);
        scoreNumber.setText(String.valueOf(mResources[position]));
        container.addView(itemView);
        return itemView;
    }

    public void updateScoreValue(int score) {

    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}

package com.primewebtech.darts.camera;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.primewebtech.darts.R;

import java.util.Locale;

/**
 * Created by benebsworth on 4/5/17.
 */

public class CustomPagerAdapter extends PagerAdapter{

    private static final String TAG = CustomPagerAdapter.class.getSimpleName();

    Context mContext;
    LayoutInflater mLayoutInflater;
    private int[] mResources;
    public TextView scoreNumber;
    private int score;


    public CustomPagerAdapter(Context context, int[] resources) {
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
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        imageView.setImageResource(mResources[position]);
        scoreNumber = (TextView) itemView.findViewById(R.id.pager_score_number);
        CameraActivity cameraActivity = (CameraActivity) mContext;
        if (cameraActivity.mScoreNumberValue != "RH") {
            scoreNumber.setText(String.format(Locale.US, "%s", cameraActivity.mScoreNumberValue));
            scoreNumber.setVisibility(View.VISIBLE);
//            scoreNumber.setTextSize(20);
        } else {
            scoreNumber.setText(String.format(Locale.US, "%s", ""));
            scoreNumber.setVisibility(View.GONE);
        }

//        scoreNumber.setText(String.format(Locale.US, "%d", ));
        Log.d(TAG, "Setting text score:"+cameraActivity.mScoreNumberValue);

        container.addView(itemView);
        itemView.setTag("pager_view");
        return itemView;
    }

    public void updateScoreValue(int score) {

    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }


}

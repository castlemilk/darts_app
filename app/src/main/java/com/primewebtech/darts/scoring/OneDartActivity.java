package com.primewebtech.darts.scoring;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.primewebtech.darts.R;

import java.util.HashMap;

/**
 * Created by benebsworth on 24/5/17.
 */

public class OneDartActivity extends AppCompatActivity {
    /**
     * This activity is responsible for the display and logging of the One Dart scoring category
     * In this scoring category the user can swipe left and right the peg value and then increment
     * the count, which is then indicated on the rim of the pin in white circles. Each white circle
     * represents that value being pegged 100 times, if a full circle is completed then it cycles to
     * green cirlces around the rim then on the next completion it goes red.
     *
     * There is a number underneath the centre of the pin which represents the number of times a given
     * peg value has been completed for the day. It will reset on the next day with the historical data
     * being logged for the statistics/analytics stage.
     */

    private ImageView pin;
    private int pegsCompleted;
    private HashMap<Integer, Integer> scoreCounts;
    private ViewPager mViewPager;
    private ScorePagerAdapter mScoringAdapter;
    private int[] mScores = {
            40, 32, 24,36,50,2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_dart_view);
        pin = (ImageView) findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_40s);
        initialisePager();


    }

    public void initialisePager() {
        mViewPager = (ViewPager) findViewById(R.id.pager_one_dart);

        if (mScoringAdapter != null) {
            mViewPager.setAdapter(mScoringAdapter);
        } else {
            mViewPager.setAdapter(new ScorePagerAdapter(this, mScores));
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //TODO: update circle indicators on scroll based on count
                //TODO: store data as its incremented into sqlite
                //TODO: fetch data on page scroll/swipe
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class ScorePagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;
        private int[] mResources;
        public TextView scoreNumber;


        public ScorePagerAdapter(Context context, int[] resources) {
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
            return view == ((FrameLayout) object);
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item_one_dart, container, false);

//            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
//            imageView.setImageResource(mResources[position]);
            scoreNumber = (TextView) itemView.findViewById(R.id.score_number_one_dart);
//            cameraActivity = (CameraActivity) mContext;
            scoreNumber.setText(Integer.toString(mResources[position]));


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

}

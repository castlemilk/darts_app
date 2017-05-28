package com.primewebtech.darts.scoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.primewebtech.darts.MainApplication;
import com.primewebtech.darts.R;
import com.primewebtech.darts.database.ScoreDatabase;
import com.primewebtech.darts.database.model.Action;
import com.primewebtech.darts.database.model.ActionSchema;
import com.primewebtech.darts.database.model.PegRecord;
import com.primewebtech.darts.homepage.HomePageActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by benebsworth on 24/5/17.
 */

public class OneDartActivity extends AppCompatActivity implements ActionSchema{
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


    private static final String TAG = OneDartActivity.class.getSimpleName();
    private ImageView pin;
    private int pegsCompleted;
    private HashMap<Integer, Integer> scoreCounts;
    private ViewPager mViewPager;
    private Button mCountButton;
    private ImageButton mMenuButton;
    private ImageButton mBackButton;
    private ScorePagerAdapter mScoringAdapter;
    public MainApplication app;
    private String curTime;
    private String lastResetTime;
    private Button mIncrementOne;
    private Button mIncrementTwo;
    private Button mIncrementThree;
    SharedPreferences prefs = null;
    private int[] mPegs = {
            40, 32, 24,36,50,2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_dart_view);
//        clearPreference();
//        initialisePegCounts();
        app = (MainApplication) getApplication();
        curTime = new SimpleDateFormat("yyyydd", Locale.getDefault()).format(new Date());

        Log.d(TAG, "CUR_TIME:"+curTime);
        prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
        lastResetTime = prefs.getString("lastResetTime", curTime);
        if ( !curTime.equals(lastResetTime)) {
            Log.d(TAG, "NEW_DAY:resetting counts");
            //TODO: reset all the required variables and carry previous data into historical logs
            initialisePegCounts();
            prefs.edit().putString("lastResetTime", curTime).apply();
        }

        pin = (ImageView) findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_40s);
        initialisePager();
        initialiseBackButton();
        initialiseCountButtons();
        initialiseMenuButton();
    }
    public void initialiseMenuButton() {
        mMenuButton = (ImageButton) findViewById(R.id.button_menu);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homePageIntent = new Intent(OneDartActivity.this, HomePageActivity.class);
                startActivity(homePageIntent);
            }
        });
    }
    public void initialiseBackButton() {
        //TODO: implement undo functionality using action SQL table of historical actions
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Action action = ScoreDatabase.mActionDoa.getAndDeleteLastHistoryAction();
                if (action != null) {
                    ScoreDatabase.mScoreDoa.rollbackScore(action);
                    mCountButton.setText(action.getRollBackValue());
                }


            }
        });
    }
    public void clearPreference() {
        SharedPreferences.Editor prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE).edit();
        prefs.clear();
        prefs.commit();
    }
    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }
    public void initialisePegCounts() {
        for (int peg : mPegs) {
            PegRecord pegRecord = new PegRecord(getDate(), 0, peg, 0);
            try {
                ScoreDatabase.mScoreDoa.addTodayPegValue(pegRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initialiseCountButtons() {
        mCountButton = (Button) findViewById(R.id.count_button);
        mIncrementOne = (Button) findViewById(R.id.increment_one);
        mIncrementTwo = (Button) findViewById(R.id.increment_two);
        mIncrementThree = (Button) findViewById(R.id.increment_three);
        int currentIndex = mViewPager.getCurrentItem();
        PegRecord pegRecord = ScoreDatabase.mScoreDoa.getTodayPegValue(mPegs[currentIndex]);
        if (pegRecord != null) {
            mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()));
        } else {
            PegRecord newPegRecord = new PegRecord(getDate(), 0,mPegs[currentIndex] , 0);
            try {
                ScoreDatabase.mScoreDoa.addTodayPegValue(newPegRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreDoa.getTodayPegValue(mPegs[currentIndex]);
                ScoreDatabase.mScoreDoa.increaseTodayPegValue(pegRecord.getPegValue(), 1);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                Action action = new Action(ADD, 1, mPegs[currentIndex], pegRecord.getPegCount()+1);
                ScoreDatabase.mActionDoa.addAction(action);
            }
        });
        mIncrementOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreDoa.getTodayPegValue(mPegs[currentIndex]);
                ScoreDatabase.mScoreDoa.increaseTodayPegValue(pegRecord.getPegValue(), 1);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                Action action = new Action(ADD, 1, mPegs[currentIndex], pegRecord.getPegCount()+1);
                ScoreDatabase.mActionDoa.addAction(action);
            }
        });
        mIncrementTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreDoa.getTodayPegValue(mPegs[currentIndex]);
                ScoreDatabase.mScoreDoa.increaseTodayPegValue(pegRecord.getPegValue(), 2);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+2));
                Action action = new Action(ADD, 2, mPegs[currentIndex], pegRecord.getPegCount()+2);
                ScoreDatabase.mActionDoa.addAction(action);
            }
        });
        mIncrementThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreDoa.getTodayPegValue(mPegs[currentIndex]);
                ScoreDatabase.mScoreDoa.increaseTodayPegValue(pegRecord.getPegValue(), 3);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+3));
                Action action = new Action(ADD, 3, mPegs[currentIndex], pegRecord.getPegCount()+3);
                ScoreDatabase.mActionDoa.addAction(action);
            }
        });
    }

    public void initialisePager() {
        mViewPager = (ViewPager) findViewById(R.id.pager_one_dart);

        if (mScoringAdapter != null) {
            mViewPager.setAdapter(mScoringAdapter);
        } else {
            mViewPager.setAdapter(new ScorePagerAdapter(this, mPegs));
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                PegRecord pegRecord = ScoreDatabase.mScoreDoa.getTodayPegValue(mPegs[position]);
                if (pegRecord != null) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()));
                } else {
                    PegRecord newPegRecord = new PegRecord(getDate(), 0,mPegs[position] , 0);
                    try {
                        ScoreDatabase.mScoreDoa.addTodayPegValue(newPegRecord);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

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
            return view == object;
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

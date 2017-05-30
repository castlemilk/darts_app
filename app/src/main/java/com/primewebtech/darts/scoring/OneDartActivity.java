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
import com.primewebtech.darts.database.model.ScoreSchema;
import com.primewebtech.darts.homepage.HomePageActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by benebsworth on 24/5/17.
 */

public class OneDartActivity extends AppCompatActivity implements ActionSchema, ScoreSchema{
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

        app = (MainApplication) getApplication();
        curTime = new SimpleDateFormat("yyyydd", Locale.getDefault()).format(new Date());

        prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
        lastResetTime = prefs.getString("lastResetTime", curTime);
        Log.d(TAG, "CUR_TIME:"+curTime);
        Log.d(TAG, "LAST_RESET_TIME:"+lastResetTime);
        if ( !curTime.equals(lastResetTime)) {
            Log.d(TAG, "NEW_DAY:resetting counts");
            //TODO: reset all the required variables and carry previous data into historical logs
            initialisePegCounts();
            initialiseCountButtons();
            prefs.edit().putString("lastResetTime", curTime).apply();
        }

        pin = (ImageView) findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_40s);
        initialiseCountIndicators();
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
    public int getPegIndex(int pegValue) {
        int index = 0;
        for (int peg : mPegs) {
            if (pegValue == peg) {
                return index;
            } else {
                index++;
            }
        }
        return 0;
    }

    public void initialiseBackButton() {
        //TODO: implement undo functionality using action SQL table of historical actions
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Action action = ScoreDatabase.mActionDoa.getAndDeleteLastHistoryAction();
                if (action != null) {
                    int currentIndex = mViewPager.getCurrentItem();
                    if (mPegs[currentIndex] == action.getPegValue()) {
                        mViewPager.setCurrentItem(getPegIndex(action.getPegValue()));
                        ScoreDatabase.mScoreOneDoa.rollbackScore(action);
                        mCountButton.setText(action.getRollBackValue());
                        updateCountIndicators(mPegs[currentIndex]);
                    } else {
                        mViewPager.setCurrentItem(getPegIndex(action.getPegValue()));
                        ScoreDatabase.mScoreOneDoa.rollbackScore(action);
                        mCountButton.setText(action.getRollBackValue());
                        updateCountIndicators(mPegs[currentIndex]);
                    }

                }


            }
        });
    }
    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }
    public void initialisePegCounts() {
        for (int peg : mPegs) {
            PegRecord pegRecord = new PegRecord(getDate(), TYPE_2, peg, 0);
            try {
                ScoreDatabase.mScoreOneDoa.addTodayPegValue(pegRecord);
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
        PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[currentIndex], TYPE_2);
        if (pegRecord != null) {
            mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()));
            updateCountIndicators(mPegs[currentIndex]);
        } else {
            PegRecord newPegRecord = new PegRecord(getDate(), TYPE_2, mPegs[currentIndex] , 0);
            try {
                ScoreDatabase.mScoreOneDoa.addTodayPegValue(newPegRecord);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", newPegRecord.getPegCount()));
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
                PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[currentIndex], TYPE_2);
                ScoreDatabase.mScoreOneDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_2,  1);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                Action action = new Action(ADD, 1, mPegs[currentIndex], TYPE_2, pegRecord.getPegCount()+1);
                ScoreDatabase.mActionDoa.addAction(action);
                updateCountIndicators(mPegs[currentIndex]);
            }
        });
        mIncrementOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[currentIndex], TYPE_2);
                ScoreDatabase.mScoreOneDoa.increaseTodayPegValue(pegRecord.getPegValue(), TYPE_2, 1);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                Action action = new Action(ADD, 1, mPegs[currentIndex], TYPE_2, pegRecord.getPegCount()+1);
                ScoreDatabase.mActionDoa.addAction(action);
                updateCountIndicators(mPegs[currentIndex]);
            }
        });
        mIncrementTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[currentIndex], TYPE_2);
                ScoreDatabase.mScoreOneDoa.increaseTodayPegValue(pegRecord.getPegValue(), TYPE_2, 2);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+2));
                Action action = new Action(ADD, 2, mPegs[currentIndex], TYPE_2, pegRecord.getPegCount()+2);
                ScoreDatabase.mActionDoa.addAction(action);
                updateCountIndicators(mPegs[currentIndex]);
            }
        });
        mIncrementThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[currentIndex], TYPE_2);
                ScoreDatabase.mScoreOneDoa.increaseTodayPegValue(pegRecord.getPegValue(), TYPE_2, 3);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+3));
                Action action = new Action(ADD, 3, mPegs[currentIndex], TYPE_2, pegRecord.getPegCount()+3);
                ScoreDatabase.mActionDoa.addAction(action);
                updateCountIndicators(mPegs[currentIndex]);
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
                PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[position], TYPE_2);
                if (pegRecord != null) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()));
                    updateCountIndicators(mPegs[position]);
                } else {
                    PegRecord newPegRecord = new PegRecord(getDate(), TYPE_2, mPegs[position], 0);
                    try {
                        ScoreDatabase.mScoreOneDoa.addTodayPegValue(newPegRecord);
                        mCountButton.setText(String.format(Locale.getDefault(),"%d", 0));
                        updateCountIndicators(mPegs[position]);
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

    public int[] indicatorResources = {
            R.id.indicator1,
            R.id.indicator2,
            R.id.indicator3,
            R.id.indicator4,
            R.id.indicator5,
            R.id.indicator6,
            R.id.indicator7,
            R.id.indicator8,
            R.id.indicator9,
            R.id.indicator10,
    };
    public void initialiseCountIndicators() {
        for (int circleIndicator : indicatorResources) {
            ImageView indicator = (ImageView) findViewById(circleIndicator);
            indicator.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the circular indicators for a given selected peg value. Will carry out a DB lookup
     * and aggregate today + historical peg counts and update the indicator where each circle
     * represents 100 pegs made for that selected peg value.
     * @param pegValue
     */
    public void updateCountIndicators(int pegValue) {
        Log.d(TAG, "updateCountIndicators:pegValue:"+pegValue);
        int total = ScoreDatabase.mScoreOneDoa.getTotalPegCount(pegValue);
        Log.d(TAG, "updateCountIndicators:total:"+total);
        int index = 0;
        if (total > 0) {
            for (int circleIndicator : indicatorResources) {

                Double rem = Math.floor((index + 1) * 100 / (total + 1) );
                Log.d(TAG, "updateCountIndicators:remainder" + rem);
                if (rem.intValue() == 0) {
                    ImageView indicator = (ImageView) findViewById(circleIndicator);
                    indicator.setVisibility(View.VISIBLE);
                } else {
                    ImageView indicator = (ImageView) findViewById(circleIndicator);
                    indicator.setVisibility(View.GONE);
                }


                index++;

            }
        } else {
            initialiseCountIndicators();
        }



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
            scoreNumber.setText(String.valueOf(mResources[position]));


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

package com.primewebtech.darts.scoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import java.util.List;
import java.util.Locale;

/**
 * Created by benebsworth on 29/5/17.
 */

public class TwoDartActivity extends AppCompatActivity implements ActionSchema, ScoreSchema {
    /**
     * The two dart activity is a little different compared to the one dart activity in that there
     * will be two values which are incremented on the pin board, the two dot and three dot values.
     * These two scores represent the counts of times the selected pagerview value has been
     * pegged with two and three darts respectively. These respective counts are incremented via the
     * 2 and 3 buttons below the peg board.
     *
     * In terms of UI we have a pinboard which changes completed per 10 peg value increment, so for
     * example
     * 50..59 -> pedboard_50s
     * 60..69 -> pegboard_60s
     *
     * We can change the peg value by swiping left or write to decrement/increment the value by one,
     * or pressing the -10 or +10 to change the score in 10's, obviously causing an immediate
     * transition in the pin board graphic.
     *
     * Ranges frm 61 to 110 without 99 102 103 105 106 108 109, as they are not 2 dart pegs.
     */
    private static final String TAG = OneDartActivity.class.getSimpleName();
    private ViewPager mViewPager;
    private ScorePagerAdapter mScoringAdapter;
    private ImageView pin;
    private List<Integer> mPinValues;
    private String curTime;
    private String lastResetTime;
    private Button mCountButtonTwo;
    private Button mCountButtonThree;
    private Button mIncrementTwo;
    private Button mIncrementThree;
    private Button mMovePagerForwardTen;
    private Button mMovePagerBackwardsTen;
    private ImageButton mMenuButton;
    private ImageButton mBackButton;
    public MainApplication app;
    SharedPreferences prefs = null;


    int[] mPinBoards = {
            R.drawable.pin_60s,
            R.drawable.pin_70s,
            R.drawable.pin_80s,
            R.drawable.pin_90s,
            R.drawable.pin_100s,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.two_dart_view);
        mMovePagerBackwardsTen = (Button) findViewById(R.id.minus_ten);
        mMovePagerForwardTen = (Button) findViewById(R.id.plus_ten);
        pin = (ImageView) findViewById(R.id.pin);
        mPinValues = generatePinValues();
        curTime = new SimpleDateFormat("yyyydd", Locale.getDefault()).format(new Date());
        prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
        lastResetTime = prefs.getString("lastResetTime_two", curTime);
        Log.d(TAG, "CUR_TIME:"+curTime);
        Log.d(TAG, "LAST_RESET_TIME:"+lastResetTime);
        if ( !curTime.equals(lastResetTime)) {
            Log.d(TAG, "NEW_DAY:resetting counts");
            new InitialisePegValueTask().execute();
            prefs.edit().putString("lastResetTime_two", curTime).apply();
        }
        updatePinBoard(0);
        initialisePager();
        initialiseCountButtons();
        initialiseBackButton();
        initialiseMenuButton();
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
                    if (mPinValues.get(currentIndex) == action.getPegValue()) {
//                        mViewPager.setCurrentItem(getPegIndex(action.getPegValue()));
                        if(ScoreDatabase.mScoreTwoDoa.rollbackScore(action)) {
                            Log.d(TAG, "Successfully Deleted action");
                        } else {
                            Log.d(TAG, "FAILED to delete");
                        }
                        if (action.getType() == TYPE_2) {
                            mCountButtonTwo.setText(action.getRollBackValue());
                        } else {
                            mCountButtonThree.setText(action.getRollBackValue());
                        }
                    } else {
                        mViewPager.setCurrentItem(getPegIndex(action.getPegValue()));
                        if(ScoreDatabase.mScoreTwoDoa.rollbackScore(action)) {
                            Log.d(TAG, "Successfully Deleted action");
                        } else {
                            Log.d(TAG, "FAILED to delete");
                        }
                        if (action.getType() == TYPE_2) {
                            mCountButtonTwo.setText(action.getRollBackValue());
                        } else {
                            mCountButtonThree.setText(action.getRollBackValue());
                        }
                    }

                }


            }
        });
    }

    public int getPegIndex(int pegValue) {
        int index = 0;
        for (int peg : mPinValues) {
            if (pegValue == peg) {
                return index;
            } else {
                index++;
            }
        }
        return 0;
    }

    public void initialiseCountButtons() {
        mCountButtonTwo = (Button) findViewById(R.id.two_count_button);
        mCountButtonThree = (Button) findViewById(R.id.three_count_button);
        mIncrementTwo = (Button) findViewById(R.id.increment_two);
        mIncrementThree = (Button) findViewById(R.id.increment_three);
        int currentIndex = mViewPager.getCurrentItem();
        PegRecord pegRecord = ScoreDatabase.mScoreTwoDoa.getTodayPegValue(mPinValues.get(currentIndex), TYPE_2);
        if (pegRecord != null) {
            mCountButtonTwo.setText(String.format(Locale.getDefault(), "%d", pegRecord.getPegCount()));
            mCountButtonThree.setText(String.format(Locale.getDefault(), "%d", pegRecord.getPegCount()));
        } else {
            try {
                PegRecord peg2 = new PegRecord(getDate(), TYPE_2, mPinValues.get(currentIndex), 0);
                PegRecord peg3 = new PegRecord(getDate(), TYPE_3, mPinValues.get(currentIndex), 0);
                ScoreDatabase.mScoreTwoDoa.addTodayPegValue(peg2);
                ScoreDatabase.mScoreTwoDoa.addTodayPegValue(peg3);
                mCountButtonTwo.setText(String.format(Locale.getDefault(), "%d", peg2.getPegCount()));
                mCountButtonThree.setText(String.format(Locale.getDefault(), "%d", peg3.getPegCount()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mCountButtonTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreTwoDoa.getTodayPegValue(
                        mPinValues.get(currentIndex), TYPE_2);
                if(ScoreDatabase.mScoreTwoDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_2,  1)) {
                    mCountButtonTwo.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(ADD, 1, mPinValues.get(currentIndex), TYPE_2, pegRecord.getPegCount()+1);
                    ScoreDatabase.mActionDoa.addAction(action);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
            }
        });
        mCountButtonThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreTwoDoa.getTodayPegValue(
                        mPinValues.get(currentIndex), TYPE_3);
                if (ScoreDatabase.mScoreTwoDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_3,  1)) {
                    mCountButtonThree.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(ADD, 1, mPinValues.get(currentIndex), TYPE_3, pegRecord.getPegCount()+1);
                    ScoreDatabase.mActionDoa.addAction(action);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
            }
        });
        mIncrementTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreTwoDoa.getTodayPegValue(
                        mPinValues.get(currentIndex), TYPE_2);
                if (ScoreDatabase.mScoreTwoDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_2,  1)) {
                    mCountButtonTwo.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(ADD, 1, mPinValues.get(currentIndex), TYPE_2, pegRecord.getPegCount()+1);
                    ScoreDatabase.mActionDoa.addAction(action);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
            }
        });
        mIncrementThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentItem();
                PegRecord pegRecord = ScoreDatabase.mScoreTwoDoa.getTodayPegValue(
                        mPinValues.get(currentIndex), TYPE_3);
                if (ScoreDatabase.mScoreTwoDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_3,  1)) {
                    mCountButtonThree.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(ADD, 1, mPinValues.get(currentIndex), TYPE_3, pegRecord.getPegCount()+1);
                    ScoreDatabase.mActionDoa.addAction(action);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
            }
        });
        mMovePagerForwardTen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "move Froward +10 clicked");
                movePagerForwardTen();
            }
        });
        mMovePagerBackwardsTen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "move Froward +10 clicked");
                movePagerBackwardsTen();
            }
        });
    }
    public void movePagerForwardTen() {
        //TODO: answer question: do we want to always arrive at the start of the next interval
        int currentIndex = mViewPager.getCurrentItem();
        if ( currentIndex+10 > mPinValues.size() ) {
            mViewPager.setCurrentItem(mPinValues.size());
        } else if (currentIndex < 8){
            mViewPager.setCurrentItem(currentIndex+9);
        } else {
            mViewPager.setCurrentItem(currentIndex+10);
        }
    }
    public void movePagerBackwardsTen() {
        //TODO: answer question: do we want to always arrive at the start of the next interval
        int currentIndex = mViewPager.getCurrentItem();
        if ( currentIndex-10 < 0) {
            mViewPager.setCurrentItem(0);
        } else if (currentIndex > mPinValues.size() - 4) {
            mViewPager.setCurrentItem(currentIndex-4);
        } else {
            mViewPager.setCurrentItem(currentIndex-10);
        }
    }
    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }
    public void initialisePegCounts() {
        for (int peg : mPinValues) {
            try {
                ScoreDatabase.mScoreTwoDoa.addTodayPegValue(new PegRecord(getDate(), TYPE_2, peg, 0));
                ScoreDatabase.mScoreTwoDoa.addTodayPegValue(new PegRecord(getDate(), TYPE_3, peg, 0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public List<Integer> generatePinValues() {
        List<Integer> values = Util.makeSequence(61, 100);
        values.add(101);
        values.add(104);
        values.add(107);
        values.add(110);

        return values;
    }


    public void initialisePager() {
        mViewPager = (ViewPager) findViewById(R.id.pager_two_dart);

        if (mScoringAdapter != null) {
            mViewPager.setAdapter(mScoringAdapter);
        } else {
            mViewPager.setAdapter(new ScorePagerAdapter(this, mPinValues));
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updatePinBoard(mPinValues.get(position));
                PegRecord pegRecord2 = ScoreDatabase.mScoreTwoDoa.getTodayPegValue(mPinValues.get(position), TYPE_2);

                if (pegRecord2 != null) {
                    mCountButtonTwo.setText(String.format(Locale.getDefault(),"%d", pegRecord2.getPegCount()));
                } else {
                    PegRecord newPegRecord2 = new PegRecord(getDate(), 0,mPinValues.get(position) , TYPE_2);
                    try {
                        ScoreDatabase.mScoreTwoDoa.addTodayPegValue(newPegRecord2);
                        mCountButtonTwo.setText(String.format(Locale.getDefault(),"%d", 0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                PegRecord pegRecord3 = ScoreDatabase.mScoreTwoDoa.getTodayPegValue(mPinValues.get(position), TYPE_3);
                if (pegRecord3 != null) {
                    mCountButtonThree.setText(String.format(Locale.getDefault(),"%d", pegRecord3.getPegCount()));
                } else {
                    PegRecord newPegRecord3 = new PegRecord(getDate(), 0,mPinValues.get(position) , TYPE_3);
                    try {
                        ScoreDatabase.mScoreTwoDoa.addTodayPegValue(newPegRecord3);
                        mCountButtonThree.setText(String.format(Locale.getDefault(),"%d", 0));
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



    private void updatePinBoard(int pinValue) {
        if (60 < pinValue && pinValue < 70) {
            pin.setImageResource(mPinBoards[0]);
        } else if ( 70 <= pinValue && pinValue < 80) {
            pin.setImageResource(mPinBoards[1]);
        } else if ( 80 <= pinValue && pinValue < 90) {
            pin.setImageResource(mPinBoards[2]);
        } else if ( 90 <= pinValue && pinValue < 100) {
            pin.setImageResource(mPinBoards[3]);
        } else if ( 100 <= pinValue && pinValue < 110) {
            pin.setImageResource(mPinBoards[4]);
        } else {
            pin.setImageResource(mPinBoards[0]);
        }

    }

    public void initialiseMenuButton() {
        mMenuButton = (ImageButton) findViewById(R.id.button_menu);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homePageIntent = new Intent(TwoDartActivity.this, HomePageActivity.class);
                startActivity(homePageIntent);
            }
        });
    }

    /**
     *
     */
    private class InitialisePegValueTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            initialisePegCounts();
            return null;
        }
    }

        /**
     * Score Pager Adapter
     */
    public class ScorePagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;
        private List<Integer> mResources;
        public TextView scoreNumber;


        public ScorePagerAdapter(Context context, List<Integer> resources) {
            mContext = context;
            mResources = resources;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mResources.size();
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
            scoreNumber.setText(String.valueOf(mResources.get(position)));
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
}

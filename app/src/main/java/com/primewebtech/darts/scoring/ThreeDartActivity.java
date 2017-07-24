package com.primewebtech.darts.scoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
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

import org.malcdevelop.cyclicview.CyclicAdapter;
import org.malcdevelop.cyclicview.CyclicView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by benebsworth on 29/5/17.
 */

public class ThreeDartActivity extends AppCompatActivity implements ActionSchema, ScoreSchema {
    /**
     * The three dart activity is very similar to the 2 dart pin but represents scores that can only
     * be achieved via 3 darts and hence we only need the 3 dart counter indicator.
     *
     * In terms of UI we have a pinboard which changes completed per 10 peg value increment, so for
     * example
     * 100..109 -> pedboard_100s
     * 130..139 -> pegboard_130s
     *
     * We can change the peg value by swiping left or write to decrement/increment the value by one,
     * or pressing the -10 or +10 to change the score in 10's, obviously causing an immediate
     * transition in the pin board graphic.
     *
     * One special consideration is the transition from 110 to 110+ we lose the 2 darts score as its
     * not achievable without 3 darts.
     *
     * We have a range between 99 to 170, but we exclude 100 101 104 107 110 which are
     * all 2 dart pegs
     */

    private static final String TAG = ThreeDartActivity.class.getSimpleName();
    private CyclicView mViewPager;
    private TwoDartActivity.ScorePagerAdapter mScoringAdapter;
    private ImageView pin;
    private List<Integer> mPinValues;
    private String curTime;
    private String lastResetTime;
    private Button mCountButtonThree;
    private Button mIncrementThree;
    private Button mMovePagerForwardTen;
    private Button mMovePagerBackwardsTen;
    private ImageButton mMenuButton;
    private ImageButton mBackButton;
    public MainApplication app;

    SharedPreferences prefs = null;

    int[] mPinBoards = {
            R.drawable.pin_99sf,
            R.drawable.pin_100sf,
            R.drawable.pin_110sf,
            R.drawable.pin_120s,
            R.drawable.pin_130sf,
            R.drawable.pin_140sf,
            R.drawable.pin_150s,
            R.drawable.pin_160sf,
            R.drawable.pin_170sf,

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.three_dart_view);
        mMovePagerBackwardsTen = (Button) findViewById(R.id.minus_ten);
        mMovePagerForwardTen = (Button) findViewById(R.id.plus_ten);
        pin = (ImageView) findViewById(R.id.pin);
        mPinValues = generatePinValues();

        curTime = new SimpleDateFormat("yyyydd", Locale.getDefault()).format(new Date());
        prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
        lastResetTime = prefs.getString("lastResetTime_three", curTime);
        Log.d(TAG, "CUR_TIME:"+curTime);
        Log.d(TAG, "LAST_RESET_TIME:"+lastResetTime);
        if ( !curTime.equals(lastResetTime)) {
            Log.d(TAG, "NEW_DAY:resetting counts");
            new InitialisePegValueTask().execute();
            prefs.edit().putString("lastResetTime_three", curTime).apply();
        }

        updatePinBoard(0);
        initialisePager();
        initialiseCountButtons();
        initialiseBackButton();
        initialiseMenuButton();



    }

    public void initialiseMenuButton() {
        mMenuButton = (ImageButton) findViewById(R.id.button_menu);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homePageIntent = new Intent(ThreeDartActivity.this, HomePageActivity.class);
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
                Action action = ScoreDatabase.mActionDoa.getAndDeleteLastHistoryAction(MODE_THREE);
                if (action != null) {
                    int currentIndex = mViewPager.getCurrentPosition();
                    if (mPinValues.get(currentIndex) == action.getPegValue()) {
                        if(ScoreDatabase.mScoreThreeDoa.rollbackScore(action)) {
                            Log.d(TAG, "Successfully Deleted action");
                            mCountButtonThree.setText(action.getRollBackValue());
                        } else {
                            Log.d(TAG, "FAILED to delete");
                        }


                    } else {
                        mViewPager.setCurrentPosition(getPegIndex(action.getPegValue()));
                        if(ScoreDatabase.mScoreThreeDoa.rollbackScore(action)) {
                            Log.d(TAG, "Successfully Deleted action");
                            mCountButtonThree.setText(action.getRollBackValue());
                        } else {
                            Log.d(TAG, "FAILED to delete");
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
        mCountButtonThree = (Button) findViewById(R.id.three_count_button);
        mIncrementThree = (Button) findViewById(R.id.increment_three);
        int currentIndex = mViewPager.getCurrentPosition();
        PegRecord pegRecord = ScoreDatabase.mScoreThreeDoa.getTodayPegValue(mPinValues.get(currentIndex), TYPE_3);
        if (pegRecord != null) {
            mCountButtonThree.setText(String.format(Locale.getDefault(), "%d", pegRecord.getPegCount()));
        } else {
            try {
                PegRecord peg3 = new PegRecord(getDate(), TYPE_3, mPinValues.get(currentIndex), 0);
                ScoreDatabase.mScoreThreeDoa.addTodayPegValue(peg3);
                mCountButtonThree.setText(String.format(Locale.getDefault(), "%d", peg3.getPegCount()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCountButtonThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentPosition();
                PegRecord pegRecord = ScoreDatabase.mScoreThreeDoa.getTodayPegValue(
                        mPinValues.get(currentIndex), TYPE_3);
                if (ScoreDatabase.mScoreThreeDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_3,  1)) {
                    mCountButtonThree.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(MODE_THREE, ADD, 1, mPinValues.get(currentIndex), TYPE_3, pegRecord.getPegCount()+1);
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
                int currentIndex = mViewPager.getCurrentPosition();
                PegRecord pegRecord = ScoreDatabase.mScoreThreeDoa.getTodayPegValue(
                        mPinValues.get(currentIndex), TYPE_3);
                if (ScoreDatabase.mScoreThreeDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_3,  1)) {
                    mCountButtonThree.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(MODE_THREE, ADD, 1, mPinValues.get(currentIndex), TYPE_3, pegRecord.getPegCount()+1);
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
        int currentIndex = mViewPager.getCurrentPosition();
        if ( mPinValues.get(currentIndex) < 110) {
            mViewPager.setCurrentPosition(8);
        } else if (currentIndex+10 > mPinValues.size() ){
            mViewPager.setCurrentPosition(mPinValues.size());
        } else {
            mViewPager.setCurrentPosition(currentIndex+10);
        }
    }
    public void movePagerBackwardsTen() {
        //TODO: answer question: do we want to always arrive at the start of the next interval
        int currentIndex = mViewPager.getCurrentPosition();
        if ( currentIndex-10 < 0) {
            mViewPager.setCurrentPosition(0);
        } else if (currentIndex > mPinValues.size() - 4) {
            mViewPager.setCurrentPosition(currentIndex-4);
        } else {
            mViewPager.setCurrentPosition(currentIndex-10);
        }
    }

    public void initialisePager() {
        mViewPager = (CyclicView) findViewById(R.id.pager_three_dart);
        final int size = generatePinValues().size();
        mViewPager.setAdapter(new CyclicAdapter() {
            @Override
            public int getItemsCount() {
                return size;
            }

            @Override
            public View createView(int i) {
                TextView scoreNumber = new TextView(ThreeDartActivity.this);
                scoreNumber.setText(String.valueOf(mPinValues.get(i)));
                scoreNumber.setTextSize(55);
                scoreNumber.setTextColor(Color.BLACK);
                scoreNumber.setGravity(Gravity.CENTER);
                return scoreNumber;
            }
            @Override
            public void removeView(int i, View view) {

            }
        });
        mViewPager.addOnPositionChangeListener(new CyclicView.OnPositionChangeListener() {

            @Override
            public void onPositionChange(int i) {
                updatePinBoard(mPinValues.get(i));
                PegRecord pegRecord3 = ScoreDatabase.mScoreThreeDoa.getTodayPegValue(mPinValues.get(i), TYPE_3);
                if (pegRecord3 != null) {
                    mCountButtonThree.setText(String.format(Locale.getDefault(),"%d", pegRecord3.getPegCount()));
                } else {
                    PegRecord newPegRecord3 = new PegRecord(getDate(), TYPE_3 ,mPinValues.get(i) , 0);
                    try {
                        ScoreDatabase.mScoreThreeDoa.addTodayPegValue(newPegRecord3);
                        mCountButtonThree.setText(String.format(Locale.getDefault(),"%d", 0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public List<Integer> generatePinValues() {
        List<Integer> values = new ArrayList<>();
        values.add(99);
        values.add(102);
        values.add(103);
        values.add(105);
        values.add(106);
        for (int i=108; i<=170; i++) {
            values.add(i);
        }
        return values;
    }
    private void updatePinBoard(int pinValue) {
        if (pinValue == 99) {
            pin.setImageResource(mPinBoards[0]);
        } else if ( 100 <= pinValue && pinValue < 110) {
            pin.setImageResource(mPinBoards[1]);
        } else if ( 110 <= pinValue && pinValue < 120) {
            pin.setImageResource(mPinBoards[2]);
        } else if ( 120 <= pinValue && pinValue < 130) {
            pin.setImageResource(mPinBoards[3]);
        } else if ( 130 <= pinValue && pinValue < 140) {
            pin.setImageResource(mPinBoards[4]);
        } else if ( 140 <= pinValue && pinValue < 150) {
            pin.setImageResource(mPinBoards[5]);
        } else if ( 150 <= pinValue && pinValue < 160) {
            pin.setImageResource(mPinBoards[6]);
        } else if ( 160 <= pinValue && pinValue < 170) {
            pin.setImageResource(mPinBoards[7]);
        } else if ( pinValue == 170) {
            pin.setImageResource(mPinBoards[8]);
        } else {
            pin.setImageResource(mPinBoards[0]);
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
                ScoreDatabase.mScoreThreeDoa.addTodayPegValue(new PegRecord(getDate(), TYPE_3, peg, 0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class InitialisePegValueTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            initialisePegCounts();
            return null;
        }
    }
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

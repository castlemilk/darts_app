package com.primewebtech.darts.scoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
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
import com.primewebtech.darts.statistics.StatsHundredActivity;

import org.malcdevelop.cyclicview.CyclicAdapter;
import org.malcdevelop.cyclicview.CyclicView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by benebsworth on 29/5/17.
 */

public class HundredDartActivity extends AppCompatActivity implements ActionSchema, ScoreSchema {
    /**
     * The HundredDart scoring layout follows a similar design to the one dart scoring
     *
     * This activity is responsible for the display and logging of the One Dart scoring category
     * In this scoring category the user can swipe left and right the peg value and then increment
     * the count, which is then indicated on the rim of the pin in white circles. Each white circle
     * represents that value being pegged 100 times, if a full circle is completed then it cycles to
     * green cirlces around the rim then on the next completion it goes red.
     *
     * There is a number underneath the centre of the pin which represents the number of times a given
     * peg value has been completed for the day. It will reset on the next day with the historical data
     * being logged for the statistics/analytics stage.
     *
     * Swiping left and right changes to the other peg values of 140+ and 180, where we can again
     * increment the score
     *
     * Thid module mirrors the functionality presented in the one dart scoring of a back/undo button
     * and the statistics view
     */
    private static final String TAG = HundredDartActivity.class.getSimpleName();
    private ImageView pin;
    private int pegsCompleted;
    private HashMap<Integer, Integer> scoreCounts;
    private CyclicView mViewPager;
    private Button mCountButton;
    private ImageButton mMenuButton;
    private ImageButton mBackButton;
    private ImageButton mStatsButton;

    private OneDartActivity.ScorePagerAdapter mScoringAdapter;
    public MainApplication app;
    private String curTime;
    private String lastResetTime;
    private Button mIncrement100;
    private Button mIncrement140;
    private Button mIncrement180;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private SoundPool soundPool;
    private AudioManager audioManager;
    private boolean loaded;
    private float volume;
    // Maximumn sound stream.
    private static final int MAX_STREAMS = 1;
    private int soundIdScroll;
    private int soundIdClick;


    SharedPreferences prefs = null;
    private int[] mPegs = {
           100,140,180
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hundred_dart_view);

        app = (MainApplication) getApplication();
        curTime = new SimpleDateFormat("yyyydd", Locale.getDefault()).format(new Date());

        prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
        lastResetTime = prefs.getString("lastResetTime_hundred", curTime);
        Log.d(TAG, "CUR_TIME:"+curTime);
        Log.d(TAG, "LAST_RESET_TIME:"+lastResetTime);
        if ( !curTime.equals(lastResetTime)) {
            Log.d(TAG, "NEW_DAY:resetting counts");
            //TODO: reset all the required variables and carry previous data into historical logs
            initialisePegCounts();
            initialiseCountButtons();
            prefs.edit().putString("lastResetTime_hundred", curTime).apply();
        }
        pin = (ImageView) findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_40s);
        initialiseCountIndicators();
        initialisePager();
        initialiseCountButtons();
        initialiseMenuButton();
        initialiseBackButton();
        initialiseSound();
        initialiseStatsButton();
        updateCountIndicators(100);
    }

    public void initialiseSound() {
        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Current volumn Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volumn (0 --> 1)
        this.volume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.setVolumeControlStream(streamType);

        // For Android SDK >= 21
        if (Build.VERSION.SDK_INT >= 21 ) {

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();
        }
        // for Android SDK < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });
        soundIdScroll = soundPool.load(this, R.raw.typing, 1);
        soundIdClick = soundPool.load(this, R.raw.click, 1);

    }
    public void playSoundClick(float speed) {
        Log.d(TAG, "playSoundScroll");
        if(loaded)  {
            Log.d(TAG, "playSoundScroll:playing");
            float leftVolumn = volume;
            float rightVolumn = volume;
            int streamId = this.soundPool.play(this.soundIdClick,leftVolumn, rightVolumn, 1, 0, speed);

        }
    }

    public void initialiseStatsButton() {
        mStatsButton = (ImageButton) findViewById(R.id.stats_button);
        mStatsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent statsIntent = new Intent(HundredDartActivity.this, StatsHundredActivity.class);
                Bundle b = new Bundle();
                b.putString("scoreType", "hundred");
                b.putInt("PEG_VALUE", mPegs[mViewPager.getCurrentPosition()]);
                statsIntent.putExtras(b);
                startActivity(statsIntent);
                finish();
            }
        });
    }
    public void initialiseMenuButton() {
        mMenuButton = (ImageButton) findViewById(R.id.button_menu);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homePageIntent = new Intent(HundredDartActivity.this, HomePageActivity.class);
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
                Action action = ScoreDatabase.mActionDoa.getAndDeleteLastHistoryAction(MODE_HUNDRED);
                if (action != null) {
                    int currentIndex = mViewPager.getCurrentPosition();
                    if (mPegs[currentIndex] == action.getPegValue()) {
                        Log.d(TAG, "UNDO:ON_ACTIVE_PEG");
                        mViewPager.setCurrentPosition(getPegIndex(action.getPegValue()));
                        ScoreDatabase.mScoreHundredDoa.rollbackScore(action);
                        mCountButton.setText(action.getRollBackValue());
                        updateCountIndicators(mPegs[currentIndex]);
                    } else {

                        mViewPager.setCurrentPosition(getPegIndex(action.getPegValue()));
                        Log.d(TAG, "UNDO:ON_NON_ACTIVE_PEG:CHANGING PEG:"+getPegIndex(action.getPegValue()));
                        ScoreDatabase.mScoreHundredDoa.rollbackScore(action);
                        mCountButton.setText(action.getRollBackValue());
                        updateCountIndicators(mPegs[getPegIndex(action.getPegValue())]);
                    }

                }


            }
        });
    }

    public void initialiseCountButtons() {
        mCountButton = (Button) findViewById(R.id.count_button);
        mIncrement100 = (Button) findViewById(R.id.increment_100plus);
        mIncrement140 = (Button) findViewById(R.id.increment_140plus);
        mIncrement180 = (Button) findViewById(R.id.increment_180plus);
        int currentIndex = mViewPager.getCurrentPosition();
        PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[currentIndex], TYPE_2);
        if (pegRecord != null) {
            mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()));
            updateCountIndicators(mPegs[currentIndex]);
        } else {
            PegRecord newPegRecord = new PegRecord(getDate(), TYPE_2, mPegs[currentIndex] , 0);
            try {
                ScoreDatabase.mScoreHundredDoa.addTodayPegValue(newPegRecord);
                mCountButton.setText(String.format(Locale.getDefault(),"%d", newPegRecord.getPegCount()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mIncrement100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentPosition();

                if ( mPegs[currentIndex] != 100) {
                    mViewPager.setCurrentPosition(getPegIndex(100));
                    updateCountIndicators(100);
                }
                PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[getPegIndex(100)], TYPE_2);
                if(ScoreDatabase.mScoreHundredDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_2,  1)) {
                    Log.d(TAG, "mIncrement100:pegRecord:"+pegRecord.toString());
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(MODE_HUNDRED, ADD, 1, mPegs[getPegIndex(100)], TYPE_2, pegRecord.getPegCount()+1);
                    Log.d(TAG, "mIncrement100:action:"+action.toString());
                    ScoreDatabase.mActionDoa.addAction(action);
                    updateCountIndicators(mPegs[getPegIndex(100)]);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
                playSoundClick(1);
            }
        });
        mIncrement140.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentPosition();

                if ( mPegs[currentIndex] != 140) {
                    mViewPager.setCurrentPosition(getPegIndex(140));
                    updateCountIndicators(140);
                }
                PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[getPegIndex(140)], TYPE_2);
                if(ScoreDatabase.mScoreHundredDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_2,  1)) {
                    Log.d(TAG, "mIncrement140:pegRecord:"+pegRecord.toString());
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(MODE_HUNDRED, ADD, 1, mPegs[getPegIndex(140)], TYPE_2, pegRecord.getPegCount()+1);
                    Log.d(TAG, "mIncrement140:action:"+action.toString());
                    ScoreDatabase.mActionDoa.addAction(action);
                    updateCountIndicators(mPegs[getPegIndex(140)]);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
                playSoundClick(1);
            }
        });
        mIncrement180.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentPosition();
                if ( mPegs[currentIndex] != 180) {
                    mViewPager.setCurrentPosition(getPegIndex(180));
                    updateCountIndicators(180);
                }
                PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[getPegIndex(180)], TYPE_2);
                if(ScoreDatabase.mScoreHundredDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_2,  1)) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(MODE_HUNDRED, ADD, 1, mPegs[getPegIndex(180)], TYPE_2, pegRecord.getPegCount()+1);
                    ScoreDatabase.mActionDoa.addAction(action);
                    updateCountIndicators(mPegs[getPegIndex(180)]);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
                playSoundClick(1);
            }
        });
    }
    public int[] indicatorResources = {
            R.id.indicator1001,
            R.id.indicator1002,
            R.id.indicator1003,
            R.id.indicator1004,
            R.id.indicator1005,
            R.id.indicator1006,
            R.id.indicator1007,
            R.id.indicator1008,
            R.id.indicator1009,
            R.id.indicator10010,
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
    /**
     * Updates the circular indicators for a given selected peg value. Will carry out a DB lookup
     * and aggregate today + historical peg counts and update the indicator where each circle
     * represents 100 pegs made for that selected peg value.
     * @param pegValue
     */
    public void updateCountIndicators(int pegValue) {
        Log.d(TAG, "updateCountIndicators:pegValue:"+pegValue);
        int total = ScoreDatabase.mScoreHundredDoa.getTotalPegCount(pegValue);
        Log.d(TAG, "updateCountIndicators:total:"+total);
        int index = 0;
        if (total > 0 && total < 1100) {
            for (int circleIndicator : indicatorResources) {

                Double rem = Math.floor((index + 1) * 100 / (total + 1) );
                Log.d(TAG, "updateCountIndicators:remainder" + rem);
                if (rem.intValue() == 0) {
                    ImageView indicator = (ImageView) findViewById(circleIndicator);
                    indicator.setVisibility(View.VISIBLE);
                    final int sdk = android.os.Build.VERSION.SDK_INT;
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        indicator.setBackgroundDrawable( getResources().getDrawable(R.drawable.pin_count_indicator_white));
                    } else {
                        indicator.setBackground( getResources().getDrawable(R.drawable.pin_count_indicator_white));
                    }
                }
                else {
                    ImageView indicator = (ImageView) findViewById(circleIndicator);
                    indicator.setVisibility(View.GONE);
                }


                index++;

            }
        } else if (total > 1100 && total < 2100) {
            for (int circleIndicator : indicatorResources) {

                Double rem = Math.floor((index + 1) * 100 / ((total - 1000) + 1) );
                Log.d(TAG, "updateCountIndicators:remainder" + rem);
                if (rem.intValue() == 0) {
                    ImageView indicator = (ImageView) findViewById(circleIndicator);
                    indicator.setVisibility(View.VISIBLE);
                    final int sdk = android.os.Build.VERSION.SDK_INT;
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        indicator.setBackgroundDrawable( getResources().getDrawable(R.drawable.pin_count_indicator_green));
                    } else {
                        indicator.setBackground( getResources().getDrawable(R.drawable.pin_count_indicator_green));
                    }
                } else {
                    ImageView indicator = (ImageView) findViewById(circleIndicator);
                    indicator.setVisibility(View.VISIBLE);
                    final int sdk = android.os.Build.VERSION.SDK_INT;
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        indicator.setBackgroundDrawable( getResources().getDrawable(R.drawable.pin_count_indicator_white));
                    } else {
                        indicator.setBackground( getResources().getDrawable(R.drawable.pin_count_indicator_white));
                    }
                }


                index++;

            }

        } else if (total > 2100 ) {
            for (int circleIndicator : indicatorResources) {

                Double rem = Math.floor((index + 1) * 100 / ((total - 2000) + 1) );
                Log.d(TAG, "updateCountIndicators:remainder" + rem);
                if (rem.intValue() == 0) {
                    ImageView indicator = (ImageView) findViewById(circleIndicator);
                    indicator.setVisibility(View.VISIBLE);
                    final int sdk = android.os.Build.VERSION.SDK_INT;
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        indicator.setBackgroundDrawable( getResources().getDrawable(R.drawable.pin_count_indicator_red));
                    } else {
                        indicator.setBackground( getResources().getDrawable(R.drawable.pin_count_indicator_red));
                    }
                } else {
                    ImageView indicator = (ImageView) findViewById(circleIndicator);
                    indicator.setVisibility(View.VISIBLE);
                    final int sdk = android.os.Build.VERSION.SDK_INT;
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        indicator.setBackgroundDrawable( getResources().getDrawable(R.drawable.pin_count_indicator_green));
                    } else {
                        indicator.setBackground( getResources().getDrawable(R.drawable.pin_count_indicator_green));
                    }
                }


                index++;

            }

        }
        else {
            initialiseCountIndicators();
        }

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
                ScoreDatabase.mScoreHundredDoa.addTodayPegValue(pegRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initialisePager() {
        mViewPager = (CyclicView) findViewById(R.id.pager_hundred_dart);
        mViewPager.setAdapter(new CyclicAdapter() {
            @Override
            public int getItemsCount() {
                return 3;
            }

            @Override
            public View createView(int i) {
                TextView scoreNumber = new TextView(HundredDartActivity.this);
                scoreNumber.setText(String.valueOf(mPegs[i]));
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
                PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[i], TYPE_2);
                if (pegRecord != null) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()));
                    updateCountIndicators(mPegs[i]);
                } else {
                    PegRecord newPegRecord = new PegRecord(getDate(), TYPE_2, mPegs[i], 0);
                    try {
                        ScoreDatabase.mScoreHundredDoa.addTodayPegValue(newPegRecord);
                        mCountButton.setText(String.format(Locale.getDefault(),"%d", 0));
                        updateCountIndicators(mPegs[i]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
}

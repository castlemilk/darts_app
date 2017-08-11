package com.primewebtech.darts.scoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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
import com.primewebtech.darts.statistics.StatsOneActivity;

import org.malcdevelop.cyclicview.CyclicAdapter;
import org.malcdevelop.cyclicview.CyclicView;

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
    private CyclicView mViewPager;
    private Button mCountButton;
    private ImageButton mMenuButton;
    private ImageButton mBackButton;
    private ImageButton mStatsButton;
    public MainApplication app;
    private String curTime;
    private String lastResetTime;
    private Button mIncrementOne;
    private Button mIncrementTwo;
    private Button mIncrementThree;
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
    private int soundIdClickMulti;

    SharedPreferences prefs = null;
    private int[] mPegs = {
            40, 32, 24,36,50,4
    };
    @Override
    protected void onPause() {
        super.onPause();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
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
            savePB();
            prefs.edit().putString("lastResetTime", curTime).apply();
        }
        int lastPosition = prefs.getInt("POSITION", 0);
        pin = (ImageView) findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_40s);
        initialiseSound();
        initialisePager(lastPosition);
        initialiseBackButton();
        initialiseCountButtons();
        initialiseMenuButton();
        initialiseStatsButton();
        initialiseSound();
        updateCountIndicators(40); //always start on pegValue 40.
    }
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
            savePB();
            initialisePegCounts();
            initialiseCountButtons();
            prefs.edit().putString("lastResetTime", curTime).apply();
        }

        int lastPosition = prefs.getInt("POSITION", 0);
        pin = (ImageView) findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_40s);
        initialiseCountIndicators();
        initialisePager(lastPosition);
        initialiseBackButton();
        initialiseCountButtons();
        initialiseMenuButton();
        initialiseStatsButton();
        initialiseSound();
        updateCountIndicators(mPegs[lastPosition]); //always start on pegValue 40.
    }

    public void savePB() {
        for (int peg : mPegs) {
            ScoreDatabase.mStatsOneDoa.savePB(peg);
        }

    }
    public void playSoundClick(float speed, int loop) {
        Log.d(TAG, "playSoundClick");
        if(loaded)  {
            Log.d(TAG, "playSoundClick:playing");
            float leftVolumn = volume;
            float rightVolumn = volume;
            int streamId = this.soundPool.play(this.soundIdClick,leftVolumn, rightVolumn, 1, loop, speed);
        }
    }
    public void playSoundClickMulti(float speed, int loop) {
        Log.d(TAG, "playSoundClickMulti");
        if(loaded)  {
            Log.d(TAG, "playSoundClickMulti:playing");
            float leftVolumn = volume;
            float rightVolumn = volume;
            int streamId = this.soundPool.play(this.soundIdClickMulti,leftVolumn, rightVolumn, 1, loop, speed);
        }
    }
    public void initialiseStatsButton() {
        mStatsButton = (ImageButton) findViewById(R.id.stats_button);
        mStatsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent statsIntent = new Intent(OneDartActivity.this, StatsOneActivity.class);
                Bundle b = new Bundle();
                b.putString("type", String.valueOf(mPegs[mViewPager.getCurrentPosition()]));
                prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
                prefs.edit().putInt("POSITION", mViewPager.getCurrentPosition()).apply();
                statsIntent.putExtras(b);
                startActivity(statsIntent);
            }
        });
    }
    public void initialiseMenuButton() {
        mMenuButton = (ImageButton) findViewById(R.id.button_menu);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putInt("POSITION", 0).apply();
                Intent homePageIntent = new Intent(OneDartActivity.this, HomePageActivity.class);
                startActivity(homePageIntent);
                finish();
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        prefs.edit().putInt("POSITION", 0).apply();
        Intent homepage = new Intent(this, HomePageActivity.class);
        startActivity(homepage);
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
        soundIdClickMulti = soundPool.load(this, R.raw.multiclick, 1);

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
        mBackButton.setSoundEffectsEnabled(false);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Action action = ScoreDatabase.mActionDoa.getAndDeleteLastHistoryAction(MODE_ONE);
                if (action != null) {
                    int currentIndex = mViewPager.getCurrentPosition();
                    if (mPegs[currentIndex] == action.getPegValue()) {
                        mViewPager.setCurrentPosition(getPegIndex(action.getPegValue()));
                        ScoreDatabase.mScoreOneDoa.rollbackScore(action);
                        mCountButton.setText(action.getRollBackValue());
                        updateCountIndicators(mPegs[currentIndex]);

                    } else {
                        mViewPager.setCurrentPosition(getPegIndex(action.getPegValue()));
                        ScoreDatabase.mScoreOneDoa.rollbackScore(action);
                        mCountButton.setText(action.getRollBackValue());
                        updateCountIndicators(mPegs[getPegIndex(action.getPegValue())]);
                    }
                    playSoundClick(1, 0);

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
        mIncrementOne.setSoundEffectsEnabled(false);
        mIncrementTwo.setSoundEffectsEnabled(false);
        mIncrementThree.setSoundEffectsEnabled(false);
        int currentIndex = mViewPager.getCurrentPosition();
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

        mIncrementOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentPosition();
                PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[currentIndex], TYPE_2);
                if(ScoreDatabase.mScoreOneDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_2,  1)) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(MODE_ONE, ADD, 1, mPegs[currentIndex], TYPE_2, pegRecord.getPegCount()+1);
                    ScoreDatabase.mActionDoa.addAction(action);
                    updateCountIndicators(mPegs[currentIndex]);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
                playSoundClickMulti(1, 0);
            }
        });
        mIncrementTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentPosition();
                PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[currentIndex], TYPE_2);
                if(ScoreDatabase.mScoreOneDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_2,  2)) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+2));
                    Action action = new Action(MODE_ONE, ADD, 2, mPegs[currentIndex], TYPE_2, pegRecord.getPegCount()+2);
                    ScoreDatabase.mActionDoa.addAction(action);
                    updateCountIndicators(mPegs[currentIndex]);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
                    playSoundClickMulti(1, 1);
            }
        });
        mIncrementThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: increment number via DB service
                Log.d(TAG, "Increment button Clicked");
                int currentIndex = mViewPager.getCurrentPosition();
                PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[currentIndex], TYPE_2);
                if(ScoreDatabase.mScoreOneDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_2,  3)) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+3));
                    Action action = new Action(MODE_ONE, ADD, 3, mPegs[currentIndex], TYPE_2, pegRecord.getPegCount()+3);
                    ScoreDatabase.mActionDoa.addAction(action);
                    updateCountIndicators(mPegs[currentIndex]);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
                    playSoundClickMulti(1, 2);
            }
        });

    }


    public void initialisePager(int position) {
        mViewPager = (CyclicView) findViewById(R.id.pager_one_dart);
        mViewPager.setChangePositionFactor(100000000);
            mViewPager.setAdapter(new CyclicAdapter() {
                @Override
                public int getItemsCount() {
                    return 6;
                }

                @Override
                public View createView(int i) {
                    TextView scoreNumber = new TextView(OneDartActivity.this);
                    scoreNumber.setText(String.valueOf(mPegs[i]));
                    scoreNumber.setTextSize(75);
                    scoreNumber.setTextColor(Color.BLACK);
                    scoreNumber.setGravity(Gravity.CENTER);
                    return scoreNumber;
                }
                @Override
                public void removeView(int i, View view) {

                }
            });
//        }
        mViewPager.addOnPositionChangeListener(new CyclicView.OnPositionChangeListener() {
            @Override
            public void onPositionChange(int i) {
                PegRecord pegRecord = ScoreDatabase.mScoreOneDoa.getTodayPegValue(mPegs[i], TYPE_2);
                if (pegRecord != null) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()));
                    updateCountIndicators(mPegs[i]);
                } else {
                    PegRecord newPegRecord = new PegRecord(getDate(), TYPE_2, mPegs[i], 0);
                    try {
                        ScoreDatabase.mScoreOneDoa.addTodayPegValue(newPegRecord);
                        mCountButton.setText(String.format(Locale.getDefault(),"%d", 0));
                        updateCountIndicators(mPegs[i]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mViewPager.setCurrentPosition(position);
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
}

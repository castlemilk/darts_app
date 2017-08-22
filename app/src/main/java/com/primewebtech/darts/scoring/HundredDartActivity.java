package com.primewebtech.darts.scoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
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
import com.primewebtech.darts.statistics.StatsHundredActivity;

import org.malcdevelop.cyclicview.CyclicAdapter;
import org.malcdevelop.cyclicview.CyclicView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private CyclicView mViewPager;
    private Button mCountButton;
    private ImageButton mMenuButton;
    private ImageButton mBackButton;
    private ImageButton mStatsButton;

    public MainApplication app;
    private String curTime;
    private String lastResetTime;
    private ImageButton mIncrement100;
    private ImageButton mIncrement140;
    private ImageButton mIncrement180;
    private Typeface tf_ios;
    private Typeface tf_ios_bold;
    private Typeface tf_viewpager;
    private Typeface tf_increment_button;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private SoundPool soundPool;
    private AudioManager audioManager;
    private boolean loaded;
    private float volume;
    // Maximumn sound stream.
    private static final int MAX_STREAMS = 1;
    private int soundIdClick;
    private int soundIdClickMulti;


    SharedPreferences prefs = null;
    private int[] mPegs = {
           100,140,180
    };
    private String[] mPegsStrings = {
            "100+",
            "140+",
            "180",
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
        setContentView(R.layout.hundred_dart_view);
        SavePBTask savePBTask = new SavePBTask();
        tf_ios = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/ios_reg.ttf");
        tf_ios_bold = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/ios_bold.ttf");
        tf_viewpager = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/arlrbd.ttf");
        tf_increment_button = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/raavi.ttf");

        app = (MainApplication) getApplication();
        curTime = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
        lastResetTime = prefs.getString("lastResetTime_hundred", "0000000");
        Log.d(TAG, "CUR_TIME:"+curTime);
        Log.d(TAG, "LAST_RESET_TIME:"+lastResetTime);
        if ( !curTime.equals(lastResetTime)) {
            Log.d(TAG, "NEW_DAY:resetting counts");
            //TODO: reset all the required variables and carry previous data into historical logs
            savePBTask.execute();
            prefs.edit().putString("lastResetTime_hundred", curTime).apply();
        }
        pin = (ImageView) findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_40s);
        int lastPosition = prefs.getInt("POSITION_HUNDRED", 0);
        initialiseCountIndicators();
        initialisePager(lastPosition);
        initialiseCountButtons();
        initialiseMenuButton();
        initialiseBackButton();
        initialiseSound();
        initialiseStatsButton();
        updateCountIndicators(mPegs[lastPosition]);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hundred_dart_view);
        SavePBTask savePBTask = new SavePBTask();
        tf_ios = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/ios_reg.ttf");
        tf_ios_bold = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/ios_bold.ttf");
        tf_viewpager = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/arlrbd.ttf");
        tf_increment_button = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/raavi.ttf");

        app = (MainApplication) getApplication();
        curTime = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
        lastResetTime = prefs.getString("lastResetTime_hundred", "0000000");
        Log.d(TAG, "CUR_TIME:"+curTime);
        Log.d(TAG, "LAST_RESET_TIME:"+lastResetTime);
        if ( !curTime.equals(lastResetTime)) {
            Log.d(TAG, "NEW_DAY:resetting counts");
            //TODO: reset all the required variables and carry previous data into historical logs
            savePBTask.execute();

            prefs.edit().putString("lastResetTime_hundred", curTime).apply();
        }
        pin = (ImageView) findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_40s);
        int lastPosition = prefs.getInt("POSITION_HUNDRED", 0);
        initialiseCountIndicators();
        initialisePager(lastPosition);
        initialiseCountButtons();
        initialiseMenuButton();
        initialiseBackButton();
        initialiseSound();
        initialiseStatsButton();
        updateCountIndicators(100);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        prefs.edit().putInt("POSITION_HUNDRED", 0).apply();
        Intent homepage = new Intent(this, HomePageActivity.class);
        startActivity(homepage);
    }
    public void savePB() {
        for (int peg : mPegs) {
            ScoreDatabase.mStatsHundredDoa.savePB(peg);
        }

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
        soundIdClick = soundPool.load(this, R.raw.click, 1);
        soundIdClickMulti = soundPool.load(this, R.raw.multiclick, 1);

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
                Intent statsIntent = new Intent(HundredDartActivity.this, StatsHundredActivity.class);
                Bundle b = new Bundle();
                b.putString("scoreType", "hundred");
                b.putInt("PEG_VALUE", mPegs[mViewPager.getCurrentPosition()]);
                prefs.edit().putInt("POSITION_HUNDRED", mViewPager.getCurrentPosition()).apply();
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
                prefs.edit().putInt("POSITION_HUNDRED", 0).apply();
                Intent homePageIntent = new Intent(HundredDartActivity.this, HomePageActivity.class);
                startActivity(homePageIntent);
                finish();
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
        mBackButton.setSoundEffectsEnabled(false);
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
                    playSoundClick(1);

                }


            }
        });
    }

    public void initialiseCountButtons() {
        mCountButton = (Button) findViewById(R.id.count_button);
        mIncrement100 = (ImageButton) findViewById(R.id.increment_100plus);
        mIncrement140 = (ImageButton) findViewById(R.id.increment_140plus);
        mIncrement180 = (ImageButton) findViewById(R.id.increment_180plus);
        mCountButton.setTypeface(tf_viewpager);
        mIncrement100.setSoundEffectsEnabled(false);
        mIncrement140.setSoundEffectsEnabled(false);
        mIncrement180.setSoundEffectsEnabled(false);
        int currentIndex = mViewPager.getCurrentPosition();
        PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[currentIndex], TYPE_3);
        if (pegRecord != null) {
            mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()));
            updateCountIndicators(mPegs[currentIndex]);
        } else {
            PegRecord newPegRecord = new PegRecord(getDate(), TYPE_3, mPegs[currentIndex] , 0);
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
                PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[getPegIndex(100)], TYPE_3);
                if(ScoreDatabase.mScoreHundredDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_3,  1)) {
                    Log.d(TAG, "mIncrement100:pegRecord:"+pegRecord.toString());
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(MODE_HUNDRED, ADD, 1, mPegs[getPegIndex(100)], TYPE_3, pegRecord.getPegCount()+1);
                    Log.d(TAG, "mIncrement100:action:"+action.toString());
                    ScoreDatabase.mActionDoa.addAction(action);
                    updateCountIndicators(mPegs[getPegIndex(100)]);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
                playSoundClickMulti(1, 2);
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
                PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[getPegIndex(140)], TYPE_3);
                if(ScoreDatabase.mScoreHundredDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_3,  1)) {
                    Log.d(TAG, "mIncrement140:pegRecord:"+pegRecord.toString());
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(MODE_HUNDRED, ADD, 1, mPegs[getPegIndex(140)], TYPE_3, pegRecord.getPegCount()+1);
                    Log.d(TAG, "mIncrement140:action:"+action.toString());
                    ScoreDatabase.mActionDoa.addAction(action);
                    updateCountIndicators(mPegs[getPegIndex(140)]);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
                playSoundClickMulti(1, 2);
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
                PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[getPegIndex(180)], TYPE_3);
                if(ScoreDatabase.mScoreHundredDoa.increaseTodayPegValue(pegRecord.getPegValue(),TYPE_3,  1)) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()+1));
                    Action action = new Action(MODE_HUNDRED, ADD, 1, mPegs[getPegIndex(180)], TYPE_3, pegRecord.getPegCount()+1);
                    ScoreDatabase.mActionDoa.addAction(action);
                    updateCountIndicators(mPegs[getPegIndex(180)]);
                } else {
                    Log.d(TAG, "onClick:FAILED_TO_INCRAEASE_TODAY_VALUE");
                }
                playSoundClickMulti(1, 2);
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
        PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(pegValue, TYPE_3);
        if (pegRecord != null) {
            if (pegRecord.getPegCount() >= 100) {
                mCountButton.setTextSize(13);
            } else if (pegRecord.getPegCount() > 1000) {
                mCountButton.setTextSize(8);
            }
        }

        int index = 0;
        if (total > 0 && total <= 1100) {
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
        } else if (total > 1100 && total <= 2100) {
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
            PegRecord pegRecord = new PegRecord(getDate(), TYPE_3, peg, 0);
            try {
                ScoreDatabase.mScoreHundredDoa.addTodayPegValue(pegRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initialisePager(int position) {
        mViewPager = (CyclicView) findViewById(R.id.pager_hundred_dart);
        final TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.score_pin_board_size_hundred_text,outValue, true);
        mViewPager.setChangePositionFactor(4000);
        mViewPager.setAdapter(new CyclicAdapter() {
            @Override
            public int getItemsCount() {
                return 3;
            }

            @Override
            public View createView(int i) {
                TextView scoreNumber = new TextView(HundredDartActivity.this);
                scoreNumber.setText(String.valueOf(mPegsStrings[i]));
                scoreNumber.setTextSize(outValue.getFloat());
                scoreNumber.setTypeface(tf_viewpager);
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
                PegRecord pegRecord = ScoreDatabase.mScoreHundredDoa.getTodayPegValue(mPegs[i], TYPE_3);
                if (pegRecord != null) {
                    mCountButton.setText(String.format(Locale.getDefault(),"%d", pegRecord.getPegCount()));
                    updateCountIndicators(mPegs[i]);
                } else {
                    PegRecord newPegRecord = new PegRecord(getDate(), TYPE_3, mPegs[i], 0);
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
        mViewPager.setCurrentPosition(position);
    }

    private class SavePBTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            savePB();
            return null;
        }
        @Override
        protected void onPostExecute(Void voids) {
            initialisePegCounts();
            initialiseCountButtons();
        }
    }
}

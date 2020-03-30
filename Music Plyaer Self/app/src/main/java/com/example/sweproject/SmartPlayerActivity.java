package com.example.sweproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.cleveroad.audiovisualization.VisualizerDbmHandler;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SmartPlayerActivity extends AppCompatActivity {

    private LinearLayout parentRelativeLayout; // defining our layout
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    private ImageView pausePlayBtn, nextBtn, previousBtn;
    private TextView songNameText;

    private ImageView imageView;
    private  RelativeLayout lowerRelativeLayout;
    private ImageView voiceEnabledBtn;

    private String mode = "ON";

    private MediaPlayer myMediaPlayer;
    private int position;
    private ArrayList mySongs;
    private String mSongName;

    private Toolbar mToolbar;

    private String voice = "NO";
    private Switch voiceSwitch, shakeSwitch;

    private SeekBar seekBar;
    private TextView startTimeSeekBar, endTimeSeekBar;
    private GLAudioVisualizationView glAudioVisualizationView;
    private AudioVisualization audioVisualization;
    private TextView textView;
    private SensorEventListener sensorEventListener;
    private SensorManager sensorManager;
    private String My_PREFS_NAME = "ShakeFeature";

    private Float accelaration = 0f, accelarationCurrent = 0f, accelarationLast = 0f;


    private Runnable updateSongTime = new Runnable() {
        @Override
        public void run() {
            final Handler handler = new Handler();
            int getDuration = myMediaPlayer.getDuration();
            int getCurrentPosition = myMediaPlayer.getCurrentPosition();
            startTimeSeekBar.setText(String.format(Locale.US, "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrentPosition),
                    TimeUnit.MILLISECONDS.toSeconds(getCurrentPosition) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrentPosition))));
            handler.postDelayed(this, 1000);

            // Set max seek ar length
            seekBar.setMax(getDuration);
            seekBar.setProgress(getCurrentPosition);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_player);

        checkVoiceCommandPermission(); // shobar age voice command permission dise kina eita check korlam

        // initializing
        initializingFields();

        validateReceiveValueAndStartPlaying();
        //imageView.setBackgroundResource(R.drawable.logo); // displaying the logo

        voiceEnabledBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Quack();
            }
        });

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseSong();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousSong();
            }
        });

        sensorManager = (SensorManager) SmartPlayerActivity.this.getSystemService(Context.SENSOR_SERVICE);
        accelaration = 0.0f;
        accelarationCurrent = SensorManager.GRAVITY_EARTH;
        accelarationLast = SensorManager.GRAVITY_EARTH;
        bindShakeListener();


    }

    private void bindShakeListener() {
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                final float xFirst = event.values[0];
                final float ySecond = event.values[1];
                final float zThird = event.values[2];
                accelarationLast = accelarationCurrent;
                accelarationCurrent = (float) Math.sqrt(xFirst * xFirst + ySecond * ySecond + zThird * zThird);
                final float delta = accelarationCurrent - accelarationLast;
                accelaration = accelaration * 0.9f + delta;

                if (accelaration > 12) {

                    final SharedPreferences sharedPreferencesEditor = SmartPlayerActivity.this.getSharedPreferences(My_PREFS_NAME, Context.MODE_PRIVATE);
                    final boolean isAllowed = sharedPreferencesEditor.getBoolean("feature", false);
                    if (isAllowed) {
                        playNextSong();

                    }
                }


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }





    private void updateSeekBarStartEndTime(MediaPlayer mediaPlayer) {
        int finalTime = mediaPlayer.getDuration();
        int startTime = mediaPlayer.getCurrentPosition();
        startTimeSeekBar.setText(String.format(Locale.US, "%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(startTime),
                TimeUnit.MILLISECONDS.toSeconds(startTime) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime))));

        endTimeSeekBar.setText(String.format(Locale.US, "%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(finalTime),
                TimeUnit.MILLISECONDS.toSeconds(finalTime) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime))));

        final Handler handler = new Handler();
        handler.postDelayed(updateSongTime, 1000);
    }

    private void Quack() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Command");

        try{
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }catch (Exception e){
            Toast.makeText(SmartPlayerActivity.this, "Command" +e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT:


                if (resultCode == -1 && null != data) {
                    ArrayList<String> result =
                            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    Toast.makeText(SmartPlayerActivity.this, "Command = " + result.get(0), Toast.LENGTH_SHORT).show();

                    if (result.get(0).equals("next")) {
                        playNextSong();
                    } else if (result.get(0).equals("play")) {
                        playPauseSong();
                    } else if (result.get(0).equals("pause")) {
                        playPauseSong();
                    } else if (result.get(0).equals("previous")) {
                        playPreviousSong();
                    }

                    break;
                }
        }
    }

    @Override
    public void onBackPressed() {
        myMediaPlayer.stop();
        myMediaPlayer.release();
        super.onBackPressed();

    }

    private void addingToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");

    }

    private void initializingFields()
    {
        pausePlayBtn = findViewById(R.id.playPauseButtonNowPlaying);
        nextBtn = findViewById(R.id.nextButtonNowPlaying);
        previousBtn = findViewById(R.id.previousButtonNowPlaying);
        //imageView = (ImageView) findViewById(R.id.logo);

        voiceEnabledBtn =  findViewById(R.id.voiceSpeakerButton);
        lowerRelativeLayout = (RelativeLayout) findViewById(R.id.controlPanelNowPlaying);
        songNameText = (TextView) findViewById(R.id.songName);






        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(SmartPlayerActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        voiceSwitch = findViewById(R.id.switchVoiceEnableSettingFrag);
        shakeSwitch = findViewById(R.id.switchShakeSettingFrag);
        textView = findViewById(R.id.textView_shake_switch);

        glAudioVisualizationView = findViewById(R.id.visualizerView);


        voiceSwitch.setOnCheckedChangeListener(
                (CompoundButton buttonView, boolean isChecked) -> {

                    if (isChecked) {
                        openVoiceModeDialogBox();
                        shakeSwitch.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                        lowerRelativeLayout.setVisibility(View.GONE);
                        voiceEnabledBtn.setVisibility(View.VISIBLE);

                    } else {
                        lowerRelativeLayout.setVisibility(View.VISIBLE);
                        voiceEnabledBtn.setVisibility(View.GONE);
                        shakeSwitch.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);

                    }
                }
        );

        shakeSwitch.setOnCheckedChangeListener(
                (CompoundButton buttonView, boolean isChecked) -> {
                    if (isChecked) {
                        openShakeModeDialogBox();
                    } else {

                    }
                }
        );


    }

    private void openShakeModeDialogBox() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(SmartPlayerActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.shake_popup, null);

        Button btn_yes = mView.findViewById(R.id.btn_yes);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setTitle("Shake Mode");

        btn_yes.setOnClickListener((v) -> {
            alertDialog.dismiss();
        });


        alertDialog.show();
    }

    private void openVoiceModeDialogBox() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(SmartPlayerActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.voice_command_popup, null);

        Button btn_yes = mView.findViewById(R.id.btn_yes);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setTitle("Voice Command Mode");

        btn_yes.setOnClickListener((v) -> {
            alertDialog.dismiss();
        });


        alertDialog.show();
    }

    private void validateReceiveValueAndStartPlaying()
    {
        // Media Player jodi null na hoi
        if(myMediaPlayer != null)
        {
            myMediaPlayer.stop(); // stop and release korlam at first
            myMediaPlayer.release();
        }

        // receiving the things from main activity that we have sent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras(); // extra send kora jinishpati bundle er maddhome collect kori

        mySongs = (ArrayList) bundle.getParcelableArrayList("song"); // got the song
        //mSongName = mySongs.get(position).toString();
        //mSongName = mySongs.get(position).getName();
        String songName = intent.getStringExtra("name"); // got the songName

        songNameText.setText(songName); // setting the song name in the textView
        songNameText.setSelected(true);

        position = bundle.getInt("position", 0); // getting the position
        Uri uri = Uri.parse(mySongs.get(position).toString());

        myMediaPlayer = MediaPlayer.create(SmartPlayerActivity.this, uri);
        myMediaPlayer.start(); // media player start korlo

    }

    // converting speech to text, then we will performing operations based on that text
    private void checkVoiceCommandPermission()
    {
        // Android 6 er uporer version gula te voice neoar capability ache, and they require permissions.
        // that's why we check first the SDK version is above 6 or not
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // jodi persmission pai voice er then
            if(!(ContextCompat.checkSelfPermission(SmartPlayerActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))
            {
                // we can start the activity
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    private void playPauseSong()
    {
        //imageView.setBackgroundResource(R.drawable.four);

        if(myMediaPlayer.isPlaying())
        {
            // jodi mediaplayer play hoite thake, taile pause korbe and play button show korbe
            pausePlayBtn.setBackgroundResource(R.drawable.play_icon);
            myMediaPlayer.pause();
        }
        else
        {
            // jodi play na thake tarmane pause kora ache, so start korbe
            pausePlayBtn.setBackgroundResource(R.drawable.pause_icon);
            myMediaPlayer.start();
            //imageView.setBackgroundResource(R.drawable.five);
        }
    }

    private void playNextSong()
    {
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        position = ((position+1))% mySongs.size();

        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(SmartPlayerActivity.this, uri);

        mSongName = mySongs.get(position).toString();
        songNameText.setText(mSongName);
        myMediaPlayer.start();

        //imageView.setBackgroundResource(R.drawable.three);

        if(myMediaPlayer.isPlaying())
        {
            // jodi mediaplayer play hoite thake, taile pause korbe
            pausePlayBtn.setBackgroundResource(R.drawable.pause_icon);
        }
        else
        {
            // jodi play na thake tarmane pause kora ache, so start korbe
            pausePlayBtn.setBackgroundResource(R.drawable.play_icon);
            //imageView.setBackgroundResource(R.drawable.five);
        }
    }

    private void playPreviousSong()
    {
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        // new song er position jodi 0 er choto hoi means the first song of the play list then playlist er last song play korbo. ar 1st song na hole immediate ager song play korbo
        position = ((position-1)< 0 ? (mySongs.size()-1) : (position-1));

        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(SmartPlayerActivity.this, uri);

        mSongName = mySongs.get(position).toString();
        songNameText.setText(mSongName);
        myMediaPlayer.start();

        //imageView.setBackgroundResource(R.drawable.two);

        if(myMediaPlayer.isPlaying())
        {
            // jodi mediaplayer play hoite thake, taile pause korbe
            pausePlayBtn.setBackgroundResource(R.drawable.pause_icon);
        }
        else
        {
            // jodi play na thake tarmane pause kora ache, so start korbe
            pausePlayBtn.setBackgroundResource(R.drawable.play_icon);
            //imageView.setBackgroundResource(R.drawable.five);
        }
    }
}

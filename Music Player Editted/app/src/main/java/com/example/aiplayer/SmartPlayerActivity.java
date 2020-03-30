package com.example.aiplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class SmartPlayerActivity extends AppCompatActivity
{
    private RelativeLayout parentRelativeLayout; // defining our layout
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";

    private ImageView pausePlayBtn, nextBtn, previousBtn;
    private TextView songNameText;

    private ImageView imageView;
    private  RelativeLayout lowerRelativeLayout;
    private Button voiceEnabledBtn;

    private String mode = "ON";

    private MediaPlayer myMediaPlayer;
    private int position;
    private ArrayList mySongs;
    private String mSongName;

    private String voice;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_player);

        checkVoiceCommandPermission(); // shobar age voice command permission dise kina eita check korlam

        // initializing
        initializingFields();

        validateReceiveValueAndStartPlaying();
        imageView.setBackgroundResource(R.drawable.logo); // displaying the logo

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matchesFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if(matchesFound != null)
                {
                    if(mode.equals("ON"))
                    {
                        keeper = matchesFound.get(0);
                        Toast.makeText(SmartPlayerActivity.this, "Result = "+keeper, Toast.LENGTH_SHORT).show();
                        if(keeper.equals("pause the song"))
                        {
                            playPauseSong();
                            Toast.makeText(SmartPlayerActivity.this, "song paused", Toast.LENGTH_SHORT).show();

                        }
                        else if(keeper.equals("play the song"))
                        {
                            playPauseSong();
                            Toast.makeText(SmartPlayerActivity.this, "song started playing", Toast.LENGTH_SHORT).show();
                        }

                        else if(keeper.equals("play next song")){
                            playNextSong();
                            Toast.makeText(SmartPlayerActivity.this, "next song started playing", Toast.LENGTH_SHORT).show();
                        }

                        else if(keeper.equals("play previous song")){
                            playNextSong();
                            Toast.makeText(SmartPlayerActivity.this, "previous song started playing", Toast.LENGTH_SHORT).show();
                        }



                    }

                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        parentRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case 0:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        keeper = "";
                        break;

                    case 1:
                        speechRecognizer.stopListening();
                        break;
                }

                return false;
            }
        });



        voiceEnabledBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mode default ON kora thakbe, so button er upore on lekha thakbe bujhar shubidharthe
                if(mode.equals("ON"))
                {
                    // jodi on thake, taile mode = off kore text set kore dilam and lower relative layout visible korlam, jekhane buttons gula ache
                    mode = "OFF";
                    voiceEnabledBtn.setText("Voice Enabled Mode - OFF");
                    lowerRelativeLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    mode = "ON";
                    voiceEnabledBtn.setText("Voice Enabled Mode - ON");
                    lowerRelativeLayout.setVisibility(View.GONE);
                }
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
    }

    private void initializingFields()
    {
        pausePlayBtn = (ImageView) findViewById(R.id.play_pause_btn);
        nextBtn = (ImageView) findViewById(R.id.next_btn);
        previousBtn = (ImageView) findViewById(R.id.previous_btn);
        imageView = (ImageView) findViewById(R.id.logo);

        voiceEnabledBtn = (Button) findViewById(R.id.voice_enabled_btn);
        lowerRelativeLayout = (RelativeLayout) findViewById(R.id.lower);
        songNameText = (TextView) findViewById(R.id.songName);


        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(SmartPlayerActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());


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
        imageView.setBackgroundResource(R.drawable.four);

        if(myMediaPlayer.isPlaying())
        {
            // jodi mediaplayer play hoite thake, taile pause korbe and play button show korbe
            pausePlayBtn.setImageResource(R.drawable.play);
            myMediaPlayer.pause();
        }
        else
        {
            // jodi play na thake tarmane pause kora ache, so start korbe
            pausePlayBtn.setImageResource(R.drawable.pause);
            myMediaPlayer.start();
            imageView.setBackgroundResource(R.drawable.five);
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

        imageView.setBackgroundResource(R.drawable.three);

        if(myMediaPlayer.isPlaying())
        {
            // jodi mediaplayer play hoite thake, taile pause korbe
            pausePlayBtn.setImageResource(R.drawable.pause);
        }
        else
        {
            // jodi play na thake tarmane pause kora ache, so start korbe
            pausePlayBtn.setImageResource(R.drawable.play);
            imageView.setBackgroundResource(R.drawable.five);
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

        imageView.setBackgroundResource(R.drawable.two);

        if(myMediaPlayer.isPlaying())
        {
            // jodi mediaplayer play hoite thake, taile pause korbe
            pausePlayBtn.setImageResource(R.drawable.pause);
        }
        else
        {
            // jodi play na thake tarmane pause kora ache, so start korbe
            pausePlayBtn.setImageResource(R.drawable.play);
            imageView.setBackgroundResource(R.drawable.five);
        }
    }
}

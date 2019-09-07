package com.example.aiplayer;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

    private String[] itemsAll;
    private ListView mSongsList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSongsList = findViewById(R.id.songsList);

        appExternalStoragePermission();

    }

    // checking the external storage permission
    public void appExternalStoragePermission()
    {
        // For each permission, register a PermissionListener implementation to receive the state of the request:
        // copy code of Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE) // for External Storage permission
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        // permission grant hole audio song name retrieve koro
                        displayAudioSongsName();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response)
                    {

                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
                    {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    // we need to read audio files from storage. that's why we created an method
    // return type arrayList, method type File. method name: readOnlyAudioSongs, takes file as a parameter
    public ArrayList<File> readOnlyAudioSongs(File file)
    {
        ArrayList<File> arrayList = new ArrayList<>(); // created a new arrayList for storing the audio files

        File[] allFiles = file.listFiles(); // memoryr shob file gula allFiles e store korlam

        // individually shob file check kortesi, for all individualFile in allFiles
        for( File individualFile : allFiles)
        {
            // jodi individual file ekta directory hoi and hidden na thake taile oi file ta abar method e pathalam
            // jate file er moddher audio type gula nite pari data gula ke nite pari
            if(individualFile.isDirectory() && !individualFile.isHidden())
            {
                arrayList.addAll(readOnlyAudioSongs(individualFile));
            }
            else
            {
                // jodi directory na hoi taile mp3 aac wav wma type file gula add korbe amader arrayList e
                // fole ensure korlam ar onno kono file jate na thake amader arrayList er moddhe
                if(individualFile.getName().endsWith(".mp3") || individualFile.getName().endsWith(".aac") || individualFile.getName().endsWith(".wav") || individualFile.getName().endsWith(".wma"))
                {
                    //arrayList.add(individualFile);
                }
                if(individualFile.getName().endsWith(".mp3") )
                {
                    arrayList.add(individualFile);
                }
            }
        }

        return  arrayList; // arrayList return korlam
    }

    // we need to display song name in the list so this method will do that
    private  void displayAudioSongsName()
    {
        // array list ta final, karon individual memory er jonne at a time 1 bar e retrieve korbe app on korle
        // audio song read korbe external Storage theke, so amra external Storage er directory diye dilam.
        final ArrayList<File> audioSongs = readOnlyAudioSongs(Environment.getExternalStorageDirectory());

        // itemsAll hocche string type array. er ekta obj create korlam
        // shob songName itemsAll namer string array te rakhbo
        itemsAll = new String[audioSongs.size()]; // jetar size hobe arraylist tar size er shoman.



        // itemsAll er moddhe shob song er name index akare shajaye rakhlam. loopta cholbe arraylist tar size er shoman
        for(int songCounter = 0; songCounter<audioSongs.size(); songCounter++)
        {
            itemsAll[songCounter] = audioSongs.get(songCounter).getName();
        }

        // main activity te amake song gulake finally show korte hobe list view er moddhe, so we can use arrayAdapter for the list view
        // arrayAdapter er ekta object create korlam, eita 3 ta parameter nibe eikhetre
        // ArrayAdapter(Context context, int resource, List<T> objects)
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, itemsAll);
        mSongsList.setAdapter(arrayAdapter); // amader listView er shathe Adapter take set/connect kore dilam

        // now user jodi kono song er upore click kore taile she oi song ta shunte parbe
        mSongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // amra songName collect korlam, kore SmartPlayer Activity te pathaye dilam song ta play korar jonne
                String songName = mSongsList.getItemAtPosition(position).toString();

                // extra hishebe songname, position o send korlam
                Intent intent = new Intent(MainActivity.this, SmartPlayerActivity.class);
                intent.putExtra("song", audioSongs);
                intent.putExtra("name", songName);
                intent.putExtra("position", position);
                startActivity(intent);

            }
        });
    }
}

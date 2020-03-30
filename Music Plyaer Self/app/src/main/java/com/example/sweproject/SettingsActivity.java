package com.example.sweproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private Switch shakeSwitch, voiceSwitch;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        shakeSwitch = findViewById(R.id.switchShakeSettingFrag);
        voiceSwitch = findViewById(R.id.switchVoiceEnableSettingFrag);

        mToolbar = findViewById(R.id.main_page_toolbar);
        addingToolbar();


        voiceSwitch.setOnCheckedChangeListener(
                (CompoundButton buttonView, boolean isChecked) -> {

                    if (isChecked) {
                        Toast.makeText(SettingsActivity.this, "Voice Mode is ENABLED", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(SettingsActivity.this, "Voice Mode is DISABLED", Toast.LENGTH_SHORT).show();

                    }
                }
        );


    }

    private void addingToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");

    }
}

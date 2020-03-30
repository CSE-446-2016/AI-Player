package com.example.sweproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public class SplashActivity extends AppCompatActivity {
    private final static String[] permissionsRequired = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (!hasPermissions(this, permissionsRequired)) {
            //we have to ask for permissions
            ActivityCompat.requestPermissions(this, permissionsRequired, 131);
        } else {
            showSplashScreen(2000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 131) {

            if (!(grantResults.length == 0)
                    && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[2] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[3] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[4] == PackageManager.PERMISSION_GRANTED)
            ) {
                showSplashScreen(1000);
            } else {
                displayToastMessage("Please grant all the permissions", Toast.LENGTH_SHORT);
                /**
                 * Kill this Activity
                 */
                this.finish();
            }

        } else {
            displayToastMessage("Something went wrong!", Toast.LENGTH_SHORT);
            this.finish();
        }

    }

    /**
     * Shows Spalsh Screen for a second
     *
     * @param delayPeriodInMilliseconds
     */
    private void showSplashScreen(int delayPeriodInMilliseconds) {
        final Handler handler = new Handler();
        final Intent intent = new Intent(this, MainActivity.class);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                SplashActivity.super.finish();
            }
        }, delayPeriodInMilliseconds);
    }


    private void displayToastMessage(String toastMessage, int toastDuration) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, toastMessage, toastDuration);
        toast.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
        toast.show();
    }


    /*
     * Check if all permissions have been granted or not
     */
    private boolean hasPermissions(final Context context, String[] permissionsRequired) {
        boolean hasAllPermissions = true;
        hasAllPermissions = !Arrays.stream(permissionsRequired).anyMatch(perm -> context.checkCallingOrSelfPermission(perm) != PackageManager.PERMISSION_GRANTED);
        return hasAllPermissions;
    }
}


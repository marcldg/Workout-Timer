package com.example.hi_timer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView previousWorkout;
    EditText workoutType;
    ImageButton play;
    ImageButton pause;
    ImageButton stop;
    Chronometer chronometer;
    boolean running;
    int count;
    long pauseDifference;
    String currentWorkout;
    String savedWorkout;
    String flag;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previousWorkout = findViewById(R.id.previousWorkout);
        workoutType = findViewById(R.id.workoutType);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.stop);
        chronometer = findViewById(R.id.chronometer);
        count = 0;

        pause.setEnabled(false);
        stop.setEnabled(false);

        sharedPreferences = getSharedPreferences("com.example.hi_timer", MODE_PRIVATE );
        checkPreviousWorkout();

        if(savedInstanceState != null){
            count = savedInstanceState.getInt("count");
            running = savedInstanceState.getBoolean("running");
            currentWorkout = savedInstanceState.getString("currentWorkout");

            if(savedInstanceState.getBoolean("running") == true){
                chronometer.setBase(savedInstanceState.getLong("stopWatchTime"));
                chronometer.start();
                workoutType.setText("");
                workoutType.setEnabled(false);
                workoutType.setVisibility(View.INVISIBLE);
                pause.setEnabled(true);
                stop.setEnabled(true);
            }
            else if(savedInstanceState.getBoolean("running") == false && count >= 1){
                pauseDifference = savedInstanceState.getLong("paused");
                count = savedInstanceState.getInt("count");
                running = savedInstanceState.getBoolean("running");
                chronometer.setBase(SystemClock.elapsedRealtime() - pauseDifference);
                chronometer.stop();
                workoutType.setText("");
                workoutType.setEnabled(false);
                workoutType.setVisibility(View.INVISIBLE);
                pause.setEnabled(true);
                stop.setEnabled(true);
            }
        }
    }

    public void startTimer(View v){
        if(count == 0 && running == false){
            currentWorkout = workoutType.getText().toString();
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseDifference);
            flag = "started";
            chronometer.start();
            running = true;
            workoutType.setText("");
            workoutType.setEnabled(false);
            workoutType.setVisibility(View.INVISIBLE);
            pause.setEnabled(true);
            stop.setEnabled(true);
            Toast.makeText(MainActivity.this, "Workout started !", Toast.LENGTH_LONG).show();
        }
        else if (running == false){
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseDifference);
            chronometer.start();
            running = true;
            workoutType.setText("");
            workoutType.setEnabled(false);
            Toast.makeText(MainActivity.this, "Workout resumed !", Toast.LENGTH_LONG).show();
        }
    }

    public void pauseTimer(View v){
        if(running == true){
            chronometer.stop();
            pauseDifference = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
            count ++;
            Toast.makeText(MainActivity.this, "Workout paused !", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void stopTimer(View v) {
        int timeSpent = Math.toIntExact((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000);
        count = 0;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseDifference = 0;
        running = false;
        workoutType.setEnabled(true);
        workoutType.setVisibility(View.VISIBLE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (TextUtils.isEmpty(currentWorkout)) {
            Toast.makeText(MainActivity.this, "Workout not saved. Reason: Empty field !", Toast.LENGTH_LONG).show();
        } else {
            if (timeSpent < 60) {
                previousWorkout.setText("You spent " + timeSpent + " s on " + currentWorkout + " last time");
                editor.putString(savedWorkout, previousWorkout.getText().toString());
                editor.apply();
            } else if (timeSpent > 60 && timeSpent < 3600) {
                float timeInMin = timeSpent / 60;
                previousWorkout.setText("You spent " + timeInMin + " min on " + currentWorkout + " last time");
                editor.putString(savedWorkout, previousWorkout.getText().toString());
                editor.apply();
            } else {
                float timeInHrs = timeSpent / 3600;
                previousWorkout.setText("You spent " + timeInHrs + " hrs on " + currentWorkout + " last time");
                editor.putString(savedWorkout, previousWorkout.getText().toString());
                editor.apply();
            }
        }
    }

    public void checkPreviousWorkout()
    {
        String lastWorkout = sharedPreferences.getString(savedWorkout, "");
        previousWorkout.setText(lastWorkout);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("stopWatchTime", chronometer.getBase());
        outState.putString("flag", flag);
        outState.putInt("count", count);
        outState.putBoolean("running", running);
        outState.putString("currentWorkout",currentWorkout);
        outState.putLong("paused", pauseDifference);
    }
}
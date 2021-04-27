package com.example.recyclerviewandcardviewmemory;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerViewAdapter adapter;
    private CustomTextView customTextView;
    private boolean gameOver;
    private int scoreMoves;
    private long scoreTime;
    private long mills;
    private CountDownTimer timer;
    private String msgDialog;

    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        int countGridColumn = Integer.parseInt(getString(R.string.count_column));
        scoreMoves = 0;
        scoreTime = 51000;
        mills = 51000;
        msgDialog = getString(R.string.dev_value_msg_dialog);
        gameOver = false;

        customTextView = findViewById(R.id.custom_text_view);

        adapter = new RecyclerViewAdapter(getApplicationContext());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, countGridColumn));

        adapter.setOnCellClickListener(new RecyclerViewAdapter.onCellClickListener() {
            @Override
            public void onCellClick(int position) {

                scoreMoves++;

                adapter.checkOpenCells();
                adapter.openCell(position);

                if (adapter.checkGameOver()) {

                    gameOver = true;
                    timer.cancel();
                    scoreTime = scoreTime - mills;
                    timer.onFinish();
                }
            }
        });

        recyclerView.setAdapter(adapter);
        downloadCountDounTimer(mills);
        timer.start();
    }

    private void downloadCountDounTimer(long millisec) {

        timer = new CountDownTimer(millisec, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                mills = millisUntilFinished;

                customTextView.setText(getTime(millisUntilFinished));

                if (millisUntilFinished < 10000) {
                    customTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                }
            }

            @Override
            public void onFinish() {

                if (gameOver) {

                    saveResult();
                    alertDialog(getString(R.string.dialog_title), msgDialog, getString(R.string.positiv_button), getString(R.string.negative_button));

                } else {
                    alertDialog(getString(R.string.dialog_title), getString(R.string.dialog_msg), getString(R.string.positiv_button), getString(R.string.negative_button));
                }
            }
        };
    }

    private String getTime(long milliseconds) {
        int seconds = (int) milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format(Locale.getDefault(), getString(R.string.time_format), minutes, seconds);
    }

    public static void restartActivity(Activity activity) {
        if (Build.VERSION.SDK_INT >= 19) {
            activity.recreate();
        } else {
            activity.finish();
            activity.startActivity(activity.getIntent());
        }
    }

    @Override
    public void onBackPressed() {

        try {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StringFormatMatches")
    public void saveResult() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int minMoves = sharedPreferences.getInt(getString(R.string.min_moves_key_for_spr), 0);
        long minTime = sharedPreferences.getLong(getString(R.string.min_time_key_for_spr), 0);

        String strMinTime = getTime(sharedPreferences.getLong(getString(R.string.min_time_key_for_spr), 0));
        String strScoreTime = getTime(scoreTime);

        if ((scoreMoves <= minMoves || minMoves == 0) && (scoreTime <= minTime || minTime == 0)) {
            sharedPreferences.edit().putInt(getString(R.string.min_moves_key_for_spr), scoreMoves).apply();
            sharedPreferences.edit().putLong(getString(R.string.min_time_key_for_spr), scoreTime).apply();
        }

        msgDialog = String.format(getString(R.string.dialog_msg_info_result), strScoreTime, scoreMoves, strMinTime, minMoves);
    }

    public void alertDialog(String title, String msg, String positivButton, String negativeButton) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(positivButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                restartActivity(MainActivity.this);
                            }
                        })

                .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        onBackPressed();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
package com.example.myapplication.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.GameSession;
import com.example.myapplication.network.LocalSessionManager;

import java.util.Random;

public class GameActivity extends BaseActivity {

    private long startTime;
    private boolean gameStarted = false;
    private LocalSessionManager sessionManager;
    private AppDatabase db;
    private View screen;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        sessionManager = LocalSessionManager.getInstance(this);
        db = AppDatabase.getInstance(this);

        screen = findViewById(R.id.gameLayout);
        text = findViewById(R.id.tvMessage);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        startNewRound();
    }

    private void startNewRound() {
        gameStarted = false;
        screen.setBackgroundColor(Color.BLACK);
        text.setTextColor(Color.WHITE);
        text.setText("Wait for White...");

        int delay = new Random().nextInt(3000) + 2000;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                screen.setBackgroundColor(Color.WHITE);
                text.setTextColor(Color.BLACK);
                text.setText("TAP NOW!");
                startTime = System.currentTimeMillis();
                gameStarted = true;
            }
        }, delay);

        screen.setOnClickListener(v -> {
            if (gameStarted) {
                long reactionTime = System.currentTimeMillis() - startTime;
                text.setText("Reaction Time: " + reactionTime + " ms");
                
                int score = (int) Math.max(0, 1000 - reactionTime);
                saveSessionLocally("Reaction Test", score, reactionTime);
                gameStarted = false;
            }
        });
    }

    private void saveSessionLocally(String gameName, int score, long reactionTime) {
        String uid = sessionManager.getUserId();
        if (uid == null) return;

        showLoading("Saving your score...");

        new Thread(() -> {
            GameSession session = new GameSession(uid, gameName, score, reactionTime);
            db.gameSessionDao().insertSession(session);
            
            runOnUiThread(() -> {
                hideLoading();
                Toast.makeText(this, "Score saved locally!", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(this::startNewRound, 2000);
            });
        }).start();
    }
}

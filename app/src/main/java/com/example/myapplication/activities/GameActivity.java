package com.example.myapplication.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.models.GameSession;
import com.example.myapplication.network.FirebaseAuthManager;
import com.example.myapplication.network.FirestoreRepository;

import java.util.Random;

/**
 * Production-level GameActivity.
 * Replaces SQLite with Firestore for saving game sessions.
 */
public class GameActivity extends BaseActivity {

    private long startTime;
    private boolean gameStarted = false;
    private FirebaseAuthManager authManager;
    private FirestoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        authManager = FirebaseAuthManager.getInstance();
        repository = FirestoreRepository.getInstance();

        View screen = findViewById(R.id.gameLayout);
        TextView text = findViewById(R.id.tvMessage);
        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        text.setText("Wait for White...");

        int delay = new Random().nextInt(3000) + 2000;

        new Handler().postDelayed(() -> {
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
                
                // Example score logic: higher score for faster reaction
                int score = (int) Math.max(0, 1000 - reactionTime);
                
                saveSessionToCloud("Reaction Test", score, reactionTime);
                gameStarted = false;
            }
        });
    }

    private void saveSessionToCloud(String gameName, int score, long reactionTime) {
        String uid = authManager.getCurrentUserId();
        if (uid == null) return;

        showLoading("Saving your score...");

        GameSession session = new GameSession(uid, gameName, score, reactionTime);

        repository.saveGameSession(session).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful()) {
                showSuccess("Score saved to cloud!");
                // Optionally finish or restart
            } else {
                showError("Failed to save score: " + task.getException().getMessage());
            }
        });
    }
}

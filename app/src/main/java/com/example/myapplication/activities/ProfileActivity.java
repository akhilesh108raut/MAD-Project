package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.models.GameSession;
import com.example.myapplication.models.User;
import com.example.myapplication.network.FirebaseAuthManager;
import com.example.myapplication.network.FirestoreRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Production-level ProfileActivity (Dashboard).
 * Displays real-time stats fetched from Firestore.
 */
public class ProfileActivity extends BaseActivity {

    private static final String TAG = "ProfileActivity";
    private TextView tvProfile;
    private FirebaseAuthManager authManager;
    private FirestoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfile = findViewById(R.id.tvProfile);
        authManager = FirebaseAuthManager.getInstance();
        repository = FirestoreRepository.getInstance();

        if (!authManager.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        loadUserData();

        MaterialCardView gameQuiz = findViewById(R.id.gameQuiz);
        if (gameQuiz != null) {
            gameQuiz.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, GameActivity.class);
                startActivity(intent);
            });
        }

        MaterialCardView gamePuzzle = findViewById(R.id.gamePuzzle);
        if (gamePuzzle != null) {
            gamePuzzle.setOnClickListener(v -> {
                showSuccess("Puzzle Game coming soon!");
            });
        }
    }

    private void loadUserData() {
        showLoading("Loading Profile...");
        String uid = authManager.getCurrentUserId();

        repository.getUserProfile(uid).addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                fetchGameStats(user);
            } else {
                hideLoading();
                showError("User data not found. Please register.");
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
            }
        }).addOnFailureListener(e -> {
            hideLoading();
            showError("Error loading profile: " + e.getMessage());
        });
    }

    private void fetchGameStats(User user) {
        repository.getUserGameSessions(user.getUserId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int totalGames = queryDocumentSnapshots.size();
            long totalReactionTime = 0;
            Map<String, Integer> highScores = new HashMap<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                GameSession session = doc.toObject(GameSession.class);
                if (session != null) {
                    totalReactionTime += session.getReactionTime();
                    
                    String gName = session.getGameName();
                    int score = session.getScore();
                    if (gName != null && (!highScores.containsKey(gName) || score > highScores.get(gName))) {
                        highScores.put(gName, score);
                    }
                }
            }

            double avgReaction = totalGames > 0 ? (double) totalReactionTime / totalGames : 0;
            updateUI(user, totalGames, avgReaction, highScores);
            hideLoading();

        }).addOnFailureListener(e -> {
            hideLoading();
            showError("Error loading stats: " + e.getMessage());
            Log.e(TAG, "Stats error", e);
        });
    }

    private void updateUI(User user, int totalGames, double avgReaction, Map<String, Integer> highScores) {
        if (tvProfile == null) return;

        StringBuilder statsBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : highScores.entrySet()) {
            statsBuilder.append(entry.getKey())
                    .append(" High Score: ")
                    .append(entry.getValue())
                    .append("\n");
        }

        StringBuilder welcomeText = new StringBuilder();
        welcomeText.append("Welcome ").append(user.getName()).append(" (@").append(user.getUsername()).append(")");
        
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            welcomeText.append("\nEmail: ").append(user.getEmail());
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            welcomeText.append("\nPhone: ").append(user.getPhone());
        }

        welcomeText.append("\n\nTotal Games Played: ").append(totalGames)
                .append("\n\nHighest Scores:\n").append(statsBuilder.toString())
                .append("\nAverage Reaction Time: ").append(String.format("%.2f ms", avgReaction));
        
        tvProfile.setText(welcomeText.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            authManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

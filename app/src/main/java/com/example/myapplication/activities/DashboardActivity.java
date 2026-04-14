package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.models.GameSession;
import com.example.myapplication.models.User;
import com.example.myapplication.network.FirebaseAuthManager;
import com.example.myapplication.network.FirestoreRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends BaseActivity {

    private TextView tvProfile;
    private FirebaseAuthManager authManager;
    private FirestoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvProfile = findViewById(R.id.tvProfile);
        authManager = FirebaseAuthManager.getInstance();
        repository = FirestoreRepository.getInstance();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btnShareStats).setOnClickListener(v -> shareStats());

        loadUserData();
    }

    private void loadUserData() {
        showLoading("Loading Stats...");
        String uid = authManager.getCurrentUserId();

        repository.getUserProfile(uid).addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                fetchStats(user);
            } else {
                hideLoading();
                showError("User data not found.");
            }
        }).addOnFailureListener(e -> {
            hideLoading();
            showError("Error: " + e.getMessage());
        });
    }

    private void fetchStats(User user) {
        Task<QuerySnapshot> reactionTask = repository.getUserGameSessions(user.getUserId(), "Reaction Test").get();
        Task<QuerySnapshot> memoryTask = repository.getUserGameSessions(user.getUserId(), "Memory Match").get();

        Tasks.whenAllComplete(reactionTask, memoryTask).addOnCompleteListener(task -> {
            hideLoading();
            
            Map<String, Integer> highScores = new HashMap<>();
            long totalReactionTime = 0;
            int reactionCount = 0;
            int totalGames = 0;

            if (reactionTask.isSuccessful() && reactionTask.getResult() != null) {
                for (DocumentSnapshot doc : reactionTask.getResult().getDocuments()) {
                    GameSession s = doc.toObject(GameSession.class);
                    if (s != null) {
                        totalReactionTime += s.getReactionTime();
                        reactionCount++;
                        totalGames++;
                        int best = highScores.getOrDefault("Reaction Test", 0);
                        if (s.getScore() > best) highScores.put("Reaction Test", s.getScore());
                    }
                }
            }

            if (memoryTask.isSuccessful() && memoryTask.getResult() != null) {
                for (DocumentSnapshot doc : memoryTask.getResult().getDocuments()) {
                    GameSession s = doc.toObject(GameSession.class);
                    if (s != null) {
                        totalGames++;
                        int best = highScores.getOrDefault("Memory Match", 0);
                        if (s.getScore() > best) highScores.put("Memory Match", s.getScore());
                    }
                }
            }

            double avgReaction = reactionCount > 0 ? (double) totalReactionTime / reactionCount : 0;
            updateUI(user, totalGames, avgReaction, highScores);
        });
    }

    private void updateUI(User user, int totalGames, double avgReaction, Map<String, Integer> highScores) {
        StringBuilder sb = new StringBuilder();
        sb.append("Performance for ").append(user.getName()).append("\n\n");
        sb.append("Total Games: ").append(totalGames).append("\n");
        sb.append("Avg Reaction: ").append(String.format("%.2f ms", avgReaction)).append("\n\n");
        sb.append("High Scores:\n");
        sb.append("- Reaction: ").append(highScores.getOrDefault("Reaction Test", 0)).append("\n");
        sb.append("- Memory: ").append(highScores.getOrDefault("Memory Match", 0));

        tvProfile.setText(sb.toString());
    }

    private void shareStats() {
        String stats = tvProfile.getText().toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out my Game Hub stats!\n\n" + stats);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share via"));
    }
}
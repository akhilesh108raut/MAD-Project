package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.User;
import com.example.myapplication.network.LocalSessionManager;

public class ProfileActivity extends BaseActivity {

    private LocalSessionManager sessionManager;
    private AppDatabase db;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = LocalSessionManager.getInstance(this);
        db = AppDatabase.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        loadUserProfile();

        findViewById(R.id.btnViewDashboard).setOnClickListener(v -> 
            startActivity(new Intent(this, DashboardActivity.class)));

        // Motor Health Assessments
        findViewById(R.id.taskSpiral).setOnClickListener(v -> 
            startPDTask("Spiral", "Trace the spiral from center outward."));

        findViewById(R.id.taskMirror).setOnClickListener(v -> 
            startPDTask("Mirror", "Trace the star path."));

        findViewById(R.id.taskZigZag).setOnClickListener(v -> 
            startPDTask("ZigZag", "Trace the zig-zag path."));

        findViewById(R.id.taskOrbit).setOnClickListener(v -> 
            startPDTask("Orbit", "Follow the moving target accurately."));

        findViewById(R.id.taskGhost).setOnClickListener(v -> 
            startPDTask("Ghost", "Trace the path before it fades away."));

        findViewById(R.id.taskWriting).setOnClickListener(v -> 
            startPDTask("Writing", "Copy the sentence provided."));

        // Training Games
        findViewById(R.id.gameQuiz).setOnClickListener(v -> 
            startActivity(new Intent(this, GameActivity.class)));

        findViewById(R.id.gamePuzzle).setOnClickListener(v -> 
            startActivity(new Intent(this, MemoryGameActivity.class)));
    }

    private void loadUserProfile() {
        String userId = sessionManager.getUserId();
        new Thread(() -> {
            User user = db.userDao().getUserById(userId);
            if (user != null) {
                runOnUiThread(() -> {
                    if (tvWelcome != null) {
                        tvWelcome.setText("Welcome, " + user.getName() + "!");
                    }
                });
            }
        }).start();
    }

    private void startPDTask(String type, String instruction) {
        Intent intent = new Intent(this, PDTaskActivity.class);
        intent.putExtra("TASK_TYPE", type);
        intent.putExtra("INSTRUCTION", instruction);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

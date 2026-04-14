package com.example.myapplication.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.User;
import com.example.myapplication.network.LocalSessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends BaseActivity {

    private TextInputEditText etEmail, etPassword;
    private AppDatabase db;
    private LocalSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        sessionManager = LocalSessionManager.getInstance(this);

        if (sessionManager.isLoggedIn()) {
            navigateToDashboard();
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        View tvRegister = findViewById(R.id.tvRegister);
        
        // Hide Google/Phone buttons (cleaned up from UI redesign)
        View btnGoogle = findViewById(R.id.btnGoogleSignIn);
        if (btnGoogle != null) btnGoogle.setVisibility(View.GONE);
        
        View btnPhone = findViewById(R.id.btnPhoneSignIn);
        if (btnPhone != null) btnPhone.setVisibility(View.GONE);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        showLoading("Verifying Credentials...");

        new Thread(() -> {
            User user = db.userDao().login(email, password);
            runOnUiThread(() -> {
                hideLoading();
                if (user != null) {
                    sessionManager.createLoginSession(user.getUserId());
                    navigateToDashboard();
                } else {
                    showError("Incorrect email or password");
                }
            });
        }).start();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        finish();
    }
}

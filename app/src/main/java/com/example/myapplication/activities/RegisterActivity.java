package com.example.myapplication.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.User;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;

public class RegisterActivity extends BaseActivity {

    private TextInputEditText etName, etUsername, etEmail, etPhone, etPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);

        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Please fill in all required fields");
            return;
        }

        showLoading("Creating your account...");

        new Thread(() -> {
            if (db.userDao().isEmailRegistered(email)) {
                runOnUiThread(() -> {
                    hideLoading();
                    showError("This email is already registered");
                });
                return;
            }

            String userId = UUID.randomUUID().toString();
            User user = new User(userId, name, username, email, phone, password);
            db.userDao().insertUser(user);

            runOnUiThread(() -> {
                hideLoading();
                Toast.makeText(this, "Welcome to NeuroDraw!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                finish();
            });
        }).start();
    }
}

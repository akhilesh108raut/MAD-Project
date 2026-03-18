package com.example.myapplication.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.network.FirebaseAuthManager;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Senior Engineer Implementation: ResetPasswordActivity.
 * Handles both "Forgot Password" (via email) and "Update Password" (for active sessions).
 * Migrated from SQLite to Firebase Authentication.
 */
public class ResetPasswordActivity extends BaseActivity {

    private TextInputEditText etEmail, etNewPassword;
    private TextInputLayout tilNewPassword;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize Firebase Manager
        authManager = FirebaseAuthManager.getInstance();

        // UI References
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        Button btnReset = findViewById(R.id.btnReset);

        setupViewMode(btnReset);

        btnReset.setOnClickListener(v -> handlePasswordAction());
    }

    /**
     * Determines UI state based on authentication status.
     */
    private void setupViewMode(Button btnReset) {
        if (authManager.isUserLoggedIn() && authManager.getCurrentUser() != null) {
            // Flow: Update Password (Logged-in)
            etEmail.setText(authManager.getCurrentUser().getEmail());
            etEmail.setEnabled(false);
            tilNewPassword.setVisibility(View.VISIBLE);
            btnReset.setText("Update Password");
        } else {
            // Flow: Reset via Email (Logged-out)
            etEmail.setEnabled(true);
            tilNewPassword.setVisibility(View.GONE);
            btnReset.setText("Send Reset Email");
        }
    }

    private void handlePasswordAction() {
        if (authManager.isUserLoggedIn()) {
            updateUserPassword();
        } else {
            sendResetEmail();
        }
    }

    /**
     * Implementation of sendPasswordResetEmail.
     * Best practice for forgotten passwords - delegates security to Firebase.
     */
    private void sendResetEmail() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Valid email required");
            return;
        }

        showLoading("Sending reset link...");
        authManager.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful()) {
                showSuccess("Security link sent to " + email);
                finish();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                showError("Reset Failed: " + error);
            }
        });
    }

    /**
     * Implementation of updatePassword for active sessions.
     * Note: Firebase may throw an exception if the user hasn't logged in recently.
     */
    private void updateUserPassword() {
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
            etNewPassword.setError("Minimum 6 characters required");
            return;
        }

        showLoading("Securing new password...");
        Task<Void> task = authManager.updatePassword(newPassword);
        
        if (task == null) {
            hideLoading();
            showError("User session invalid");
            return;
        }

        task.addOnCompleteListener(t -> {
            hideLoading();
            if (t.isSuccessful()) {
                showSuccess("Password updated successfully");
                finish();
            } else {
                String error = t.getException() != null ? t.getException().getMessage() : "Update failed";
                showError(error);
                // Tip: In production, catch 'FirebaseAuthRecentLoginRequiredException' 
                // and prompt user to re-login before updating.
            }
        });
    }
}

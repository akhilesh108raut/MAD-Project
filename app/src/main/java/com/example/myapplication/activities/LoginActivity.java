package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.myapplication.R;
import com.example.myapplication.network.FirebaseAuthManager;
import com.example.myapplication.network.FirestoreRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Production-level LoginActivity handling Email, Google, and Phone Authentication.
 */
public class LoginActivity extends BaseActivity {

    private TextInputEditText etEmail, etPassword;
    private FirebaseAuthManager authManager;
    private FirestoreRepository repository;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        showError("Google sign in failed: " + e.getMessage());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = FirebaseAuthManager.getInstance();
        repository = FirestoreRepository.getInstance();

        // Redirect based on login AND registration state
        if (authManager.isUserLoggedIn()) {
            checkUserExistsAndNavigate();
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgot = findViewById(R.id.tvForgot);
        TextView tvRegister = findViewById(R.id.tvRegister);
        Button btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        Button btnPhoneSignIn = findViewById(R.id.btnPhoneSignIn);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> loginUser());

        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class)));

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        btnPhoneSignIn.setOnClickListener(v -> 
                startActivity(new Intent(this, PhoneLoginActivity.class)));
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

        showLoading("Logging in...");

        authManager.login(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                checkUserExistsAndNavigate();
            } else {
                hideLoading();
                showError("Login Failed: " + task.getException().getMessage());
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading("Authenticating with Google...");
        authManager.signInWithGoogle(idToken).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                checkUserExistsAndNavigate();
            } else {
                hideLoading();
                showError("Google Authentication Failed");
            }
        });
    }

    private void checkUserExistsAndNavigate() {
        showLoading("Checking profile...");
        String uid = authManager.getCurrentUserId();
        repository.checkUserExists(uid).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    navigateToDashboard();
                } else {
                    // Force registration if profile document doesn't exist
                    startActivity(new Intent(this, RegisterActivity.class));
                    finish();
                }
            } else {
                showError("Error checking user profile: " + task.getException().getMessage());
            }
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
